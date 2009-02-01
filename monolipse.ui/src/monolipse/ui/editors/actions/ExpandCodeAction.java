package monolipse.ui.editors.actions;

import monolipse.core.compiler.CompilerServices;
import monolipse.ui.editors.BooEditor;

import org.eclipse.core.runtime.CoreException;

public class ExpandCodeAction extends AbstractCodeExpansionAction {

	public static final String ID = "monolipse.ui.editors.actions.ExpandCodeAction";

	public ExpandCodeAction(BooEditor editor) {
		setText("Expand All");
		setDescription("Completely expands the current code in a new editor");
		setActionDefinitionId(ID);
		setId(ID);
		_editor = editor;
	}

	@Override
	protected String expand() throws CoreException {
		return compilerServices().expand(getEditorContents(), sourceLanguage());
	}

	private CompilerServices compilerServices() throws CoreException {
		return CompilerServices.getInstance();
	}
}
