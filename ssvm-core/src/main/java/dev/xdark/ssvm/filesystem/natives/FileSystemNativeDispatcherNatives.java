package dev.xdark.ssvm.filesystem.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.mirror.member.JavaField;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.member.area.ClassArea;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Initializes sun/nio/fs/WindowsNativeDispatcher.
 *
 * @author xDark
 */
@UtilityClass
public class FileSystemNativeDispatcherNatives {

	/**
	 * @param vm          VM instance.
	 * @param fileManager File manager.
	 */
	public void init(VirtualMachine vm, FileManager fileManager) {
		InstanceClass windowsDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/WindowsNativeDispatcher");
		if (windowsDispatcher != null) {
			initWindows(vm, fileManager, windowsDispatcher);
			return;
		}

		InstanceClass unixDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/UnixNativeDispatcher");
		if (unixDispatcher != null) {
			initUnix(vm, fileManager, unixDispatcher);
			return;
		}

		InstanceClass linuxDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/LinuxNativeDispatcher");
		if (linuxDispatcher != null) {
			initLinux(vm, fileManager, linuxDispatcher);
			return;
		}

		InstanceClass bsdDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/BsdNativeDispatcher");
		if (bsdDispatcher != null) {
			initBsd(vm, fileManager, bsdDispatcher);
			return;
		}

		InstanceClass macDispatcher = (InstanceClass) vm.findBootstrapClass("sun/nui/fs/MacOSXNativeDispatcher");
		if (macDispatcher != null) {
			initMac(vm, fileManager, macDispatcher);
		}
	}

	private static void initMac(VirtualMachine vm, FileManager fileManager, InstanceClass macDispatcher) {
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(macDispatcher, "normalizepath", "([CI)[C", ctx -> {
			Locals locals = ctx.getLocals();
			VMOperations ops = vm.getOperations();
			ArrayValue path = ops.checkNotNull(locals.loadReference(0));
			// int form = locals.load(1).asInt();
			ctx.setResult(path);
			return Result.ABORT;
		});
	}

	private static void initBsd(VirtualMachine vm, FileManager fileManager, InstanceClass bsdDispatcher) {
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(bsdDispatcher, "initIDs", "()V", MethodInvoker.noop());
	}

	private static void initLinux(VirtualMachine vm, FileManager fileManager, InstanceClass linuxDispatcher) {
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(linuxDispatcher, "init", "()V", MethodInvoker.noop());
	}

	private static void initUnix(VirtualMachine vm, FileManager fileManager, InstanceClass unixDispatcher) {
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(unixDispatcher, "getcwd", "()[B", ctx -> {
			byte[] cwd = fileManager.getCurrentWorkingDirectory().getBytes(StandardCharsets.UTF_8);
			ctx.setResult(vm.getOperations().toVMBytes(cwd));
			return Result.ABORT;
		});
		vmi.setInvoker(unixDispatcher, "init", "()V", MethodInvoker.noop());
		vmi.setInvoker(unixDispatcher, "init", "()I", ctx -> {
			ctx.setResult(0);
			return Result.ABORT;
		});
	}

	private static void initWindows(VirtualMachine vm, FileManager fileManager, InstanceClass windowsDispatcher) {
		VMOperations ops = vm.getOperations();
		VMInterface vmi = vm.getInterface();
		vmi.setInvoker(windowsDispatcher, "initIDs", "()V", MethodInvoker.noop());

		InstanceClass winPath = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/WindowsPath");
		JavaField winPathString = winPath.getField("path", "Ljava/lang/String;");

		InstanceClass winAttrs = (InstanceClass) vm.findBootstrapClass("sun/nio/fs/WindowsFileAttributes");
		JavaMethod winAttrsCtor = winAttrs.getMethod("<init>", "(IJJJJIIII)V");
		vmi.setInvoker(winAttrs, "get", "(Lsun/nio/fs/WindowsPath;Z)Lsun/nio/fs/WindowsFileAttributes;", ctx -> {
			ObjectValue pathParam = ctx.getLocals().loadReference(0);
			String pathLiteral = ops.toString(vm.getMemoryManager().readReference(pathParam, winPathString.getOffset()));
			try {
				InstanceValue attrsInstance = vm.getMemoryManager().newInstance(winAttrs);
				BasicFileAttributes attrs = fileManager.getAttributes(pathLiteral, BasicFileAttributes.class);
				if (attrs != null) {
					Locals newLocals = vm.getThreadStorage().newLocals(winAttrsCtor);
					long creationTime = attrs.creationTime().toMillis();
					long lastAccessTime = attrs.lastAccessTime().toMillis();
					long lastWriteTime = attrs.lastModifiedTime().toMillis();
					long size = attrs.size();
					newLocals.setReference(0, attrsInstance);
					newLocals.setInt(1, 32); // fileAttrs
					newLocals.setLong(2, creationTime);
					newLocals.setLong(4, lastAccessTime);
					newLocals.setLong(6, lastWriteTime);
					newLocals.setLong(8, size);
					newLocals.setInt(10, 0); // reparseTag
					newLocals.setInt(11, 0); // volSerialNumber
					newLocals.setInt(12, 0); // fileIndexHigh
					newLocals.setInt(13, 0); // fileIndexLow
					ops.invokeReference(winAttrsCtor, newLocals);
					ctx.setResult(attrsInstance);
				} else {
					ops.throwException(vm.getSymbols().java_io_FileNotFoundException(), pathLiteral);
				}
			} catch (IOException ex) {
				ops.throwException(vm.getSymbols().java_io_IOException(), ex.getMessage());
			}
			return Result.ABORT;
		});
	}
}
