package monolipse.core;

import monolipse.core.internal.IO;
import monolipse.core.runtime.DotNetRuntimeImpl;
import monolipse.core.runtime.MonoRuntimeImpl;

public class MonoRuntimeFactory {
	
	static IMonoRuntime createDefaultRuntime() {
		if (OperatingSystem.isWindows() && !isMonoHomeSet())
			return newDotnetRuntime();
		return newMonoRuntime(getMonoHome());
	}

	private static boolean isMonoHomeSet() {
		return System.getProperty(MONO_HOME, null) != null;
	}

	private static String getMonoHome() {
		return System.getProperty(MONO_HOME, defaultMonoPathForCurrentPlatform());
	}

	private static String defaultMonoPathForCurrentPlatform() {
		if (OperatingSystem.isMacOSX())
			return "/Library/Frameworks/Mono.framework/Home";
		if (IO.existsFile("/usr/local/bin/mono"))
			return "/usr/local";
		return "/usr";
	}

	private static final String MONO_HOME = "MONO_HOME";
	
	private static IMonoRuntime newMonoRuntime(String location) {
		return new MonoRuntimeImpl(location);
	}

	private static IMonoRuntime newDotnetRuntime() {
		return new DotNetRuntimeImpl(getDotNetHome());
	}

	private static String getDotNetHome() {
		String[] candidates = {
				System.getProperty("DOTNET_HOME", null),
				"c:/Windows/Microsoft.NET/Framework/v4.0.30319",
				"c:/Windows/Microsoft.NET/Framework/v3.5",
		};
		for (String candidate : candidates) {
			if (candidate == null) continue;
			if (IO.existsFile(candidate)) {
				BooCore.logInfo(".NET runtime found at '{0}'.", candidate);
				return candidate;
			}
			BooCore.logInfo(".NET runtime '{0}' not found, trying next...", candidate);
		}
		throw new IllegalStateException("DOTNET_HOME is not set to a valid .NET runtime and no runtime could be found!");
	}
}
