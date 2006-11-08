package monolipse.ui.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;

public class SingleQuotedStringScanner extends RuleBasedScanner {
	
	public SingleQuotedStringScanner(Color baseColor) {
		IToken tqs = new Token(
			new TextAttribute(
				baseColor));
		setDefaultReturnToken(tqs);
	}

}
