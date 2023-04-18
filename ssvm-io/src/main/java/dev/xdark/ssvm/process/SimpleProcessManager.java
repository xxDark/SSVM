package dev.xdark.ssvm.process;

/**
 * Simple stub version of {@link ProcessManager} that does not support any process management.
 *
 * @author Justus Garbe
 */
public class SimpleProcessManager implements ProcessManager {

	@Override
	public long createProcessHandle(String cmdLine, String[] env, String dir, long[] fds, boolean errorRedirect) {
		return 0L;
	}

	@Override
	public long getExitCode(long handle) {
		return 0L;
	}

	@Override
	public void waitForProcess(long handle, long timeout) {
		// do nothing
	}

	@Override
	public void terminateProcess(long handle) {
		// do nothing
	}

	@Override
	public boolean processAlive(long handle) {
		return false; // always dead
	}

	@Override
	public void closeProcessHandle(long handle) {
		// do nothing
	}
}
