package dev.xdark.ssvm;

import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.asm.Modifier;
import dev.xdark.ssvm.classloading.ClassLoaderData;
import dev.xdark.ssvm.classloading.ClassParseResult;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.PanicException;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.execution.asm.*;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.mirror.*;
import dev.xdark.ssvm.thread.Backtrace;
import dev.xdark.ssvm.thread.SimpleBacktrace;
import dev.xdark.ssvm.util.CrcUtil;
import dev.xdark.ssvm.value.*;
import lombok.val;
import me.coley.cafedude.ClassFile;
import me.coley.cafedude.attribute.AnnotationsAttribute;
import me.coley.cafedude.attribute.Attribute;
import me.coley.cafedude.constant.*;
import me.coley.cafedude.io.AnnotationWriter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.ZipEntry;

import static dev.xdark.ssvm.asm.Modifier.ACC_VM_HIDDEN;
import static dev.xdark.ssvm.asm.VMOpcodes.METHOD_HANDLE;
import static org.objectweb.asm.Opcodes.*;

/**
 * A class to setup the VM instance.
 *
 * @author xDark
 */
public final class NativeJava {

	public static final String CLASS_LOADER_OOP = "classLoaderOop";
	public static final String VM_INDEX = "vmindex";
	public static final String VM_TARGET = "vmtarget";
	public static final String VM_HOLDER = "vmholder";
	public static final String PROTECTION_DOMAIN = "protectionDomain";
	public static final String CONSTANT_POOL = "constantPool";

	/**
	 * Sets up VM instance.
	 *
	 * @param vm
	 * 		VM to set up.
	 */
	static void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		setInstructions(vmi);
		injectPhase2(vm);
		initClass(vm);
		initObject(vm);
		initSystem(vm);
		initThread(vm);
		initClassLoader(vm);
		initThrowable(vm);
		initNativeLibrary(vm);
		initVM(vm);
		initUnsafe(vm);
		initRuntime(vm);
		initDouble(vm);
		initFloat(vm);
		initArray(vm);
		initConstantPool(vm);
		initFS(vm);
		initIoFs(vm);
		initStackTraceElement(vm);
		initReflection(vm);
		initNativeConstructorAccessor(vm);
		initNativeMethodAccessor(vm);
		initAccessController(vm);
		initMethodHandles(vm);
		initModuleSystem(vm);
		initSignal(vm);
		initString(vm);
		initAtomicLong(vm);
		initUcp(vm);
		initZipFile(vm);
		initVMManagementImpl(vm);
		initPackage(vm);
		initPerf(vm);
		initJarFile(vm);
		initStrictMath(vm);
		initTimeZone(vm);
		initCRC32(vm);
		initNativeSeedGenerator(vm);
		initNetworkInterface(vm);
		initSeedGenerator(vm);
		initProxy(vm);

