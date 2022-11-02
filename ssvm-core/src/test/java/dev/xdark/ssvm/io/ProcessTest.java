package dev.xdark.ssvm.io;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.Locals;
import dev.xdark.ssvm.filesystem.FileDescriptorManager;
import dev.xdark.ssvm.filesystem.HostFileDescriptorManager;
import dev.xdark.ssvm.mirror.member.JavaMethod;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.process.HostProcessHandleManager;
import dev.xdark.ssvm.process.ProcessHandleManager;
import dev.xdark.ssvm.value.ObjectValue;
import org.junit.jupiter.api.Test;

public class ProcessTest {

    @Test
    public void testHostBootup() {
        VirtualMachine vm = new VirtualMachine();
        vm.initialize();
        HostProcessHandleManager manager = new HostProcessHandleManager(vm); // should call method resolver
    }

}
