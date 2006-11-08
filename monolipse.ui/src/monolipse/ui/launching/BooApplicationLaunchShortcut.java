package monolipse.ui.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.launching.BooLauncher;
import monolipse.ui.BooUI;

public class BooApplicationLaunchShortcut extends AbstractBooLaunchShortcut {

	public void launch(IEditorPart editor, String mode) {
		IFileEditorInput editorInput = (IFileEditorInput) editor
				.getEditorInput();

		launch(editorInput.getFile(), mode);
	}

	protected void launch(IFile file, String mode) {
		IAssemblySource source = BooCore.getAssemblySourceContainer(file);
		if (null == source) return;
		
		try {
			ILaunchConfiguration configuration = BooLauncher
					.getAppLaunchConfiguration(source);
			DebugUITools.launch(configuration, mode);
		} catch (CoreException e) {
			BooUI.logException(e);
		}

	}
}
