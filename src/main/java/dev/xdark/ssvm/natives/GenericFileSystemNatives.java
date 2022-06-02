package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.memory.MemoryData;
import dev.xdark.ssvm.memory.MemoryManager;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.IntValue;
import dev.xdark.ssvm.value.LongValue;
import lombok.experimental.UtilityClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
		VMInterface vmi = vm.getInterface();
		InstanceJavaClass fd = vm.getSymbols().java_io_FileDescriptor();

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
				InstanceValue in = (InstanceValue) fd.getStaticValue("in", "Ljava/io/FileDescriptor;");
				in.setLong("handle", mapVMStream(vm, 0));
				InstanceValue out = (InstanceValue) fd.getStaticValue("out", "Ljava/io/FileDescriptor;");
				out.setLong("handle", mapVMStream(vm, 1));
				InstanceValue err = (InstanceValue) fd.getStaticValue("err", "Ljava/io/FileDescriptor;");
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
			long handle = ctx.getLocals().<InstanceValue>load(0).getLong("handle");
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				vm.getHelper().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		InstanceJavaClass fos = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileOutputStream");
		vmi.setInvoker(fos, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fos, "writeBytes", "([BIIZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			OutputStream out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) return Result.ABORT;
			byte[] bytes = helper.toJavaBytes(locals.load(1));
			int off = locals.load(2).asInt();
			int len = locals.load(3).asInt();
			try {
				out.write(bytes, off, len);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "write", "(BZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			OutputStream out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) return Result.ABORT;
			try {
				out.write(locals.load(1).asByte());
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "open0", "(Ljava/lang/String;Z)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			String path = helper.readUtf8(locals.load(1));
			boolean append = locals.load(2).asBoolean();
			try {
				long handle = vm.getFileDescriptorManager().open(path, append ? FileDescriptorManager.APPEND : FileDescriptorManager.WRITE);
				((InstanceValue) _this.getValue("fd", "Ljava/io/FileDescriptor;")).setLong("handle", handle);
			} catch (FileNotFoundException ex) {
				helper.throwException(vm.getSymbols().java_io_FileNotFoundException(), ex.getMessage());
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "close0", "()V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		InstanceJavaClass fis = (InstanceJavaClass) vm.findBootstrapClass("java/io/FileInputStream");
		vmi.setInvoker(fis, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fis, "readBytes", "([BII)I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(IntValue.M_ONE);
			} else {
				try {
					int off = locals.load(2).asInt();
					int len = locals.load(3).asInt();
					byte[] bytes = new byte[len];
					int read = in.read(bytes);
					if (read > 0) {
						ArrayValue vmBuffer = locals.<ArrayValue>load(1);
						MemoryManager memoryManager = vm.getMemoryManager();
						int start = memoryManager.arrayBaseOffset(byte.class) + off;
						MemoryData data = vmBuffer.getMemory().getData();
						data.write(start, bytes, 0, read);
					}
					ctx.setResult(IntValue.of(read));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "read0", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(IntValue.M_ONE);
			} else {
				try {
					ctx.setResult(IntValue.of(in.read()));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "skip0", "(J)J", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(LongValue.ZERO);
			} else {
				try {
					ctx.setResult(LongValue.of(in.skip(locals.load(1).asLong())));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "open0", "(Ljava/lang/String;)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.load(0);
			VMHelper helper = vm.getHelper();
			String path = helper.readUtf8(locals.load(1));
			try {
				long handle = vm.getFileDescriptorManager().open(path, FileDescriptorManager.READ);
				((InstanceValue) _this.getValue("fd", "Ljava/io/FileDescriptor;")).setLong("handle", handle);
			} catch (FileNotFoundException ex) {
				helper.throwException(vm.getSymbols().java_io_FileNotFoundException(), ex.getMessage());
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "available0", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(IntValue.ZERO);
			} else {
				try {
					ctx.setResult(IntValue.of(in.available()));
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "close0", "()V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.<InstanceValue>load(0);
			VMHelper helper = vm.getHelper();
			long handle = helper.getFileStreamHandle(_this);
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
	}

	private long mapVMStream(VirtualMachine vm, int d) {
		try {
			return vm.getFileDescriptorManager().newFD(d);
		} catch (IllegalStateException ex) {
			vm.getHelper().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
		}
		return 0L;
	}
}
