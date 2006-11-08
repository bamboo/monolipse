package monolipse.ui.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISharedTextColors;

public class RegexScanner extends RuleBasedScanner {

	RegexScanner(ISharedTextColors manager) {
		IToken regexToken = 
			new Token(
				new TextAttribute(manager.getColor(BooColorConstants.REGEX)));
		
		setDefaultReturnToken(regexToken);
	}
}
