package monolipse.ui.launching;

import monolipse.core.*;
import monolipse.core.launching.*;
import monolipse.ui.BooUI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;

public class BooApplicationLaunchShortcut extends AbstractBooLaunchShortcut {

	protected void launch(IFile file, String mode) {
		IAssemblySource source = BooCore.assemblySourceContaining(file);
		if (null == source) return;
		
		try {
			ILaunchConfiguration configuration = source.getLanguage().equals(IAssemblySourceLanguage.BOOJAY)
				? BoojayLauncher.launchConfigurationFor(file)
				: BooLauncher.getAppLaunchConfiguration(source);
			DebugUITools.launch(configuration, mode);
		} catch (CoreException e) {
			BooUI.logException(e);
		}

	}
}
