package monolipse.nunit.launching;

import java.io.IOException;

import monolipse.core.BooCore;
import monolipse.core.IMonoLauncher;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.launching.BooLauncher;
import monolipse.nunit.NUnitPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;


public class TestRunnerLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		try {
			IMonoLauncher launcher = BooCore.getRuntime().createLauncher(getTestRunnerLocation());
			launcher.add(BooLauncher.getProcessMessengerPort(configuration));
			launch.addProcess(DebugPlugin.newProcess(launch, launcher.launch(), configuration.getName()));
		} catch (IOException e) {
			NUnitPlugin.logException(e);
		}	
	}
	
	private String getTestRunnerLocation() throws IOException {
		return WorkspaceUtilities.getResourceLocalPath(NUnitPlugin.getDefault().getBundle(), "bin/monolipse.nunit.server.exe");
	}

}