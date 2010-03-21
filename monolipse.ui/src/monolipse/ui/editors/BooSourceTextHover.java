package monolipse.ui.editors;

import monolipse.core.BooCore;
import monolipse.core.compiler.CompilerServices;
import monolipse.ui.BooUI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;

public class BooSourceTextHover implements ITextHover {
	
	private int _line;
	private int _column;

	public BooSourceTextHover() {
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			if (hoverRegion.getLength() > -1)
				return getHoverInformation(
					textViewer.getDocument().get(), _line, _column);
		}

		return "empty";
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		try {
			_line = textViewer.getDocument().getLineOfOffset(offset);
			_column = calculateColumn(textViewer, offset, _line);
			return new Region(offset, 1);
		} catch (BadLocationException e) {
			BooUI.logException(e);
		}
		return null;
	}

	private int calculateColumn(ITextViewer textViewer, int offset, int line)
			throws BadLocationException {
		int startOffset = textViewer.getDocument().getLineOffset(line);
		int column = offset - startOffset;
		String text = textViewer.getDocument().get(startOffset, column);
		int textSize = 0;
		int tabSize = 4; // FIX: get real document tab size
		for(char ch: text.toCharArray()) {
			if (ch == '\t') {
				textSize += tabSize;
			}
			else textSize++;
		}
		return textSize;
	}
	
	private String getHoverInformation(String code, int line, int column) {
		String info =  getCompilerServices().getHoverInformation(code, line, column);
		return info;
	}
	
	private CompilerServices getCompilerServices() {
		try {
			return CompilerServices.getInstance();
		} catch (CoreException e) {
			BooUI.logException(e);
		}
		
		return null;
	}
}
