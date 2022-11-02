package dev.xdark.ssvm.process;

public interface ProcessHandleManager {

    int STILL_ACTIVE = 259;

    long createProcessHandle(String cmdLine, String[] env, String dir, long[] fds, boolean errorRedirect);

    long getExitCode(long handle);

    /**
     * Wait for process to exit, can be interrupted via a thread interrupt.
     * @param handle process handle
     * @param timeout timeout in milliseconds or 0 for infinite
     */
    void waitForProcess(long handle, long timeout);

    void terminateProcess(long handle);

    boolean processAlive(long handle);

    void closeProcessHandle(long handle);

}
