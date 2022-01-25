package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.io.IOException;

/**
 * Initializes multiple classes:
 * <p>
 * {@link java.io.FileDescriptor}
 * <p>
 * {@link java.io.FileInputStream}
 * <p>
 * {@link java.io.FileOutputStream}
 *
 * @author xDark
 */
@UtilityClass
public class GenericFileSystemNatives {

	/**
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		val vmi = vm.getInterface();
		val fd = vm.getSymbols().java_io_FileDescriptor;

		MethodInvoker set = ctx -> {
			ctx.setResult(LongValue.of(mapVMStream(vm, ctx.getLocals().load(0).asInt())));
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
					val bytes = new byte[len];
					val read = in.read(bytes);
					if (read > 0) {
						val vmBuffer = locals.<ArrayValue>load(1);
						val memoryManager = vm.getMemoryManager();
						val start = memoryManager.arrayBaseOffset(byte.class) + off;
						val data = vmBuffer.getMemory().getData().slice();
						data.position(start);
						data.put(bytes, 0, read);
					}
					ctx.setResult(IntValue.of(read));
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
					ctx.setResult(IntValue.of(in.read()));
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
					ctx.setResult(LongValue.of(in.skip(locals.load(1).asLong())));
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
					ctx.setResult(IntValue.of(in.available()));
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

	private long mapVMStream(VirtualMachine vm, int d) {
		try {
			return vm.getFileDescriptorManager().newFD(d);
		} catch (IllegalStateException ex) {
			vm.getHelper().throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
		}
		return 0L;
	}
}
