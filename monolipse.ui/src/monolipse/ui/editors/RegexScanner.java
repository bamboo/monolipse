package monolipse.ui.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

public class RegexScanner extends RuleBasedScanner {

	RegexScanner(ISharedTextColors manager) {
		final Color baseColor = manager.getColor(BooColorConstants.REGEX);
		IToken regexToken = 
			new Token(
				new TextAttribute(baseColor));
		
		IToken binding = new Token(
				new TextAttribute(
					baseColor,
					null,
					SWT.BOLD));
		IRule[] rules = new IRule[] {
			new SingleLineRule("?<", ">", binding),
		};
		setRules(rules);
		
		setDefaultReturnToken(regexToken);
	}
}
