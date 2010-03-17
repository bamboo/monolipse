package monolipse.ui.editors;

import monolipse.core.compiler.CompilerServices;
import monolipse.ui.BooUI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Point;

public class BooSourceTextHover implements ITextHover {
	
	public BooSourceTextHover() {
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion != null) {
			try {
				if (hoverRegion.getLength() > -1)
					return getHoverInformation(
						textViewer.getDocument().get(0, textViewer.getDocument().getLength()),
						hoverRegion.getOffset(), 
						hoverRegion.getLength());

			} catch (BadLocationException x) {
			}
		}

		return "empty";
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}
	
	private String getHoverInformation(String code, int offset, int length) {
		return getCompilerServices().getHoverInformation(code, offset, offset+length);
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
