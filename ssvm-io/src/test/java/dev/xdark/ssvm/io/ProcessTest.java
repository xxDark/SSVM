package dev.xdark.ssvm.io;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.process.HostProcessManager;
import org.junit.jupiter.api.Test;

/**
 * Tests for Process API.
 * @author Justus Garbe
 */
public class ProcessTest {

    @Test
    public void testHostBootup() {
        VirtualMachine vm = new VirtualMachine();
        vm.initialize();
        HostProcessManager manager = new HostProcessManager(vm); // should call method resolver
    }

}
