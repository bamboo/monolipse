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

import org.eclipse.jface.text.*;

public class BooAutoEditStrategy implements IAutoEditStrategy {

	public void customizeDocumentCommand(IDocument d,
			DocumentCommand c) {
		if (c.text == null) return;

		if (c.length == 0 && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1) {
			autoIndentAfterNewLine(d, c);
		}
		
	}

	private int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
		while (offset < end) {
			char c = document.getChar(offset);
			if (c != ' ' && c != '\t') {
				return offset;
			}
			offset++;
		}
		return end;
	}

	private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

		if (c.offset == -1 || d.getLength() == 0) return;

		try {
			// find start of line
			int p = (c.offset == d.getLength() ? c.offset  - 1 : c.offset);
			IRegion lineInfo = d.getLineInformationOfOffset(p);
			int start = lineInfo.getOffset();
			int endOfLineOffset = start+lineInfo.getLength();
			
			// find white spaces
			int end = findEndOfWhiteSpace(d, start, c.offset);

			StringBuffer buf = new StringBuffer(c.text);
			if (end > start) {
				// append to input
				buf.append(d.get(start, end - start));
			}
			
			if (c.offset == endOfLineOffset
				&& isIndentingChar(d.getChar(endOfLineOffset-1))) {
				buf.append('\t');
			}
			c.text = buf.toString();

		} catch (BadLocationException ignored) {
		}
	}

	private boolean isIndentingChar(char c) {
		return ':' == c || '\\' == c || '(' == c; 
	}


}
