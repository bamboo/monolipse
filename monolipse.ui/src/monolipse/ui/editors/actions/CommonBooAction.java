package monolipse.ui.editors.actions;

import monolipse.ui.editors.BooEditor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.*;

public class CommonBooAction extends Action {
	protected BooEditor _editor;
	
	public CommonBooAction() {
		super();
	}
	
	public CommonBooAction(String text) {
		super(text);
	}

	public CommonBooAction(String text, ImageDescriptor image) {
		super(text, image);
	}

	public CommonBooAction(String text, int style) {
		super(text, style);
	}

	protected IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = _editor.getSite().getPage().getWorkbenchWindow();
		return window.getActivePage();
	}

	protected String getEditorContents() {
		return _editor.getDocumentProvider().getDocument(editorInput()).get();
	}

	protected IEditorInput editorInput() {
		return _editor.getEditorInput();
	}
	
	protected Point getSelectedRange() {
		return _editor.getSelectedRange();
	}
	
	protected Point getSelectedPosition() {
		return _editor.getSelectedPosition();	
	}
}
