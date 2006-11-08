package monolipse.ui.actions;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.*;

public class FolderAction extends Action {
	private IWorkbenchPartSite _site;

	protected FolderAction(IViewPart view, String text, ImageDescriptor image) {
		super(text, image);
		_site = view.getSite();
	}
	
	public void run() {		
		System.out.println(getSelectedFolder().toString());
	}
	
	protected IFolder getSelectedFolder() {
		IStructuredSelection selection = getSelection();
		return selection.size() == 1
			? (IFolder)selection.getFirstElement()
			: null;
	}

	protected IStructuredSelection getSelection() {
		return (IStructuredSelection)_site.getSelectionProvider().getSelection();
	}

}
