package monolipse.core.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import monolipse.core.BooCore;
import monolipse.core.IMonoLauncher;
import monolipse.core.foundation.ArrayUtilities;


final class MonoLauncherImpl implements IMonoLauncher {
	
	private List _cmd = new ArrayList();
	
	private File _workingDir;

	public MonoLauncherImpl(String executablePath) throws IOException {				
		add(executablePath);
	}

	public void add(String arg) {
		_cmd.add(arg);
	}
	
	public void setWorkingDir(File dir) {
		_workingDir = dir;
	}
	
	public Process launch() throws IOException {
		final String[] cmdLine = (String[]) _cmd.toArray(new String[_cmd.size()]);
		String cmdLineText = ArrayUtilities.join(cmdLine);
		BooCore.logInfo(cmdLineText);
		return launch(cmdLine);
	}
	
	private Process launch(final String[] cmdLine) throws IOException {
		return Runtime.getRuntime().exec(cmdLine, null, _workingDir);
	}
}
