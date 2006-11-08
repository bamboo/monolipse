package monolipse.ui.editors.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import monolipse.core.compiler.CompilerServices;
import monolipse.ui.BooUI;
import monolipse.ui.editors.BooEditor;
import monolipse.ui.editors.input.StringInput;

public class ExpandCodeAction extends Action {

	public static final String ID = "monolipse.ui.editors.actions.ExpandCodeAction";

	private BooEditor _editor;

	public ExpandCodeAction(BooEditor editor) {
		setText("Expand");
		setDescription("Expands the current code and opens a new editor");
		setActionDefinitionId(ID);
		setId(ID);
		_editor = editor;
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
			} catch (PartInitException e) {
				BooUI.logException(e);
			}
		}
	}

	private String expand() {
		CompilerServices services;
		try {
			services = CompilerServices.getInstance();
		} catch (CoreException e) {
			BooUI.logException(e);
			return null;
		}
		return services.expand(getEditorContents());
	}

	private String getEditorContents() {
		return _editor.getDocumentProvider().getDocument(_editor.getEditorInput()).get();
	}
}
