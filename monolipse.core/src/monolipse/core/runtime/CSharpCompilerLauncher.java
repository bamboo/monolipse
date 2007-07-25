package monolipse.core.runtime;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CSharpCompilerLauncher extends CompilerLauncher {
	
	public static final String CSC_EXECUTABLE = "lib/mono/2.0/gmcs.exe";
	
	Pattern LINE_ERROR_PATTERN = Pattern
		.compile("(.+)\\((\\d+),\\d+\\):\\s(error|warning) (\\w+\\d+):\\s(.+)");

	protected CSharpCompilerLauncher() throws IOException {
		super(CSC_EXECUTABLE);
	}

	protected CompilerError parseCompilerError(String line) {		
		if (line.startsWith("error")) {
			CompilerError error = new CompilerError();
			error.code = line.substring(6, 12);
			error.message = line;
			return error;
		} else {
			Matcher matcher = LINE_ERROR_PATTERN.matcher(line);
			if (matcher.matches()) {
				CompilerError error = new CompilerError();
				error.path = matcher.group(1);
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
