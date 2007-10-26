package monolipse.ui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.Token;


/**
 * An implementation of <code>IRule</code> capable of detecting whitespace.
 * A whitespace rule uses a whitespace detector in order to find out which
 * characters are whitespace characters.
 *
 * @see IWhitespaceDetector
 */
public class BooWhitespaceRule implements IRule {

	/** The whitespace detector used by this rule */
	protected final IWhitespaceDetector fDetector;
	
	private final IToken whitespace;

	/**
	 * Creates a rule which, with the help of an
	 * whitespace detector, will return a whitespace
	 * token when a whitespace is detected.
	 *
	 * @param detector the rule's whitespace detector, may not be <code>null</code>
	 */
	public BooWhitespaceRule(IWhitespaceDetector detector, IToken whitespaceToken) {
		Assert.isNotNull(detector);
		fDetector= detector;
		whitespace = whitespaceToken;
	}
	
	public BooWhitespaceRule(IWhitespaceDetector detector) {
		this(detector, Token.WHITESPACE);
	}

	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int c= scanner.read();
		if (fDetector.isWhitespace((char) c)) {
			do {
				c= scanner.read();
			} while (fDetector.isWhitespace((char) c));
			scanner.unread();
			return whitespace;
		}

		scanner.unread();
		return Token.UNDEFINED;
	}
}
