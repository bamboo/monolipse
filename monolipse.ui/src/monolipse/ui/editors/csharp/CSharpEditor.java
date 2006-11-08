package monolipse.ui.editors.csharp;

import org.eclipse.ui.editors.text.TextEditor;

public class CSharpEditor extends TextEditor {
	public CSharpEditor() {
		super();
		setSourceViewerConfiguration(new CSharpSourceViewerConfiguration(
				getSharedColors()));
		setDocumentProvider(new CSharpDocumentProvider());
	}
}
