package dev.xdark.ssvm;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.execution.asm.*;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.mirror.ArrayJavaClass;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.mirror.JavaClass;
import dev.xdark.ssvm.mirror.JavaMethod;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.value.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.attribute.BasicFileAttributes;

import static org.objectweb.asm.Opcodes.*;

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
public final class NativeJava {

	public static final String CLASS_LOADER_OOP = "classLoaderOop";

	/**
	 * Sets up VM instance.
	 *
	 * @param vm
	 * 		VM to set up.
	 */
	static void vmInit(VirtualMachine vm) {
		var vmi = vm.getInterface();
		injectVMFields(vm);
		setInstructions(vmi);
		initClass(vm);
		initObject(vm);
		initSystem(vm);
		initThread(vm);
		initClassLoader(vm);
		initThrowable(vm);

		// JDK9+
		var utf16 = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StringUTF16");
		if (utf16 != null) {
			vmi.setInvoker(utf16, "isBigEndian", "()Z", ctx -> {
				ctx.setResult(new IntValue(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0));
				return Result.ABORT;
			});
		}

		initVM(vm);

		var unsafe = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Unsafe");
		var newUnsafe = unsafe != null;
		if (unsafe == null) {
			unsafe = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Unsafe");
		}
		vmi.setInvoker(unsafe, "registerNatives", "()V", ctx -> Result.ABORT);
		if (newUnsafe) {
			initNewUnsafe(vm, unsafe);
		}

		initRuntime(vm);
		initDouble(vm);
		initFloat(vm);
		initArray(vm);
		initConstantPool(vm);
		initFS(vm);
		initWinFS(vm);
		initStackTraceElement(vm);
		initReflection(vm);
		initNativeConstructorAccessor(vm);
		initNativeMethodAccessor(vm);
		initAccessController(vm);
		initMethodHandles(vm);
		initModuleSystem(vm);
		initSignal(vm);
		initString(vm);

		var win32ErrorMode = (InstanceJavaClass) vm.findBootstrapClass("sun/io/Win32ErrorMode");
		if (win32ErrorMode != null) {
			vmi.setInvoker(win32ErrorMode, "setErrorMode", "(J)J", ctx -> {
				ctx.setResult(new LongValue(0L));
				return Result.ABORT;
			});
		}
	}

