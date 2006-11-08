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
package monolipse.ui.editors.csharp;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.*;

public class CSharpPartitionScanner extends RuleBasedPartitionScanner {
	
	static final String MULTILINE_COMMENT_TYPE = "cs___mlc";
	
	static final String SINGLELINE_COMMENT_TYPE = "cs___slc";
	
	static final String SINGLE_QUOTED_STRING = "cs___sqs";
	
	static final String DOUBLE_QUOTED_STRING = "cs___dqs";
	
	public static final String[] PARTITION_TYPES = new String[] {
		IDocument.DEFAULT_CONTENT_TYPE,
		MULTILINE_COMMENT_TYPE,
		SINGLELINE_COMMENT_TYPE,
		DOUBLE_QUOTED_STRING,
		SINGLE_QUOTED_STRING,
	};

	public CSharpPartitionScanner() {
		
		IToken multiLineComment = new Token(MULTILINE_COMMENT_TYPE);
		IToken singleLineComment = new Token(SINGLELINE_COMMENT_TYPE);
		IToken sqs = new Token(SINGLE_QUOTED_STRING);
		IToken dqs = new Token(DOUBLE_QUOTED_STRING);
		
		IPredicateRule[] rules = new IPredicateRule[] {
			new EndOfLineRule("//", singleLineComment),
			new EndOfLineRule("#", singleLineComment),
			new MultiLineRule("/*", "*/", multiLineComment, (char)0, true),
			new SingleLineRule("\"", "\"", dqs, '\\'),
			new SingleLineRule("'", "'", sqs, '\\'),
		};
		setPredicateRules(rules);
	}
}
