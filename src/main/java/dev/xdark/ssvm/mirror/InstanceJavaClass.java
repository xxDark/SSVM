package dev.xdark.ssvm.mirror;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.value.Value;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class InstanceJavaClass implements JavaClass {

	private final VirtualMachine vm;
	private final Value classLoader;
	private final Lock initializationLock;
	private final Condition signal;
	private final ClassReader classReader;
	public final ClassNode node;
	private Value oop;
	private ClassLayout layout;
	private InstanceJavaClass superClass;
	private InstanceJavaClass[] interfaces;

	// Initialization
	private volatile State state = State.PENDING;
	private volatile Thread initializer;

	// Stuff to cache
	private String normalName;
	private String descriptor;

	/**
	 * @param vm
	 * 		VM.
	 * @param classLoader
	 * 		Loader of the class.
	 * @param classReader
	 * 		Source of the class.
	 * @param node
	 * 		ASM class data.
	 * @param oop
	 * 		Clas oop.
	 */
	public InstanceJavaClass(VirtualMachine vm, Value classLoader, ClassReader classReader, ClassNode node, Value oop) {
		this.vm = vm;
		this.classLoader = classLoader;
		this.classReader = classReader;
		this.node = node;
		this.oop = oop;
		var lock = new ReentrantLock();
		initializationLock = lock;
		signal = lock.newCondition();
	}

	/**
	 * This constructor must be invoked ONLY
	 * by the VM.
	 *
	 * @param vm
	 * 		VM instance.
	 * @param classLoader
	 * 		Loader of the class.
	 * @param classReader
	 * 		Source of the class.
	 * @param node
	 * 		ASM class data.
	 */
	public InstanceJavaClass(VirtualMachine vm, Value classLoader, ClassReader classReader, ClassNode node) {
		this(vm, classLoader, classReader, node, null);
	}

	@Override
	public String getName() {
		var normalName = this.normalName;
		if (normalName == null) {
			return this.normalName = node.name.replace('/', '.');
		}
		return normalName;
	}

	@Override
	public String getInternalName() {
		return node.name;
	}

	@Override
	public String getDescriptor() {
		var descriptor = this.descriptor;
		if (descriptor == null) {
			return this.descriptor = 'L' + node.name + ';';
		}
		return descriptor;
	}

	@Override
	public int getModifiers() {
		return node.access;
	}

	@Override
	public Value getClassLoader() {
		return classLoader;
	}

	@Override
	public Value getOop() {
		return oop;
	}

	/**
	 * Class initialization.
	 */
	public void initialize() {
		var lock = initializationLock;
		lock.lock();
		if (state == State.COMPLETE) {
			lock.unlock();
			return;
		}
		if (state == State.FAILED) {
			lock.unlock();
			// TODO
			throw new IllegalStateException();
		}
		if (state == State.IN_PROGRESS) {
			if (Thread.currentThread() == initializer) {
				lock.unlock();
				return;
			}
			// Wait for initialization to complete
			// and invoke initialize again
			// 'cause maybe we crashed
			var signal = this.signal;
			while (true) {
				try {
					signal.wait();
					break;
				} catch (InterruptedException ignored) {
				}
			}
			lock.unlock();
			initialize();
			return;
		}
		state = State.IN_PROGRESS;
		initializer = Thread.currentThread();
		var superName = node.superName;
		if (superName != null) {
			// Load parent class.

		}
		// TODO interfaces.
		// Build class layout
		var offsetMap = new HashMap<FieldInfo, Long>();
		var offset = 0L;
		InstanceJavaClass javaClass = this;
		do {
			var fields = javaClass.node.fields;
			for (int i = 0, j = fields.size(); i < j; i++) {
				var field = fields.get(i);
				if ((field.access & Opcodes.ACC_STATIC) == 0) {
					var desc = field.desc;
					offsetMap.put(new FieldInfo(field.name, desc), offset);
					offset += getSizeFor(desc);
				}
			}
			javaClass = javaClass.superClass;
		} while (javaClass != null);
		layout = new ClassLayout(Collections.unmodifiableMap(offsetMap), offset);
		state = State.COMPLETE;
		signal.signalAll();
		lock.unlock();
		initializer = null;
	}

	/**
	 * Returns class layout.
	 *
	 * @return class layout.
	 */
	@Override
	public ClassLayout getLayout() {
		return layout;
	}

	@Override
	public InstanceJavaClass getSuperClass() {
		initialize();
		return superClass;
	}

	@Override
	public InstanceJavaClass[] getInterfaces() {
		initialize();
		return interfaces;
	}

	@Override
	public ArrayJavaClass newArrayClass() {
		return new ArrayJavaClass(vm, '[' + descriptor, 1, this);
	}

	/**
	 * Returns VM instance in which this class
	 * was loaded.
	 *
	 * @return VM instance.
	 */
	public VirtualMachine getVM() {
		return vm;
	}

	/**
	 * Sets oop of the class.
	 *
	 * @param oop
	 * 		Class oop.
	 */
	public void setOop(Value oop) {
		this.oop = oop;
	}

	/**
	 * Searches for a method by it's name and descriptor.
	 *
	 * @param name
	 * 		Name of the method.
	 * @param desc
	 * 		Descriptor of the method.
	 *
	 * @return class method or {@code null}, if not found.
	 */
	public MethodNode getMethod(String name, String desc) {
		var methods = node.methods;
		for (int i = 0, j = methods.size(); i < j; i++) {
			var mn = methods.get(i);
			if (name.equals(mn.name) && desc.equals(mn.desc)) {
				return mn;
			}
		}
		return null;
	}

	@SuppressWarnings("DuplicateBranchesInSwitch")
	private static long getSizeFor(String desc) {
		switch (desc) {
			case "J":
			case "D":
				return 8L;
			case "I":
			case "F":
				return 4L;
			case "C":
			case "S":
				return 2L;
			case "B":
			case "Z":
				return 1L;
			default:
				return 8L;
		}
	}

	@Override
	public String toString() {
		return getName();
	}

	private enum State {
		PENDING,
		IN_PROGRESS,
		COMPLETE,
		FAILED,
	}
}
