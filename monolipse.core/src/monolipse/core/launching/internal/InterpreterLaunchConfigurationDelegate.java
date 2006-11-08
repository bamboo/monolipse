package monolipse.core.launching.internal;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

import monolipse.core.BooCore;
import monolipse.core.IMonoLauncher;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.launching.BooLauncher;

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
		return WorkspaceUtilities.getResourceLocalPath(BooCore.getDefault().getBundle(), path);
	}

}