	/**
	 * Initializes misc/VM.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initVM(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var klass = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/VM");
		if (klass != null) {
			vmi.setInvoker(klass, "initializeFromArchive", "(Ljava/lang/Class;)V", ctx -> Result.ABORT);
		} else {
			klass = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/VM");
			if (klass == null) {
				throw new IllegalStateException("Unable to locate VM class");
			}
			vmi.setInvoker(klass, "latestUserDefinedLoader0", "()Ljava/lang/ClassLoader;", ctx -> {
				vm.getHelper().throwException(vm.getSymbols().java_lang_UnsatisfiedLinkError, "TODO implement me");
				return Result.ABORT;
			});
		}
		vmi.setInvoker(klass, "initialize", "()V", ctx -> Result.ABORT);
	}

	/**
	 * Initializes misc/Signal.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initSignal(VirtualMachine vm) {
		var signal = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Signal");
		if (signal == null) {
			signal = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Signal");
			if (signal == null) {
				throw new IllegalStateException("Unable to locate Signal class");
			}
		}
		// TODO: implement this?
		var vmi = vm.getInterface();
		vmi.setInvoker(signal, "findSignal0", "(Ljava/lang/String;)I", ctx -> {
			ctx.setResult(new IntValue(0));
			return Result.ABORT;
		});
		vmi.setInvoker(signal, "handle0", "(IJ)J", ctx -> {
			ctx.setResult(new LongValue(0L));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/String.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initString(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var string = symbols.java_lang_String;
		vmi.setInvoker(string, "intern", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(ctx.getLocals().load(0));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Runtime.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initRuntime(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var runtime = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Runtime");
		vmi.setInvoker(runtime, "availableProcessors", "()I", ctx -> {
			ctx.setResult(new IntValue(Runtime.getRuntime().availableProcessors()));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Double.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initDouble(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var doubleClass = symbols.java_lang_Double;
		vmi.setInvoker(doubleClass, "doubleToRawLongBits", "(D)J", ctx -> {
			ctx.setResult(new LongValue(Double.doubleToRawLongBits(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(doubleClass, "longBitsToDouble", "(J)D", ctx -> {
			ctx.setResult(new DoubleValue(Double.longBitsToDouble(ctx.getLocals().load(0).asLong())));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Float.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initFloat(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var floatClass = symbols.java_lang_Float;
		vmi.setInvoker(floatClass, "floatToRawIntBits", "(F)I", ctx -> {
			ctx.setResult(new IntValue(Float.floatToRawIntBits(ctx.getLocals().load(0).asFloat())));
			return Result.ABORT;
		});
		vmi.setInvoker(floatClass, "intBitsToFloat", "(I)F", ctx -> {
			ctx.setResult(new FloatValue(Float.intBitsToFloat(ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/reflect/Array.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initArray(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var array = symbols.java_lang_reflect_Array;
		vmi.setInvoker(array, "getLength", "(Ljava/lang/Object;)I", ctx -> {
			var value = ctx.getLocals().load(0);
			vm.getHelper().checkArray(value);
			ctx.setResult(new IntValue(((ArrayValue) value).getLength()));
			return Result.ABORT;
		});
		vmi.setInvoker(array, "newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;", ctx -> {
			var locals = ctx.getLocals();
			var local = locals.load(0);
			var helper = vm.getHelper();
			helper.checkNotNull(local);
			if (!(local instanceof JavaValue)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			var wrapper = ((JavaValue<?>) local).getValue();
			if (!(wrapper instanceof JavaClass)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			var klass = (JavaClass) wrapper;
			if (klass.isArray()) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			var length = locals.load(1).asInt();
			var result = helper.newArray(klass, length);
			ctx.setResult(result);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes file system.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initFS(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var fd = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileDescriptor");
		vmi.setInvoker(fd, "initIDs", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(fd, "getHandle", "(I)J", ctx -> {
			try {
				ctx.setResult(new LongValue(vm.getFileDescriptorManager().newFD(ctx.getLocals().load(0).asInt())));
			} catch (VMException ex) {
				vm.getHelper().throwException(vm.getSymbols().java_io_IOException, ex.getOop());
			} catch (IllegalStateException ex) {
				vm.getHelper().throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fd, "getAppend", "(I)Z", ctx -> {
			ctx.setResult(new IntValue(vm.getFileDescriptorManager().isAppend(ctx.getLocals().load(0).asInt()) ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(fd, "close0", "()V", ctx -> {
			var handle = ctx.getLocals().<InstanceValue>load(0).getLong("handle");
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				vm.getHelper().throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		var fos = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileOutputStream");
		vmi.setInvoker(fos, "initIDs", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(fos, "writeBytes", "([BIIZ)V", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var handle = helper.getFileOutputStreamHandle(_this);
			var out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) return Result.ABORT;
			var bytes = helper.toJavaBytes(locals.load(1));
			var off = locals.load(2).asInt();
			var len = locals.load(3).asInt();
			try {
				out.write(bytes, off, len);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "write", "(BZ)V", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var handle = helper.getFileOutputStreamHandle(_this);
			var out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) return Result.ABORT;
			try {
				out.write(locals.load(1).asByte());
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "open0", "(Ljava/lang/String;Z)V", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var path = helper.readUtf8(locals.load(1));
			var append = locals.load(2).asBoolean();
			try {
				var handle = vm.getFileDescriptorManager().open(path, append ? FileDescriptorManager.APPEND : FileDescriptorManager.WRITE);
				((InstanceValue) _this.getValue("fd", "Ljava/io/FileDescriptor;")).setLong("handle", handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		var fis = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileInputStream");
		vmi.setInvoker(fis, "initIDs", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(fis, "readBytes", "([BII)I", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var handle = helper.getFileOutputStreamHandle(_this);
			var in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(new IntValue(-1));
			} else {
				try {
					var off = locals.load(2).asInt();
					var len = locals.load(3).asInt();
					var arraySize = len - off;
					var bytes = new byte[arraySize];
					var read = in.read(bytes);
					if (read > 0) {
						var vmBuffer = locals.<ArrayValue>load(1);
						var memoryManager = vm.getMemoryManager();
						var start = memoryManager.arrayBaseOffset(byte.class) + off;
						var data = vmBuffer.getMemory().getData().slice();
						data.position(start);
						data.put(bytes);
					}
					ctx.setResult(new IntValue(read));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "read0", "()I", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var handle = helper.getFileOutputStreamHandle(_this);
			var in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(new IntValue(-1));
			} else {
				try {
					ctx.setResult(new IntValue(in.read()));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "skip0", "(J)J", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var handle = helper.getFileOutputStreamHandle(_this);
			var in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(new LongValue(0L));
			} else {
				try {
					ctx.setResult(new LongValue(in.skip(locals.load(1).asLong())));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "open0", "(Ljava/lang/String;)V", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<InstanceValue>load(0);
			var helper = vm.getHelper();
			var path = helper.readUtf8(locals.load(1));
			try {
				var handle = vm.getFileDescriptorManager().open(path, FileDescriptorManager.READ);
				((InstanceValue) _this.getValue("fd", "Ljava/io/FileDescriptor;")).setLong("handle", handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/StackTraceElement.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initStackTraceElement(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var stackTraceElement = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		vmi.setInvoker(stackTraceElement, "initStackTraceElements", "([Ljava/lang/StackTraceElement;Ljava/lang/Throwable;)V", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var arr = locals.load(0);
			helper.checkNotNull(arr);
			var ex = locals.load(1);
			helper.checkNotNull(ex);
			var backtrace = ((JavaValue<Backtrace>) ((InstanceValue) ex).getValue("backtrace", "Ljava/lang/Object;")).getValue();
			var storeTo = (ArrayValue) arr;

			var x = 0;
			for (int i = backtrace.count(); i != 0; ) {
				var frame = backtrace.get(--i);
				var element = helper.newStackTraceElement(frame, true);
				storeTo.setValue(x++, element);
			}
			return Result.ABORT;
		});
	}

	/**
	 * Initializes module system.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initModuleSystem(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var bootLoader = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/loader/BootLoader");
		if (bootLoader != null) {
			vmi.setInvoker(bootLoader, "setBootLoaderUnnamedModule0", "(Ljava/lang/Module;)V", ctx -> Result.ABORT);
			vmi.setInvoker(bootLoader, "getSystemPackageLocation", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			});
		}
		var module = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Module");
		if (module != null) {
			vmi.setInvoker(module, "defineModule0", "(Ljava/lang/Module;ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V", ctx -> Result.ABORT);
			vmi.setInvoker(module, "addReads0", "(Ljava/lang/Module;Ljava/lang/Module;)V", ctx -> Result.ABORT);
			vmi.setInvoker(module, "addExports0", "(Ljava/lang/Module;Ljava/lang/String;Ljava/lang/Module;)V", ctx -> Result.ABORT);
			vmi.setInvoker(module, "addExportsToAll0", "(Ljava/lang/Module;Ljava/lang/String;)V", ctx -> Result.ABORT);
			vmi.setInvoker(module, "addExportsToAllUnnamed0", "(Ljava/lang/Module;Ljava/lang/String;)V", ctx -> Result.ABORT);
		}
		var moduleLayer = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ModuleLayer");
		if (moduleLayer != null) {
			vmi.setInvoker(moduleLayer, "defineModules", "(Ljava/lang/module/Configuration;Ljava/util/function/Function;)Ljava/lang/ModuleLayer;", ctx -> {
				ctx.setResult(ctx.getLocals().load(0));
				return Result.ABORT;
			});
		}
	}

	/**
	 * Initializes java/security/AccessController.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initAccessController(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var accController = (InstanceJavaClass) vm.findBootstrapClass("java/security/AccessController");
		vmi.setInvoker(accController, "getStackAccessControlContext", "()Ljava/security/AccessControlContext;", ctx -> {
			// TODO implement?
			ctx.setResult(NullValue.INSTANCE);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;", ctx -> {
			var action = ctx.getLocals().load(0);
			var helper = vm.getHelper();
			helper.checkNotNull(action);
			var result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			var action = ctx.getLocals().load(0);
			var helper = vm.getHelper();
			helper.checkNotNull(action);
			var result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedExceptionAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Class.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initClass(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var jlc = symbols.java_lang_Class;
		vmi.setInvoker(jlc, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(jlc, "getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			var name = vm.getHelper().readUtf8(ctx.getLocals().load(0));
			var primitives = vm.getPrimitives();
			Value result;
			switch (name) {
				case "long":
					result = primitives.longPrimitive.getOop();
					break;
				case "double":
					result = primitives.doublePrimitive.getOop();
					break;
				case "int":
					result = primitives.intPrimitive.getOop();
					break;
				case "float":
					result = primitives.floatPrimitive.getOop();
					break;
				case "char":
					result = primitives.charPrimitive.getOop();
					break;
				case "short":
					result = primitives.shortPrimitive.getOop();
					break;
				case "byte":
					result = primitives.bytePrimitive.getOop();
					break;
				case "boolean":
					result = primitives.booleanPrimitive.getOop();
					break;
				case "void":
					result = primitives.voidPrimitive.getOop();
					break;
				default:
					vm.getHelper().throwException(symbols.java_lang_IllegalArgumentException);
					result = null;
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "desiredAssertionStatus0", "(Ljava/lang/Class;)Z", ctx -> {
			ctx.setResult(new IntValue(0));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "forName0", "(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", ctx -> {
			var locals = ctx.getLocals();
			var helper = vm.getHelper();
			var $name = locals.load(0);
			helper.checkNotNull($name);
			var name = helper.readUtf8($name);
			var initialize = locals.load(1).asBoolean();
			var loader = locals.load(2);
			//noinspection ConstantConditions
			var klass = helper.findClass(loader, name.replace('.', '/'), initialize);
			if (klass == null) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, name);
			} else {
				ctx.setResult(klass.getOop());
			}
			return Result.ABORT;
		});
		var classNameInit = (MethodInvoker) ctx -> {
			ctx.setResult(vm.getHelper().newUtf8(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getName()));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(jlc, "getName0", "()Ljava/lang/String;", classNameInit)) {
			if (!vmi.setInvoker(jlc, "initClassName", "()Ljava/lang/String;", classNameInit)) {
				throw new IllegalStateException("Unable to locate Class name init method");
			}
		}
		vmi.setInvoker(jlc, "isArray", "()Z", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().isArray() ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isAssignableFrom", "(Ljava/lang/Class;)Z", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			var arg = locals.<JavaValue<JavaClass>>load(1).getValue();
			ctx.setResult(new IntValue(_this.isAssignableFrom(arg) ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInterface", "()Z", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(new IntValue(_this.isInterface() ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isPrimitive", "()Z", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(new IntValue(_this.isPrimitive() ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSuperclass", "()Ljava/lang/Class;", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			var superClass = _this.getSuperClass();
			ctx.setResult(superClass == null ? NullValue.INSTANCE : superClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getModifiers", "()I", ctx -> {
			var locals = ctx.getLocals();
			var _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(new IntValue(_this.getModifiers()));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredConstructors0", "(Z)[Ljava/lang/reflect/Constructor;", ctx -> {
			var locals = ctx.getLocals();
			var klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			var helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				var empty = helper.emptyArray(symbols.java_lang_reflect_Constructor);
				ctx.setResult(empty);
			} else {
				klass.initialize();
				var publicOnly = locals.load(1).asBoolean();
				var methods = ((InstanceJavaClass) klass).getDeclaredConstructors(publicOnly);
				var loader = klass.getClassLoader();
				var refFactory = symbols.reflect_ReflectionFactory;
				var reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
				var result = helper.newArray(symbols.java_lang_reflect_Constructor, methods.size());
				var classArray = helper.emptyArray(symbols.java_lang_Class);
				var callerOop = klass.getOop();
				var emptyByteArray = helper.emptyArray(vm.getPrimitives().bytePrimitive);
				for (int j = 0; j < methods.size(); j++) {
					var mn = methods.get(j);
					var types = mn.getType().getArgumentTypes();
					var parameters = helper.convertClasses(helper.convertTypes(loader, types, true));
					var c = helper.invokeVirtual("newConstructor", "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;IILjava/lang/String;[B[B)Ljava/lang/reflect/Constructor;", new Value[0], new Value[]{
							reflectionFactory,
							callerOop,
							parameters,
							classArray,
							new IntValue(mn.getAccess()),
							new IntValue(mn.getSlot()),
							helper.newUtf8(mn.getSignature()),
							emptyByteArray,
							emptyByteArray
					}).getResult();
					result.setValue(j, (ObjectValue) c);
				}
				ctx.setResult(result);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredMethods0", "(Z)[Ljava/lang/reflect/Method;", ctx -> {
			var locals = ctx.getLocals();
			var klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			var helper = vm.getHelper();
			if (klass.isPrimitive()) {
				var empty = helper.emptyArray(symbols.java_lang_reflect_Method);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			if (klass.isArray()) {
				klass = symbols.java_lang_Object;
			}
			klass.initialize();
			var publicOnly = locals.load(1).asBoolean();
			var methods = ((InstanceJavaClass) klass).getDeclaredMethods(publicOnly);
			var loader = klass.getClassLoader();
			var refFactory = symbols.reflect_ReflectionFactory;
			var reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
			var result = helper.newArray(symbols.java_lang_reflect_Method, methods.size());
			var classArray = helper.emptyArray(symbols.java_lang_Class);
			var callerOop = klass.getOop();
			var emptyByteArray = helper.emptyArray(vm.getPrimitives().bytePrimitive);
			for (int j = 0; j < methods.size(); j++) {
				var mn = methods.get(j);
				var type = mn.getType();
				var types = type.getArgumentTypes();
				var rt = helper.findClass(loader, type.getReturnType().getInternalName(), true);
				var parameters = helper.convertClasses(helper.convertTypes(loader, types, true));
				var c = helper.invokeVirtual("newMethod", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;IILjava/lang/String;[B[B[B)Ljava/lang/reflect/Method;", new Value[0], new Value[]{
						reflectionFactory,
						callerOop,
						helper.newUtf8(mn.getName()),
						parameters,
						rt.getOop(),
						classArray,
						new IntValue(mn.getAccess()),
						new IntValue(mn.getSlot()),
						helper.newUtf8(mn.getSignature()),
						emptyByteArray,
						emptyByteArray,
						emptyByteArray
				}).getResult();
				result.setValue(j, (ObjectValue) c);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getInterfaces0", "()[Ljava/lang/Class;", ctx -> {
			var _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			var interfaces = _this.getInterfaces();
			var types = vm.getHelper().convertClasses(interfaces);
			ctx.setResult(types);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Object.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initObject(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var object = symbols.java_lang_Object;
		vmi.setInvoker(object, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(object, "<init>", "()V", ctx -> {
			ctx.getLocals().<InstanceValue>load(0).initialize();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "getClass", "()Ljava/lang/Class;", ctx -> {
			ctx.setResult(ctx.getLocals().<ObjectValue>load(0).getJavaClass().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(object, "notify", "()V", ctx -> {
			ctx.getLocals().<ObjectValue>load(0).vmNotify();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "notifyAll", "()V", ctx -> {
			ctx.getLocals().<ObjectValue>load(0).vmNotifyAll();
			return Result.ABORT;
		});
		vmi.setInvoker(object, "wait", "(J)V", ctx -> {
			var locals = ctx.getLocals();
			try {
				locals.<ObjectValue>load(0).vmWait(locals.load(1).asLong());
			} catch (InterruptedException ex) {
				vm.getHelper().throwException(symbols.java_lang_InterruptedException);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(object, "hashCode", "()I", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(object, "clone", "()Ljava/lang/Object;", ctx -> {
			var _this = ctx.getLocals().<ObjectValue>load(0);
			var type = _this.getJavaClass();
			var helper = vm.getHelper();
			var memoryManager = vm.getMemoryManager();
			ObjectValue clone;
			if (type instanceof ArrayJavaClass) {
				var arr = (ArrayValue) _this;
				clone = memoryManager.newArray((ArrayJavaClass) type, arr.getLength(),
						memoryManager.arrayIndexScale(type.getComponentType()));
			} else {
				clone = memoryManager.newInstance((InstanceJavaClass) type);
			}
			var originalOffset = memoryManager.valueBaseOffset(_this);
			var offset = memoryManager.valueBaseOffset(clone);
			helper.checkEquals(originalOffset, offset);
			var copyTo = clone.getMemory().getData().slice().position(offset);
			var copyFrom = _this.getMemory().getData().slice().position(offset);
			copyTo.put(copyFrom);
			ctx.setResult(clone);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/System.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initSystem(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var sys = symbols.java_lang_System;
		vmi.setInvoker(sys, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(sys, "currentTimeMillis", "()J", ctx -> {
			ctx.setResult(new LongValue(System.currentTimeMillis()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "nanoTime", "()J", ctx -> {
			ctx.setResult(new LongValue(System.nanoTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var $src = locals.load(0);
			helper.checkNotNull($src);
			helper.checkArray($src);
			var $dst = locals.load(2);
			helper.checkNotNull($dst);
			helper.checkArray($dst);
			var srcPos = locals.load(1).asInt();
			var dstPos = locals.load(3).asInt();
			var length = locals.load(4).asInt();
			var src = (ArrayValue) $src;
			var dst = (ArrayValue) $dst;
			var memoryManager = vm.getMemoryManager();
			var srcComponent = src.getJavaClass().getComponentType();
			var dstComponent = dst.getJavaClass().getComponentType();

			var srcScale = memoryManager.arrayIndexScale(srcComponent);
			var dstScale = memoryManager.arrayIndexScale(dstComponent);
			if (srcScale != dstScale) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			var srcStart = memoryManager.arrayBaseOffset(srcComponent);
			var dstStart = memoryManager.arrayBaseOffset(dstComponent);
			if (srcStart != dstStart) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			for (int i = 0; i < length; i++) {
				switch (srcScale) {
					case 8:
						dst.setLong(dstPos++, src.getLong(srcPos++));
						break;
					case 4:
						dst.setInt(dstPos++, src.getInt(srcPos++));
						break;
					case 2:
						dst.setShort(dstPos++, src.getShort(srcPos++));
						break;
					case 1:
						dst.setByte(dstPos++, src.getByte(srcPos++));
						break;
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			var value = ctx.getLocals().<InstanceValue>load(0);
			var jc = (InstanceJavaClass) value.getJavaClass();
			var mn = jc.getVirtualMethod("put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
			var properties = vm.getProperties();
			var helper = vm.getHelper();

			for (var entry : properties.entrySet()) {
				var key = entry.getKey();
				var property = entry.getValue();
				helper.invokeExact(jc, mn, new Value[0], new Value[]{
						value,
						helper.newUtf8(key.toString()),
						helper.newUtf8(property.toString())
				});
			}
			ctx.setResult(value);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setIn0", "(Ljava/io/InputStream;)V", ctx -> {
			var stream = ctx.getLocals().load(0);
			sys.setFieldValue("in", "Ljava/io/InputStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setOut0", "(Ljava/io/PrintStream;)V", ctx -> {
			var stream = ctx.getLocals().load(0);
			sys.setFieldValue("out", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setErr0", "(Ljava/io/PrintStream;)V", ctx -> {
			var stream = ctx.getLocals().load(0);
			sys.setFieldValue("err", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "mapLibraryName", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			var name = ctx.getLocals().<ObjectValue>load(0);
			var helper = vm.getHelper();
			helper.checkNotNull(name);
			ctx.setResult(helper.newUtf8(vm.getNativeLibraryManager().mapLibraryName(helper.readUtf8(name))));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Thread.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initThread(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var thread = symbols.java_lang_Thread;
		vmi.setInvoker(thread, "registerNatives", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(thread, "currentThread", "()Ljava/lang/Thread;", ctx -> {
			ctx.setResult(vm.currentThread().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "interrupt", "()V", ctx -> {
			var th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.interrupt();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "setPriority0", "(I)V", ctx -> {
			var locals = ctx.getLocals();
			var th = vm.getThreadManager().getVmThread(locals.<InstanceValue>load(0));
			th.setPriority(locals.load(1).asInt());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "start0", "()V", ctx -> {
			var th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.start();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isAlive", "()Z", ctx -> {
			var th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			ctx.setResult(new IntValue(th.isAlive() ? 1 : 0));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/ClassLoader.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initClassLoader(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var classLoader = symbols.java_lang_ClassLoader;
		vmi.setInvoker(classLoader, "registerNatives", "()V", ctx -> Result.ABORT);
		var clInitHook = (MethodInvoker) ctx -> {
			var oop = vm.getMemoryManager().newJavaInstance(symbols.java_lang_Object, new ClassLoaderData());
			ctx.getLocals().<InstanceValue>load(0)
					.setValue(CLASS_LOADER_OOP, "Ljava/lang/Object;", oop);
			return Result.CONTINUE;
		};
		if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/String;Ljava/lang/ClassLoader;)V", clInitHook)) {
			if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/ClassLoader;)V", clInitHook)) {
				throw new IllegalStateException("Unable to locate ClassLoader init constructor");
			}
		}
		vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			var locals = ctx.getLocals();
			var loader = locals.<ObjectValue>load(0);
			var name = locals.<ObjectValue>load(1);
			var b = locals.<ArrayValue>load(2);
			var off = locals.load(3).asInt();
			var length = locals.load(4).asInt();
			var pd = locals.<ObjectValue>load(5);
			var source = locals.<ObjectValue>load(6);
			var helper = vm.getHelper();
			var bytes = helper.toJavaBytes(b);
			var defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, helper.readUtf8(source));
			//noinspection ConstantConditions
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/Throwable.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initThrowable(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var symbols = vm.getSymbols();
		var throwable = symbols.java_lang_Throwable;
		vmi.setInvoker(throwable, "fillInStackTrace", "(I)Ljava/lang/Throwable;", ctx -> {
			var exception = ctx.getLocals().<InstanceValue>load(0);
			var copy = vm.currentThread().getBacktrace().copy();
			var backtrace = vm.getMemoryManager().newJavaInstance(symbols.java_lang_Object, copy);
			exception.setValue("backtrace", "Ljava/lang/Object;", backtrace);
			exception.setInt("depth", copy.count());
			ctx.setResult(exception);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes reflect/Reflection class.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initReflection(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var reflection = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/Reflection");
		if (reflection == null) {
			reflection = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/Reflection");
			if (reflection == null) {
				throw new IllegalStateException("Unable to locate Reflection class");
			}
		}
		vmi.setInvoker(reflection, "getCallerClass", "()Ljava/lang/Class;", ctx -> {
			var backtrace = vm.currentThread().getBacktrace();
			ctx.setResult(backtrace.get(backtrace.count() - 3).getOwner().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(reflection, "getClassAccessFlags", "(Ljava/lang/Class;)I", ctx -> {
			var klass = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(new IntValue(klass.getModifiers()));
			return Result.ABORT;
		});
		// TODO FIXME
		vmi.setInvoker(reflection, "isCallerSensitive", "(Ljava/lang/reflect/Method;)Z", ctx -> {
			ctx.setResult(new IntValue(0));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes reflect/NativeConstructorAccessorImpl.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initNativeConstructorAccessor(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeConstructorAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeConstructorAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeConstructorAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "newInstance0", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			var locals = ctx.getLocals();
			var c = locals.<InstanceValue>load(0);
			var slot = c.getInt("slot");
			var declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) c.getValue("clazz", "Ljava/lang/Class;")).getValue();
			var helper = vm.getHelper();
			var methods = declaringClass.getDeclaredConstructors(false);
			JavaMethod mn = null;
			for (var m : methods) {
				if (slot == m.getSlot()) {
					mn = m;
					break;
				}
			}
			if (mn == null || !"<init>".equals(mn.getName())) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var values = locals.load(1);
			Value[] converted;
			var types = mn.getType().getArgumentTypes();
			if (!values.isNull()) {
				var passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
				converted = convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			} else {
				helper.checkEquals(types.length, 0);
				converted = new Value[0];
			}
			var instance = vm.getMemoryManager().newInstance(declaringClass);
			var args = new Value[converted.length + 1];
			System.arraycopy(converted, 0, args, 1, converted.length);
			args[0] = instance;
			helper.invokeExact(declaringClass, "<init>", mn.getDesc(), new Value[0], args);
			ctx.setResult(instance);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes reflect/NativeMethodAccessorImpl.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initNativeMethodAccessor(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeMethodAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeMethodAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeMethodAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "invoke0", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			var locals = ctx.getLocals();
			var m = locals.<InstanceValue>load(0);
			var slot = m.getInt("slot");
			var declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) m.getValue("clazz", "Ljava/lang/Class;")).getValue();
			var helper = vm.getHelper();
			var methods = declaringClass.getDeclaredMethods(false);
			JavaMethod mn = null;
			for (var candidate : methods) {
				if (slot == candidate.getSlot()) {
					mn = candidate;
					break;
				}
			}
			if (mn == null) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var instance = locals.load(1);
			var isStatic = (mn.getAccess() & ACC_STATIC) != 0;
			if (!isStatic && instance.isNull()) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var values = locals.load(2);
			var types = mn.getType().getArgumentTypes();
			var passedArgs = (ArrayValue) values;
			helper.checkEquals(passedArgs.getLength(), types.length);
			var args = convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			if (!isStatic) {
				var prev = args;
				args = new Value[args.length + 1];
				System.arraycopy(prev, 0, args, 1, prev.length);
				args[0] = instance;
			}
			var name = mn.getName();
			var desc = mn.getDesc();
			Value result;
			if (isStatic) {
				result = helper.invokeStatic(declaringClass, name, desc, new Value[0], args).getResult();
			} else {
				result = helper.invokeVirtual(name, desc, new Value[0], args).getResult();
			}
			if (result == null) result = NullValue.INSTANCE; // void
			ctx.setResult(result);
			return Result.ABORT;
		});
	}

	/**
	 * Sets up jdk/internal/misc/Unsafe.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param unsafe
	 * 		Unsafe class.
	 */
	private static void initNewUnsafe(VirtualMachine vm, InstanceJavaClass unsafe) {
		var vmi = vm.getInterface();
		vmi.setInvoker(unsafe, "allocateMemory0", "(J)J", ctx -> {
			var memoryManager = vm.getMemoryManager();
			var block = memoryManager.allocateDirect(ctx.getLocals().load(1).asLong());
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(new LongValue(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "reallocateMemory0", "(JJ)J", ctx -> {
			var memoryManager = vm.getMemoryManager();
			var locals = ctx.getLocals();
			var address = locals.load(1).asLong();
			var bytes = locals.load(3).asLong();
			var block = memoryManager.reallocateDirect(address, bytes);
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(new LongValue(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "freeMemory", "(J)V", ctx -> {
			var memoryManager = vm.getMemoryManager();
			var locals = ctx.getLocals();
			var address = locals.load(1).asLong();
			if (!memoryManager.freeMemory(address)) {
				throw new PanicException("Segfault");
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "setMemory0", "(Ljava/lang/Object;JJB)V", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.<ObjectValue>load(1);
			var memory = value.getMemory().getData();
			var offset = locals.load(2).asLong();
			var bytes = locals.load(4).asLong();
			var b = locals.load(6).asByte();
			for (; offset < bytes; offset++) {
				memory.put((int) offset, b);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "arrayBaseOffset0", "(Ljava/lang/Class;)I", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.<JavaValue<JavaClass>>load(1);
			var klass = value.getValue();
			var component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(new IntValue(vm.getMemoryManager().arrayBaseOffset(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "arrayIndexScale0", "(Ljava/lang/Class;)I", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.<JavaValue<JavaClass>>load(1);
			var klass = value.getValue();
			var component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(new IntValue(vm.getMemoryManager().arrayIndexScale(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "addressSize0", "()I", ctx -> {
			ctx.setResult(new IntValue(vm.getMemoryManager().addressSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "isBigEndian0", "()Z", ctx -> {
			ctx.setResult(new IntValue(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "unalignedAccess0", "()Z", ctx -> {
			ctx.setResult(new IntValue(0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "objectFieldOffset1", "(Ljava/lang/Class;Ljava/lang/String;)J", ctx -> {
			var locals = ctx.getLocals();
			var klass = locals.<JavaValue<JavaClass>>load(1);
			var wrapper = klass.getValue();
			if (!(wrapper instanceof InstanceJavaClass)) {
				ctx.setResult(new LongValue(-1L));
			} else {
				var utf = vm.getHelper().readUtf8(locals.load(2));
				var offset = ((InstanceJavaClass) wrapper).getFieldOffsetRecursively(utf);
				if (offset != -1L) offset += vm.getMemoryManager().valueBaseOffset(klass);
				ctx.setResult(new LongValue(offset));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "loadFence", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(unsafe, "storeFence", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(unsafe, "fullFence", "()V", ctx -> Result.ABORT);
		vmi.setInvoker(unsafe, "compareAndSetInt", "(Ljava/lang/Object;JII)Z", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var expected = locals.load(4).asInt();
			var x = locals.load(5).asInt();
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.readInt(obj, offset) == expected;
			if (result) {
				memoryManager.writeInt(obj, offset, x);
			}
			ctx.setResult(new IntValue(result ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObjectVolatile", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue(obj, offset));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "compareAndSetObject", "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var expected = locals.<ObjectValue>load(4);
			var x = locals.<ObjectValue>load(5);
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.readValue(obj, offset) == expected;
			if (result) {
				memoryManager.writeValue(obj, offset, x);
			}
			ctx.setResult(new IntValue(result ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "compareAndSetLong", "(Ljava/lang/Object;JJJ)Z", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var value = locals.load(1);
			helper.checkNotNull(value);
			if (!(value instanceof ObjectValue)) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			var obj = (ObjectValue) value;
			var offset = (int) locals.load(2).asLong();
			var expected = locals.load(4).asLong();
			var x = locals.load(6).asLong();
			var memoryManager = vm.getMemoryManager();
			var result = memoryManager.readLong(obj, offset) == expected;
			if (result) {
				memoryManager.writeLong(obj, offset, x);
			}
			ctx.setResult(new IntValue(result ? 1 : 0));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putObjectVolatile", "(Ljava/lang/Object;JLjava/lang/Object;)V", ctx -> {
			var helper = vm.getHelper();
			var locals = ctx.getLocals();
			var o = locals.load(1);
			helper.checkNotNull(o);
			var offset = (int) locals.load(2).asLong();
			var value = locals.load(4);
			var memoryManager = vm.getMemoryManager();
			memoryManager.writeValue((ObjectValue) o, offset, (ObjectValue) value);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getIntVolatile", "(Ljava/lang/Object;J)I", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			var offset = locals.load(2).asInt();
			var memoryManager = vm.getMemoryManager();
			ctx.setResult(new IntValue(memoryManager.readInt((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "ensureClassInitialized0", "(Ljava/lang/Class;)V", ctx -> {
			var value = ctx.getLocals().load(1);
			vm.getHelper().checkNotNull(value);
			((JavaValue<JavaClass>) value).getValue().initialize();
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			var locals = ctx.getLocals();
			var value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			var offset = locals.load(2).asInt();
			var memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue((ObjectValue) value, offset));
			return Result.ABORT;
		});
	}

	/**
	 * Sets up xx/reflect/ConstantPool
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initConstantPool(VirtualMachine vm) {
		var cpClass = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/ConstantPool");
		if (cpClass == null) {
			cpClass = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/ConstantPool");
			if (cpClass == null) {
				throw new IllegalStateException("Unable to locate ConstantPool class");
			}
		}
		var vmi = vm.getInterface();
		vmi.setInvoker(cpClass, "getSize0", "(Ljav/lang/Object;)I", ctx -> {
			var wrapper = getCpOop(ctx);
			if (wrapper instanceof InstanceJavaClass) {
				var cr = ((InstanceJavaClass) wrapper).getClassReader();
				ctx.setResult(new IntValue(cr.getItemCount()));
			} else {
				ctx.setResult(new IntValue(0));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAt0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			var wrapper = getInstanceCpOop(ctx);
			var cr = wrapper.getClassReader();
			var index = cpRangeCheck(ctx, cr);
			var offset = cr.getItem(index);
			var className = cr.readClass(offset, new char[cr.getMaxStringLength()]);
			var helper = vm.getHelper();
			var result = helper.findClass(ctx.getOwner().getClassLoader(), className, true);
			if (result == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, className);
			}
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAtIfLoaded0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			var wrapper = getInstanceCpOop(ctx);
			var cr = wrapper.getClassReader();
			var index = cpRangeCheck(ctx, cr);
			var offset = cr.getItem(index);
			var className = cr.readClass(offset, new char[cr.getMaxStringLength()]);
			var result = vm.getHelper().findClass(ctx.getOwner().getClassLoader(), className, true);
			if (result == null) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				ctx.setResult(result.getOop());
			}
			return Result.ABORT;
		});
		// getClassRefIndexAt0?
		// TODO all reflection stuff
		vmi.setInvoker(cpClass, "getIntAt0", "(Ljava/lang/Object;I)I", ctx -> {
			var wrapper = getInstanceCpOop(ctx);
			var cr = wrapper.getClassReader();
			var index = cpRangeCheck(ctx, cr);
			var offset = cr.getItem(index);
			ctx.setResult(new IntValue(cr.readInt(offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getLongAt0", "(Ljava/lang/Object;I)J", ctx -> {
			var wrapper = getInstanceCpOop(ctx);
			var cr = wrapper.getClassReader();
			var index = cpRangeCheck(ctx, cr);
			var offset = cr.getItem(index);
			ctx.setResult(new LongValue(cr.readLong(offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getFloatAt0", "(Ljava/lang/Object;I)F", ctx -> {
			var wrapper = getInstanceCpOop(ctx);
			var cr = wrapper.getClassReader();
			var index = cpRangeCheck(ctx, cr);
			var offset = cr.getItem(index);
			ctx.setResult(new FloatValue(Float.intBitsToFloat(cr.readInt(offset))));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getDoubleAt0", "(Ljava/lang/Object;I)D", ctx -> {
			var wrapper = getInstanceCpOop(ctx);
			var cr = wrapper.getClassReader();
			var index = cpRangeCheck(ctx, cr);
			var offset = cr.getItem(index);
			ctx.setResult(new DoubleValue(Double.longBitsToDouble(cr.readLong(offset))));
			return Result.ABORT;
		});
	}

	private static void wrongCpType(ExecutionContext ctx) {
		var vm = ctx.getVM();
		vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException, "Wrong cp entry type");
	}

	private static int cpRangeCheck(ExecutionContext ctx, ClassReader cr) {
		var index = ctx.getLocals().load(1).asInt();
		if (index < 0 || index >= cr.getItemCount()) {
			var vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return index;
	}

	private static InstanceJavaClass getInstanceCpOop(ExecutionContext ctx) {
		var jc = getCpOop(ctx);
		if (!(jc instanceof InstanceJavaClass)) {
			var vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return (InstanceJavaClass) jc;
	}

	private static JavaClass getCpOop(ExecutionContext ctx) {
		var vm = ctx.getVM();
		var value = ctx.getLocals().load(1);
		if (!(value instanceof JavaValue)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		var wrapper = ((JavaValue<?>) value).getValue();
		if (!(wrapper instanceof JavaClass)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return (JavaClass) wrapper;
	}

	/**
	 * Initializes win32 file system class.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initWinFS(VirtualMachine vm) {
		var winNTfs = (InstanceJavaClass) vm.findBootstrapClass("java/io/WinNTFileSystem");
		if (winNTfs != null) {
			var vmi = vm.getInterface();
			vmi.setInvoker(winNTfs, "initIDs", "()V", ctx -> Result.ABORT);
			vmi.setInvoker(winNTfs, "canonicalize0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
				var helper = vm.getHelper();
				var path = helper.readUtf8(ctx.getLocals().load(1));
				try {
					ctx.setResult(helper.newUtf8(vm.getFileDescriptorManager().canonicalize(path)));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
				}
				return Result.ABORT;
			});
			vmi.setInvoker(winNTfs, "getBooleanAttributes", "(Ljava/io/File;)I", ctx -> {
				var helper = vm.getHelper();
				var value = ctx.getLocals().load(1);
				helper.checkNotNull(value);
				try {
					var path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
					var attributes = vm.getFileDescriptorManager().getAttributes(path, BasicFileAttributes.class);
					if (attributes == null) {
						ctx.setResult(new IntValue(0));
					} else {
						var res = 1;
						if (attributes.isDirectory()) {
							res |= 4;
						} else {
							res |= 2;
						}
						ctx.setResult(new IntValue(res));
					}
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
				}
				return Result.ABORT;
			});
		}
	}

	/**
	 * Initializes method handles related classes.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initMethodHandles(VirtualMachine vm) {
		var vmi = vm.getInterface();
		var natives = (InstanceJavaClass) vm.findBootstrapClass("java/lang/invoke/MethodHandleNatives");
		vmi.setInvoker(natives, "registerNatives", "()V", ctx -> Result.ABORT);
	}

	/**
	 * Injects VM fields.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void injectVMFields(VirtualMachine vm) {
		var classLoader = vm.getSymbols().java_lang_ClassLoader;

		classLoader.getNode().fields.add(new FieldNode(
				ACC_PRIVATE | ACC_FINAL,
				CLASS_LOADER_OOP,
				"Ljava/lang/Object;",
				null,
				null
		));
	}

	/**
	 * Converts array off values back to their original
	 * values.
	 * Used for reflection calls.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param loader
	 * 		Class laoder to use.
	 * @param argTypes
	 * 		Original types.
	 * @param array
	 * 		Array to convert.
	 *
	 * @return original values array.
	 */
	private static Value[] convertReflectionArgs(VirtualMachine vm, Value loader, Type[] argTypes, ArrayValue array) {
		var helper = vm.getHelper();
		var result = new Value[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			var originalClass = helper.findClass(loader, argTypes[i].getInternalName(), true);
			var value = (ObjectValue) array.getValue(i);
			if (value.isNull() || !originalClass.isPrimitive()) {
				result[i] = value;
			} else {
				result[i] = helper.unboxGeneric(value);
			}
		}
		return result;
	}

	/**
	 * Sets up default opcode set.
	 *
	 * @param vmi
	 * 		VM interface.
	 */
	private static void setInstructions(VMInterface vmi) {
		var nop = new NopProcessor();
		vmi.setProcessor(NOP, nop);

		vmi.setProcessor(ACONST_NULL, new ConstantProcessor(NullValue.INSTANCE));

		// ICONST_M1..INCONST_5
		for (int x = ICONST_M1; x <= ICONST_5; x++) {
			vmi.setProcessor(x, new ConstantIntProcessor(x - ICONST_0));
		}

		vmi.setProcessor(LCONST_0, new ConstantLongProcessor(0L));
		vmi.setProcessor(LCONST_1, new ConstantLongProcessor(1L));

		vmi.setProcessor(FCONST_0, new ConstantFloatProcessor(0.0F));
		vmi.setProcessor(FCONST_1, new ConstantFloatProcessor(1.0F));
		vmi.setProcessor(FCONST_2, new ConstantFloatProcessor(2.0F));

		vmi.setProcessor(DCONST_0, new ConstantDoubleProcessor(0.0D));
		vmi.setProcessor(DCONST_1, new ConstantDoubleProcessor(1.0D));

		vmi.setProcessor(BIPUSH, new BytePushProcessor());
		vmi.setProcessor(SIPUSH, new ShortPushProcessor());
		vmi.setProcessor(LDC, new LdcProcessor());

		vmi.setProcessor(LLOAD, new LongLoadProcessor());
		vmi.setProcessor(ILOAD, new IntLoadProcessor());
		vmi.setProcessor(FLOAD, new FloatLoadProcessor());
		vmi.setProcessor(DLOAD, new DoubleLoadProcessor());
		vmi.setProcessor(ALOAD, new ValueLoadProcessor());

		vmi.setProcessor(IALOAD, new LoadArrayIntProcessor());
		vmi.setProcessor(LALOAD, new LoadArrayLongProcessor());
		vmi.setProcessor(FALOAD, new LoadArrayFloatProcessor());
		vmi.setProcessor(DALOAD, new LoadArrayDoubleProcessor());
		vmi.setProcessor(AALOAD, new LoadArrayValueProcessor());
		vmi.setProcessor(BALOAD, new LoadArrayByteProcessor());
		vmi.setProcessor(CALOAD, new LoadArrayCharProcessor());
		vmi.setProcessor(SALOAD, new LoadArrayShortProcessor());

		vmi.setProcessor(IASTORE, new StoreArrayIntProcessor());
		vmi.setProcessor(LASTORE, new StoreArrayLongProcessor());
		vmi.setProcessor(FASTORE, new StoreArrayFloatProcessor());
		vmi.setProcessor(DASTORE, new StoreArrayDoubleProcessor());
		vmi.setProcessor(AASTORE, new StoreArrayValueProcessor());
		vmi.setProcessor(BASTORE, new StoreArrayByteProcessor());
		vmi.setProcessor(CASTORE, new StoreArrayCharProcessor());
		vmi.setProcessor(SASTORE, new StoreArrayShortProcessor());

		vmi.setProcessor(ISTORE, new IntStoreProcessor());
		vmi.setProcessor(LSTORE, new LongStoreProcessor());
		vmi.setProcessor(FSTORE, new FloatStoreProcessor());
		vmi.setProcessor(DSTORE, new DoubleStoreProcessor());
		vmi.setProcessor(ASTORE, new ValueStoreProcessor());

		vmi.setProcessor(POP, new PopProcessor());
		vmi.setProcessor(POP2, new Pop2Processor());
		vmi.setProcessor(DUP, new DupProcessor());
		vmi.setProcessor(DUP_X1, new DupX1Processor());
		vmi.setProcessor(DUP_X2, new DupX2Processor());
		vmi.setProcessor(DUP2, new Dup2Processor());
		vmi.setProcessor(DUP2_X1, new Dup2X1Processor());
		vmi.setProcessor(DUP2_X2, new Dup2X2Processor());
		vmi.setProcessor(SWAP, new SwapProcessor());

		vmi.setProcessor(IADD, new BiIntProcessor(Integer::sum));
		vmi.setProcessor(LADD, new BiLongProcessor(Long::sum));
		vmi.setProcessor(FADD, new BiFloatProcessor(Float::sum));
		vmi.setProcessor(DADD, new BiDoubleProcessor(Double::sum));

		vmi.setProcessor(ISUB, new BiIntProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(LSUB, new BiLongProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(FSUB, new BiFloatProcessor((v1, v2) -> v1 - v2));
		vmi.setProcessor(DSUB, new BiDoubleProcessor((v1, v2) -> v1 - v2));

		vmi.setProcessor(IMUL, new BiIntProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(LMUL, new BiLongProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(FMUL, new BiFloatProcessor((v1, v2) -> v1 * v2));
		vmi.setProcessor(DMUL, new BiDoubleProcessor((v1, v2) -> v1 * v2));

		vmi.setProcessor(IDIV, new BiIntProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(LDIV, new BiLongProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(FDIV, new BiFloatProcessor((v1, v2) -> v1 / v2));
		vmi.setProcessor(DDIV, new BiDoubleProcessor((v1, v2) -> v1 / v2));

		vmi.setProcessor(IREM, new BiIntProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(LREM, new BiLongProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(FREM, new BiFloatProcessor((v1, v2) -> v1 % v2));
		vmi.setProcessor(DREM, new BiDoubleProcessor((v1, v2) -> v1 % v2));

		vmi.setProcessor(INEG, new NegativeIntProcessor());
		vmi.setProcessor(LNEG, new NegativeLongProcessor());
		vmi.setProcessor(FNEG, new NegativeFloatProcessor());
		vmi.setProcessor(DNEG, new NegativeDoubleProcessor());

		vmi.setProcessor(ISHL, new BiIntProcessor((v1, v2) -> v1 << v2));
		vmi.setProcessor(LSHL, new LongIntProcessor((v1, v2) -> v1 << v2));
		vmi.setProcessor(ISHR, new BiIntProcessor((v1, v2) -> v1 >> v2));
		vmi.setProcessor(LSHR, new LongIntProcessor((v1, v2) -> v1 >> v2));
		vmi.setProcessor(IUSHR, new BiIntProcessor((v1, v2) -> v1 >>> v2));
		vmi.setProcessor(LUSHR, new LongIntProcessor((v1, v2) -> v1 >>> v2));

		vmi.setProcessor(IAND, new BiIntProcessor((v1, v2) -> v1 & v2));
		vmi.setProcessor(LAND, new BiLongProcessor((v1, v2) -> v1 & v2));
		vmi.setProcessor(IOR, new BiIntProcessor((v1, v2) -> v1 | v2));
		vmi.setProcessor(LOR, new BiLongProcessor((v1, v2) -> v1 | v2));
		vmi.setProcessor(IXOR, new BiIntProcessor((v1, v2) -> v1 ^ v2));
		vmi.setProcessor(LXOR, new BiLongProcessor((v1, v2) -> v1 ^ v2));

		vmi.setProcessor(IINC, new VariableIncrementProcessor());

		vmi.setProcessor(I2L, new IntToLongProcessor());
		vmi.setProcessor(I2F, new IntToFloatProcessor());
		vmi.setProcessor(I2D, new IntToDoubleProcessor());
		vmi.setProcessor(L2I, new LongToIntProcessor());
		vmi.setProcessor(L2F, new LongToFloatProcessor());
		vmi.setProcessor(L2D, new LongToDoubleProcessor());
		vmi.setProcessor(F2I, new FloatToIntProcessor());
		vmi.setProcessor(F2L, new FloatToLongProcessor());
		vmi.setProcessor(F2D, new FloatToDoubleProcessor());
		vmi.setProcessor(D2I, new DoubleToIntProcessor());
		vmi.setProcessor(D2L, new DoubleToLongProcessor());
		vmi.setProcessor(D2F, new DoubleToFloatProcessor());
		vmi.setProcessor(I2B, new IntToByteProcessor());
		vmi.setProcessor(I2C, new IntToCharProcessor());
		vmi.setProcessor(I2S, new IntToShortProcessor());

		vmi.setProcessor(LCMP, new LongCompareProcessor());
		vmi.setProcessor(FCMPL, new FloatCompareProcessor(-1));
		vmi.setProcessor(FCMPG, new FloatCompareProcessor(1));
		vmi.setProcessor(DCMPL, new DoubleCompareProcessor(-1));
		vmi.setProcessor(DCMPG, new DoubleCompareProcessor(1));

		vmi.setProcessor(IFEQ, new IntJumpProcessor(value -> value == 0));
		vmi.setProcessor(IFNE, new IntJumpProcessor(value -> value != 0));
		vmi.setProcessor(IFLT, new IntJumpProcessor(value -> value < 0));
		vmi.setProcessor(IFGE, new IntJumpProcessor(value -> value >= 0));
		vmi.setProcessor(IFGT, new IntJumpProcessor(value -> value > 0));
		vmi.setProcessor(IFLE, new IntJumpProcessor(value -> value <= 0));

		vmi.setProcessor(IF_ICMPEQ, new BiIntJumpProcessor((v1, v2) -> v1 == v2));
		vmi.setProcessor(IF_ICMPNE, new BiIntJumpProcessor((v1, v2) -> v1 != v2));
		vmi.setProcessor(IF_ICMPLT, new BiIntJumpProcessor((v1, v2) -> v1 < v2));
		vmi.setProcessor(IF_ICMPGE, new BiIntJumpProcessor((v1, v2) -> v1 >= v2));
		vmi.setProcessor(IF_ICMPGT, new BiIntJumpProcessor((v1, v2) -> v1 > v2));
		vmi.setProcessor(IF_ICMPLE, new BiIntJumpProcessor((v1, v2) -> v1 <= v2));

		vmi.setProcessor(IF_ACMPEQ, new BiValueJumpProcessor((v1, v2) -> v1 == v2));
		vmi.setProcessor(IF_ACMPNE, new BiValueJumpProcessor((v1, v2) -> v1 != v2));

		vmi.setProcessor(GOTO, new GotoProcessor());

		// JSR/RET?

		vmi.setProcessor(TABLESWITCH, new TableSwitchProcessor());
		vmi.setProcessor(LOOKUPSWITCH, new LookupSwitchProcessor());

		vmi.setProcessor(IRETURN, new ReturnIntProcessor());
		vmi.setProcessor(LRETURN, new ReturnLongProcessor());
		vmi.setProcessor(FRETURN, new ReturnFloatProcessor());
		vmi.setProcessor(DRETURN, new ReturnDoubleProcessor());
		vmi.setProcessor(ARETURN, new ReturnValueProcessor());
		vmi.setProcessor(RETURN, new ReturnVoidProcessor());

		vmi.setProcessor(GETSTATIC, new GetStaticProcessor());
		vmi.setProcessor(PUTSTATIC, new PutStaticProcessor());
		vmi.setProcessor(GETFIELD, new GetFieldProcessor());
		vmi.setProcessor(PUTFIELD, new PutFieldProcessor());
		vmi.setProcessor(INVOKEVIRTUAL, new VirtualCallProcessor());
		vmi.setProcessor(INVOKESPECIAL, new SpecialCallProcessor());
		vmi.setProcessor(INVOKESTATIC, new StaticCallProcessor());
		vmi.setProcessor(INVOKEINTERFACE, new InterfaceCallProcessor());

		vmi.setProcessor(INVOKEDYNAMIC, new InvokeDynamicLinkerProcessor());
		vmi.setProcessor(NEW, new NewProcessor());
		vmi.setProcessor(ANEWARRAY, new InstanceArrayProcessor());
		vmi.setProcessor(NEWARRAY, new PrimitiveArrayProcessor());
		vmi.setProcessor(ARRAYLENGTH, new ArrayLengthProcessor());

		vmi.setProcessor(ATHROW, new ThrowProcessor());

		vmi.setProcessor(CHECKCAST, new CastProcessor());
		vmi.setProcessor(INSTANCEOF, new InstanceofProcessor());

		vmi.setProcessor(MONITORENTER, new MonitorEnterProcessor());
		vmi.setProcessor(MONITOREXIT, new MonitorExitProcessor());

		vmi.setProcessor(IFNONNULL, new ValueJumpProcessor(value -> !value.isNull()));
		vmi.setProcessor(IFNULL, new ValueJumpProcessor(Value::isNull));
	}
}
