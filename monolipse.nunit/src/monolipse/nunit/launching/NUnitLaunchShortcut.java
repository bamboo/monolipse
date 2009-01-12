package monolipse.nunit.launching;

import monolipse.core.*;
import monolipse.nunit.NUnitPlugin;
import monolipse.ui.launching.AbstractBooLaunchShortcut;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;


public class NUnitLaunchShortcut extends AbstractBooLaunchShortcut {

	@Override
	protected void launch(IFile file, String mode) {
		try {
			final IAssemblySource source = BooCore.assemblySourceContaining(file);
			NUnitLauncher.launch(source, mode);
		} catch (CoreException e) {
			NUnitPlugin.logException(e);
		}
	}

}
