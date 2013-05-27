package monolipse.core.runtime;

import java.util.regex.*;

public class BooErrorParser {

	private static final Pattern LINE_ERROR_PATTERN = Pattern
		.compile("(.+)\\((\\d+),\\d+\\):\\s(BC\\w\\d+):\\s(.+)");

	private static final Pattern GLOBAL_ERROR_PATTERN = Pattern
		.compile("^BCE.+");

	public CompilerError parse(String line) {
		Matcher matcher = LINE_ERROR_PATTERN.matcher(line);
		if (matcher.matches()) {
			CompilerError error = new CompilerError();
			error.setPath(matcher.group(1));
			error.line = Integer.parseInt(matcher.group(2));
			error.code = matcher.group(3);
			error.severity = error.code.startsWith("BCE") ? CompilerError.ERROR : CompilerError.WARNING;
			error.message = matcher.group(4);
			return error;
		}
		
		if (GLOBAL_ERROR_PATTERN.matcher(line).matches()) {
			CompilerError error = new CompilerError();
			error.code = "ERROR";
			error.message = line;
			return error;			
		}
		return null;
	}
}
