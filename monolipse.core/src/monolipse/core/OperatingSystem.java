package monolipse.core;

public class OperatingSystem {

	public static boolean isMacOSX() {
		return "Mac OS X".equalsIgnoreCase(osName());
	}
	
	public static boolean isWindows() {
		return osName().startsWith("Windows");
	}

	private static String osName() {
		return System.getProperty("os.name");
	}

}
