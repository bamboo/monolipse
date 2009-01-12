package monolipse.core.runtime;

import java.io.IOException;

public class BooCompilerLauncher extends CompilerLauncher {
	
	public static final String BOO_COMPILER_EXECUTABLE = "lib/boo/booc.exe";
	
	private final BooErrorParser _errorParser = new BooErrorParser();
	
	protected BooCompilerLauncher() throws IOException {
		super(BOO_COMPILER_EXECUTABLE);
		add("-utf8");
	}

	protected CompilerError parseCompilerError(String line) {
		return _errorParser.parse(line);
	}
}
