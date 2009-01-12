package monolipse.core.launching.internal;

import java.io.IOException;

import monolipse.core.*;
import monolipse.core.launching.BooLauncher;

import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.*;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;


public class InterpreterLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		try {
			IMonoLauncher launcher = BooCore.getRuntime().createLauncher(getInterpreterLocation());
			launcher.add(BooLauncher.getProcessMessengerPort(configuration));
			launch.addProcess(DebugPlugin.newProcess(launch, launcher.launch(), configuration.getName()));
		} catch (IOException e) {
			BooCore.logException(e);
		}	
	}

	private String getInterpreterLocation() throws IOException {
		// force unpacking
//		getResourceLocalPath("bin/monolipse.core.dll");
		return getResourceLocalPath("bin/monolipse.server.exe");
	}

	private String getResourceLocalPath(String path) throws IOException {
		return BooCore.resolveBundlePath(path);
	}

}
