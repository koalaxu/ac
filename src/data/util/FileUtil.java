package ac.data.util;

import java.net.URL;

import ac.GameConsole;


public class FileUtil {
	public static String GeFilePath(String filename) {
		URL res = GameConsole.class.getResource(filename);
		if (res == null) {
			System.err.println("Can not find file: " + filename);
			System.exit(0);
		}
		return res.getPath();
	}
}
