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
		super.setOutput(output);
	}

	@Override
	protected CompilerError parseCompilerError(String line) {
		return _errorParser.parse(line);
	}

	public void addClasspaths(String[] projectClasspaths) {
		for(String classpath: projectClasspaths) {
			addClasspath(classpath);
		}
	}

	public void addClasspath(String classpath) {
		BooCore.logInfo("-cp:" + classpath);
		add("-cp:" + classpath);
	}
}
