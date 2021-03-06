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

import java.util.*;

import monolipse.ui.editors.BooColorConstants;
import monolipse.ui.editors.BooWhitespaceDetector;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;


public class CSharpScanner extends RuleBasedScanner {
	
	static final String[] MEMBERS = new String[] {
		"class",
		"interface",
		"struct",
		"delegate",
		"event",
		"enum",		
		
		"get",
		"set",
		
		"private",
		"public",
		"protected",
		"internal",
		
		"implicit",
		"explicit",
		"operator",
		
		"readonly",
		"const",
	};	            
	
	static final String[] MODIFIERS = new String[] {
		"partial",
		"override",
		"static",
		"virtual",
		"abstract",
		"sealed",
	};
	
	static final String[] NAMESPACE = new String[] {
		"global",
		"using",		
		"namespace",
	};
	
	static final String[] OPERATORS = new String[] {
		"is",
		"as",
	};
	
	static final String[] KEYWORDS = new String[] {
		
		"break",
		
		"case",
		"continue",
		"do",
		"else",

		"catch",
		"finally",
		"for",
		"foreach",
		"in",
		"if",
		
		"new",
				
		"try",
		"throw",
		"return",
		"switch",
		"yield",
		"typeof",
		
		"try",
		"while",
	};
	
	static final String[] PRIMITIVES = new String[] {
		"bool", "byte", "sbyte", "char",
		"short", "ushort",
		"int", "uint",
		"long", "ulong",
		"string", "object",
		"float", "double",
		"void", "decimal",
	};
	
	static final String[] BUILTINS = new String[] {
		"using",
		"lock",
		"var",
		"default",
	};
	
	static final String[] LITERALS = new String[] {
		"null",
		"true",
		"false",
		"this",	
		"base",
	};
	
	static class BooWordRule implements IRule {

		private Map<String, IToken> _words = new HashMap<String, IToken>();
		private StringBuffer _buffer = new StringBuffer();
		private IToken _identifier;
		private IToken _invocationTarget;

		public BooWordRule(ISharedTextColors manager) {
			IToken identifierToken = createToken(manager, BooColorConstants.DEFAULT);
			IToken keywordToken = createBoldToken(manager, BooColorConstants.KEYWORD);
			IToken primitiveToken = createBoldToken(manager, BooColorConstants.PRIMITIVE);
			IToken invocationTarget = createToken(manager, BooColorConstants.INVOCATION);
			IToken memberToken = createBoldToken(manager, BooColorConstants.MEMBER);			
			IToken modifierToken = createToken(manager, BooColorConstants.MODIFIER);
			IToken builtinToken = createBoldToken(manager, BooColorConstants.BUILTIN);			
			IToken literalsToken = createBoldToken(manager, BooColorConstants.LITERAL);
			IToken namespaceToken = createBoldToken(manager, BooColorConstants.NAMESPACE);
			IToken operatorsToken = createBoldToken(manager, BooColorConstants.OPERATORS);

			_identifier = identifierToken;
			_invocationTarget = invocationTarget;
			addWords(KEYWORDS, keywordToken);
			addWords(MEMBERS, memberToken);
			addWords(MODIFIERS, modifierToken);
			addWords(PRIMITIVES, primitiveToken);
			addWords(BUILTINS, builtinToken);
			addWords(LITERALS, literalsToken);
			addWords(NAMESPACE, namespaceToken);
			addWords(OPERATORS, operatorsToken);
		}

		private Token createBoldToken(ISharedTextColors manager, RGB rgb) {
			return new Token(
				new TextAttribute(
					manager.getColor(rgb),
					null,
					SWT.BOLD));
		}

		private Token createToken(ISharedTextColors manager, RGB rgb) {
			return new Token(
				new TextAttribute(manager.getColor(rgb)));
		}
		
		private boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c);
		}

		private boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}
		
		public IToken evaluate(ICharacterScanner scanner) {
			int c = scanner.read();
			if (isWordStart((char) c)) {
				_buffer.setLength(0);
				do {
					_buffer.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF && isWordPart((char) c));
				scanner.unread();

				IToken token = _words.get(_buffer.toString());
				if (token != null) return token;
				return '(' == peekNextValidChar(scanner) ? _invocationTarget : _identifier;
			}

			scanner.unread();
			return Token.UNDEFINED;

		}
		
		private int peekNextValidChar(ICharacterScanner scanner) {
			int readCount = 1;
			int c = scanner.read();
			while (c != ICharacterScanner.EOF && Character.isWhitespace((char)c)) {
				c = scanner.read();
				++readCount;
			}
			while (readCount > 0) {
				scanner.unread();
				--readCount;
			}
			return c;
		}

		private void addWords(String[] words, IToken token) {
			for (int i=0; i<words.length; ++i) {
				_words.put(words[i], token);
			}
		}
		
	}

	public CSharpScanner(ISharedTextColors manager) {
		IToken defaultToken = 
			new Token(
				new TextAttribute(manager.getColor(BooColorConstants.DEFAULT)));
		IToken numberToken = 
			new Token(
				new TextAttribute(
					manager.getColor(BooColorConstants.NUMBER)));
		
		IRule[] rules = new IRule[] {
			new WhitespaceRule(new BooWhitespaceDetector()),
			new NumberRule(numberToken),
			new BooWordRule(manager),
		};

		setRules(rules);
		setDefaultReturnToken(defaultToken);
	}
}
