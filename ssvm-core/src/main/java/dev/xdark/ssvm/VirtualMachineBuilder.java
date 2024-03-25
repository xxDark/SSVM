package dev.xdark.ssvm;

import dev.xdark.ssvm.api.VMInterface;
import dev.xdark.ssvm.classloading.BootClassFinder;
import dev.xdark.ssvm.classloading.ClassDefiner;
import dev.xdark.ssvm.classloading.ClassLoaders;
import dev.xdark.ssvm.classloading.ClassStorage;
import dev.xdark.ssvm.execution.ExecutionEngine;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.jni.NativeLibraryManager;
import dev.xdark.ssvm.jvm.ManagementInterface;
import dev.xdark.ssvm.memory.allocation.MemoryAllocator;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.mirror.MirrorFactory;
import dev.xdark.ssvm.operation.VMOperations;
import dev.xdark.ssvm.symbol.Primitives;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.synchronizer.ObjectSynchronizer;
import dev.xdark.ssvm.thread.ThreadManager;
import dev.xdark.ssvm.timezone.TimeManager;
import dev.xdark.ssvm.util.Reflection;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An intermediate 'builder' for {@link VirtualMachine} which allows swapping out of a few components of an existing VM.
 *
 * @author Matt Coley
 */
public class VirtualMachineBuilder {
	private AtomicReference<InitializationState> state;
	private VMInterface vmInterface;
	private MemoryAllocator memoryAllocator;
	private ObjectSynchronizer objectSynchronizer;
	private MemoryManager memoryManager;
	private ClassDefiner classDefiner;
	private ThreadManager threadManager;
	private FileManager fileManager;
	private NativeLibraryManager nativeLibraryManager;
	private TimeManager timeManager;
	private ManagementInterface managementInterface;
	private StringPool stringPool;
	private ClassLoaders classLoaders;
	private ExecutionEngine executionEngine;
	private MirrorFactory mirrorFactory;
	private BootClassFinder bootClassFinder;
	private ClassStorage classStorage;
	private LinkResolver linkResolver;
	private RuntimeResolver runtimeResolver;
	private Map<String, String> properties;
	private Map<String, String> env;
	private Reflection reflection;
	private JVMTI jvmti;
	private VMOperations operations;
	private Symbols symbols;
	private Primitives primitives;
	private InstanceValue systemThreadGroup;
	private InstanceValue mainThreadGroup;

	/**
	 * @param vm Base VM to copy values from.
	 */
	VirtualMachineBuilder(VirtualMachine vm) {
		this.vmInterface = vm.getInterface();
		this.memoryAllocator = vm.getMemoryAllocator();
		this.objectSynchronizer = vm.getObjectSynchronizer();
		this.memoryManager = vm.getMemoryManager();
		this.classDefiner = vm.getClassDefiner();
		this.threadManager = vm.getThreadManager();
		this.fileManager = vm.getFileManager();
		this.nativeLibraryManager = vm.getNativeLibraryManager();
		this.timeManager = vm.getTimeManager();
		this.managementInterface = vm.getManagementInterface();
		this.stringPool = vm.getStringPool();
		this.classLoaders = vm.getClassLoaders();
		this.executionEngine = vm.getExecutionEngine();
		this.mirrorFactory = vm.getMirrorFactory();
		this.bootClassFinder = vm.getBootClassFinder();
		this.classStorage = vm.getClassStorage();
		this.linkResolver = vm.getLinkResolver();
		this.runtimeResolver = vm.getRuntimeResolver();
		this.properties = vm.getProperties();
		this.env = vm.getenv();
		this.reflection = vm.getReflection();
		this.jvmti = vm.getJvmti();
		this.operations = vm.getOperations();
		this.symbols = vm.getSymbols();
		this.primitives = vm.getPrimitives();
		this.systemThreadGroup = vm.getSystemThreadGroup();
		this.mainThreadGroup = vm.getMainThreadGroup();
	}

	/**
	 * @see VMInterface#copy() Used to deep copy an existing VM interface.
	 * @param vmInterface New VM interface to use.
	 * @return Self.
	 */
	public VirtualMachineBuilder withVmInterface(VMInterface vmInterface) {
		this.vmInterface = vmInterface;
		return this;
	}

	/**
	 * @param fileManager New file manager to use.
	 * @return Self.
	 */
	public VirtualMachineBuilder withFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
		return this;
	}

	/**
	 * @param timeManager New time manager to use.
	 * @return Self.
	 */
	public VirtualMachineBuilder withTimeManager(TimeManager timeManager) {
		this.timeManager = timeManager;
		return this;
	}

	/**
	 * @param executionEngine New execution engine to use.
	 * @return Self.
	 */
	public VirtualMachineBuilder withExecutionEngine(ExecutionEngine executionEngine) {
		this.executionEngine = executionEngine;
		return this;
	}

	/**
	 * @return New VM with values from this builder.
	 */
	public VirtualMachine build() {
		return new VirtualMachine(vmInterface,
				memoryAllocator,
				objectSynchronizer,
				memoryManager,
				classDefiner,
				threadManager,
				fileManager,
				nativeLibraryManager,
				timeManager,
				managementInterface,
				stringPool,
				classLoaders,
				executionEngine,
				mirrorFactory,
				bootClassFinder,
				classStorage,
				linkResolver,
				runtimeResolver,
				properties,
				env,
				reflection,
				jvmti,
				operations,
				symbols,
				primitives,
				systemThreadGroup,
				mainThreadGroup);
	}
}
