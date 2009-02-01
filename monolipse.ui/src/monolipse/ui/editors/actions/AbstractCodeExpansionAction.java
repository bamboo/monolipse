package monolipse.ui.editors.actions;

import monolipse.ui.BooUI;
import monolipse.ui.editors.BooEditor;
import monolipse.ui.editors.input.StringInput;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.*;

public abstract class AbstractCodeExpansionAction extends Action {

	protected BooEditor _editor;

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
		IWorkbenchWindow window = _editor.getSite().getPage()
				.getWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		if (page != null) {
			try {				
				String expansion = expand();
				if (null == expansion) return;
				IStorageEditorInput input = new StringInput(expansion);
				page.openEditor(input, BooEditor.ID_EDITOR);
			} catch (CoreException e) {
				BooUI.logException(e);
			}
		}
	}

	protected String getEditorContents() {
		return _editor.getDocumentProvider().getDocument(_editor.getEditorInput()).get();
	}

}