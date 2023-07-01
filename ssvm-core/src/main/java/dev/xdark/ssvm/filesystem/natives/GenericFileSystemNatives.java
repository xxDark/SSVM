package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.ExecutionContext;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.memory.allocation.MemoryData;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.nio.file.attribute.BasicFileAttributes;

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
	 * @param vm          VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		VMInterface vmi = vm.getInterface();
		VMOperations ops = vm.getOperations();
		InstanceClass fd = vm.getSymbols().java_io_FileDescriptor();

		MethodInvoker set = ctx -> {
			ctx.setResult(mapVMStream(vm, fileManager, ctx.getLocals().loadInt(0)));
			return Result.ABORT;
		};
		boolean lateinit = false;
		if (!vmi.setInvoker(fd, "getHandle", "(I)J", set)) {
			lateinit = !vmi.setInvoker(fd, "set", "(I)J", set);
		}
		if (lateinit) {
			vmi.setInvoker(fd, "initIDs", "()V", ctx -> {
				InstanceValue in = (InstanceValue) ops.getReference(fd, "in", "Ljava/io/FileDescriptor;");
				ops.putLong(in, fd, "handle", mapVMStream(vm, fileManager, 0));
				InstanceValue out = (InstanceValue) ops.getReference(fd, "out", "Ljava/io/FileDescriptor;");
				ops.putLong(out, fd, "handle", mapVMStream(vm, fileManager, 1));
				InstanceValue err = (InstanceValue) ops.getReference(fd, "err", "Ljava/io/FileDescriptor;");
				ops.putLong(err, fd, "handle", mapVMStream(vm, fileManager, 2));
				return Result.ABORT;
			});
		} else {
			vmi.setInvoker(fd, "initIDs", "()V", MethodInvoker.noop());
		}

		vmi.setInvoker(fd, "getAppend", "(I)Z", ctx -> {
			ctx.setResult(fileManager.isAppend(ctx.getLocals().loadInt(0)) ? 1 : 0);
			return Result.ABORT;
		});
		vmi.setInvoker(fd, "close0", "()V", ctx -> {
			long handle = vm.getOperations().getLong(ctx.getLocals().loadReference(0), fd, "handle");
			try {
				fileManager.close(handle);
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
			long handle = getFileStreamHandle(vm, _this);
			OutputStream out = fileManager.getFdOut(handle);
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
			long handle = getFileStreamHandle(vm, _this);
			OutputStream out = fileManager.getFdOut(handle);
			if (out == null) {
				return Result.ABORT;
			}
			try {
				out.write(locals.loadInt(1));
			} catch (IOException ex) {
				vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fos, "open0", "(Ljava/lang/String;Z)V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			String path = ops.readUtf8(locals.loadReference(1));
			boolean append = locals.loadInt(2) != 0;
			try {
				long handle = fileManager.open(path, append ? FileManager.APPEND : FileManager.WRITE);
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
			long handle = getFileStreamHandle(vm, _this);
			try {
				fileManager.close(handle);
			} catch (IOException ex) {
				vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
		InstanceClass fis = (InstanceClass) vm.findBootstrapClass("java/io/FileInputStream");
		vmi.setInvoker(fis, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fis, "readBytes", "([BII)I", ctx -> readBytes(ctx, vm, fileManager));
		vmi.setInvoker(fis, "read0", "()I", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			long handle = getFileStreamHandle(vm, _this);
			InputStream in = fileManager.getFdIn(handle);
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
			long handle = getFileStreamHandle(vm, _this);
			InputStream in = fileManager.getFdIn(handle);
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
			String path = ops.readUtf8(locals.loadReference(1));
			try {
				long handle = fileManager.open(path, FileManager.READ);
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
			long handle = getFileStreamHandle(vm, _this);
			InputStream in = fileManager.getFdIn(handle);
			if (in == null) {
				ctx.setResult(0);
			} else {
				try {
					ctx.setResult(in.available());
				} catch (IOException ex) {
					vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fis, "close0", "()V", ctx -> {
			Locals locals = ctx.getLocals();
			InstanceValue _this = locals.loadReference(0);
			long handle = getFileStreamHandle(vm, _this);
			try {
				fileManager.close(handle);
			} catch (IOException ex) {
				vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});

		InstanceClass raf = (InstanceClass) vm.findBootstrapClass("java/io/RandomAccessFile");
		if (raf != null) {
			JavaField pathField = raf.getField("path", "Ljava/lang/String;");
			vmi.setInvoker(raf, "initIDs", "()V", MethodInvoker.noop());
			vmi.setInvoker(raf, "length", "()J", ctx -> {
				Locals locals = ctx.getLocals();
				InstanceValue _this = locals.loadReference(0);
				String pathLiteral = ops.toString(vm.getMemoryManager().readReference(_this, pathField.getOffset()));
				try {
					long size = fileManager.getAttributes(pathLiteral, BasicFileAttributes.class).size();
					ctx.setResult(size);
				} catch (FileNotFoundException ex) {
					ops.throwException(vm.getSymbols().java_io_FileNotFoundException(), ex.getMessage());
				} catch (IOException ex) {
					ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
				return Result.ABORT;
			});
			vmi.setInvoker(raf, "readBytes", "([BII)I",ctx -> readBytes(ctx, vm, fileManager));
			vmi.setInvoker(raf, "seek0", "(J)V", ctx -> {
				Locals locals = ctx.getLocals();
				InstanceValue _this = locals.loadReference(0);
				long handle = getFileStreamHandle(vm, _this);
				InputStream in = fileManager.getFdIn(handle);
				if (in != null) {
					try {
						long pos = locals.loadLong(1);
						in.reset();
						in.skip(pos);
					} catch (IOException ex) {
						ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
					}
				}
				return Result.ABORT;
			});
			vmi.setInvoker(raf, "open0", "(Ljava/lang/String;I)V", ctx -> {
				Locals locals = ctx.getLocals();
				InstanceValue _this = locals.loadReference(0);
				String path = ops.readUtf8(locals.loadReference(1));
				int mode = locals.loadInt(2);
				try {
					int fmMode = FileManager.READ;
					if (mode == 2) fmMode = FileManager.ACCESS_READ | FileManager.ACCESS_WRITE;
					long handle = fileManager.open(path, fmMode);
					ObjectValue _fd = ops.getReference(_this, fos, "fd", "Ljava/io/FileDescriptor;");
					ops.putLong(_fd, fd, "handle", handle);
				} catch (FileNotFoundException ex) {
					ops.throwException(vm.getSymbols().java_io_FileNotFoundException(), ex.getMessage());
				} catch (IOException ex) {
					ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
				}
				return Result.ABORT;
			});
		}
	}

	private static long mapVMStream(VirtualMachine vm, FileManager fileManager, int d) {
		try {
			return fileManager.newFD(d);
		} catch (IllegalStateException ex) {
			vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
		}
		return 0L;
	}

	private static Result readBytes(ExecutionContext<?> ctx, VirtualMachine vm, FileManager fileManager) {
		// Both FileInputStream/RandomAccessFile declare this method in the exact same way and should be treated similarly
		Locals locals = ctx.getLocals();
		InstanceValue _this = locals.loadReference(0);
		long handle = getFileStreamHandle(vm, _this);
		InputStream in = fileManager.getFdIn(handle);
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
				vm.getOperations().throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
		}
		return Result.ABORT;
	}

	private static long getFileStreamHandle(VirtualMachine vm, InstanceValue fs) {
		// Both FileInputStream/RandomAccessFile declare this method in the exact same way
		JavaMethod getFD = vm.getRuntimeResolver().resolveVirtualMethod(fs, "getFD", "()Ljava/io/FileDescriptor;");
		Locals locals = vm.getThreadStorage().newLocals(getFD);
		locals.setReference(0, fs);
		VMOperations ops = vm.getOperations();
		ObjectValue fd = ops.invokeReference(getFD, locals);
		return ops.getLong(fd, vm.getSymbols().java_io_FileDescriptor(), "handle");
	}
}
