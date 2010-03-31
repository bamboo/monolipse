package monolipse.nunit.launching;

import monolipse.core.*;
import monolipse.nunit.NUnitPlugin;
import monolipse.ui.launching.AbstractBooLaunchShortcut;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;


public class NUnitLaunchShortcut extends AbstractBooLaunchShortcut {

	@Override
	protected void launch(IFile file, String mode) {
		final IAssemblySource source = BooCore.assemblySourceContaining(file);
		launch(source, mode);
	}
	
	@Override
	protected void launch(IAssemblySource source, String mode) {
		try {
			NUnitLauncher.launch(source, mode);
		} catch (CoreException e) {
			NUnitPlugin.logException(e);
		}
	}

}
