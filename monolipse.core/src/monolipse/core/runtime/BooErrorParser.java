package monolipse.core.runtime;

import java.util.regex.*;

public class BooErrorParser {

	private static final Pattern LINE_ERROR_PATTERN = Pattern
		.compile("(.+)\\((\\d+),\\d+\\):\\s(BC\\w\\d+):\\s(.+)");

	private static final Pattern GLOBAL_ERROR_PATTERN = Pattern
		.compile("^BCE.+");

	public CompilerError parse(String line) {
		CompilerError error = null;
		
		Matcher matcher = LINE_ERROR_PATTERN.matcher(line);
		if (matcher.matches()) {
			error = new CompilerError();
			error.path = matcher.group(1);
			error.line = Integer.parseInt(matcher.group(2));
			error.code = matcher.group(3);
			error.severity = error.code.startsWith("BCE") ? CompilerError.ERROR : CompilerError.WARNING;
			error.message = matcher.group(4);
			
//					addMarker(path, message, lineNumber, code.startsWith("BCE")
//							? IMarker.SEVERITY_ERROR
//							: IMarker.SEVERITY_WARNING);
		} else {
			if (GLOBAL_ERROR_PATTERN.matcher(line).matches()) {
				error = new CompilerError();
				error.code = "ERROR";
				error.message = line;
//						addErrorMarker(source, line);
			} else {
				//System.err.println(line);
			}
		}
		return error;
	}


}
