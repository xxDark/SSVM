package dev.xdark.ssvm.process;

import dev.xdark.ssvm.VirtualMachine;
import dev.xdark.ssvm.filesystem.FileManager;
import dev.xdark.ssvm.io.Handle;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Host implementation for {@link ProcessManager}.
 * @author Justus Garbe
 */
public class HostProcessManager implements ProcessManager {

    private final Map<Handle, Process> processes = new HashMap<>();
    private Constructor<?> createProcess;
    private boolean isUnixProcessImpl;
    private final VirtualMachine vm;

    public HostProcessManager(VirtualMachine vm) {
        this.vm = vm;
        findCreateMethod();
    }

    @Override
    public synchronized long createProcessHandle(String cmdLine, String[] env, String dir, long[] fds, boolean errorRedirect) {
        // prepare real fds
        FileManager fdm = vm.getFileManager();
        long[] realFds = new long[fds.length];
        for (int i = 0; i < fds.length; i++) {
            if(fds[i] == -1) {
                realFds[i] = -1; // pipe
            } else {
                realFds[i] = fdm.getRealHandle(fds[i]);
            }
        }
        Process process = callHostCreate(cmdLine, env, dir, realFds, errorRedirect);
        Handle handle = Handle.threadLocal(newHandle(-1));
        processes.put(handle, process);
        return handle.get();
    }

    @Override
    public synchronized long getExitCode(long handle) {
        Process process = processes.get(Handle.threadLocal(handle));
        if(process != null) {
            return process.exitValue();
        }
        return -1;
    }

    @Override
    public void waitForProcess(long handle, long timeout) {
        try {
            Process process = processes.get(Handle.threadLocal(handle));
            if(process == null) return;
            if(timeout == 0) {
                process.waitFor();
            } else {
                process.waitFor(timeout, java.util.concurrent.TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("Interrupted while waiting for process", e);
        }
    }

    @Override
    public synchronized void terminateProcess(long handle) {
        Process process = processes.get(Handle.threadLocal(handle));
        if(process != null) {
            process.destroy();
        }
    }

    @Override
    public synchronized boolean processAlive(long handle) {
        Process process = processes.get(Handle.threadLocal(handle));
        if(process != null) {
            return process.isAlive();
        }
        return false;
    }

    @Override
    public synchronized void closeProcessHandle(long handle) {
        processes.remove(Handle.threadLocal(handle));
    }

    private void findCreateMethod() {
        try {
            Class<?> genericClass = Class.forName("java.lang.ProcessImpl");
            try {
                genericClass = Class.forName("java.lang.ProcessImpl");
                createProcess = genericClass.getDeclaredConstructor(String[].class, String.class, String.class, long[].class, boolean.class);
                return;
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                // try next
            }
            isUnixProcessImpl = true;
            Class<byte[]> bk = byte[].class;
            try {
                createProcess = genericClass.getDeclaredConstructor(
                    bk, // cmd
                    bk, int.class, // argv
                    bk, int.class, // envp
                    bk, // dir
                    int[].class, // fds
                    boolean.class); // redirectErrorStream
            } catch (NoSuchMethodException e1) {
                // not in generic class, try in unix class
                try {
                    Class<?> unixClass = Class.forName("java.lang.UNIXProcess");
                    createProcess = unixClass.getDeclaredConstructor(
                        bk, // cmd
                        bk, int.class, // argv
                        bk, int.class, // envp
                        bk, // dir
                        int[].class, // fds
                        boolean.class); // redirectErrorStream
                } catch (ClassNotFoundException | NoSuchMethodException e2) {
                    throw new UnsupportedOperationException("Unable to find ProcessImpl mirror class for jvm runner");
                }
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to find ProcessImpl mirror class for jvm runner", e);
        }
        createProcess.setAccessible(true);
    }

    private Process callHostCreate(String cmdLine, String[] env, String dir, long[] fds, boolean errorRedirect) {
        StringTokenizer st = new StringTokenizer(cmdLine);
        String[] cmd = new String[st.countTokens()];
        for(int i = 0; i < cmd.length; i++) {
            cmd[i] = st.nextToken();
        }
        if(isUnixProcessImpl) {
            // argv is args seperated by \0
            byte[] argv = new byte[cmdLine.length() - cmd[0].length() + (cmd.length - 1)];
            int argvOffset = 0;
            for(int i = 1; i < cmd.length; i++) {
                for(int j = 0; j < cmd[i].length(); j++) {
                    argv[argvOffset++] = (byte) cmd[i].charAt(j);
                }
                argv[argvOffset++] = 0;
            }
            byte[] envp = null;
            if(env != null) {
                // envp is env seperated by \0 and terminated by \0\0
                int length = 0;
                for (String s : env) {
                    length += s.length() + 1;
                }
                envp = new byte[length + env.length + 1];
                int envpOffset = 0;
                for (String s : env) {
                    for (int j = 0; j < s.length(); j++) {
                        envp[envpOffset++] = (byte) s.charAt(j);
                    }
                    envp[envpOffset++] = 0;
                }
                envp[envpOffset] = 0;
            }
            // dir is dir
            // fds needs to be int[]
            int[] intFds = new int[fds.length];
            for(int i = 0; i < fds.length; i++) {
                intFds[i] = (int) fds[i];
            }
            try {
                return (Process) createProcess.newInstance(
                    cmd[0].getBytes(), // cmd
                    argv, cmd.length - 1, // argv
                    envp, envp == null ? 0 : env.length, // envp
                    dir == null ? null : dir.getBytes(), // dir
                    intFds, // fds
                    errorRedirect); // redirectErrorStream
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e.getCause().getMessage());
            }
        } else {
            try {
                return (Process) createProcess.newInstance(
                    cmd, // cmd
                    env, // env
                    dir, // dir
                    fds, // fds
                    errorRedirect); // redirectErrorStream
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private int newHandle(int mask) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Map<Handle, Process> processes = this.processes;
        Handle handle = Handle.threadLocal();
        int raw;
        do {
            raw = random.nextInt() & mask;
            handle.set(raw);
        } while (raw == 0L || processes.containsKey(handle));
        return raw;
    }
}
