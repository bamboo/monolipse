package monolipse.core;

import monolipse.core.runtime.DotNetRuntimeImpl;
import monolipse.core.runtime.MonoRuntimeImpl;

public class MonoRuntimeFactory {
	
	static IMonoRuntime createDefaultRuntime() {
		if (OperatingSystem.isWindows() && !isMonoHomeSet())
			return newDotnetRuntime();
		return newMonoRuntime(getMonoHome());
	}

	private static boolean isMonoHomeSet() {
		return null != System.getProperty(MONO_HOME, null);
	}

	private static String getMonoHome() {
		return System.getProperty(MONO_HOME, defaultMonoPathForCurrentPlatform());
	}

	private static String defaultMonoPathForCurrentPlatform() {
		if (OperatingSystem.isMacOSX())
			return "/Library/Frameworks/Mono.framework/Home";
		return "/usr/local";
	}

	private static final String MONO_HOME = "MONO_HOME";

	private static IMonoRuntime newMonoRuntime(String location) {
		return new MonoRuntimeImpl(location);
	}

	private static IMonoRuntime newDotnetRuntime() {
		return new DotNetRuntimeImpl();
	}
}
