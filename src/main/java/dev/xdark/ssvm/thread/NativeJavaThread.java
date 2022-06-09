package dev.xdark.ssvm.thread;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.execution.VMException;
import dev.xdark.ssvm.util.VMHelper;
import dev.xdark.ssvm.value.InstanceValue;
import dev.xdark.ssvm.value.ObjectValue;
import dev.xdark.ssvm.value.Value;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Java thread wrapper around VM thread.
 *
 * @author xDark
 */
public class NativeJavaThread extends Thread {

	private final InstanceValue oop;
	private final VMThread vmThread;

	/**
	 * @param oop      Thread oop.
	 * @param vmThread VM thread.
	 */
	public NativeJavaThread(InstanceValue oop, VMThread vmThread) {
		this.oop = oop;
		this.vmThread = vmThread;
	}

	@Override
	public void run() {
		InstanceValue oop = this.oop;
		VirtualMachine vm = oop.getJavaClass().getVM();
		VMHelper helper = vm.getHelper();
		oop.setInt("threadStatus", ThreadState.JVMTI_THREAD_STATE_ALIVE | ThreadState.JVMTI_THREAD_STATE_RUNNABLE);
		try {
			helper.invokeVirtual("run", "()V", new Value[0], new Value[]{oop});
		} catch (VMException ex) {
			ObjectValue uncaughtExceptionHandler = oop.getValue("uncaughtExceptionHandler", "Ljava/lang/Thread$UncaughtExceptionHandler;");
			if (uncaughtExceptionHandler.isNull()) {
				uncaughtExceptionHandler = (ObjectValue) vm.getSymbols().java_lang_Thread().getStaticValue("defaultUncaughtExceptionHandler", "Ljava/lang/Thread$UncaughtExceptionHandler;");
			}
			if (!uncaughtExceptionHandler.isNull()) {
				try {
					helper.invokeVirtual("uncaughtException", "(Ljava/lang/Thread;Ljava/lang/Throwable;)V", new Value[0], new Value[]{
						oop,
						ex.getOop()
					});
				} catch (VMException uex) {
					OutputStream os = vm.getFileDescriptorManager().getStreamOut(2);
					if (os != null) {
						String msg = "Exception: " + uex.getOop().getJavaClass().getName() + " thrown from the UncaughtExceptionHandler in thread \"" + helper.readUtf8(oop.getValue("name", "Ljava/lang/String;")) + '\"';
						if (os instanceof PrintStream) {
							((PrintStream) os).println(msg);
						} else {
							try {
								os.write(msg.getBytes(StandardCharsets.UTF_8));
								os.write((byte) '\n');
								os.flush();
							} catch (IOException ignored) {
							}
						}
					}
				}
			}
		} finally {
			oop.setInt("threadStatus", ThreadState.JVMTI_THREAD_STATE_TERMINATED);
			oop.monitorEnter();
			try {
				oop.monitorNotifyAll();
			} finally {
				oop.monitorExit();
			}
		}
	}

	/**
	 * @return VM thread.
	 */
	public VMThread getVmThread() {
		return vmThread;
	}
}
