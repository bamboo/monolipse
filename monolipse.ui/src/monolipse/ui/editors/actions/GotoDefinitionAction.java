package monolipse.ui.editors.actions;

import monolipse.core.compiler.CompilerServices;
import monolipse.core.compiler.Element;
import monolipse.ui.BooUI;
import monolipse.ui.editors.BooEditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPage;

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
	}

	private CompilerServices compilerServices() throws CoreException {
		return CompilerServices.getInstance();
	}
}
