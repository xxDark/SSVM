package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@UtilityClass
public class IOHacks {

    /**
     * Returns either the file descriptor of a {@link FileDescriptor} object or a handle depending on the platform.
     * @param fd the file descriptor
     * @return the file descriptor or handle
     */
    public long getHandleOrFd(FileDescriptor fd) {
        try {
            return fdHandle == null ? fdField.getInt(fd) : fdHandle.getLong(fd);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to get handle from FileDescriptor", e);
        }
    }

    private final Field fdField;
    private final Field fdHandle;

    static {
        Field fd = null;
        Field handle = null;
        try {
            fd = FileDescriptor.class.getDeclaredField("fd");
            fd.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // ignore
        }
        try {
            handle = FileDescriptor.class.getDeclaredField("handle");
            handle.setAccessible(true);
        } catch (NoSuchFieldException e) {
            // ignore
        }
        fdField = fd;
        fdHandle = handle;
    }

}
