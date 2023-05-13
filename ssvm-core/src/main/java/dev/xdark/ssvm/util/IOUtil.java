package dev.xdark.ssvm.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Hacks and utils for io java classes
 * @author Justus Garbe
 */
@UtilityClass
public class IOUtil {

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

    /**
     * @param inputStream Input stream, may be {@code null}.
     * @return Bytes of stream, or {@code null} if the stream was {@code null}
     * @throws IOException When the stream cannot be read from.
     */
    public static byte[] readAll(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;
        int bufferSize = 2048;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[bufferSize];
            int bytesRead;
            int readCount = 0;
            while ((bytesRead = inputStream.read(data, 0, bufferSize)) != -1) {
                outputStream.write(data, 0, bytesRead);
                readCount++;
            }
            outputStream.flush();
            if (readCount == 1) {
                return data;
            }
            return outputStream.toByteArray();
        } finally {
            inputStream.close();
        }
    }
}
