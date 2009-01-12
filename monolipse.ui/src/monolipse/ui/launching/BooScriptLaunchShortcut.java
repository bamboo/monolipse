package monolipse.ui.launching;

import monolipse.core.launching.BooLauncher;
import monolipse.ui.BooUI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;


public class BooScriptLaunchShortcut extends AbstractBooLaunchShortcut {

	protected void launch(IFile file, String mode) {
		BooUI.logInfo("Launching '" + file.getLocation() + "' as a script.");
		
		try {
			ILaunchConfiguration configuration = BooLauncher.getScriptLaunchConfiguration(file);
			DebugUITools.launch(configuration, mode);
		} catch (CoreException e) {
			BooUI.logException(e);
		}
	}
}
