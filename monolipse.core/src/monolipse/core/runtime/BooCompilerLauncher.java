package monolipse.core.runtime;

import java.io.IOException;

import monolipse.core.BooCore;

public class BooCompilerLauncher extends CompilerLauncher {
	
	public static final String BOO_COMPILER_EXECUTABLE = "lib/boojay/booc.exe";
	
	private final BooErrorParser _errorParser = new BooErrorParser();
	
	protected BooCompilerLauncher() throws IOException {
		super(BooCore.createLauncherFromBundle(BOO_COMPILER_EXECUTABLE));
		add("-utf8");
	}

	protected CompilerError parseCompilerError(String line) {
		return _errorParser.parse(line);
	}
}
