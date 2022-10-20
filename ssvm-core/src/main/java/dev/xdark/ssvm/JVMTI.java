package dev.xdark.ssvm;

import dev.xdark.ssvm.jvmti.JVMTIEnv;
import dev.xdark.ssvm.jvmti.VMEventCollection;
import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.jvmti.event.ClsasPrepare;

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
	private final ClsasPrepare clsasPrepare;
	private final ClassLink classLink;

	JVMTI(VirtualMachine vm) {
		this.vm = vm;
		List<JVMTIEnv> environmentList = new CopyOnWriteArrayList<>();
		this.environmentList = environmentList;
		clsasPrepare = klass -> {
			synchronized (environmentList) {
				for (int i = 0; i < environmentList.size(); i++) {
					JVMTIEnv env = environmentList.get(i);
					ClsasPrepare cfp = env.getClassPrepare();
					if (cfp != null) {
						cfp.invoke(klass);
					}
				}
			}
		};
		classLink = klass -> {
			synchronized (environmentList) {
				for (int i = 0; i < environmentList.size(); i++) {
					JVMTIEnv env = environmentList.get(i);
					ClassLink cfl = env.getClassLink();
					if (cfl != null) {
						cfl.invoke(klass);
					}
				}
			}
		};
	}

	@Override
	public ClsasPrepare getClassPrepare() {
		return clsasPrepare;
	}

	@Override
	public ClassLink getClassLink() {
		return classLink;
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
		private ClsasPrepare clsasPrepare;
		private ClassLink classLink;

		JVMTIEnvImpl(VirtualMachine vm, List<JVMTIEnv> environmentList) {
			this.vm = vm;
			this.environmentList = environmentList;
		}

		@Override
		public VirtualMachine vm() {
			return vm;
		}

		@Override
		public void setClassPrepare(ClsasPrepare cfp) {
			clsasPrepare = cfp;
		}

		@Override
		public void setClassLink(ClassLink cl) {
			classLink = cl;
		}

		@Override
		public ClsasPrepare getClassPrepare() {
			return clsasPrepare;
		}

		@Override
		public ClassLink getClassLink() {
			return classLink;
		}

		@Override
		public void close() {
			if (disposed.compareAndSet(false, true)) {
				synchronized (environmentList) {
					environmentList.remove(this);
				}
			}
		}
	}
}
