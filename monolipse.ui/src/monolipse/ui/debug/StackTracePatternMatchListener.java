package monolipse.ui.debug;

import org.eclipse.ui.console.PatternMatchEvent;

public class StackTracePatternMatchListener extends AbstractStackTracePatternMatchListener {

	@Override
	public synchronized void matchFound(PatternMatchEvent event) {
		final int offset = event.getOffset() + 3;
		final int length = event.getLength() - 3;
		
		hyperlinkStackTraceMatch(offset, length);		
	}
}
