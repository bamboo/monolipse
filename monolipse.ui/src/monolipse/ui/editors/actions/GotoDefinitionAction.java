package monolipse.ui.editors.actions;

import monolipse.core.compiler.CompilerServices;
import monolipse.core.compiler.Element;
import monolipse.ui.BooUI;
import monolipse.ui.editors.BooEditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class GotoDefinitionAction extends CommonBooAction {

	public static final String ID = "monolipse.ui.editors.actions.GotoDefinitionAction";

	public GotoDefinitionAction(BooEditor editor) {
		setText("Go To Definition");
		setDescription("Go To type definition");
		setActionDefinitionId(ID);
		setId(ID);
		_editor = editor;
	}

	public void run() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage == null)
			return;
		
		try {				
			Element element = getElementAt();
			if (null == element)
				return;
			
			openBooEditorForElement(activePage, element);
		} catch (CoreException e) {
			BooUI.logException(e);
		}
	}
	
	private Element getElementAt() throws CoreException {
		Point pos = getSelectedPosition();
		return compilerServices().getElementAt(getEditorContents(), pos.x, pos.y);
	}
	
	private void openBooEditorForElement(IWorkbenchPage activePage, Element element) {
		BooUI.logInfo("Need to open type: " + element);
		if (element.getFile().equals("code")) {
			BooUI.logInfo("Going to");
			gotoPosition(activePage, element.getLine(), element.getColumn());
		}
	}

	private void gotoPosition(IWorkbenchPage activePage, int line, int column) {
		IEditorPart editorPart = activePage.getActiveEditor();
		ITextEditor textEditor = (ITextEditor) editorPart;
		IEditorInput input = editorPart.getEditorInput();
		IDocumentProvider provider = textEditor.getDocumentProvider();

		int offset = 0;
		int length = 0;

		try {
			provider.connect(input);
		} catch (CoreException e) {
			// unable to link
			BooUI.logException(e);
			return;
		}
		IDocument document = provider.getDocument(input);
		try {
			IRegion region= document.getLineInformation(line - 1);
			offset = region.getOffset();
			length = region.getLength();
		} catch (BadLocationException e) {
			// unable to link
			BooUI.logException(e);
		}
		provider.disconnect(input);
		if (offset >= 0 && length >=0) {
			textEditor.selectAndReveal(offset, length);
		}
	}

	private CompilerServices compilerServices() throws CoreException {
		return CompilerServices.getInstance();
	}
}

