package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.util.Helper;
import dev.xdark.ssvm.util.Operations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
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
	 * @param vm VM instance.
	 */
	public void init(VirtualMachine vm) {
		VMInterface vmi = vm.getInterface();
		InstanceClass fd = vm.getSymbols().java_io_FileDescriptor();

		MethodInvoker set = ctx -> {
			ctx.setResult(mapVMStream(vm, ctx.getLocals().loadInt(0)));
			return Result.ABORT;
		};
		boolean lateinit = false;
		if (!vmi.setInvoker(fd, "getHandle", "(I)J", set)) {
			lateinit = !vmi.setInvoker(fd, "set", "(I)J", set);
		}
		if (lateinit) {
			vmi.setInvoker(fd, "initIDs", "()V", ctx -> {
				VMOperations ops = vm.getOperations();
				InstanceValue in = (InstanceValue) ops.getReference(fd, "in", "Ljava/io/FileDescriptor;");
				ops.putLong(in, fd, "handle", mapVMStream(vm, 0));
				InstanceValue out = (InstanceValue) ops.getReference(fd, "out", "Ljava/io/FileDescriptor;");
				ops.putLong(out, fd, "handle", mapVMStream(vm, 1));
				InstanceValue err = (InstanceValue) ops.getReference(fd, "err", "Ljava/io/FileDescriptor;");
				ops.putLong(err, fd, "handle", mapVMStream(vm, 2));
				return Result.ABORT;
			});
		} else {
			vmi.setInvoker(fd, "initIDs", "()V", MethodInvoker.noop());
		}

		vmi.setInvoker(fd, "getAppend", "(I)Z", ctx -> {
			ctx.setResult(vm.getFileDescriptorManager().isAppend(ctx.getLocals().loadInt(0)) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fd, "close0", "()V", ctx -> {
			long handle = vm.getOperations().getLong(ctx.getLocals().loadReference(0), fd, "handle");
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		InstanceClass fos = (InstanceClass) vm.findBootstrapClass("java/io/FileOutputStream");
		vmi.setInvoker(fos, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fos, "writeBytes", "([BIIZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = helper.getFileStreamHandle(_this);
			OutputStream out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) {
				return Result.ABORT;
			}
			byte[] bytes = ops.toJavaBytes(locals.loadReference(1));
			int off = locals.loadInt(2);
			int len = locals.loadInt(3);
			try {
				out.write(bytes, off, len);
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "write", "(BZ)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = helper.getFileStreamHandle(_this);
			OutputStream out = vm.getFileDescriptorManager().getFdOut(handle);
			if (out == null) {
				return Result.ABORT;
			}
			try {
				out.write(locals.loadInt(1));
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "open0", "(Ljava/lang/String;Z)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			String path = ops.readUtf8(locals.loadReference(1));
			boolean append = locals.loadInt(2) != 0;
			try {
				long handle = vm.getFileDescriptorManager().open(path, append ? FileDescriptorManager.APPEND : FileDescriptorManager.WRITE);
				ObjectValue _fd = ops.getReference(_this, fos, "fd", "Ljava/io/FileDescriptor;");
				ops.putLong(_fd, fd, "handle", handle);
			} catch (FileNotFoundException ex) {
				ops.throwException(vm.getSymbols().java_io_FileNotFoundException(), ex.getMessage());
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "close0", "()V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = ops.getFileStreamHandle(_this);
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		InstanceClass fis = (InstanceClass) vm.findBootstrapClass("java/io/FileInputStream");
		vmi.setInvoker(fis, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fis, "readBytes", "([BII)I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			Helper helper = vm.getOperations();
			long handle = helper.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(-1);
			} else {
				try {
					int off = locals.loadInt(2);
					int len = locals.loadInt(3);
					byte[] bytes = new byte[len];
					int read = in.read(bytes);
					if (read > 0) {
						ArrayValue vmBuffer = locals.loadReference(1);
						MemoryManager memoryManager = vm.getMemoryManager();
						int start = memoryManager.arrayBaseOffset(byte.class) + off;
						MemoryData data = vmBuffer.getMemory().getData();
						data.write(start, bytes, 0, read);
					}
					ctx.setResult(read);
				} catch (IOException ex) {
					helper.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "read0", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = ops.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(-1);
			} else {
				try {
					ctx.setResult(in.read());
				} catch (IOException ex) {
					ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "skip0", "(J)J", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = ops.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(0L);
			} else {
				try {
					ctx.setResult(in.skip(locals.loadLong(1)));
				} catch (IOException ex) {
					ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "open0", "(Ljava/lang/String;)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			String path = ops.readUtf8(locals.loadReference(1));
			try {
				long handle = vm.getFileDescriptorManager().open(path, FileDescriptorManager.READ);
				ObjectValue _fd = ops.getReference(_this, fos, "fd", "Ljava/io/FileDescriptor;");
				ops.putLong(_fd, fd, "handle", handle);
			} catch (FileNotFoundException ex) {
				ops.throwException(vm.getSymbols().java_io_FileNotFoundException(), ex.getMessage());
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "available0", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = helper.getFileStreamHandle(_this);
			InputStream in = vm.getFileDescriptorManager().getFdIn(handle);
			if (in == null) {
				ctx.setResult(0);
			} else {
				try {
					ctx.setResult(in.available());
				} catch (IOException ex) {
					ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "close0", "()V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			VMOperations ops = vm.getOperations();
			long handle = helper.getFileStreamHandle(_this);
			try {
				vm.getFileDescriptorManager().close(handle);
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
	}

	private static long mapVMStream(VirtualMachine vm, int d) {
		try {
			return vm.getFileDescriptorManager().newFD(d);
		} catch (IllegalStateException ex) {
			vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
		}
		return 0L;
	}
}
