package monolipse.ui.launching;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

public abstract class AbstractBooLaunchShortcut implements ILaunchShortcut  {

	public void launch(ISelection selection, String mode) {
		if (!(selection instanceof IStructuredSelection)) return;
		
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (ss.isEmpty()) return;
		
		IFile file = (IFile) Platform.getAdapterManager().getAdapter(ss.getFirstElement(), IFile.class);
		if (null == file) return;
		
		launch(file, mode);
		
	}
	
	protected abstract void launch(IFile file, String mode);

}
