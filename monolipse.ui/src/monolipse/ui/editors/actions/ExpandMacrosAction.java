package monolipse.ui.editors.actions;

import monolipse.core.compiler.CompilerServices;

import monolipse.ui.editors.BooEditor;

import org.eclipse.core.runtime.CoreException;

public class ExpandMacrosAction extends AbstractCodeExpansionAction {

	public static final String ID = "monolipse.ui.editors.actions.ExpandMacrosAction";

	public ExpandMacrosAction(BooEditor editor) {
		setText("Expand Macros and Attributes");
		setDescription("Expands the macros and attributes in the current code in a new editor");
		setActionDefinitionId(ID);
		setId(ID);
		_editor = editor;
	}

	@Override
	protected String expand() throws CoreException {
		return compilerServices().expandMacros(getEditorContents(), sourceLanguage());
	}

	private CompilerServices compilerServices() throws CoreException {
		return CompilerServices.getInstance();
	}
}
