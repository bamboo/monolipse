package monolipse.core.launching.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;

import monolipse.core.BooCore;

public abstract class AbstractBooLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate2 {

	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {

		logInfo("getLaunch");
		Launch launch = new Launch(configuration, mode, null);
		return launch;
	}

	public boolean buildForLaunch(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		logInfo("buildForLaunch");
		return false;
	}

	public boolean preLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		logInfo("preLaunchCheck");
		return true;
	}
	
	public boolean finalLaunchCheck(ILaunchConfiguration configuration,
			String mode, IProgressMonitor monitor) throws CoreException {
		logInfo("finalLaunchCheck");
		return true;
	}

	protected void logInfo(String message) {
		BooCore.logInfo(message);
	}
}
