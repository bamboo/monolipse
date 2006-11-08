package monolipse.ui;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;

public class TextViewerUtilities {

	public static String getLineAtOffset(ITextViewer viewer, final int offset) {
		final StyledText widget = viewer.getTextWidget();
		final int line = widget.getLineAtOffset(offset);
		return widget.getContent().getLine(line);
	}

}
