package monolipse.ui.debug;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.ui.BooUI;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;


public class CompilerOutputPatternMatchListener implements IPatternMatchListenerDelegate {

	private TextConsole _console;

	public void connect(TextConsole console) {
		_console = console;
	}

	public void disconnect() {
		_console = null;
	}

	public void matchFound(PatternMatchEvent event) {
		
		final int offset = event.getOffset();
		final int length = event.getLength();
		
		String match = null;
		try {
			match = _console.getDocument().get(offset, length);
		} catch (BadLocationException e) {
			BooUI.logException(e);
			return;
		}
		
		int position = match.lastIndexOf('(');
		String fname = match.substring(0, position);
		final IFile file = WorkspaceUtilities.getFileForLocation(fname);
		if (null != file) {
			try {
				FileLink link = new FileLink(file, null, -1, -1, getLineNumber(match));
				_console.addHyperlink(link, offset, length);
			} catch (BadLocationException e) {
				BooUI.logException(e);
			}
		}		
	}

	private int getLineNumber(String match) {
		Matcher matcher = Pattern.compile("\\((\\d+)").matcher(match);
		return matcher.find()
			? Integer.parseInt(matcher.group(1))
			: -1;
	}
}
