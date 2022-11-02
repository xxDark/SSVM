package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;
import sun.misc.JavaIOFileDescriptorAccess;
import sun.misc.SharedSecrets;

import java.io.FileDescriptor;

@UtilityClass
public class IOHacks {

    private final JavaIOFileDescriptorAccess fdAccess = SharedSecrets.getJavaIOFileDescriptorAccess();

    public long getHandleOrFd(FileDescriptor fd) {
        try {
            return fdAccess.getHandle(fd);
        } catch (UnsupportedOperationException e) {
            return fdAccess.get(fd);
        }
    }

}
