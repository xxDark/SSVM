package dev.xdark.ssvm;

import dev.xdark.ssvm.jvmti.JVMTIEnv;
import dev.xdark.ssvm.jvmti.VMEventCollection;
import dev.xdark.ssvm.jvmti.event.ClassLink;
import dev.xdark.ssvm.jvmti.event.ClassPrepare;
import dev.xdark.ssvm.jvmti.event.MethodEnter;
import dev.xdark.ssvm.jvmti.event.MethodExit;

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
	private final ClassPrepare classPrepare;
	private final ClassLink classLink;
	private final MethodEnter methodEnter;
	private final MethodExit methodExit;

	JVMTI(VirtualMachine vm) {
		this.vm = vm;
		List<JVMTIEnv> environmentList = new CopyOnWriteArrayList<>();
		this.environmentList = environmentList;
		// TODO: use method handles here
		// and possibly split up each event into its own list
		classPrepare = klass -> {
			for (JVMTIEnv env : environmentList) {
				ClassPrepare cfp = env.getClassPrepare();
				if (cfp != null) {
					cfp.invoke(klass);
				}
			}
		};
		classLink = klass -> {
			for (JVMTIEnv env : environmentList) {
				ClassLink cfl = env.getClassLink();
				if (cfl != null) {
					cfl.invoke(klass);
				}
			}
		};
		methodEnter = ctx -> {
			for (JVMTIEnv env : environmentList) {
				MethodEnter me = env.getMethodEnter();
				if (me != null) {
					me.invoke(ctx);
				}
			}
		};
		methodExit = ctx -> {
			for (JVMTIEnv env : environmentList) {
				MethodExit mx = env.getMethodExit();
				if (mx != null) {
					mx.invoke(ctx);
				}
			}
		};
	}

	@Override
	public ClassPrepare getClassPrepare() {
		return classPrepare;
	}

	@Override
	public ClassLink getClassLink() {
		return classLink;
	}

	@Override
	public MethodEnter getMethodEnter() {
		return methodEnter;
	}

	@Override
	public MethodExit getMethodExit() {
		return methodExit;
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
		private ClassPrepare classPrepare;
		private ClassLink classLink;
		private MethodEnter methodEnter;
		private MethodExit methodExit;

		JVMTIEnvImpl(VirtualMachine vm, List<JVMTIEnv> environmentList) {
			this.vm = vm;
			this.environmentList = environmentList;
		}

		@Override
		public VirtualMachine vm() {
			return vm;
		}

		@Override
		public void setClassPrepare(ClassPrepare cfp) {
			classPrepare = cfp;
		}

		@Override
		public void setClassLink(ClassLink cl) {
			classLink = cl;
		}

		@Override
		public void setMethodEnter(MethodEnter methodEnter) {
			this.methodEnter = methodEnter;
		}

		@Override
		public void setMethodExit(MethodExit methodExit) {
			this.methodExit = methodExit;
		}

		@Override
		public ClassPrepare getClassPrepare() {
			return classPrepare;
		}

		@Override
		public ClassLink getClassLink() {
			return classLink;
		}

		@Override
		public MethodEnter getMethodEnter() {
			return methodEnter;
		}

		@Override
		public MethodExit getMethodExit() {
			return methodExit;
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
