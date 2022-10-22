package dev.xdark.ssvm.util;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.RuntimeResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.classloading.ClassStorage;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;

/**
 * Shortcuts for VM functions.
 *
 * @apiNote xDark
 */
public interface VMFunctions {

	/**
	 * @return VM instance shortcuts are delegated to.
	 */
	VirtualMachine getVM();

	/**
	 * @return VM operations.
	 */
	default VMOperations getOperations() {
		return getVM().getOperations();
	}

	/**
	 * @return Link resolver.
	 */
	default LinkResolver getLinkResolver() {
		return getVM().getLinkResolver();
	}

	/**
	 * @return Runtime resolver.
	 */
	default RuntimeResolver getRuntimeResolver() {
		return getVM().getRuntimeResolver();
	}

	/**
	 * @return Symbols.
	 */
	default Symbols getSymbols() {
		return getVM().getSymbols();
	}

	/**
	 * @return Primitives.
	 */
	default Primitives getPrimitives() {
		return getVM().getPrimitives();
	}

	/**
	 * @return Memory manager.
	 */
	default MemoryManager getMemoryManager() {
		return getVM().getMemoryManager();
	}

	/**
	 * @return Class storage.
	 */
	default ClassStorage getClassStorage() {
		return getVM().getClassStorage();
	}
}
