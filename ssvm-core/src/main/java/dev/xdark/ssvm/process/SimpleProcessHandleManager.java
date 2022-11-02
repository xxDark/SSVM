package dev.xdark.ssvm.process;

public class SimpleProcessHandleManager implements ProcessHandleManager {
    @Override
    public long createProcessHandle(String cmdLine, String[] env, String dir, long[] fds, boolean errorRedirect) {
        return 0;
    }

    @Override
    public long getExitCode(long handle) {
        return 0;
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
