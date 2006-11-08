package monolipse.core.launching.internal;

import java.io.IOException;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.IMonoLauncher;
import monolipse.core.IMonoRuntime;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.core.launching.BooLauncher;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;


public class BooApplicationLaunchConfigurationDelegate extends AbstractBooLaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		logInfo("BooApplicationLaunchConfigurationDelegate.launch");
		
		IAssemblySource source = BooLauncher.getConfiguredAssemblySource(configuration);
		try {
			launch.addProcess(DebugPlugin.newProcess(launch,
					launchApp(source.getOutputFile()), configuration.getName()));
		} catch (IOException e) {
			BooCore.logException(e);
			WorkspaceUtilities.throwCoreException(e);
		}
	}

	private Process launchApp(IFile file) throws IOException {
		IMonoRuntime runtime = BooCore.getRuntime();		
		IMonoLauncher launcher = runtime.createLauncher(WorkspaceUtilities.getLocation(file));
		launcher.setWorkingDir(file.getParent().getLocation().toFile());
		return launcher.launch();
	}
}
