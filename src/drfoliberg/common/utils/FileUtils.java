package drfoliberg.common.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class FileUtils extends org.apache.commons.io.FileUtils {

	/**
	 * Sets permissions to file or directory given. Fails silently and returns false if an exception occurred at some
	 * point.
	 * 
	 * @param f
	 *            The file or directory
	 * @param perms
	 *            The set of POSIX permissions
	 * @param recursive
	 *            Change permissions recursively for directory
	 * @return True if all files/directory were set to permissions
	 */
	public static boolean givePerms(File f, Set<PosixFilePermission> perms, boolean recursive) {
		boolean success = givePerm(f, perms);
		if (f.isDirectory() && recursive) {
			File[] files = f.listFiles();
			for (File file : files) {
				if (!givePerms(file, perms, recursive)) {
					success = false;
				}
			}
		}
		return success;
	}

	/**
	 * Sets permissions to file or directory given. Fails silently and returns false if an exception occurred at some
	 * point.
	 * 
	 * @param f
	 *            The file or directory
	 * @param perms
	 *            The POSIX permissions as string
	 * @param recursive
	 *            Change permissions recursively for directory
	 * @return True if all files/directory were set to permissions
	 */
	public static boolean givePerms(File f, String perms, boolean recursive) {
		return givePerms(f, PosixFilePermissions.fromString(perms), recursive);
	}

	/**
	 * Sets specified permissions to a single file or directory.
	 * 
	 * @param f
	 *            The file/directory to apply permissions on.
	 * @param perms
	 *            The POSIX permissions as string
	 * @return True if file permissions were applied
	 */
	private static boolean givePerm(File f, Set<PosixFilePermission> perms) {
		try {
			Path p = Paths.get(f.toURI());
			Files.setPosixFilePermissions(p, perms);
		} catch (IOException e) {
			System.err.printf("Could not set permissions '%s' to %s\n", perms, f.toString());
			return false;
		}
		return true;
	}

	/**
	 * Sets permissions rwxrwxrwx to file or directory given. Fails silently and returns false if an exception occurred
	 * at some point.
	 * 
	 * @param f
	 *            The file or directory
	 * @param recursive
	 *            Change permissions recursively for directory
	 * @return True if all files/directory were set to permissions
	 */
	public static boolean givePerms(File f, boolean recursive) {
		return givePerms(f, "rwxrwxrwx", recursive);
	}

}
