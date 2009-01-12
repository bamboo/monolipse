package monolipse.core.launching.internal;

import monolipse.core.BooCore;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;


public abstract class AbstractBooLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate2 {

	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode)
			throws CoreException {

		logInfo("getLaunch");
		return new Launch(configuration, mode, null);
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
