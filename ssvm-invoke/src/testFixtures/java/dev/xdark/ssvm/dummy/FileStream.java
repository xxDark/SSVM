package dev.xdark.ssvm.dummy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileStream {
	public static String streamImportantFile() throws IOException {
		FileInputStream fis = new FileInputStream("README.md");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			return sb.toString();
		}
	}
}
