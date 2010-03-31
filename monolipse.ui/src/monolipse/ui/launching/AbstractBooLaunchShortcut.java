package monolipse.ui.launching;

import monolipse.core.*;
import monolipse.core.foundation.*;

import org.eclipse.core.resources.*;
import org.eclipse.debug.ui.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;

public abstract class AbstractBooLaunchShortcut implements ILaunchShortcut  {

	public void launch(ISelection selection, String mode) {
		if (!(selection instanceof IStructuredSelection)) return;
		
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (ss.isEmpty()) return;
		
		IFile file = Adapters.getAdapter(ss.getFirstElement(), IFile.class);
		if (null != file)
			launch(file, mode);
		
		IAssemblySource source = Adapters.getAdapter(ss.getFirstElement(), IAssemblySource.class);
		if (null != source)
			launch(source, mode);
		
	}
	
	public void launch(IEditorPart editor, String mode) {
		IFileEditorInput editorInput = (IFileEditorInput) editor
				.getEditorInput();

		launch(editorInput.getFile(), mode);
	}

	
	protected abstract void launch(IFile file, String mode);

	/**
	 * Can be overridden in subclasses to handle assembly source launching.
	 */
	protected void launch(IAssemblySource source, String mode) {
	}


}
