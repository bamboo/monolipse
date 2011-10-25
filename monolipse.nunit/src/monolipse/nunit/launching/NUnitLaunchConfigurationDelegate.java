package monolipse.nunit.launching;

import java.io.IOException;
import java.util.*;

import monolipse.core.IAssemblySource;
import monolipse.core.launching.BooLauncher;
import monolipse.nunit.NUnitPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;

public class NUnitLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		NUnitPlugin.logInfo("NUnitLaunchConfigurationDelegate.launch");
		
		final IAssemblySource source = BooLauncher.getConfiguredAssemblySource(configuration);
		if (null == source)
			return;
		
		@SuppressWarnings("unchecked")
		final List<String> testNames = configuration.getAttribute(NUnitLaunchConfigurationConstants.ATTR_TEST_NAMES, Collections.emptyList());
		try {
			new TestRunner(source, testNames).run();
		} catch (IOException e) {
			NUnitPlugin.logException(e);
		}
	}
}
