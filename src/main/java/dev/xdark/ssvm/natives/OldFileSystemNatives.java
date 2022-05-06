package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.InstanceJavaClass;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.*;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Initializes file system in java.io package.
 *
 * @author xDark
 */
@UtilityClass
public class OldFileSystemNatives {

	/**
	 * Initializes win32/unix file system class.
	 *
	 * @param vm
	 * 		VM instance.
	 */
	public void init(VirtualMachine vm) {
		InstanceJavaClass fs = (InstanceJavaClass) vm.findBootstrapClass("java/io/WinNTFileSystem");
		boolean unix = false;
		if (fs == null) {
			fs = (InstanceJavaClass) vm.findBootstrapClass("java/io/UnixFileSystem");
			if (fs == null) {
				throw new IllegalStateException("Unable to locate file system implementation class for java.io package");
			}
			unix = true;
		}
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(fs, "initIDs", "()V", MethodInvoker.noop());
		vmi.setInvoker(fs, "canonicalize0", "(Ljava/lang/String;)Ljava/lang/String;", ctx -> {
			VMHelper helper = vm.getHelper();
			String path = helper.readUtf8(ctx.getLocals().load(1));
			try {
				ctx.setResult(helper.newUtf8(vm.getFileDescriptorManager().canonicalize(path)));
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, unix ? "getBooleanAttributes0" : "getBooleanAttributes", "(Ljava/io/File;)I", ctx -> {
			VMHelper helper = vm.getHelper();
			Value value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			try {
				String path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
				BasicFileAttributes attributes = vm.getFileDescriptorManager().getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(IntValue.ZERO);
				} else {
					int res = 1;
					if (attributes.isDirectory()) {
						res |= 4;
					} else {
						res |= 2;
					}
					ctx.setResult(IntValue.of(res));
				}
			} catch (IOException ex) {
				helper.throwException(vm.getSymbols().java_io_IOException, ex.getMessage());
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "list", "(Ljava/io/File;)[Ljava/lang/String;", ctx -> {
			VMHelper helper = vm.getHelper();
			Value value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			String path = helper.readUtf8(((InstanceValue) value).getValue("path", "Ljava/lang/String;"));
			String[] list = vm.getFileDescriptorManager().list(path);
			if (list == null) {
				ctx.setResult(NullValue.INSTANCE);
			} else {
				ObjectValue[] values = new ObjectValue[list.length];
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
			VMHelper helper = vm.getHelper();
			Value value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			try {
				String path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
				BasicFileAttributes attributes = vm.getFileDescriptorManager().getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(LongValue.ZERO);
				} else {
					ctx.setResult(LongValue.of(attributes.lastModifiedTime().toMillis()));
				}
			} catch (IOException ex) {
				ctx.setResult(LongValue.ZERO);
			}
			return Result.ABORT;
		});
		vmi.setInvoker(fs, "getLength", "(Ljava/io/File;)J", ctx -> {
			VMHelper helper = vm.getHelper();
			Value value = ctx.getLocals().load(1);
			helper.checkNotNull(value);
			try {
				String path = helper.readUtf8(helper.invokeVirtual("getAbsolutePath", "()Ljava/lang/String;", new Value[0], new Value[]{value}).getResult());
				BasicFileAttributes attributes = vm.getFileDescriptorManager().getAttributes(path, BasicFileAttributes.class);
				if (attributes == null) {
					ctx.setResult(LongValue.ZERO);
				} else {
					ctx.setResult(LongValue.of(attributes.size()));
				}
			} catch (IOException ex) {
				ctx.setResult(LongValue.ZERO);
			}
			return Result.ABORT;
		});
	}
}
