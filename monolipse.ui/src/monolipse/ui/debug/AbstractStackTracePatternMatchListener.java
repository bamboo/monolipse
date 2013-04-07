package monolipse.ui.debug;

import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.ui.BooUI;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.TextConsole;

public abstract class AbstractStackTracePatternMatchListener implements IPatternMatchListenerDelegate {

	private TextConsole _console;

	public AbstractStackTracePatternMatchListener() {
		super();
	}

	public void connect(TextConsole console) {
		_console = console;
	}

	public void disconnect() {
		_console = null;
	}
	
	protected void hyperlinkStackTraceMatch(final int offset, final int length) {
		String match = null;
		try {
			match = _console.getDocument().get(offset, length);
		} catch (BadLocationException e) {
			BooUI.logException(e);
			return;
		}
		int position = match.lastIndexOf(':');
		String fname = match.substring(0, position);
		final int lineNumber = Integer.parseInt(match.substring(position+1));		
		final IFile file = WorkspaceUtilities.getFileForLocation(fname);
		if (file != null) {
			try {
				FileLink link = new FileLink(file, null, -1, -1, lineNumber);
				_console.addHyperlink(link, offset, length);
			} catch (BadLocationException e) {
				BooUI.logException(e);
			}
		}
	}

}