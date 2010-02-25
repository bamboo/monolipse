package monolipse.nunit.launching;

import monolipse.core.IAssemblySource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.*;
import org.eclipse.debug.ui.DebugUITools;

public class NUnitLauncher {

	public static void launch(final IAssemblySource source, String modeRunOrDebug)
			throws CoreException {
		DebugUITools.launch(configurationFor(source), modeRunOrDebug);
	}

	private static ILaunchConfiguration configurationFor(
			final IAssemblySource source) throws CoreException {
		return NUnitLaunchConfigurations.forAssemblySource(source);
	}

	public static void launch(IAssemblySource assemblySource, String modeRunOrDebug, java.util.List<String> testNames)
		throws CoreException {
		
		final ILaunchConfigurationWorkingCopy copy = configurationFor(assemblySource).copy(testNames.get(0) + "...");
		copy.setAttribute(NUnitLaunchConfigurationConstants.ATTR_TEST_NAMES, testNames);
		DebugUITools.launch(copy, modeRunOrDebug);
	}

}
