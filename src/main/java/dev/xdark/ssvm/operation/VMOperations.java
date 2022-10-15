package dev.xdark.ssvm.operation;

import dev.xdark.ssvm.LinkResolver;
import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.memory.management.MemoryManager;
import dev.xdark.ssvm.memory.management.StringPool;
import dev.xdark.ssvm.symbol.Symbols;
import dev.xdark.ssvm.thread.ThreadManager;
import lombok.experimental.Delegate;

/**
 * All VM operations.
 *
 * @author xDark
 */
public final class VMOperations implements
	AllocationOperations,
	ConversionOperations,
	ArrayOperations,
	ExceptionOperations,
	FieldOperations,
	InvocationOperations,
	PrimitiveOperations,
	StringOperations,
	SynchronizationOperations,
	VerificationOperations,
	ClassOperations,
	MethodHandleOperations,
	InvokeDynamicOperations,
	ConstantOperations {

	// This class is a hack to not
	// copy code in one place.
	@Delegate
	private final AllocationOperations allocationOperations;
	@Delegate
	private final ConversionOperations conversionOperations;
	@Delegate
	private final ArrayOperations arrayOperations;
	@Delegate
	private final ExceptionOperations exceptionOperations;
	@Delegate
	private final FieldOperations fieldOperations;
	@Delegate
	private final InvocationOperations invocationOperations;
	@Delegate
	private final PrimitiveOperations primitiveOperations;
	@Delegate
	private final StringOperations stringOperations;
	@Delegate
	private final SynchronizationOperations synchronizationOperations;
	@Delegate
	private final VerificationOperations verificationOperations;
	@Delegate
	private final ClassOperations classOperations;
	@Delegate
	private final MethodHandleOperations methodHandleOperations;
	@Delegate
	private final InvokeDynamicOperations invokeDynamicOperations;
	@Delegate
	private final ConstantOperations constantOperations;

	public VMOperations(VirtualMachine vm) {
		MemoryManager memoryManager = vm.getMemoryManager();
		Symbols symbols = vm.getSymbols();
		ThreadManager threadManager = vm.getThreadManager();
		LinkResolver linkResolver = vm.getLinkResolver();
		StringPool stringPool = vm.getStringPool();
		allocationOperations = new DefaultAllocationOperations(memoryManager, symbols, vm.getPrimitives(), this, this, this);
		conversionOperations = new DefaultConversionOperations(symbols, memoryManager, allocationOperations);
		arrayOperations = new DefaultArrayOperations(symbols, this, this);
		exceptionOperations = new DefaultExceptionOperations(memoryManager, symbols, this, this, this);
		fieldOperations = new DefaultFieldOperations(memoryManager, linkResolver, this);
		invocationOperations = new DefaultInvocationOperations(vm.getExecutionEngine(), threadManager);
		primitiveOperations = new DefaultPrimitiveOperations(symbols, threadManager, linkResolver, invocationOperations, this);
		stringOperations = new DefaultStringOperations(memoryManager, threadManager, symbols, linkResolver, allocationOperations, invocationOperations, conversionOperations);
		synchronizationOperations = new DefaultSynchronizationOperations(symbols, memoryManager, this, exceptionOperations);
		verificationOperations = new DefaultVerificationOperations(symbols, exceptionOperations);
		classOperations = new DefaultClassOperations(vm.getMirrorFactory(), memoryManager, threadManager, vm.getBootClassFinder(), linkResolver, symbols, vm.getClassLoaders(), vm.getClassDefiner(), null, exceptionOperations, invocationOperations, stringOperations, verificationOperations, this);
		methodHandleOperations = new DefaultMethodHandleOperations(symbols, threadManager, linkResolver, classOperations, invocationOperations, allocationOperations, stringOperations);
		invokeDynamicOperations = new DefaultInvokeDynamicOperations(symbols, threadManager, stringPool, linkResolver, vm.getClassStorage(), memoryManager, exceptionOperations, allocationOperations, methodHandleOperations, invocationOperations, verificationOperations, fieldOperations, classOperations, primitiveOperations);
		constantOperations = new DefaultConstantOperations(memoryManager, threadManager, stringPool, classOperations, methodHandleOperations);
	}
}
