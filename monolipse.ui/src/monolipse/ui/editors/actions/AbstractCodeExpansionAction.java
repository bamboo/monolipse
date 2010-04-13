package monolipse.ui.editors.actions;

import monolipse.core.*;
import monolipse.ui.BooUI;
import monolipse.ui.editors.BooEditor;
import monolipse.ui.editors.input.StringInput;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;

public abstract class AbstractCodeExpansionAction extends CommonBooAction {

	public AbstractCodeExpansionAction() {
		super();
	}

	public AbstractCodeExpansionAction(String text) {
		super(text);
	}

	public AbstractCodeExpansionAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	protected abstract String expand() throws CoreException;

	public AbstractCodeExpansionAction(String text, int style) {
		super(text, style);
	}

	public void run() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null)
			return;
		
		try {				
			String expansion = expand();
			if (null == expansion)
				return;
			
			openBooEditorForString(activePage, expansion);
			
		} catch (CoreException e) {
			BooUI.logException(e);
		}
	}

	private void openBooEditorForString(IWorkbenchPage activePage,
			String booCode) throws PartInitException {
		IStorageEditorInput input = new StringInput(booCode);
		activePage.openEditor(input, BooEditor.ID_EDITOR);
	}

	protected AssemblySourceLanguage sourceLanguage() {
		final IFile file = (IFile) editorInput().getAdapter(IFile.class);
		if (null != file) {
			final IAssemblySource source = BooCore.assemblySourceContaining(file);
			if (null != source) {
				return source.getLanguage();
			}
		}
		return AssemblySourceLanguage.BOO;
	}

}