package dev.xdark.ssvm.dummy;

import dev.xdark.ssvm.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Dummy classes to test various IO operations
 */
public class FileToString {
	public static String readReadmePath() throws IOException {
		return read(Paths.get("README.md"));
	}

	public static String readReadmeFile() throws IOException {
		return read(new File("README.md"));
	}

	public static String readReadmeResource() throws IOException {
		return read(FileToString.class.getResourceAsStream("/README.md"));
	}

	public static String readPath(String path) throws IOException {
		return read(new File(path));
	}

	public static String read(Path file) throws IOException {
		return read(Files.readAllBytes(file));
	}

	public static String read(File file) throws IOException {
		return read(new FileInputStream(file));
	}

	private static String read(InputStream stream) throws IOException {
		return read(IOUtil.readAll(stream));
	}

	private static String read(byte[] bytes) {
		int end = bytes.length;
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				end = i;
				break;
			}
		}
		return new String(bytes, 0, end, StandardCharsets.UTF_8);
	}
}
