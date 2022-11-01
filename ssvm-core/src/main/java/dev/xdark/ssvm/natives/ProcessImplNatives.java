package dev.xdark.ssvm.natives;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.api.MethodInvoker;
import dev.xdark.ssvm.execution.Result;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.InstanceClass;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcessImplNatives {

    public void init(VirtualMachine vm) {

        InstanceClass genericProcessImpl = (InstanceClass) vm.findBootstrapClass("java/lang/ProcessImpl");
        InstanceClass unixProcessImpl = (InstanceClass) vm.findBootstrapClass("java/lang/UNIXProcess");

        // TODO: introduce process manager

        // JDK 8
        // windows has the implementation in ProcessImpl
        JavaMethod createWindowsProcess = genericProcessImpl.getMethod("create",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[JZ)J");
        if(createWindowsProcess != null) { // windows jdk 8
            vm.getInterface().setInvoker(createWindowsProcess, (ctx) -> {
                // optimally we would want to point to the direct win32 api call, but we cannot do that. So either
                // we handle process creation ourselves, or we just set a limit for sandboxing here.
                ctx.setResult(0);
                return Result.ABORT;
            });
            // same goes for all other handle related methods
            vm.getInterface().setInvoker(genericProcessImpl, "getStillActive", "()J", (ctx) -> {
                ctx.setResult(-1); // needs to be -1 because it needs to be different from getExitCodeProcess
                return Result.ABORT;
            });
            vm.getInterface().setInvoker(genericProcessImpl, "getExitCodeProcess", "(J)I", (ctx) -> {
                ctx.setResult(0);
                return Result.ABORT;
            });
            vm.getInterface().setInvoker(genericProcessImpl, "waitForInterruptibly", "(J)V", MethodInvoker.noop());
            vm.getInterface().setInvoker(genericProcessImpl, "waitForTimeoutInterruptibly", "(JJ)V", MethodInvoker.noop());
            vm.getInterface().setInvoker(genericProcessImpl, "terminateProcess", "(J)V", MethodInvoker.noop());
            vm.getInterface().setInvoker(genericProcessImpl, "isProcessAlive", "(J)Z", (ctx) -> {
                ctx.setResult(0);
                return Result.ABORT;
            });
            vm.getInterface().setInvoker(genericProcessImpl, "openForAtomicAppend", "(Ljava/lang/String;)J", (ctx) -> {
                ctx.setResult(0);
                return Result.ABORT;
            });
            vm.getInterface().setInvoker(genericProcessImpl, "closeHandle", "(J)V", MethodInvoker.noop());
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
            }
            if(forkAndExec != null) {
                // java has different implementations for: solaris, bsd and linux
                // ,but they follow the same pattern:
                // 		init;
                //		waitForProcessExit;
                //		forkAndExec;
                //		destroyProcess;
                vm.getInterface().setInvoker(forkAndExec, (ctx) -> {
                    // optimally we would want to point to the direct fork and execvp syscall, but we cannot do that. So either
                    // we handle process creation ourselves, or we just set a limit for sandboxing here.
                    ctx.setResult(0);
                    return Result.ABORT;
                });
                // same goes for all other handle related methods
                vm.getInterface().setInvoker(unixProcessImpl, "init", "()V", MethodInvoker.noop());
                vm.getInterface().setInvoker(unixProcessImpl, "waitForProcessExit", "(I)I", (ctx) -> {
                    ctx.setResult(0);
                    return Result.ABORT;
                });
                vm.getInterface().setInvoker(unixProcessImpl, "destroyProcess", "(IZ)V", MethodInvoker.noop());
            } else { // no implementation found
                throw new IllegalStateException("Could not find a suitable ProcessImpl class");
            }
        }


    }

}
