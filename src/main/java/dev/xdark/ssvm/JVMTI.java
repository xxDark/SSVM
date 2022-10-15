package dev.xdark.ssvm;

import dev.xdark.ssvm.jvmti.JVMTIEnv;
import dev.xdark.ssvm.jvmti.VMEventCollection;
import dev.xdark.ssvm.jvmti.event.ClassFileLoad;
import dev.xdark.ssvm.jvmti.event.ClassFilePrepare;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * JVMTI manager.
 *
 * @author xDark
 */
final class JVMTI implements VMEventCollection {

	private final VirtualMachine vm;
	private final List<JVMTIEnv> environmentList;
	private final ClassFileLoad classFileLoad;
	private final ClassFilePrepare classFilePrepare;

	JVMTI(VirtualMachine vm) {
		this.vm = vm;
		List<JVMTIEnv> environmentList = new CopyOnWriteArrayList<>();
		this.environmentList = environmentList;
		classFileLoad = (classBeingRedefined, classLoader, protectionDomain, data) -> {
			synchronized (environmentList) {
				for (int i = 0; i < environmentList.size(); i++) {
					JVMTIEnv env = environmentList.get(i);
					ClassFileLoad cfl = env.getClassFileLoad();
					if (cfl != null) {
						cfl.invoke(classBeingRedefined, classLoader, protectionDomain, data);
					}
				}
			}
		};
		classFilePrepare = klass -> {
			synchronized (environmentList) {
				for (int i = 0; i < environmentList.size(); i++) {
					JVMTIEnv env = environmentList.get(i);
					ClassFilePrepare cfp = env.getClassFilePrepare();
					if (cfp != null) {
						cfp.invoke(klass);
					}
				}
			}
		};
	}

	@Override
	public ClassFileLoad getClassFileLoad() {
		return classFileLoad;
	}

	@Override
	public ClassFilePrepare getClassFilePrepare() {
		return classFilePrepare;
	}

	JVMTIEnv create() {
		JVMTIEnv env = new JVMTIEnvImpl(vm, environmentList);
		synchronized (environmentList) {
			environmentList.add(env);
		}
		return env;
	}

	private static final class JVMTIEnvImpl implements JVMTIEnv {

		private final AtomicBoolean disposed = new AtomicBoolean();
		private final VirtualMachine vm;
		private final List<JVMTIEnv> environmentList;
		private ClassFileLoad classFileLoad;
		private ClassFilePrepare classFilePrepare;

		JVMTIEnvImpl(VirtualMachine vm, List<JVMTIEnv> environmentList) {
			this.vm = vm;
			this.environmentList = environmentList;
		}

		@Override
		public VirtualMachine vm() {
			return vm;
		}

		@Override
		public void setClassFileLoad(ClassFileLoad cfl) {
			classFileLoad = cfl;
		}

		@Override
		public void setClassFilePrepare(ClassFilePrepare cfp) {
			classFilePrepare = cfp;
		}

		@Override
		public ClassFileLoad getClassFileLoad() {
			return classFileLoad;
		}

		@Override
		public ClassFilePrepare getClassFilePrepare() {
			return classFilePrepare;
		}

		@Override
		public void dispose() {
			if (disposed.compareAndSet(false, true)) {
				synchronized (environmentList) {
					environmentList.remove(this);
				}
			}
		}
	}
}
