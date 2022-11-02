package dev.xdark.ssvm.process;

import org.jetbrains.annotations.Nullable;

public interface ProcessHandleManager {

    /**
     * Windows still active exit code.
     */
    int STILL_ACTIVE = 259;

    /**
     * Create a process handle
     * @param cmdLine command line
     * @param env environment blocks (KEY=VALUE)
     * @param dir working directory (null for current)
     * @param fds an array containing 3 file descriptors for stdin, stdout and stderr.
     *            if they are -1, then the process will be created with a pipe.
     *            if they are not 0, 1 or 2 then it is a file descriptor (vm mapped)
     * @param errorRedirect redirect error stream to output stream
     * @return process handle
     */
    long createProcessHandle(String cmdLine, @Nullable String[] env, @Nullable String dir, long[] fds, boolean errorRedirect);

    /**
     * Get the exit code of a process
     * @param handle process handle
     * @return exit code or {@link #STILL_ACTIVE} if on Windows and the process is still active,
     *         will throw exception on linux if not done yet
     */
    long getExitCode(long handle);

    /**
     * Wait for process to exit, can be interrupted via a thread interrupt.
     * @param handle process handle
     * @param timeout timeout in milliseconds or 0 for infinite
     */
    void waitForProcess(long handle, long timeout);

    /**
     * Stop a process
     * @param handle process handle
     */
    void terminateProcess(long handle);

    /**
     * Returns if the process is alive
     * @param handle process handle
     * @return true if alive false if not
     */
    boolean processAlive(long handle);

    /**
     * Close a process handle, is called after process terminates
     * @param handle process handle
     */
    void closeProcessHandle(long handle);

}
