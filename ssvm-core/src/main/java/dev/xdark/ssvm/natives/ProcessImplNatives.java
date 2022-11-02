package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.filesystem.FileDescriptorManager;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.process.ProcessHandleManager;
import dev.xdark.ssvm.value.ArrayValue;
import dev.xdark.ssvm.value.ObjectValue;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * ProcessImpl classes natives
 * @author Justus Garbe
 */
@UtilityClass
public class ProcessImplNatives {

    public void init(VirtualMachine vm) {

        InstanceClass genericProcessImpl = (InstanceClass) vm.findBootstrapClass("java/lang/ProcessImpl");
        InstanceClass unixProcessImpl = (InstanceClass) vm.findBootstrapClass("java/lang/UNIXProcess");

        ProcessHandleManager manager = vm.getProcessHandleManager();

        VMOperations ops = vm.getOperations();
        VMInterface vmi = vm.getInterface();

        // JDK 8
        // windows has the implementation in ProcessImpl
        JavaMethod createWindowsProcess = genericProcessImpl.getMethod("create",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[JZ)J");
        if(createWindowsProcess != null) { // windows jdk 8
            vmi.setInvoker(createWindowsProcess, (ctx) -> {
                Locals locals = ctx.getLocals();
                String cmd = ops.readUtf8(locals.loadReference(0));
                String env = ops.readUtf8(locals.loadReference(1));
                String dir = ops.readUtf8(locals.loadReference(2));
                ArrayValue array = locals.loadReference(3);
                long[] fps = ops.toJavaLongs(array);
                boolean redirectErrorStream = locals.loadInt(4) != 0;
                String[] envp = env.split("\0");
                long handle = manager.createProcessHandle(cmd, envp, dir, fps, redirectErrorStream);
                ctx.setResult(handle);
                return Result.ABORT;
            });
            // same goes for all other handle related methods
            vmi.setInvoker(genericProcessImpl, "getStillActive", "()J", (ctx) -> {
                ctx.setResult(ProcessHandleManager.STILL_ACTIVE); // needs to be -1 because it needs to be different from getExitCodeProcess
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "getExitCodeProcess", "(J)I", (ctx) -> {
                ctx.setResult(manager.getExitCode(ctx.getLocals().loadLong(0)));
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "waitForInterruptibly", "(J)V", (ctx) -> {
                manager.waitForProcess(ctx.getLocals().loadLong(0), 0);
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "waitForTimeoutInterruptibly", "(JJ)V", (ctx) -> {
                Locals locals = ctx.getLocals();
                manager.waitForProcess(locals.loadLong(0), locals.loadLong(1));
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "terminateProcess", "(J)V", (ctx) -> {
                manager.terminateProcess(ctx.getLocals().loadLong(0));
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "isProcessAlive", "(J)Z", (ctx) -> {
                ctx.setResult(manager.processAlive(ctx.getLocals().loadLong(0)) ? 1 : 0);
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "openForAtomicAppend", "(Ljava/lang/String;)J", (ctx) -> {
                try {
                    vm.getFileDescriptorManager().open(ops.readUtf8(ctx.getLocals().loadReference(0)),
                            FileDescriptorManager.APPEND);
                } catch (IOException e) {
                    ops.throwException(vm.getSymbols().java_io_IOException(), e.getMessage());
                }
                return Result.ABORT;
            });
            vmi.setInvoker(genericProcessImpl, "closeHandle", "(J)V", (ctx) -> {
                manager.closeProcessHandle(ctx.getLocals().loadLong(0));
                return Result.ABORT;
            });
        } else {
            JavaMethod forkAndExec;
            if(unixProcessImpl == null) {
                // try jdk 9+
                // in jdk 9+ these methods are directly in ProcessImpl
                forkAndExec = genericProcessImpl.getMethod("forkAndExec",
                        "(I[B[B[BI[BI[B[IZ)I");
                if(forkAndExec != null) unixProcessImpl = genericProcessImpl;
                else throw new IllegalStateException("Could not find a suitable ProcessImpl class");
            } else {
                // unix has the implementation in UNIXProcess
                // main method: forkAndExec
                forkAndExec = unixProcessImpl.getMethod("forkAndExec",
                        "(I[B[B[BI[BI[B[IZ)I");
            }
            if(forkAndExec == null) {
                // try jdk 9+
                // in jdk 9+ these methods are directly in ProcessImpl
                forkAndExec = genericProcessImpl.getMethod("forkAndExec",
                        "(I[B[B[BI[BI[B[IZ)I");
                if(forkAndExec != null) unixProcessImpl = genericProcessImpl;
                else throw new IllegalStateException("Could not find a suitable ProcessImpl class");
            }
            // java has different implementations for: solaris, bsd and linux
            // ,but they follow the same pattern:
            // 		init;
            //		waitForProcessExit;
            //		forkAndExec;
            //		destroyProcess;
            vmi.setInvoker(forkAndExec, (ctx) -> {
                Locals locals = ctx.getLocals();
                // 1 - mode (int) - ignored
                // 2 - helperpath (byte[]) - ignored
                ArrayValue cmd = locals.loadReference(3); // byte array
                ArrayValue argv = locals.loadReference(4); // byte array
                int argc = locals.loadInt(5);
                ObjectValue envpValue = locals.loadReference(6); // byte array
                int envc = locals.loadInt(7);
                ObjectValue dirValue = locals.loadReference(8); // byte array
                ArrayValue fps = locals.loadReference(9); // int array
                boolean redirectErrorStream = locals.loadInt(10) != 0;
                String cmdStr = new String(ops.toJavaBytes(cmd));
                StringBuilder cmdLine = new StringBuilder(cmdStr + " ");
                // assert st.countTokens() == argc;
                StringTokenizer st = new StringTokenizer(new String(ops.toJavaBytes(argv)), "\0");
                for(int i = 0; i < argc; i++) {
                    cmdLine.append(st.nextToken()).append(" ");
                }
                String[] envp = null;
                if(envpValue != vm.getMemoryManager().nullValue()) {
                    envp = new String(ops.toJavaBytes((ArrayValue) envpValue)).split("\0");
                }
                String dir = null;
                if(dirValue != vm.getMemoryManager().nullValue()) {
                    dir = new String(ops.toJavaBytes((ArrayValue) dirValue));
                }
                int[] fpsInt = ops.toJavaInts(fps);
                // upcast to long
                long[] fpsLong = new long[fpsInt.length];
                for(int i = 0; i < fpsInt.length; i++) {
                    fpsLong[i] = fpsInt[i];
                }
                try {
                    long handle = manager.createProcessHandle(cmdLine.toString(), envp, dir, fpsLong, redirectErrorStream);
                    ctx.setResult((int) handle);
                    return Result.ABORT;
                } catch (Exception e) {
                    ops.throwException(vm.getSymbols().java_io_IOException(), e.getMessage());
                    return Result.ABORT;
                }
            });
            // same goes for all other handle related methods
            vmi.setInvoker(unixProcessImpl, "init", "()V", MethodInvoker.noop());
            vmi.setInvoker(unixProcessImpl, "waitForProcessExit", "(I)I", (ctx) -> {
                int handle = ctx.getLocals().loadInt(1);
                manager.waitForProcess(handle, 0);
                ctx.setResult(manager.getExitCode(handle));
                return Result.ABORT;
            });
            vmi.setInvoker(unixProcessImpl, "destroyProcess", "(IZ)V", (ctx) -> {
                int handle = ctx.getLocals().loadInt(0);
                manager.terminateProcess(handle);
                manager.closeProcessHandle(handle);
                return Result.ABORT;
            });
        }


    }

}
