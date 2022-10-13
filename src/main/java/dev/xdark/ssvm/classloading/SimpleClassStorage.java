package dev.xdark.ssvm.classloading;

import dev.xdark.ssvm.inject.InjectedClassLayout;
import dev.xdark.ssvm.mirror.type.JavaClass;
import dev.xdark.ssvm.operation.FieldOperations;
import dev.xdark.ssvm.util.Assertions;
import dev.xdark.ssvm.value.InstanceValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Constantly expanding class storage.
 *
 * @author xDark
 */
public final class SimpleClassStorage implements ClassStorage {

	private final List<JavaClass> classes = new ArrayList<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final FieldOperations ops;

	/**
	 * @param ops Field operations.
	 */
	public SimpleClassStorage(FieldOperations ops) {
		this.ops = ops;
	}

	@Override
	public synchronized int register(JavaClass klass) {
		Lock lock = this.lock.writeLock();
		lock.lock();
		try {
			List<JavaClass> classes = this.classes;
			int id = classes.size();
			classes.add(klass);
			ops.putInt(klass.getOop(), InjectedClassLayout.java_lang_Class_id.name(), id);
			return id;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public JavaClass lookup(InstanceValue oop) {
		int id = ops.getInt(oop, InjectedClassLayout.java_lang_Class_id.name());
		JavaClass mirror = lookup(id);
		Assertions.notNull(mirror, "no mirror");
		return mirror;
	}

	@Override
	public JavaClass lookup(int classId) {
		if (classId < 0) {
			return null;
		}
		Lock lock = this.lock.readLock();
		lock.lock();
		try {
			List<JavaClass> classes = this.classes;
			if (classId >= classes.size()) {
				return null;
			}
			return classes.get(classId);
		} finally {
			lock.unlock();
		}
	}
}
