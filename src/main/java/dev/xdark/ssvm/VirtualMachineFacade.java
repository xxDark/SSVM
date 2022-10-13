package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassFinder;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassStorage;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.fs.FileDescriptorManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.nt.NativeLibraryManager;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.tz.TimeManager;

import java.util.Map;

/**
 * Virtual machine facade.
 *
 * @author xDark
 */
public interface VirtualMachineFacade {

	/**
	 * @return VM interface.
	 */
	VMInterface getInterface();

	/**
	 * @return Memory allocator.
	 */
	MemoryAllocator getMemoryAllocator();

	/**
	 * @return Memory manager.
	 */
	MemoryManager getMemoryManager();

	/**
	 * @return Link resolver.
	 */
	LinkResolver getLinkResolver();

	/**
	 * @return Class definer.
	 */
	ClassDefiner getClassDefiner();

	/**
	 * Returns thread manager.
	 *
	 * @return thread manager.
	 */
	ThreadManager getThreadManager();

	/**
	 * Returns file descriptor manager.
	 *
	 * @return file descriptor manager.
	 */
	FileDescriptorManager getFileDescriptorManager();

	/**
	 * Returns native library manager.
	 *
	 * @return native library manager.
	 */
	NativeLibraryManager getNativeLibraryManager();

	/**
	 * Returns string pool.
	 *
	 * @return string pool.
	 */
	StringPool getStringPool();

	/**
	 * Returns management interface.
	 *
	 * @return management interface.
	 */
	ManagementInterface getManagementInterface();

	/**
	 * Returns time manager.
	 *
	 * @return time manager.
	 */
	TimeManager getTimeManager();

	/**
	 * Returns class loaders storage.
	 *
	 * @return class loaders storage.
	 */
	ClassLoaders getClassLoaders();

	/**
	 * Returns execution engine.
	 *
	 * @return execution engine.
	 */
	ExecutionEngine getExecutionEngine();

	/**
	 * Returns mirror factory.
	 *
	 * @return mirror factory.
	 */
	MirrorFactory getMirrorFactory();

	/**
	 * Returns object synchronizer.
	 *
	 * @return object synchronizer.
	 */
	ObjectSynchronizer getObjectSynchronizer();

	/**
	 * Returns bootstrap class finder.
	 *
	 * @return Bootstrap class finder.
	 */
	BootClassFinder getBootClassFinder();

	/**
	 * Returns properties that will be used
	 * for initialization.
	 *
	 * @return system properties.
	 */
	Map<String, String> getProperties();

	/**
	 * Returns process environment variables that will be used
	 * for initialization.
	 *
	 * @return environment variables.
	 */
	Map<String, String> getenv();

	/**
	 * @return VM symbols.
	 */
	Symbols getSymbols();

	/**
	 * @return VM primitives.
	 */
	Primitives getPrimitives();

	/**
	 * @return Class storage.
	 */
	ClassStorage getClassStorage();
}
