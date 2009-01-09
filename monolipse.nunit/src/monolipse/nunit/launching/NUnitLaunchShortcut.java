package monolipse.nunit.launching;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.nunit.NUnitPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;


public class NUnitLaunchShortcut implements ILaunchShortcut {

	public void launch(ISelection selection, String mode) {
		// TODO Auto-generated method stub

	}

	public void launch(IEditorPart editor, String mode) {
		IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
		
		try {
			run(editorInput.getFile(), mode);
		} catch (CoreException e) {
			NUnitPlugin.logException(e);
		}
	}

	private void run(IFile file, String mode) throws CoreException {
		final IAssemblySource source = BooCore.assemblySourceContaining(file);
		
		NUnitLauncher.launch(source, mode);
	}

}
