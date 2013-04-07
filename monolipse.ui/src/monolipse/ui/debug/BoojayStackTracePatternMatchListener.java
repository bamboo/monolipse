package monolipse.ui.debug;

import org.eclipse.ui.console.PatternMatchEvent;

public class BoojayStackTracePatternMatchListener extends AbstractStackTracePatternMatchListener {
	
	@Override
	public synchronized void matchFound(PatternMatchEvent event) {
		final int offset = event.getOffset() + 1;
		final int length = event.getLength() - 2;
		hyperlinkStackTraceMatch(offset, length);		
	}
}
