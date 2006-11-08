/*
 * Boo Development Tools for the Eclipse IDE
 * Copyright (C) 2005 Rodrigo B. de Oliveira (rbo@acm.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package monolipse.ui.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;

public class StringScanner extends RuleBasedScanner {
	
	public StringScanner(Color baseColor) {
		IToken tqs = new Token(
			new TextAttribute(
				baseColor));
		IToken expression = new Token(
			new TextAttribute(
				baseColor,
				null,
				SWT.BOLD));
		IRule[] rules = new IRule[] {
			new SingleLineRule("${", "}", expression),
		};
		setRules(rules);
		setDefaultReturnToken(tqs);
	}

}
