package monolipse.core.runtime;

import java.io.IOException;

import org.eclipse.core.resources.IResource;

import monolipse.core.BooCore;

public class BoojayCompilerLauncher extends CompilerLauncher {

	private final BooErrorParser _errorParser = new BooErrorParser();
	
	protected BoojayCompilerLauncher() throws IOException {
		super(BooCore.createLauncherFromBundle("lib/boojay/boojay.exe"));
	}
	
	@Override
	public void setOutputType(String outputType) {
		// not applicable
	}
	
	@Override
	public void setOutput(IResource output) {
		super.setOutput(output.getParent());
	}

	@Override
	protected CompilerError parseCompilerError(String line) {
		return _errorParser.parse(line);
	}

	public void addClasspath(String classpath) {	
	}
}
