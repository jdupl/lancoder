package drfoliberg.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;

public class FileUtils {

	public static void givePerms(File f) {
		String permissions = "rw-rw-rw-";
		try {
			Path p = Paths.get(f.toURI());
			Files.setPosixFilePermissions(p, PosixFilePermissions.fromString(permissions));
		} catch (IOException e) {
			System.err.printf("Could not set permissions '%s' to %s\n", permissions, f.toString());
		}
	}

}
