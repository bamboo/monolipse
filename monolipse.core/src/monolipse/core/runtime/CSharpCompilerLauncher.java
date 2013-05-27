package monolipse.core.runtime;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monolipse.core.AssemblySourceLanguage;
import monolipse.core.BooCore;
import monolipse.core.IMonoLauncher;


public class CSharpCompilerLauncher extends CompilerLauncher {
	
	private static final String MCS_EXECUTABLE = "lib/mono/1.0/mcs.exe";

	private static final String GMCS_EXECUTABLE = "lib/mono/2.0/gmcs.exe";
	
	Pattern LINE_ERROR_PATTERN = Pattern
		.compile("(.+)\\((\\d+),\\d+\\):\\s(error|warning) (\\w+\\d+):\\s(.+)");

	CSharpCompilerLauncher(AssemblySourceLanguage language) throws IOException {
		super(compilerLauncherFor(language));
		if (isDotnet()) add("/utf8output");
	}

	private static IMonoLauncher compilerLauncherFor(AssemblySourceLanguage language) throws IOException {
		if (isDotnet())
			return BooCore.createLauncherWithRuntimeLocation("csc.exe");
		return BooCore.createLauncherWithRuntimeLocation(monoExecutableFor(language));
	}

	private static boolean isDotnet() {
		return BooCore.getRuntime().isDotnet();
	}

	private static String monoExecutableFor(AssemblySourceLanguage language) {
		return language.equals(AssemblySourceLanguage.CSHARP_1_1)
			? MCS_EXECUTABLE
			: GMCS_EXECUTABLE;
	}

	protected CompilerError parseCompilerError(String line) {		
		if (line.startsWith("error")) {
			final CompilerError error = new CompilerError();
			error.code = line.substring(6, 12);
			error.message = line;
			return error;
		} else {
			Matcher matcher = LINE_ERROR_PATTERN.matcher(line);
			if (matcher.matches()) {
				final CompilerError error = new CompilerError();
				error.setPath(matcher.group(1));
				error.line = Integer.parseInt(matcher.group(2));
				error.code = matcher.group(4);
				error.message = matcher.group(5);
				error.severity = matcher.group(3).equals("error") ? CompilerError.ERROR : CompilerError.WARNING;
				return error;
			}
		}
		return null;
	}

}
