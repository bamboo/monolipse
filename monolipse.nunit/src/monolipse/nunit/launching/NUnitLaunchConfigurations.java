package monolipse.nunit.launching;

import monolipse.core.IAssemblySource;
import monolipse.core.launching.BooLauncher;
import monolipse.nunit.INUnitLaunchConfigurationTypes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;

public class NUnitLaunchConfigurations {

	public static ILaunchConfiguration forAssemblySource(final IAssemblySource source) throws CoreException {
		final ILaunchConfiguration configuration = existingLaunchConfigurationFor(source);
		if (null != configuration)
			return configuration;
		return newLaunchConfigurationFor(source);
	}

	private static ILaunchConfiguration existingLaunchConfigurationFor(
			final IAssemblySource source) throws CoreException {
		return BooLauncher.findAssemblySourceLaunchConfiguration(source, getNUnitConfigurationType());
	}

	private static ILaunchConfiguration newLaunchConfigurationFor(
			final IAssemblySource source) throws CoreException {
		return BooLauncher.createAssemblySourceLaunchConfiguration(source, getNUnitConfigurationType());
	}

	private static ILaunchConfigurationType getNUnitConfigurationType() {
		return BooLauncher.getLaunchConfigurationType(INUnitLaunchConfigurationTypes.ID_NUNIT);
	}

}