		val utf16 = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StringUTF16");
		if (utf16 != null) {
			vmi.setInvoker(utf16, "isBigEndian", "()Z", ctx -> {
				ctx.setResult(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? IntValue.ONE : IntValue.ZERO);
				return Result.ABORT;
			});
		}
		val win32ErrorMode = (InstanceJavaClass) vm.findBootstrapClass("sun/io/Win32ErrorMode");
		if (win32ErrorMode != null) {
			vmi.setInvoker(win32ErrorMode, "setErrorMode", "(J)J", ctx -> {
				ctx.setResult(LongValue.ZERO);
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
		val vmi = vm.getInterface();
		InstanceJavaClass klass = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/VM");
		if (klass != null) {
			vmi.setInvoker(klass, "initializeFromArchive", "(Ljava/lang/Class;)V", MethodInvoker.noop());
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
		vmi.setInvoker(klass, "initialize", "()V", MethodInvoker.noop());
	}

	/**
	 * Initializes misc/Signal.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initSignal(VirtualMachine vm) {
		InstanceJavaClass signal = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Signal");
		if (signal == null) {
			signal = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Signal");
			if (signal == null) {
				throw new IllegalStateException("Unable to locate Signal class");
			}
		}
		// TODO: implement this?
		val vmi = vm.getInterface();
		MethodInvoker findSignal = ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		};
		if (!vmi.setInvoker(signal, "findSignal0", "(Ljava/lang/String;)I", findSignal)) {
			if (!vmi.setInvoker(signal, "findSignal", "(Ljava/lang/String;)I", findSignal)) {
				throw new IllegalStateException("Could not locate Signal#findSignal");
			}
		}
		vmi.setInvoker(signal, "handle0", "(IJ)J", ctx -> {
			ctx.setResult(LongValue.ZERO);
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val string = symbols.java_lang_String;
		vmi.setInvoker(string, "intern", "()Ljava/lang/String;", ctx -> {
			val str = ctx.getLocals().<InstanceValue>load(0);
			ctx.setResult(vm.getStringPool().intern(str));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/util/concurrent/AtomicLong.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initAtomicLong(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val atomicLong = symbols.java_util_concurrent_atomic_AtomicLong;
		vmi.setInvoker(atomicLong, "VMSupportsCS8", "()Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes sun/misc/URLClassPath.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initUcp(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val ucp = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/URLClassPath");
		if (ucp != null) {
			// static jobjectArray get_lookup_cache_urls(JNIEnv *env, jobject loader, TRAPS) {return NULL;}
			vmi.setInvoker(ucp, "getLookupCacheURLs", "(Ljava/lang/ClassLoader;)[Ljava/net/URL;", ctx -> {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			});
		}
	}

	/**
	 * Initializes java/util/zip/ZipFile.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initZipFile(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val zf = symbols.java_util_zip_ZipFile;
		vmi.setInvoker(zf, "initIDs", "()V", MethodInvoker.noop());
		if (vmi.setInvoker(zf, "open", "(Ljava/lang/String;IJZ)J", ctx -> {
			// Old-style zip file implementation.
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val path = locals.<ObjectValue>load(0);
			helper.checkNotNull(path);
			val zipPath = helper.readUtf8(path);
			int mode = locals.load(1).asInt();
			// last file modification & usemmap are ignored.
			try {
				val handle = vm.getFileDescriptorManager().openZipFile(zipPath, mode);
				if (handle == 0L) {
					helper.throwException(symbols.java_io_IOException, zipPath);
				}
				ctx.setResult(new LongValue(handle));
			} catch (IOException ex) {
				helper.throwException(symbols.java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		})) {
			vmi.setInvoker(zf, "getTotal", "(J)I", ctx -> {
				val zip = vm.getFileDescriptorManager().getZipFile(ctx.getLocals().load(0).asLong());
				if (zip == null) {
					vm.getHelper().throwException(symbols.java_io_IOException, "zip closed");
				}
				ctx.setResult(new IntValue((int) zip.stream().count()));
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "startsWithLOC", "(J)Z", ctx -> {
				ctx.setResult(IntValue.ONE);
				return Result.ABORT;
			});
			vmi.setInvoker(zf, "getEntry", "(J[BZ)J", ctx -> {
				val locals = ctx.getLocals();
				val handle = locals.load(0).asLong();
				val zip = vm.getFileDescriptorManager().getZipFile(handle);
				val helper = vm.getHelper();
				if (zip == null) {
					helper.throwException(symbols.java_io_IOException, "zip closed");
				}
				String entryName = new String(helper.toJavaBytes(locals.load(2)), StandardCharsets.UTF_8);
				if (locals.load(3).asBoolean()) entryName += '/';
				val entry = zip.getEntry(entryName);
				if (entry == null) {
					ctx.setResult(LongValue.ZERO);
				} else {
					// This is really stupid, but I guess that works...
					int position = 0;
					val entries = zip.entries();
					while (entries.hasMoreElements()) {
						if (entry.equals(entries.nextElement())) break;
						position++;
					}
					ctx.setResult(new LongValue(handle + 8L * position));
				}
				return Result.ABORT;
			});
		}
	}

	/**
	 * Initializes sun/management/VMManagementImpl.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initVMManagementImpl(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.sun_management_VMManagementImpl;
		vmi.setInvoker(jc, "getVersion0", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getStringPool().intern(vm.getManagementInterface().getVersion()));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getStartupTime", "()J", ctx -> {
			ctx.setResult(new LongValue(vm.getManagementInterface().getStartupTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getVmArguments0", "()[Ljava/lang/String;", ctx -> {
			val helper = vm.getHelper();
			val args = vm.getManagementInterface().getInputArguments();
			val vmArgs = new ObjectValue[args.size()];
			for (int i = 0; i < args.size(); i++) {
				vmArgs[i] = helper.newUtf8(args.get(i));
			}
			ctx.setResult(helper.toVMValues(vmArgs));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "initOptionalSupportFields", "()V", MethodInvoker.noop());
	}

	/**
	 * Initializes java/lang/Package.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initPackage(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_lang_Package;
		vmi.setInvoker(jc, "getSystemPackage0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(NullValue.INSTANCE);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes Perf.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initPerf(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.perf_Perf;
		vmi.setInvoker(jc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jc, "createLong", "(Ljava/lang/String;IIJ)Ljava/nio/ByteBuffer;", ctx -> {
			val buf = vm.getHelper().invokeStatic(symbols.java_nio_ByteBuffer, "allocateDirect", "(I)Ljava/nio/ByteBuffer;", new Value[0], new Value[]{new IntValue(8)}).getResult();
			ctx.setResult(buf);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/util/jar/JarFile.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initJarFile(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val zf = symbols.java_util_jar_JarFile;
		vmi.setInvoker(zf, "getMetaInfEntryNames", "()[Ljava/lang/String;", ctx -> {
			val handle = ctx.getLocals().<InstanceValue>load(0).getLong("jzfile");
			val zip = vm.getFileDescriptorManager().getZipFile(handle);
			val helper = vm.getHelper();
			if (zip == null) {
				helper.throwException(symbols.java_io_IOException, "zip closed");
			}
			val paths = zip.stream()
					.map(ZipEntry::getName)
					.filter(name -> name.toUpperCase(Locale.ENGLISH).startsWith("META-INF/"))
					.map(helper::newUtf8)
					.toArray(ObjectValue[]::new);
			ctx.setResult(helper.toVMValues(paths));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/StrictMath.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initStrictMath(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_lang_StrictMath;
		vmi.setInvoker(jc, "sin", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.sin(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cos", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.cos(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "tan", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.tan(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "asin", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.asin(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "acos", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.acos(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "atan", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.atan(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "exp", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.exp(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.log(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log10", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.log10(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "sqrt", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.sqrt(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cbrt", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.cbrt(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "IEEEremainder", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.IEEEremainder(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "atan2", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.atan2(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "pow", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.pow(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "sinh", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.sinh(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "cosh", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.cosh(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "tanh", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.tanh(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "hypot", "(DD)D", ctx -> {
			val locals = ctx.getLocals();
			ctx.setResult(new DoubleValue(Math.hypot(locals.load(0).asDouble(), locals.load(2).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "expm1", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.expm1(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "log1p", "(D)D", ctx -> {
			ctx.setResult(new DoubleValue(Math.log1p(ctx.getLocals().load(0).asDouble())));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/util/TimeZone.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initTimeZone(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_util_TimeZone;
		vmi.setInvoker(jc, "getSystemTimeZoneID", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			val helper = vm.getHelper();
			ctx.setResult(helper.newUtf8(vm.getTimeZoneManager().getSystemTimeZoneId(helper.readUtf8(ctx.getLocals().load(0)))));
			return Result.ABORT;
		});
		vmi.setInvoker(jc, "getSystemGMTOffsetID", "()Ljava/lang/String;", ctx -> {
			ctx.setResult(vm.getHelper().newUtf8(vm.getTimeZoneManager().getSystemGMTOffsetId()));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/util/zip/CRC32.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initCRC32(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_util_zip_CRC32;
		vmi.setInvoker(jc, "updateBytes", "(I[BII)I", ctx -> {
			val locals = ctx.getLocals();
			int crc = locals.load(0).asInt();
			val bytes = locals.<ArrayValue>load(1);
			int off = locals.load(2).asInt();
			int len = locals.load(3).asInt();
			for (int x = off + len; off < x; off++) {
				crc = CrcUtil.update(crc, bytes.getByte(off));
			}
			ctx.setResult(new IntValue(crc));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes sun/security/provider/NativeSeedGenerator.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initNativeSeedGenerator(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.sun_security_provider_NativeSeedGenerator;
		vmi.setInvoker(jc, "nativeGenerateSeed", "([B)Z", ctx -> {
			val array = vm.getHelper().checkNotNullArray(ctx.getLocals().load(0));
			int len = array.getLength();
			val rng = new SecureRandom();
			val tmp = new byte[1];
			while (len-- != 0) {
				rng.nextBytes(tmp);
				array.setByte(len, tmp[0]);
			}
			ctx.setResult(IntValue.ONE);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/net/NetworkInterface.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initNetworkInterface(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_net_NetworkInterface;
		vmi.setInvoker(jc, "init", "()V", MethodInvoker.noop());
		// TODO introduce NetworkManager or something like that.
		vmi.setInvoker(jc, "getAll", "()[Ljava/net/NetworkInterface;", ctx -> {
			val helper = vm.getHelper();
			ctx.setResult(helper.emptyArray(jc));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes sun/security/provider/SeedGenerator.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initSeedGenerator(VirtualMachine vm) {
		// TODO remove this stub, let VM decide.
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.sun_security_provider_SeedGenerator;
		vmi.setInvoker(jc, "getSystemEntropy", "()[B", ctx -> {
			val bytes = new byte[20];
			new SecureRandom().nextBytes(bytes);
			ctx.setResult(vm.getHelper().toVMBytes(bytes));
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/reflect/Proxy.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initProxy(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jc = symbols.java_lang_reflect_Proxy;
		vmi.setInvoker(jc, "defineClass0", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BII)Ljava/lang/Class;", ctx -> {
			// Simply invoke defineClass in a loader.
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val loader = locals.load(0);
			val name =locals.load(1);
			val bytes = helper.checkNotNullArray(locals.load(2));
			val off = locals.load(3);
			val len = locals.load(4);
			InstanceJavaClass result;
			if (loader.isNull()) {
				val parsed = vm.getClassDefiner().parseClass(helper.readUtf8(name), helper.toJavaBytes(bytes), off.asInt(), len.asInt(), "JVM_DefineClass");
				if (parsed == null) {
					helper.throwException(symbols.java_lang_InternalError, "Invalid bytecode");
				}
				result = helper.newInstanceClass(NullValue.INSTANCE, NullValue.INSTANCE, parsed.getClassReader(), parsed.getNode());
				vm.getBootClassLoaderData().forceLinkClass(result);
			} else {
				result = ((JavaValue<InstanceJavaClass>)helper.invokeVirtual("defineClass", "(Ljava/lang/String;[BII)Ljava/lang/Class;", new Value[0], new Value[]{
						loader,
						name,
						bytes,
						off,
						len
				}).getResult()).getValue();
				((JavaValue<ClassLoaderData>) ((InstanceValue) loader).getValue(CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue()
						.forceLinkClass(result);
			}
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
	}

	/**
	 * /**
	 * Initializes java/lang/Runtime.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initRuntime(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val runtime = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Runtime");
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val doubleClass = symbols.java_lang_Double;
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val floatClass = symbols.java_lang_Float;
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val array = symbols.java_lang_reflect_Array;
		vmi.setInvoker(array, "getLength", "(Ljava/lang/Object;)I", ctx -> {
			val value = ctx.getLocals().load(0);
			vm.getHelper().checkArray(value);
			ctx.setResult(new IntValue(((ArrayValue) value).getLength()));
			return Result.ABORT;
		});
		vmi.setInvoker(array, "newArray", "(Ljava/lang/Class;I)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val local = locals.load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(local);
			if (!(local instanceof JavaValue)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			val wrapper = ((JavaValue<?>) local).getValue();
			if (!(wrapper instanceof JavaClass)) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			val klass = (JavaClass) wrapper;
			if (klass.isArray()) {
				helper.throwException(symbols.java_lang_IllegalArgumentException);
			}
			val length = locals.load(1).asInt();
			val result = helper.newArray(klass, length);
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
		val vmi = vm.getInterface();
		val fd = vm.getSymbols().java_io_FileDescriptor;

		MethodInvoker set = ctx -> {
			ctx.setResult(new LongValue(mapVMStream(vm, ctx.getLocals().load(0).asInt())));
			return Result.ABORT;
		};
		boolean lateinit = false;
		if (!vmi.setInvoker(fd, "getHandle", "(I)J", set)) {
			lateinit = !vmi.setInvoker(fd, "set", "(I)J", set);
		}
		if (lateinit) {
			vmi.setInvoker(fd, "initIDs", "()V", ctx -> {
				val in = (InstanceValue) fd.getStaticValue("in", "Ljava/io/FileDescriptor;");
				in.setLong("handle", mapVMStream(vm, 0));
				val out = (InstanceValue) fd.getStaticValue("out", "Ljava/io/FileDescriptor;");
				out.setLong("handle", mapVMStream(vm, 1));
				val err = (InstanceValue) fd.getStaticValue("err", "Ljava/io/FileDescriptor;");
				err.setLong("handle", mapVMStream(vm, 2));
				return Result.ABORT;
			});
		} else {
			vmi.setInvoker(fd, "initIDs", "()V", MethodInvoker.noop());
		}

		vmi.setInvoker(fd, "getAppend", "(I)Z", ctx -> {
			ctx.setResult(vm.getFileDescriptorManager().isAppend(ctx.getLocals().load(0).asInt()) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(fd, "close0", "()V", ctx -> {
			val handle = ctx.getLocals().<InstanceValue>load(0).getLong("handle");
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				vm.getHelper().throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		val fos = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileOutputStream");
		vmi.setInvoker(fos, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fos, "writeBytes", "([BIIZ)V", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			val out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) return Result.ABORT;
			val bytes = helper.toJavaBytes(locals.load(1));
			val off = locals.load(2).asInt();
			val len = locals.load(3).asInt();
			try {
				out.write(bytes, off, len);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "write", "(BZ)V", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			val out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) return Result.ABORT;
			try {
				out.write(locals.load(1).asByte());
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "open0", "(Ljava/lang/String;Z)V", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val path = helper.readUtf8(locals.load(1));
			val append = locals.load(2).asBoolean();
			try {
				val handle = vm.getFileDescriptorManager().open(path, append ? FileDescriptorManager.APPEND : FileDescriptorManager.WRITE);
				((InstanceValue) _this.getValue("fd", "Ljava/io/FileDescriptor;")).setLong("handle", handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "close0", "()V", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		val fis = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileInputStream");
		vmi.setInvoker(fis, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fis, "readBytes", "([BII)I", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			val in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(IntValue.M_ONE);
			} else {
				try {
					val off = locals.load(2).asInt();
					val len = locals.load(3).asInt();
					val arraySize = len - off;
					val bytes = new byte[arraySize];
					val read = in.read(bytes);
					if (read > 0) {
						val vmBuffer = locals.<ArrayValue>load(1);
						val memoryManager = vm.getMemoryManager();
						val start = memoryManager.arrayBaseOffset(byte.class) + off;
						val data = vmBuffer.getMemory().getData().slice();
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
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			val in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(IntValue.M_ONE);
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
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			val in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(LongValue.ZERO);
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
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val path = helper.readUtf8(locals.load(1));
			try {
				val handle = vm.getFileDescriptorManager().open(path, FileDescriptorManager.READ);
				((InstanceValue) _this.getValue("fd", "Ljava/io/FileDescriptor;")).setLong("handle", handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "available0", "()I", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			val in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(IntValue.ZERO);
			} else {
				try {
					ctx.setResult(new IntValue(in.available()));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "close0", "()V", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<InstanceValue>load(0);
			val helper = vm.getHelper();
			val handle = helper.getFileStreamHandle(_this);
			try {
				vm.getFileDescriptorManager().close(handle);
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
		val vmi = vm.getInterface();
		val stackTraceElement = (InstanceJavaClass) vm.findBootstrapClass("java/lang/StackTraceElement");
		vmi.setInvoker(stackTraceElement, "initStackTraceElements", "([Ljava/lang/StackTraceElement;Ljava/lang/Throwable;)V", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val arr = helper.checkArray(locals.load(0));
			val ex = helper.<InstanceValue>checkNotNull(locals.load(1));
			val backtrace = ((JavaValue<Backtrace>) ex.getValue("backtrace", "Ljava/lang/Object;")).getValue();

			int x = 0;
			for (int i = backtrace.count(); i != 0; ) {
				val frame = backtrace.get(--i);
				val element = helper.newStackTraceElement(frame, true);
				arr.setValue(x++, element);
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
		val vmi = vm.getInterface();
		val bootLoader = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/loader/BootLoader");
		if (bootLoader != null) {
			vmi.setInvoker(bootLoader, "setBootLoaderUnnamedModule0", "(Ljava/lang/Module;)V", MethodInvoker.noop());
			vmi.setInvoker(bootLoader, "getSystemPackageLocation", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			});
		}
		val module = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Module");
		if (module != null) {
			vmi.setInvoker(module, "defineModule0", "(Ljava/lang/Module;ZLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addReads0", "(Ljava/lang/Module;Ljava/lang/Module;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addExports0", "(Ljava/lang/Module;Ljava/lang/String;Ljava/lang/Module;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addExportsToAll0", "(Ljava/lang/Module;Ljava/lang/String;)V", MethodInvoker.noop());
			vmi.setInvoker(module, "addExportsToAllUnnamed0", "(Ljava/lang/Module;Ljava/lang/String;)V", MethodInvoker.noop());
		}
		val moduleLayer = (InstanceJavaClass) vm.findBootstrapClass("java/lang/ModuleLayer");
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
		val vmi = vm.getInterface();
		val accController = (InstanceJavaClass) vm.findBootstrapClass("java/security/AccessController");
		vmi.setInvoker(accController, "getStackAccessControlContext", "()Ljava/security/AccessControlContext;", ctx -> {
			// TODO implement?
			ctx.setResult(NullValue.INSTANCE);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedExceptionAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedExceptionAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedExceptionAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
					action
			}).getResult();
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(accController, "doPrivileged", "(Ljava/security/PrivilegedAction;Ljava/security/AccessControlContext;)Ljava/lang/Object;", ctx -> {
			val action = ctx.getLocals().load(0);
			val helper = vm.getHelper();
			helper.checkNotNull(action);
			val result = helper.invokeInterface(vm.getSymbols().java_security_PrivilegedAction, "run", "()Ljava/lang/Object;", new Value[0], new Value[]{
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val jlc = symbols.java_lang_Class;
		vmi.setInvoker(jlc, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(jlc, "getPrimitiveClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			val name = vm.getHelper().readUtf8(ctx.getLocals().load(0));
			val primitives = vm.getPrimitives();
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
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "forName0", "(Ljava/lang/String;ZLjava/lang/ClassLoader;Ljava/lang/Class;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val name = helper.readUtf8(helper.checkNotNull(locals.load(0)));
			val initialize = locals.load(1).asBoolean();
			val loader = locals.load(2);
			val klass = helper.findClass(loader, name.replace('.', '/'), initialize);
			if (Modifier.isHiddenMember(klass.getModifiers())) {
				helper.throwException(symbols.java_lang_ClassNotFoundException, name);
			}
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		val classNameInit = (MethodInvoker) ctx -> {
			ctx.setResult(vm.getStringPool().intern(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getName()));
			return Result.ABORT;
		};
		if (!vmi.setInvoker(jlc, "getName0", "()Ljava/lang/String;", classNameInit)) {
			if (!vmi.setInvoker(jlc, "initClassName", "()Ljava/lang/String;", classNameInit)) {
				throw new IllegalStateException("Unable to locate Class name init method");
			}
		}
		vmi.setInvoker(jlc, "isArray", "()Z", ctx -> {
			ctx.setResult(ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().isArray() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isAssignableFrom", "(Ljava/lang/Class;)Z", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			val arg = locals.<JavaValue<JavaClass>>load(1).getValue();
			ctx.setResult(_this.isAssignableFrom(arg) ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInterface", "()Z", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(_this.isInterface() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isPrimitive", "()Z", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(_this.isPrimitive() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSuperclass", "()Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			val superClass = _this.getSuperClass();
			ctx.setResult(superClass == null ? NullValue.INSTANCE : superClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getModifiers", "()I", ctx -> {
			val locals = ctx.getLocals();
			val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(new IntValue(Modifier.erase(_this.getModifiers())));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredConstructors0", "(Z)[Ljava/lang/reflect/Constructor;", ctx -> {
			val locals = ctx.getLocals();
			val klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				val empty = helper.emptyArray(symbols.java_lang_reflect_Constructor);
				ctx.setResult(empty);
			} else {
				klass.initialize();
				val pool = vm.getStringPool();
				val publicOnly = locals.load(1).asBoolean();
				val methods = ((InstanceJavaClass) klass).getDeclaredConstructors(publicOnly);
				val loader = klass.getClassLoader();
				val refFactory = symbols.reflect_ReflectionFactory;
				val reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
				val result = helper.newArray(symbols.java_lang_reflect_Constructor, methods.size());
				val classArray = helper.emptyArray(symbols.java_lang_Class);
				val callerOop = klass.getOop();
				for (int j = 0; j < methods.size(); j++) {
					val mn = methods.get(j);
					val types = mn.getArgumentTypes();
					val parameters = helper.convertClasses(helper.convertTypes(loader, types, false));
					val c = helper.invokeVirtual("newConstructor", "(Ljava/lang/Class;[Ljava/lang/Class;[Ljava/lang/Class;IILjava/lang/String;[B[B)Ljava/lang/reflect/Constructor;", new Value[0], new Value[]{
							reflectionFactory,
							callerOop,
							parameters,
							classArray,
							new IntValue(Modifier.erase(mn.getAccess())),
							new IntValue(mn.getSlot()),
							pool.intern(mn.getSignature()),
							NullValue.INSTANCE,
							NullValue.INSTANCE
					}).getResult();
					result.setValue(j, (ObjectValue) c);
				}
				ctx.setResult(result);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredMethods0", "(Z)[Ljava/lang/reflect/Method;", ctx -> {
			val locals = ctx.getLocals();
			JavaClass klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				val empty = helper.emptyArray(symbols.java_lang_reflect_Method);
				ctx.setResult(empty);
				return Result.ABORT;
			}
			klass.initialize();
			val pool = vm.getStringPool();
			val publicOnly = locals.load(1).asBoolean();
			val methods = ((InstanceJavaClass) klass).getDeclaredMethods(publicOnly);
			val loader = klass.getClassLoader();
			val refFactory = symbols.reflect_ReflectionFactory;
			val reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
			val result = helper.newArray(symbols.java_lang_reflect_Method, methods.size());
			val classArray = helper.emptyArray(symbols.java_lang_Class);
			val callerOop = klass.getOop();
			for (int j = 0; j < methods.size(); j++) {
				val mn = methods.get(j);
				val types = mn.getArgumentTypes();
				val rt = helper.findClass(loader, mn.getReturnType().getInternalName(), false);
				val parameters = helper.convertClasses(helper.convertTypes(loader, types, false));
				val c = helper.invokeVirtual("newMethod", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;Ljava/lang/Class;[Ljava/lang/Class;IILjava/lang/String;[B[B[B)Ljava/lang/reflect/Method;", new Value[0], new Value[]{
						reflectionFactory,
						callerOop,
						pool.intern(mn.getName()),
						parameters,
						rt.getOop(),
						classArray,
						new IntValue(Modifier.erase(mn.getAccess())),
						new IntValue(mn.getSlot()),
						pool.intern(mn.getSignature()),
						NullValue.INSTANCE,
						NullValue.INSTANCE,
						NullValue.INSTANCE
				}).getResult();
				result.setValue(j, (ObjectValue) c);
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaredFields0", "(Z)[Ljava/lang/reflect/Field;", ctx -> {
			val locals = ctx.getLocals();
			val klass = locals.<JavaValue<JavaClass>>load(0).getValue();
			val helper = vm.getHelper();
			if (!(klass instanceof InstanceJavaClass)) {
				val empty = helper.emptyArray(symbols.java_lang_reflect_Field);
				ctx.setResult(empty);
			} else {
				klass.initialize();
				val pool = vm.getStringPool();
				val publicOnly = locals.load(1).asBoolean();
				val fields = ((InstanceJavaClass) klass).getDeclaredFields(publicOnly);
				val loader = klass.getClassLoader();
				val refFactory = symbols.reflect_ReflectionFactory;
				val reflectionFactory = (InstanceValue) helper.invokeStatic(refFactory, "getReflectionFactory", "()" + refFactory.getDescriptor(), new Value[0], new Value[0]).getResult();
				val result = helper.newArray(symbols.java_lang_reflect_Field, fields.size());
				val callerOop = klass.getOop();
				for (int j = 0; j < fields.size(); j++) {
					val fn = fields.get(j);
					val type = helper.findClass(loader, fn.getType().getInternalName(), false);
					val c = helper.invokeVirtual("newField", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;IILjava/lang/String;[B)Ljava/lang/reflect/Field;", new Value[0], new Value[]{
							reflectionFactory,
							callerOop,
							pool.intern(fn.getName()),
							type.getOop(),
							new IntValue(Modifier.erase(fn.getAccess())),
							new IntValue(fn.getSlot()),
							pool.intern(fn.getSignature()),
							NullValue.INSTANCE
					}).getResult();
					result.setValue(j, (ObjectValue) c);
				}
				ctx.setResult(result);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getInterfaces0", "()[Ljava/lang/Class;", ctx -> {
			val _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			val interfaces = _this.getInterfaces();
			val types = vm.getHelper().convertClasses(interfaces);
			ctx.setResult(types);
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getEnclosingMethod0", "()[Ljava/lang/Object;", ctx -> {
			val klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val node = ((InstanceJavaClass) klasas).getNode();
				val enclosingClass = node.outerClass;
				val enclosingMethod = node.outerMethod;
				val enclosingDesc = node.outerMethodDesc;
				if (enclosingClass == null || enclosingMethod == null || enclosingDesc == null) {
					ctx.setResult(NullValue.INSTANCE);
				} else {
					val helper = vm.getHelper();
					val pool = vm.getStringPool();
					val outerHost = helper.findClass(ctx.getOwner().getClassLoader(), enclosingClass, false);
					ctx.setResult(helper.toVMValues(new ObjectValue[]{
							outerHost.getOop(),
							pool.intern(enclosingMethod),
							pool.intern(enclosingDesc)
					}));
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getDeclaringClass0", "()Ljava/lang/Class;", ctx -> {
			val klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val node = ((InstanceJavaClass) klasas).getNode();
				val nestHostClass = node.nestHostClass;
				if (nestHostClass == null) {
					ctx.setResult(NullValue.INSTANCE);
				} else {
					val helper = vm.getHelper();
					val oop = helper.findClass(ctx.getOwner().getClassLoader(), nestHostClass, false);
					ctx.setResult(oop.getOop());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getSimpleBinaryName0", "()Ljava/lang/String;", ctx -> {
			val klasas = ((JavaValue<JavaClass>) ctx.getLocals().load(0)).getValue();
			if (!(klasas instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val name = klasas.getInternalName();
				val idx = name.lastIndexOf('$');
				if (idx != -1) {
					ctx.setResult(vm.getStringPool().intern(name.substring(idx + 1)));
				} else {
					ctx.setResult(NullValue.INSTANCE);
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "isInstance", "(Ljava/lang/Object;)Z", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				ctx.setResult(IntValue.ZERO);
			} else {
				val klass = ((ObjectValue) value).getJavaClass();
				val _this = locals.<JavaValue<JavaClass>>load(0).getValue();
				ctx.setResult(_this.isAssignableFrom(klass) ? IntValue.ONE : IntValue.ZERO);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getComponentType", "()Ljava/lang/Class;", ctx -> {
			val type = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue().getComponentType();
			ctx.setResult(type == null ? NullValue.INSTANCE : type.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getProtectionDomain0", "()Ljava/security/ProtectionDomain;", ctx -> {
			val _this = ctx.getLocals().<InstanceValue>load(0);
			ctx.setResult(_this.getValue(PROTECTION_DOMAIN, "Ljava/security/ProtectionDomain;"));
			return Result.ABORT;
		});
		vmi.setInvoker(jlc, "getRawAnnotations", "()[B", ctx -> {
			val _this = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			if (!(_this instanceof InstanceJavaClass)) {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			}
			val classFile = ((InstanceJavaClass) _this).getRawClassFile();
			Attribute attribute = null;
			for (val candidate : classFile.getAttributes()) {
				if ("RuntimeVisibleAnnotations".equals(((CpUtf8) classFile.getCp(candidate.getNameIndex())).getText())) {
					attribute = candidate;
					break;
				}
			}
			if (!(attribute instanceof AnnotationsAttribute)) {
				ctx.setResult(NullValue.INSTANCE);
				return Result.ABORT;
			}
			val baos = new ByteArrayOutputStream();
			val writer = new AnnotationWriter(new DataOutputStream(baos));
			try {
				writer.writeAnnotations((AnnotationsAttribute) attribute);
			} catch (IOException ex) {
				throw new RuntimeException(ex); // Should never happen.
			}
			ctx.setResult(vm.getHelper().toVMBytes(baos.toByteArray()));
			return Result.ABORT;
		});

		val cpClass = symbols.reflect_ConstantPool;
		vmi.setInvoker(jlc, "getConstantPool", "()" + cpClass.getDescriptor(), ctx -> {
			val _this = ctx.getLocals().<InstanceValue>load(0);
			ObjectValue cp = _this.getValue(CONSTANT_POOL, "Ljava/lang/Object;");
			if (cp.isNull()) {
				cpClass.initialize();
				val instance = vm.getMemoryManager().newInstance(cpClass);
				instance.initialize();
				instance.setValue("constantPoolOop", "Ljava/lang/Object;", _this);
				_this.setValue(CONSTANT_POOL, "Ljava/lang/Object;", instance);
				cp = instance;
			}
			ctx.setResult(cp);
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val object = symbols.java_lang_Object;
		vmi.setInvoker(object, "registerNatives", "()V", MethodInvoker.noop());
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
			val locals = ctx.getLocals();
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
			val _this = ctx.getLocals().<ObjectValue>load(0);
			val type = _this.getJavaClass();
			val helper = vm.getHelper();
			val memoryManager = vm.getMemoryManager();
			ObjectValue clone;
			if (type instanceof ArrayJavaClass) {
				val arr = (ArrayValue) _this;
				clone = memoryManager.newArray((ArrayJavaClass) type, arr.getLength(),
						memoryManager.arrayIndexScale(type.getComponentType()));
			} else {
				clone = memoryManager.newInstance((InstanceJavaClass) type);
			}
			val originalOffset = memoryManager.valueBaseOffset(_this);
			val offset = memoryManager.valueBaseOffset(clone);
			helper.checkEquals(originalOffset, offset);
			val copyTo = (ByteBuffer) clone.getMemory().getData().slice().position(offset);
			val copyFrom = (ByteBuffer) _this.getMemory().getData().slice().position(offset);
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val sys = symbols.java_lang_System;
		vmi.setInvoker(sys, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(sys, "currentTimeMillis", "()J", ctx -> {
			ctx.setResult(new LongValue(System.currentTimeMillis()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "nanoTime", "()J", ctx -> {
			ctx.setResult(new LongValue(System.nanoTime()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val src = helper.checkNotNullArray(locals.load(0));
			val dst = helper.checkNotNullArray(locals.load(2));
			int srcPos = locals.load(1).asInt();
			int dstPos = locals.load(3).asInt();
			int length = locals.load(4).asInt();
			val memoryManager = vm.getMemoryManager();
			val srcComponent = src.getJavaClass().getComponentType();
			val dstComponent = dst.getJavaClass().getComponentType();

			int scale = memoryManager.arrayIndexScale(srcComponent);
			helper.checkEquals(scale, memoryManager.arrayIndexScale(dstComponent));

			int start = memoryManager.arrayBaseOffset(srcComponent);
			helper.checkEquals(start, memoryManager.arrayBaseOffset(dstComponent));

			val srcData = src.getMemory().getData().slice();
			int dataStartPos = start + srcPos * scale;
			srcData.position(dataStartPos).limit(dataStartPos + length * scale);
			val dstData = dst.getMemory().getData().slice();
			dstData.position(start + dstPos * scale);
			dstData.put(srcData);

			return Result.ABORT;
		});
		vmi.setInvoker(sys, "identityHashCode", "(Ljava/lang/Object;)I", ctx -> {
			ctx.setResult(new IntValue(ctx.getLocals().load(0).hashCode()));
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "initProperties", "(Ljava/util/Properties;)Ljava/util/Properties;", ctx -> {
			val value = ctx.getLocals().<InstanceValue>load(0);
			val jc = (InstanceJavaClass) value.getJavaClass();
			val mn = jc.getVirtualMethod("setProperty", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
			val properties = vm.getProperties();
			val helper = vm.getHelper();

			for (val entry : properties.entrySet()) {
				val key = entry.getKey();
				val property = entry.getValue();
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
			val stream = ctx.getLocals().load(0);
			sys.setFieldValue("in", "Ljava/io/InputStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setOut0", "(Ljava/io/PrintStream;)V", ctx -> {
			val stream = ctx.getLocals().load(0);
			sys.setFieldValue("out", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "setErr0", "(Ljava/io/PrintStream;)V", ctx -> {
			val stream = ctx.getLocals().load(0);
			sys.setFieldValue("err", "Ljava/io/PrintStream;", stream);
			return Result.ABORT;
		});
		vmi.setInvoker(sys, "mapLibraryName", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			val name = ctx.getLocals().<ObjectValue>load(0);
			val helper = vm.getHelper();
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val thread = symbols.java_lang_Thread;
		vmi.setInvoker(thread, "registerNatives", "()V", MethodInvoker.noop());
		vmi.setInvoker(thread, "currentThread", "()Ljava/lang/Thread;", ctx -> {
			ctx.setResult(vm.currentThread().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "interrupt", "()V", ctx -> {
			val th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.interrupt();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "setPriority0", "(I)V", ctx -> {
			val locals = ctx.getLocals();
			val th = vm.getThreadManager().getVmThread(locals.<InstanceValue>load(0));
			th.setPriority(locals.load(1).asInt());
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "start0", "()V", ctx -> {
			val th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			th.start();
			return Result.ABORT;
		});
		vmi.setInvoker(thread, "isAlive", "()Z", ctx -> {
			val th = vm.getThreadManager().getVmThread(ctx.getLocals().<InstanceValue>load(0));
			ctx.setResult(th.isAlive() ? IntValue.ONE : IntValue.ZERO);
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val classLoader = symbols.java_lang_ClassLoader;
		vmi.setInvoker(classLoader, "registerNatives", "()V", MethodInvoker.noop());
		val clInitHook = (MethodInvoker) ctx -> {
			val oop = vm.getMemoryManager().newJavaInstance(symbols.java_lang_Object, new ClassLoaderData());
			ctx.getLocals().<InstanceValue>load(0)
					.setValue(CLASS_LOADER_OOP, "Ljava/lang/Object;", oop);
			return Result.CONTINUE;
		};
		if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/String;Ljava/lang/ClassLoader;)V", clInitHook)) {
			if (!vmi.setInvoker(classLoader, "<init>", "(Ljava/lang/Void;Ljava/lang/ClassLoader;)V", clInitHook)) {
				throw new IllegalStateException("Unable to locate ClassLoader init constructor");
			}
		}
		val defineClass1 = (MethodInvoker) ctx -> {
			val helper = ctx.getHelper();
			val locals = ctx.getLocals();
			val loader = locals.<ObjectValue>load(0);
			val name = locals.<ObjectValue>load(1);
			val b = helper.checkArray(locals.load(2));
			val off = locals.load(3).asInt();
			val length = locals.load(4).asInt();
			val pd = locals.<ObjectValue>load(5);
			val source = locals.<ObjectValue>load(6);
			val bytes = helper.toJavaBytes(b);
			val defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, helper.readUtf8(source));
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		};
		if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
			if (!vmi.setInvoker(classLoader, "defineClass1", "(Ljava/lang/String;[BIILjava/security/ProtectionDomain;Ljava/lang/String;)Ljava/lang/Class;", defineClass1)) {
				throw new IllegalStateException("Could not locate ClassLoader#defineClass1");
			}
		}
		vmi.setInvoker(classLoader, "findLoadedClass0", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val name = locals.load(1);
			val helper = vm.getHelper();
			helper.checkNotNull(name);
			val loader = locals.<InstanceValue>load(0);
			val oop = ((JavaValue<ClassLoaderData>) loader.getValue(CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
			InstanceJavaClass loadedClass = oop.getClass(helper.readUtf8(name).replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? NullValue.INSTANCE : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "findBootstrapClass", "(Ljava/lang/String;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val name = locals.load(1);
			val helper = vm.getHelper();
			helper.checkNotNull(name);
			val s = helper.readUtf8(name);
			InstanceJavaClass loadedClass = (InstanceJavaClass) vm.findBootstrapClass(s.replace('.', '/'));
			if (loadedClass != null && Modifier.isHiddenMember(loadedClass.getModifiers())) {
				loadedClass = null;
			}
			ctx.setResult(loadedClass == null ? NullValue.INSTANCE : loadedClass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "findBuiltinLib", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(ctx.getLocals().load(0));
			return Result.ABORT;
		});
		vmi.setInvoker(classLoader, "resolveClass0", "(Ljava/lang/Class;)V", ctx -> {
			val c = ctx.getLocals().load(1);
			val helper = vm.getHelper();
			helper.<JavaValue<JavaClass>>checkNotNull(c).getValue().initialize();
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
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val throwable = symbols.java_lang_Throwable;
		vmi.setInvoker(throwable, "fillInStackTrace", "(I)Ljava/lang/Throwable;", ctx -> {
			val exception = ctx.getLocals().<InstanceValue>load(0);
			val threadManager = vm.getThreadManager();
			val vmBacktrace = threadManager.currentThread().getBacktrace();
			val copy = new SimpleBacktrace();
			for (val frame : vmBacktrace) {
				val frameCtx = frame.getExecutionContext();
				if (frameCtx != null && Modifier.isHiddenFrame(frameCtx.getMethod().getAccess())) {
					continue;
				}
				copy.push(frame.freeze());
			}
			val backtrace = vm.getMemoryManager().newJavaInstance(symbols.java_lang_Object, copy);
			exception.setValue("backtrace", "Ljava/lang/Object;", backtrace);
			if (throwable.hasVirtualField("depth", "I")) {
				exception.setInt("depth", copy.count());
			}
			ctx.setResult(exception);
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceDepth", "()I", ctx -> {
			val backtrace = ((JavaValue<Backtrace>) ((InstanceValue) ctx.getLocals().load(0)).getValue("backtrace", "Ljava/lang/Object;")).getValue();
			ctx.setResult(new IntValue(backtrace.count()));
			return Result.ABORT;
		});
		vmi.setInvoker(throwable, "getStackTraceElement", "(I)Ljava/lang/StackTraceElement;", ctx -> {
			val locals = ctx.getLocals();
			val backtrace = ((JavaValue<Backtrace>) ((InstanceValue) locals.load(0)).getValue("backtrace", "Ljava/lang/Object;")).getValue();
			int idx = locals.load(1).asInt();
			val helper = vm.getHelper();
			int len = backtrace.count();
			helper.rangeCheck(idx, 0, len);
			val element = helper.newStackTraceElement(backtrace.get(len - idx - 1), false);
			ctx.setResult(element);
			return Result.ABORT;
		});
	}

	/**
	 * Initializes java/lang/ClassLoader$NativeLibrary.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initNativeLibrary(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val library = symbols.java_lang_ClassLoader$NativeLibrary;
		vmi.setInvoker(library, "load", "(Ljava/lang/String;Z)V", ctx -> {
			// TODO
			val _this = ctx.getLocals().<InstanceValue>load(0);
			_this.setBoolean("loaded", true);
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
		val vmi = vm.getInterface();
		InstanceJavaClass reflection = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/Reflection");
		if (reflection == null) {
			reflection = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/Reflection");
			if (reflection == null) {
				throw new IllegalStateException("Unable to locate Reflection class");
			}
		}
		vmi.setInvoker(reflection, "getCallerClass", "()Ljava/lang/Class;", ctx -> {
			val backtrace = vm.currentThread().getBacktrace();
			ctx.setResult(backtrace.get(backtrace.count() - 3).getDeclaringClass().getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(reflection, "getClassAccessFlags", "(Ljava/lang/Class;)I", ctx -> {
			val klass = ctx.getLocals().<JavaValue<JavaClass>>load(0).getValue();
			ctx.setResult(new IntValue(Modifier.erase(klass.getModifiers())));
			return Result.ABORT;
		});
		// TODO FIXME
		vmi.setInvoker(reflection, "isCallerSensitive", "(Ljava/lang/reflect/Method;)Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
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
		val vmi = vm.getInterface();
		InstanceJavaClass accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeConstructorAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeConstructorAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeConstructorAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "newInstance0", "(Ljava/lang/reflect/Constructor;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val c = locals.<InstanceValue>load(0);
			val slot = c.getInt("slot");
			val declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) c.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val helper = vm.getHelper();
			val methods = declaringClass.getDeclaredConstructors(false);
			JavaMethod mn = null;
			for (val m : methods) {
				if (slot == m.getSlot()) {
					mn = m;
					break;
				}
			}
			if (mn == null || !"<init>".equals(mn.getName())) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			val values = locals.load(1);
			Value[] converted;
			val types = mn.getArgumentTypes();
			if (!values.isNull()) {
				val passedArgs = (ArrayValue) values;
				helper.checkEquals(passedArgs.getLength(), types.length);
				converted = convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			} else {
				helper.checkEquals(types.length, 0);
				converted = new Value[0];
			}
			val instance = vm.getMemoryManager().newInstance(declaringClass);
			val args = new Value[converted.length + 1];
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
		val vmi = vm.getInterface();
		InstanceJavaClass accessor = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/reflect/NativeMethodAccessorImpl");
		if (accessor == null) {
			accessor = (InstanceJavaClass) vm.findBootstrapClass("sun/reflect/NativeMethodAccessorImpl");
			if (accessor == null) {
				throw new IllegalStateException("Unable to locate NativeMethodAccessorImpl class");
			}
		}
		vmi.setInvoker(accessor, "invoke0", "(Ljava/lang/reflect/Method;Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val m = locals.<InstanceValue>load(0);
			val slot = m.getInt("slot");
			val declaringClass = (InstanceJavaClass) ((JavaValue<JavaClass>) m.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val helper = vm.getHelper();
			val methods = declaringClass.getDeclaredMethods(false);
			JavaMethod mn = null;
			for (val candidate : methods) {
				if (slot == candidate.getSlot()) {
					mn = candidate;
					break;
				}
			}
			if (mn == null) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			val instance = locals.load(1);
			val isStatic = (mn.getAccess() & ACC_STATIC) != 0;
			if (!isStatic && instance.isNull()) {
				helper.throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			}
			val values = locals.load(2);
			val types = mn.getArgumentTypes();
			val passedArgs = (ArrayValue) values;
			helper.checkEquals(passedArgs.getLength(), types.length);
			Value[] args = convertReflectionArgs(vm, declaringClass.getClassLoader(), types, passedArgs);
			if (!isStatic) {
				val prev = args;
				args = new Value[args.length + 1];
				args[0] = instance;
				System.arraycopy(prev, 0, args, 1, prev.length);
			}
			val name = mn.getName();
			val desc = mn.getDesc();
			ExecutionContext executed;
			if (isStatic) {
				executed = helper.invokeStatic(declaringClass, name, desc, new Value[0], args);
			} else {
				executed = helper.invokeVirtual(name, desc, new Value[0], args);
			}
			Value result = executed.getResult();
			if (result == null) result = NullValue.INSTANCE; // void
			else {
				result = helper.boxGeneric(result, executed.getMethod().getReturnType());
			}
			ctx.setResult(result);
			return Result.ABORT;
		});
	}

	/**
	 * Sets up jdk/internal/misc/Unsafe.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initUnsafe(VirtualMachine vm) {
		InstanceJavaClass unsafe = (InstanceJavaClass) vm.findBootstrapClass("jdk/internal/misc/Unsafe");
		UnsafeHelper unsafeHelper;
		if (unsafe == null) {
			unsafe = (InstanceJavaClass) vm.findBootstrapClass("sun/misc/Unsafe");
			unsafeHelper = new OldUnsafeHelper();
		} else {
			unsafeHelper = new NewUnsafeHelper();
		}
		vm.getInterface().setInvoker(unsafe, "registerNatives", "()V", MethodInvoker.noop());
		initUnsafe(vm, unsafe, unsafeHelper);
	}

	/**
	 * Sets up jdk/internal/misc/Unsafe.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param unsafe
	 * 		Unsafe class.
	 * @param uhelper
	 * 		Platform-specific implementation provider.
	 */
	private static void initUnsafe(VirtualMachine vm, InstanceJavaClass unsafe, UnsafeHelper uhelper) {
		val vmi = vm.getInterface();
		vmi.setInvoker(unsafe, uhelper.allocateMemory(), "(J)J", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val block = memoryManager.allocateDirect(ctx.getLocals().load(1).asLong());
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(new LongValue(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.reallocateMemory(), "(JJ)J", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val address = locals.load(1).asLong();
			val bytes = locals.load(3).asLong();
			val block = memoryManager.reallocateDirect(address, bytes);
			if (block == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_OutOfMemoryError);
			}
			ctx.setResult(new LongValue(block.getAddress()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "freeMemory", "(J)V", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val address = locals.load(1).asLong();
			if (!memoryManager.freeMemory(address)) {
				throw new PanicException("Segfault");
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.setMemory(), "(Ljava/lang/Object;JJB)V", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.<ObjectValue>load(1);
			val memory = value.getMemory().getData();
			long offset = locals.load(2).asLong();
			val bytes = locals.load(4).asLong();
			val b = locals.load(6).asByte();
			for (; offset < bytes; offset++) {
				memory.put((int) offset, b);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayBaseOffset(), "(Ljava/lang/Class;)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.<JavaValue<JavaClass>>load(1);
			val klass = value.getValue();
			val component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(new IntValue(vm.getMemoryManager().arrayBaseOffset(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.arrayIndexScale(), "(Ljava/lang/Class;)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.<JavaValue<JavaClass>>load(1);
			val klass = value.getValue();
			val component = klass.getComponentType();
			if (component == null) {
				vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
			} else {
				ctx.setResult(new IntValue(vm.getMemoryManager().arrayIndexScale(component)));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.addressSize(), "()I", ctx -> {
			ctx.setResult(new IntValue(vm.getMemoryManager().addressSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "isBigEndian0", "()Z", ctx -> {
			ctx.setResult(vm.getMemoryManager().getByteOrder() == ByteOrder.BIG_ENDIAN ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "unalignedAccess0", "()Z", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "objectFieldOffset1", "(Ljava/lang/Class;Ljava/lang/String;)J", ctx -> {
			val locals = ctx.getLocals();
			val klass = locals.<JavaValue<JavaClass>>load(1);
			val wrapper = klass.getValue();
			if (!(wrapper instanceof InstanceJavaClass)) {
				ctx.setResult(LongValue.M_ONE);
			} else {
				val utf = vm.getHelper().readUtf8(locals.load(2));
				long offset = ((InstanceJavaClass) wrapper).getFieldOffsetRecursively(utf);
				if (offset != -1L) offset += vm.getMemoryManager().valueBaseOffset(klass);
				ctx.setResult(new LongValue(offset));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "loadFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "storeFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, "fullFence", "()V", MethodInvoker.noop());
		vmi.setInvoker(unsafe, uhelper.compareAndSetInt(), "(Ljava/lang/Object;JII)Z", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) obj;
			val offset = (int) locals.load(2).asLong();
			val expected = locals.load(4).asInt();
			val x = locals.load(5).asInt();
			val memoryManager = vm.getMemoryManager();
			val result = memoryManager.readInt(value, offset) == expected;
			if (result) {
				memoryManager.writeInt(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObjectVolatile", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) obj;
			val offset = (int) locals.load(2).asLong();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue(value, offset));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.compareAndSetReference(), "(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val obj = locals.load(1);
			if (obj.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) obj;
			val offset = (int) locals.load(2).asLong();
			val expected = locals.<ObjectValue>load(4);
			val x = locals.<ObjectValue>load(5);
			val memoryManager = vm.getMemoryManager();
			val result = memoryManager.readValue(value, offset) == expected;
			if (result) {
				memoryManager.writeValue(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.compareAndSetLong(), "(Ljava/lang/Object;JJJ)Z", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val $value = locals.load(1);
			if ($value.isNull()) {
				throw new PanicException("Segfault");
			}
			val value = (ObjectValue) $value;
			val offset = (int) locals.load(2).asLong();
			val expected = locals.load(4).asLong();
			val x = locals.load(6).asLong();
			val memoryManager = vm.getMemoryManager();
			val result = memoryManager.readLong(value, offset) == expected;
			if (result) {
				memoryManager.writeLong(value, offset, x);
			}
			ctx.setResult(result ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putObjectVolatile", "(Ljava/lang/Object;JLjava/lang/Object;)V", ctx -> {
			val helper = vm.getHelper();
			val locals = ctx.getLocals();
			val o = locals.load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = (int) locals.load(2).asLong();
			val value = locals.load(4);
			val memoryManager = vm.getMemoryManager();
			memoryManager.writeValue((ObjectValue) o, offset, (ObjectValue) value);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getIntVolatile", "(Ljava/lang/Object;J)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(new IntValue(memoryManager.readInt((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.ensureClassInitialized(), "(Ljava/lang/Class;)V", ctx -> {
			val value = ctx.getLocals().load(1);
			vm.getHelper().<JavaValue<JavaClass>>checkNotNull(value).getValue().initialize();
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(memoryManager.readValue((ObjectValue) value, offset));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.objectFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			val helper = vm.getHelper();
			val field = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(1));
			val declaringClass = ((JavaValue<InstanceJavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val slot = field.getInt("slot");
			for (val fn : declaringClass.getDeclaredFields(false)) {
				if (slot == fn.getSlot()) {
					val offset = vm.getMemoryManager().valueBaseOffset(declaringClass) + fn.getOffset();
					ctx.setResult(new LongValue(offset));
					return Result.ABORT;
				}
			}
			ctx.setResult(LongValue.M_ONE);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldOffset(), "(Ljava/lang/reflect/Field;)J", ctx -> {
			val helper = vm.getHelper();
			val field = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(1));
			val declaringClass = ((JavaValue<InstanceJavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			val slot = field.getInt("slot");
			for (val fn : declaringClass.getDeclaredFields(false)) {
				if (slot == fn.getSlot()) {
					val offset = vm.getMemoryManager().getStaticOffset(declaringClass) + fn.getOffset();
					ctx.setResult(new LongValue(offset));
					return Result.ABORT;
				}
			}
			ctx.setResult(LongValue.M_ONE);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putLong", "(JJ)V", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val block = memoryManager.getMemory(locals.load(1).asLong());
			if (block == null) {
				throw new PanicException("Segfault");
			}
			block.getData().putLong(0, locals.load(3).asLong());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getByte", "(J)B", ctx -> {
			val memoryManager = vm.getMemoryManager();
			val locals = ctx.getLocals();
			val block = memoryManager.getMemory(locals.load(1).asLong());
			if (block == null) {
				throw new PanicException("Segfault");
			}
			ctx.setResult(new IntValue(block.getData().get(0)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "putInt", "(Ljava/lang/Object;JI)V", ctx -> {
			val locals = ctx.getLocals();
			val o = locals.<ObjectValue>load(1);
			if (o.isNull()) {
				throw new PanicException("Segfault");
			}
			o.getMemory().getData().putInt((int) locals.load(2).asLong(), locals.load(4).asInt());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.staticFieldBase(), "(Ljava/lang/reflect/Field;)Ljava/lang/Object;", ctx -> {
			val field = vm.getHelper().<InstanceValue>checkNotNull(ctx.getLocals().<ObjectValue>load(1));
			val klass = ((JavaValue<JavaClass>) field.getValue("clazz", "Ljava/lang/Class;")).getValue();
			ctx.setResult(klass.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.defineClass(), "(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val helper = ctx.getHelper();
			val loader = locals.<ObjectValue>load(5);
			val name = locals.<ObjectValue>load(1);
			val b = helper.checkArray(locals.load(2));
			val off = locals.load(3).asInt();
			val length = locals.load(4).asInt();
			val pd = locals.<ObjectValue>load(6);
			val bytes = helper.toJavaBytes(b);
			val defined = helper.defineClass(loader, helper.readUtf8(name), bytes, off, length, pd, "JVM_DefineClass");
			ctx.setResult(defined.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.shouldBeInitialized(), "(Ljava/lang/Class;)Z", ctx -> {
			val value = vm.getHelper().<JavaValue<JavaClass>>checkNotNull(ctx.getLocals().load(1)).getValue();
			ctx.setResult(value instanceof InstanceJavaClass && ((InstanceJavaClass) value).shouldBeInitialized() ? IntValue.ONE : IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, uhelper.pageSize(), "()I", ctx -> {
			ctx.setResult(new IntValue(vm.getMemoryManager().pageSize()));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLongVolatile", "(Ljava/lang/Object;J)J", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(new LongValue(memoryManager.readLong((ObjectValue) value, offset)));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getLong", "(J)J", ctx -> {
			val address = ctx.getLocals().load(1).asLong();
			val block = vm.getMemoryManager().getMemory(address);
			if (block == null) {
				throw new PanicException("Segfault");
			}
			ctx.setResult(new LongValue(block.getData().getLong((int) (address - block.getAddress()))));
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "defineAnonymousClass", "(Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;", ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val host = helper.<JavaValue<JavaClass>>checkNotNull(locals.load(1));
			val bytes = helper.checkArray(locals.load(2));
			val klass = host.getValue();
			val array = helper.toJavaBytes(bytes);
			val result = vm.getClassDefiner().parseClass(null, array, 0, array.length, "JVM_DefineClass");
			if (result == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, "Invalid class");
			}
			val loader = klass.getClassLoader();
			val generated = helper.newInstanceClass(loader, NullValue.INSTANCE, result.getClassReader(), result.getNode());

			// force link
			val node = generated.getNode();
			node.access |= ACC_VM_HIDDEN;
			ClassLoaderData classLoaderData;
			if (loader.isNull()) {
				classLoaderData = vm.getBootClassLoaderData();
			} else {
				classLoaderData = ((JavaValue<ClassLoaderData>) ((InstanceValue) loader).getValue(CLASS_LOADER_OOP, "Ljava/lang/Object;")).getValue();
			}
			classLoaderData.forceLinkClass(generated);
			ctx.setResult(generated.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(unsafe, "getInt", "(Ljava/lang/Object;J)I", ctx -> {
			val locals = ctx.getLocals();
			val value = locals.load(1);
			if (value.isNull()) {
				throw new PanicException("Segfault");
			}
			val offset = locals.load(2).asInt();
			val memoryManager = vm.getMemoryManager();
			ctx.setResult(new IntValue(memoryManager.readInt((ObjectValue) value, offset)));
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
		val cpClass = vm.getSymbols().reflect_ConstantPool;
		val vmi = vm.getInterface();
		vmi.setInvoker(cpClass, "getSize0", "(Ljav/lang/Object;)I", ctx -> {
			val wrapper = getCpOop(ctx);
			if (wrapper instanceof InstanceJavaClass) {
				val cf = ((InstanceJavaClass) wrapper).getRawClassFile();
				ctx.setResult(new IntValue(cf.getPool().size()));
			} else {
				ctx.setResult(IntValue.ZERO);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAt0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			val className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			val helper = vm.getHelper();
			val result = helper.findClass(ctx.getOwner().getClassLoader(), className, false);
			if (result == null) {
				helper.throwException(vm.getSymbols().java_lang_ClassNotFoundException, className);
			}
			ctx.setResult(result.getOop());
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getClassAtIfLoaded0", "(Ljava/lang/Object;I)Ljava/lang/Class;", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			val className = ((CpUtf8) cf.getCp(((CpClass) item).getIndex())).getText();
			val result = vm.getHelper().findClass(ctx.getOwner().getClassLoader(), className, false);
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
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new IntValue(((CpInt) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getLongAt0", "(Ljava/lang/Object;I)J", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new LongValue(((CpLong) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getFloatAt0", "(Ljava/lang/Object;I)F", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new FloatValue(((CpFloat) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getDoubleAt0", "(Ljava/lang/Object;I)D", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(new DoubleValue(((CpDouble) item).getValue()));
			return Result.ABORT;
		});
		vmi.setInvoker(cpClass, "getUTF8At0", "(Ljava/lang/Object;I)Ljava/lang/String;", ctx -> {
			val wrapper = getInstanceCpOop(ctx);
			val cf = wrapper.getRawClassFile();
			val index = cpRangeCheck(ctx, cf);
			val item = cf.getCp(index);
			ctx.setResult(vm.getHelper().newUtf8(((CpUtf8) item).getText()));
			return Result.ABORT;
		});
	}

	private static int cpRangeCheck(ExecutionContext ctx, ClassFile cf) {
		val index = ctx.getLocals().load(2).asInt();
		if (index < 0 || index >= cf.getPool().size()) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return index;
	}

	private static int cpRangeCheck(ExecutionContext ctx, ClassReader cr) {
		val index = ctx.getLocals().load(2).asInt();
		if (index < 0 || index >= cr.getItemCount()) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return index;
	}

	private static InstanceJavaClass getInstanceCpOop(ExecutionContext ctx) {
		val jc = getCpOop(ctx);
		if (!(jc instanceof InstanceJavaClass)) {
			val vm = ctx.getVM();
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return (InstanceJavaClass) jc;
	}

	private static JavaClass getCpOop(ExecutionContext ctx) {
		val vm = ctx.getVM();
		val value = ctx.getLocals().load(1);
		if (!(value instanceof JavaValue)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		val wrapper = ((JavaValue<?>) value).getValue();
		if (!(wrapper instanceof JavaClass)) {
			vm.getHelper().throwException(vm.getSymbols().java_lang_IllegalArgumentException);
		}
		return (JavaClass) wrapper;
	}

	/**
	 * Initializes win32/unix file system class.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initIoFs(VirtualMachine vm) {
		InstanceJavaClass fs = (InstanceJavaClass) vm.findBootstrapClass("java/io/WinNTFileSystem");
		boolean unix = false;
		if (fs == null) {
			fs = (InstanceJavaClass) vm.findBootstrapClass("java/io/UnixFileSystem");
			if (fs == null) {
				throw new IllegalStateException("Unable to locate file system implementation class for java.io package");
			}
			unix = true;
		}
		val vmi = vm.getInterface();
		vmi.setInvoker(fs, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fs, "canonicalize0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			val helper = vm.getHelper();
			val path = helper.readUtf8(ctx.getLocals().load(1));
			try {
				ctx.setResult(helper.newUtf8(vm.getFileDescriptorManager().canonicalize(path)));
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, unix ? "getBooleanAttributes0" : "getBooleanAttributes", "(Ljava/io/File;)I", ctx -> {
			val helper = vm.getHelper();
			val value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			try {
				val path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
				val attributes = vm.getFileDescriptorManager().getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(IntValue.ZERO);
				} else {
					int res = 1;
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
		vmi.setInvoker(fs, "list", "(Ljava/io/File;)[Ljava/lang/String;", ctx -> {
			val helper = vm.getHelper();
			val value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			val path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
			val list = vm.getFileDescriptorManager().list(path);
			if (list == null) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				val values = new ObjectValue[list.length];
				for (int i = 0; i < list.length; i++) {
					values[i] = helper.newUtf8(list[i]);
				}
				ctx.setResult(helper.toVMValues(values));
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "canonicalizeWithPrefix0", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			ctx.setResult(ctx.getLocals().load(2));
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "getLastModifiedTime", "(Ljava/io/File;)J", ctx -> {
			val helper = vm.getHelper();
			val value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			try {
				val path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
				val attributes = vm.getFileDescriptorManager().getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(LongValue.ZERO);
				} else {
					ctx.setResult(new LongValue(attributes.lastModifiedTime().toMillis()));
				}
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
	}

	private static final int MN_IS_METHOD = 0x00010000,
			MN_IS_CONSTRUCTOR = 0x00020000,
			MN_IS_FIELD = 0x00040000,
			MN_IS_TYPE = 0x00080000,
			MN_CALLER_SENSITIVE = 0x00100000,
			MN_REFERENCE_KIND_SHIFT = 24,
			MN_REFERENCE_KIND_MASK = 0x0F000000 >> MN_REFERENCE_KIND_SHIFT,
			MN_SEARCH_SUPERCLASSES = 0x00100000,
			MN_SEARCH_INTERFACES = 0x00200000;
	private static final byte
			REF_NONE = 0,
			REF_getField = 1,
			REF_getStatic = 2,
			REF_putField = 3,
			REF_putStatic = 4,
			REF_invokeVirtual = 5,
			REF_invokeStatic = 6,
			REF_invokeSpecial = 7,
			REF_newInvokeSpecial = 8,
			REF_invokeInterface = 9,
			REF_LIMIT = 10;
	private static final int
			IS_METHOD = MN_IS_METHOD,
			IS_CONSTRUCTOR = MN_IS_CONSTRUCTOR,
			IS_FIELD = MN_IS_FIELD,
			IS_TYPE = MN_IS_TYPE;
	static final int ALL_KINDS = IS_METHOD | IS_CONSTRUCTOR | IS_FIELD | IS_TYPE;
	private static final int RECOGNIZED_METHOD_MODIFIERS = ACC_PUBLIC |
			ACC_PRIVATE |
			ACC_PROTECTED |
			ACC_STATIC |
			ACC_FINAL |
			ACC_SYNCHRONIZED |
			ACC_BRIDGE |
			ACC_VARARGS |
			ACC_NATIVE |
			ACC_ABSTRACT |
			ACC_STRICT |
			ACC_SYNTHETIC;
	private static final int RECOGNIZED_FIELD_MODIFIERS = ACC_PUBLIC |
			ACC_PRIVATE |
			ACC_PROTECTED |
			ACC_STATIC |
			ACC_FINAL |
			ACC_VOLATILE |
			ACC_TRANSIENT |
			ACC_ENUM |
			ACC_SYNTHETIC;

	private static boolean refKindIsField(byte refKind) {
		return (refKind <= REF_putStatic);
	}

	private static boolean refKindIsGetter(byte refKind) {
		return (refKind <= REF_getStatic);
	}

	private static boolean refKindHasReceiver(byte refKind) {
		return (refKind & 1) != 0;
	}

	/**
	 * Initializes method handles related classes.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	private static void initMethodHandles(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val symbols = vm.getSymbols();
		val natives = symbols.java_lang_invoke_MethodHandleNatives;
		vmi.setInvoker(natives, "registerNatives", "()V", MethodInvoker.noop());
		val resolve = (MethodInvoker) ctx -> {
			val helper = vm.getHelper();
			val memberName = helper.<InstanceValue>checkNotNull(ctx.getLocals().load(0));
			val classWrapper = (JavaValue<InstanceJavaClass>) memberName.getValue("clazz", "Ljava/lang/Class;");
			val clazz = classWrapper.getValue();
			val name = helper.readUtf8(memberName.getValue("name", "Ljava/lang/String;"));
			val mt = memberName.getValue("type", "Ljava/lang/Object;");
			int flags = memberName.getInt("flags");
			int refKind = (flags >> MN_REFERENCE_KIND_SHIFT) & MN_REFERENCE_KIND_MASK;
			switch (flags & ALL_KINDS) {
				case IS_METHOD:
					initMethodMember(refKind, vm, memberName, clazz, name, mt, IS_METHOD);
					break;
				case IS_FIELD:
					initFieldMember(refKind, vm, memberName, clazz, name, mt);
					break;
				case IS_CONSTRUCTOR:
					initMethodMember(refKind, vm, memberName, clazz, name, mt, IS_CONSTRUCTOR);
					break;
				default:
					helper.throwException(symbols.java_lang_InternalError, "Not implemented for " + refKind + " " + (flags & ALL_KINDS));
			}

			ctx.setResult(memberName);
			return Result.ABORT;
		};
		if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;Z)Ljava/lang/invoke/MemberName;", resolve)) {
			if (!vmi.setInvoker(natives, "resolve", "(Ljava/lang/invoke/MemberName;Ljava/lang/Class;)Ljava/lang/invoke/MemberName;", resolve)) {
				throw new IllegalStateException("Unable to locate MethodHandleNatives#resolve method");
			}
		}
		vmi.setInvoker(natives, "getConstant", "(I)I", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "init", "(Ljava/lang/invoke/MemberName;Ljava/lang/Object;)V", ctx -> {
			val locals = ctx.getLocals();
			val helper = ctx.getHelper();
			val memberName = helper.<InstanceValue>checkNotNull(locals.load(0));
			val obj = helper.<InstanceValue>checkNotNull(locals.load(1));
			val objClass = obj.getJavaClass();
			if (objClass == symbols.java_lang_reflect_Method) {
				initMemberNameMethod(vm, memberName, obj);
			} else if (objClass == symbols.java_lang_reflect_Field) {
				initMemberNameField(vm, memberName, obj);
			} else {
				helper.throwException(symbols.java_lang_InternalError, "Not implemented");
			}
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "objectFieldOffset", "(Ljava/lang/invoke/MemberName;)J", ctx -> {
			ctx.setResult(new LongValue(ctx.getLocals().<InstanceValue>load(0).getInt(VM_INDEX)));
			return Result.ABORT;
		});
		vmi.setInvoker(natives, "getMembers", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;ILjava/lang/Class;I[Ljava/lang/invoke/MemberName;)I", ctx -> {
			ctx.setResult(IntValue.ZERO);
			return Result.ABORT;
		});
		val mh = symbols.java_lang_invoke_MethodHandle;
		val invoke = (MethodInvoker) ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val _this = locals.<InstanceValue>load(0);
			val form = helper.<InstanceValue>checkNotNull(_this.getValue("form", "Ljava/lang/invoke/LambdaForm;"));
			val vmentry = helper.<InstanceValue>checkNotNull(form.getValue("vmentry", "Ljava/lang/invoke/MemberName;"));
			val resolved = (InstanceValue) vmentry.getValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor());
			val vmtarget = ((JavaValue<Object>) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getValue();
			if (vmtarget instanceof JavaMethod) {
				val jm = (JavaMethod) vmtarget;
				val name = jm.getName();
				if ("<init>".equals(name)) {
					throw new PanicException("TODO");
				}
				Value[] lvt = locals.getTable();
				if (jm.getMaxArgs() > lvt.length) {
					// TODO figure uot what the ... is going on?
					val last = lvt[lvt.length - 1];
					if (last instanceof ArrayValue) {
						val arr = (ArrayValue) last;
						int length = arr.getLength();
						int x = lvt.length;
						lvt = Arrays.copyOf(lvt, x + length - 1);
						for (int i = 0, k = x + length; x < k; x++) {
							lvt[x - 1] = arr.getValue(i++);
						}
					}
				}
				if ((jm.getAccess() & ACC_STATIC) == 0) {
					ctx.setResult(helper.invokeVirtual(name, jm.getDesc(), new Value[0], lvt).getResult());
				} else {
					ctx.setResult(helper.invokeStatic(jm.getOwner(), jm, new Value[0], lvt).getResult());
				}
			} else {
				throw new PanicException("TODO: " + vmtarget);
			}
			return Result.ABORT;
		};
		vmi.setInvoker(mh, "invoke", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeBasic", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);
		vmi.setInvoker(mh, "invokeExact", "([Ljava/lang/Object;)Ljava/lang/Object;", invoke);

		val linkToXX = (MethodInvoker) ctx -> {
			val locals = ctx.getLocals();
			val helper = vm.getHelper();
			val table = locals.getTable();
			int length = table.length;
			val memberName = locals.<InstanceValue>load(length - 1);
			val resolved = (InstanceValue) memberName.getValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor());
			val vmtarget = ((JavaValue<JavaMethod>) resolved.getValue(VM_TARGET, "Ljava/lang/Object;")).getValue();

			val args = Arrays.copyOfRange(locals.getTable(), 0, length - 1);

			if ((vmtarget.getAccess() & ACC_STATIC) == 0) {
				ctx.setResult(helper.invokeVirtual(vmtarget.getName(), vmtarget.getDesc(), new Value[0], args).getResult());
			} else {
				ctx.setResult(helper.invokeStatic(vmtarget.getOwner(), vmtarget, new Value[0], args).getResult());
			}
			return Result.ABORT;
		};

		vmi.setInvoker(mh, "linkToStatic", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToVirtual", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);
		vmi.setInvoker(mh, "linkToInterface", "([Ljava/lang/Object;)Ljava/lang/Object;", linkToXX);

		val lookup = symbols.java_lang_invoke_MethodHandles$Lookup;
		vmi.setInvoker(lookup, "checkAccess", "(BLjava/lang/Class;Ljava/lang/invoke/MemberName;)V", MethodInvoker.noop());
	}

	private static void initMemberNameMethod(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		val helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		val clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		val slot = obj.getInt("slot");
		val method = helper.getMethodBySlot(clazz, slot);

		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(method.getName()));
		val mt = helper.methodType(clazz.getClassLoader(), method.getType());
		memberName.setValue("type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((method.getAccess() & ACC_STATIC) == 0) {
			refKind = REF_invokeVirtual;
		} else {
			refKind = REF_invokeStatic;
		}
		initMethodMember(refKind, vm, memberName, method, IS_METHOD);
	}

	private static void initMemberNameField(VirtualMachine vm, InstanceValue memberName, InstanceValue obj) {
		val helper = vm.getHelper();
		// Copy over clazz, name, type & invoke expand
		val clazz = ((JavaValue<InstanceJavaClass>) obj.getValue("clazz", "Ljava/lang/Class;")).getValue();
		val slot = obj.getInt("slot");
		val field = helper.getFieldBySlot(clazz, slot);
		memberName.setValue("clazz", "Ljava/lang/Class;", clazz.getOop());
		memberName.setValue("name", "Ljava/lang/String;", vm.getStringPool().intern(field.getName()));
		val mt = helper.methodType(clazz.getClassLoader(), field.getType());
		memberName.setValue("type", "Ljava/lang/Object;", mt);
		int refKind;
		if ((field.getAccess() & ACC_STATIC) == 0) {
			refKind = REF_getField;
		} else {
			refKind = REF_getStatic;
		} // JDK code will change that later on.
		initFieldMember(refKind, vm, memberName, field);
	}

	private static void initMethodMember(int refKind, VirtualMachine vm, InstanceValue memberName, JavaMethod handle, int mnType) {
		val symbols = vm.getSymbols();
		// Inject vmholder & vmtarget into resolved name
		memberName.setInt(VM_INDEX, handle.getSlot());
		val memoryManager = vm.getMemoryManager();
		val rmn = symbols.java_lang_invoke_ResolvedMethodName;
		rmn.initialize();
		val resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		val jlo = symbols.java_lang_Object;
		resolvedName.setValue(VM_TARGET, "Ljava/lang/Object;", memoryManager.newJavaInstance(jlo, handle));
		resolvedName.setValue(VM_HOLDER, "Ljava/lang/Object;", handle.getOwner().getOop());
		memberName.setValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & RECOGNIZED_METHOD_MODIFIERS;
		flags |= mnType | (refKind << MN_REFERENCE_KIND_SHIFT);
		memberName.setInt("flags", flags);
	}

	private static void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, JavaField handle) {
		val symbols = vm.getSymbols();
		// Inject vmholder & vmtarget into resolved name
		val memoryManager = vm.getMemoryManager();
		val owner = handle.getOwner();
		long offset = handle.getOffset();
		if ((handle.getAccess() & ACC_STATIC) == 0) {
			offset += memoryManager.valueBaseOffset(owner);
		} else {
			offset += memoryManager.getStaticOffset(owner);
		}
		memberName.setInt(VM_INDEX, (int) offset);
		val rmn = symbols.java_lang_invoke_ResolvedMethodName;
		rmn.initialize();
		val resolvedName = memoryManager.newInstance(rmn);
		resolvedName.initialize();
		val jlo = symbols.java_lang_Object;
		resolvedName.setValue(VM_TARGET, "Ljava/lang/Object;", memoryManager.newJavaInstance(jlo, handle));
		resolvedName.setValue(VM_HOLDER, "Ljava/lang/Object;", owner.getOop());
		memberName.setValue("method", symbols.java_lang_invoke_ResolvedMethodName.getDescriptor(), resolvedName);
		// Inject flags
		int flags = handle.getAccess() & RECOGNIZED_FIELD_MODIFIERS;
		flags |= IS_FIELD | (refKind << MN_REFERENCE_KIND_SHIFT);
		memberName.setInt("flags", flags);
	}

	private static void initMethodMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceJavaClass clazz, String name, Value methodType, int type) {
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val desc = helper.readUtf8(helper.invokeExact(symbols.java_lang_invoke_MethodType, "toMethodDescriptorString", "()Ljava/lang/String;", new Value[0], new Value[]{
				methodType
		}).getResult());
		JavaMethod handle;
		switch (refKind) {
			case REF_invokeStatic:
				handle = clazz.getStaticMethod(name, desc);
				break;
			case REF_invokeVirtual:
			case REF_invokeSpecial:
			case REF_newInvokeSpecial:
			case REF_invokeInterface:
				handle = clazz.getVirtualMethod(name, desc);
				break;
			default:
				throw new PanicException("TODO ? " + refKind);
		}
		if (handle == null) {
			helper.throwException(symbols.java_lang_NoSuchMethodError, clazz.getInternalName() + '.' + name + desc);
		}
		initMethodMember(refKind, vm, memberName, handle, type);
	}

	private static void initFieldMember(int refKind, VirtualMachine vm, InstanceValue memberName, InstanceJavaClass clazz, String name, Value type) {
		val helper = vm.getHelper();
		val symbols = vm.getSymbols();
		val desc = ((JavaValue<JavaClass>) type).getValue().getDescriptor();
		JavaField handle;
		switch (refKind) {
			case REF_getStatic:
			case REF_putStatic:
				handle = clazz.getStaticField(name, desc);
				break;
			case REF_getField:
			case REF_putField:
				handle = clazz.getVirtualField(name, desc);
				break;
			default:
				throw new PanicException("TODO ?");
		}
		if (handle == null) {
			helper.throwException(symbols.java_lang_NoSuchFieldError, name);
		}
		initFieldMember(refKind, vm, memberName, handle);
	}

	/**
	 * Injects VM related things.
	 * This must be invoked as early as
	 * possible.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	static void injectPhase1(VirtualMachine vm) {
		val cl = (InstanceJavaClass) vm.findBootstrapClass("java/lang/Class");
		val fields = cl.getNode().fields;
		fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				PROTECTION_DOMAIN,
				"Ljava/security/ProtectionDomain;",
				null,
				null
		));
		fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				CONSTANT_POOL,
				"Ljava/lang/Object;",
				null,
				null
		));
	}

	/**
	 * Injects VM related things.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	static void injectPhase2(VirtualMachine vm) {
		val symbols = vm.getSymbols();
		val classLoader = symbols.java_lang_ClassLoader;

		classLoader.getNode().fields.add(new FieldNode(
				ACC_PRIVATE | ACC_VM_HIDDEN,
				CLASS_LOADER_OOP,
				"Ljava/lang/Object;",
				null,
				null
		));

		inject:
		{
			val memberName = symbols.java_lang_invoke_MemberName;
			val fields = memberName.getNode().fields;
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					VM_INDEX,
					"I",
					null,
					null
			));
			for (int i = 0; i < fields.size(); i++) {
				val fn = fields.get(i);
				if ("method".equals(fn.name) && "Ljava/lang/invoke/MemberName$ResolvedMethodName;".equals(fn.desc)) {
					break inject;
				}
			}
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					"method",
					"Ljava/lang/invoke/MemberName$ResolvedMethodName;",
					null,
					null
			));
		}

		{
			val resolvedMethodName = symbols.java_lang_invoke_ResolvedMethodName;
			val fields = resolvedMethodName.getNode().fields;
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					VM_TARGET,
					"Ljava/lang/Object;",
					null,
					null
			));
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					VM_HOLDER,
					"Ljava/lang/Object;",
					null,
					null
			));
		}
		inject:
		{
			val fd = symbols.java_io_FileDescriptor;
			// For whatever reason unix/macos does not have
			// 'handle' field, we need to inject it
			val fields = fd.getNode().fields;
			for (int i = 0; i < fields.size(); i++) {
				val fn = fields.get(i);
				if ("handle".equals(fn.name) && "J".equals(fn.desc)) {
					break inject;
				}
			}
			fields.add(new FieldNode(
					ACC_PRIVATE | ACC_VM_HIDDEN,
					"handle",
					"J",
					null,
					null
			));
		}
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
		val helper = vm.getHelper();
		val result = new Value[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			val originalClass = helper.findClass(loader, argTypes[i].getInternalName(), true);
			val value = (ObjectValue) array.getValue(i);
			if (value.isNull() || !originalClass.isPrimitive()) {
				result[i] = value;
			} else {
				result[i] = helper.unboxGeneric(value, originalClass);
			}
		}
		return result;
	}

	private static long mapVMStream(VirtualMachine vm, int d) {
		try {
			return vm.getFileDescriptorManager().newFD(d);
		} catch (IllegalStateException ex) {
			vm.getHelper().throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
		}
		return 0L;
	}

	/**
	 * Sets up default opcode set.
	 *
	 * @param vmi
	 * 		VM interface.
	 */
	private static void setInstructions(VMInterface vmi) {
		val nop = new NopProcessor();
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

		vmi.setProcessor(JSR, new JSRProcessor());
		vmi.setProcessor(RET, new RetProcessor());

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

		vmi.setProcessor(MULTIANEWARRAY, new MultiNewArrayProcessor());

		vmi.setProcessor(IFNONNULL, new ValueJumpProcessor(value -> !value.isNull()));
		vmi.setProcessor(IFNULL, new ValueJumpProcessor(Value::isNull));

		// VM opcodes
		vmi.setProcessor(METHOD_HANDLE, new DynamicCallProcessor());
	}

	private interface UnsafeHelper {

		String allocateMemory();

		String reallocateMemory();

		String setMemory();

		String arrayBaseOffset();

		String arrayIndexScale();

		String addressSize();

		String ensureClassInitialized();

		String objectFieldOffset();

		String staticFieldOffset();

		String compareAndSetReference();

		String compareAndSetInt();

		String compareAndSetLong();

		String staticFieldBase();

		String defineClass();

		String shouldBeInitialized();

		String pageSize();
	}

	private static class OldUnsafeHelper implements UnsafeHelper {

		@Override
		public String allocateMemory() {
			return "allocateMemory";
		}

		@Override
		public String reallocateMemory() {
			return "reallocateMemory";
		}

		@Override
		public String setMemory() {
			return "setMemory";
		}

		@Override
		public String arrayBaseOffset() {
			return "arrayBaseOffset";
		}

		@Override
		public String arrayIndexScale() {
			return "arrayIndexScale";
		}

		@Override
		public String addressSize() {
			return "addressSize";
		}

		@Override
		public String ensureClassInitialized() {
			return "ensureClassInitialized";
		}

		@Override
		public String objectFieldOffset() {
			return "objectFieldOffset";
		}

		@Override
		public String staticFieldOffset() {
			return "staticFieldOffset";
		}

		@Override
		public String compareAndSetReference() {
			return "compareAndSwapObject";
		}

		@Override
		public String compareAndSetInt() {
			return "compareAndSwapInt";
		}

		@Override
		public String compareAndSetLong() {
			return "compareAndSwapLong";
		}

		@Override
		public String staticFieldBase() {
			return "staticFieldBase";
		}

		@Override
		public String defineClass() {
			return "defineClass";
		}

		@Override
		public String shouldBeInitialized() {
			return "shouldBeInitialized";
		}

		@Override
		public String pageSize() {
			return "pageSize";
		}
	}

	private static final class NewUnsafeHelper implements UnsafeHelper {

		@Override
		public String allocateMemory() {
			return "allocateMemory0";
		}

		@Override
		public String reallocateMemory() {
			return "reallocateMemory0";
		}

		@Override
		public String setMemory() {
			return "setMemory0";
		}

		@Override
		public String arrayBaseOffset() {
			return "arrayBaseOffset0";
		}

		@Override
		public String arrayIndexScale() {
			return "arrayIndexScale0";
		}

		@Override
		public String addressSize() {
			return "addressSize0";
		}

		@Override
		public String ensureClassInitialized() {
			return "ensureClassInitialized0";
		}

		@Override
		public String objectFieldOffset() {
			return "objectFieldOffset0";
		}

		@Override
		public String staticFieldOffset() {
			return "staticFieldOffset0";
		}

		@Override
		public String compareAndSetReference() {
			return "compareAndSetReference";
		}

		@Override
		public String compareAndSetInt() {
			return "compareAndSetInt";
		}

		@Override
		public String compareAndSetLong() {
			return "compareAndSetLong";
		}

		@Override
		public String staticFieldBase() {
			return "staticFieldBase0";
		}

		@Override
		public String defineClass() {
			return "defineClass0";
		}

		@Override
		public String shouldBeInitialized() {
			return "shouldBeInitialized0";
		}

		@Override
		public String pageSize() {
			return "pageSize0";
		}
	}
}
