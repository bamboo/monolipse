package monolipse.ui.editors;

import monolipse.core.compiler.CompilerServices;
import monolipse.ui.views.BooContentAssistProcessor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextViewer;


public class BooEditorContentAssistProcessor extends BooContentAssistProcessor {
	
	public BooEditorContentAssistProcessor() throws CoreException {
		super(CompilerServices.getInstance());
	}
	
	protected String getCompletionText(ITextViewer viewer, int offset) {
		String text = viewer.getDocument().get();
		return text.substring(0, offset) + "__codecomplete__" + text.substring(offset);
	}

}
