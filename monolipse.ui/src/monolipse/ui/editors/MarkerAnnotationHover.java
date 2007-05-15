/*
 * Copyright (c) 2003, 2004, Chris Leung. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package monolipse.ui.editors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.internal.text.html.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * Determines all markers for the given line and collects, concatenates, and formates
 * their messages.
 * 
 * @author chrisl
 */
public class MarkerAnnotationHover implements IAnnotationHover {

	//////////////////////////////////////////////////////////////////////

	/*
	 * @see IVerticalRulerHover#getHoverInfo(ISourceViewer, int)
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		List markers = getMarkerAnnotationsForLine(sourceViewer, lineNumber);
		if (markers != null) {
			IMarker marker;
			String message;
			if (markers.size() == 1) {
				// optimization
				marker = (IMarker) markers.get(0);
				message = marker.getAttribute(IMarker.MESSAGE, "");
				if (message != null && message.trim().length() > 0)
					return formatSingleMessage(message);
			} else {
				List messages = new ArrayList();
				for (int i = 0; i < markers.size(); ++i) {
					marker = (IMarker) markers.get(i);
					message = marker.getAttribute(IMarker.MESSAGE, "");
					if (message != null && message.trim().length() > 0)
						messages.add(message.trim());
				}
				if (messages.size() == 1)
					return formatSingleMessage((String) messages.get(0));
				if (messages.size() > 1)
					return formatMultipleMessages(messages);
			}
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////

	/**
	 * Returns the distance to the ruler line. 
	 */
	protected int compareRulerLine(Position position, IDocument document, int line) {

		if (position.getOffset() > -1 && position.getLength() > -1) {
			try {
				int annotationLine = document.getLineOfOffset(position.getOffset());
				if (line == annotationLine)
					return 1;
				if (annotationLine <= line
					&& line <= document.getLineOfOffset(position.getOffset() + position.getLength()))
					return 2;
			} catch (BadLocationException x) {
			}
		}

		return 0;
	}

	/**
	 * Selects a set of markers from the two lists. By default, it just returns
	 * the set of exact matches.
	 */
	protected List select(List exactMatch, List including) {
		return exactMatch;
	}

	/**
	 * Returns one marker which includes the ruler's line of activity.
	 */
	protected List getMarkerAnnotationsForLine(ISourceViewer viewer, int line) {
		IDocument document = viewer.getDocument();
		IAnnotationModel model = viewer.getAnnotationModel();
		if (model == null)
			return null;
		//
		List exact = new ArrayList();
		Iterator e = model.getAnnotationIterator();
		while (e.hasNext()) {
			Object o = e.next();
			if (o instanceof MarkerAnnotation) {
				IMarker marker = ((MarkerAnnotation) o).getMarker();
				int linenumber = marker.getAttribute(IMarker.LINE_NUMBER, -1)-1;
				if (linenumber == line) {
					exact.add(marker);
					continue;
				}
				int start = marker.getAttribute(IMarker.CHAR_START, -1);
				if (start < 0)
					continue;
				try {
					start = document.getLineOfOffset(start);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
					continue;
				}
				if (start == line)
					exact.add(marker);
			}
		}
		return exact;
	}

	/*
	 * Formats a message as HTML text.
	 */
	private String formatSingleMessage(String message) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	/*
	 * Formats several message as HTML text.
	 */
	private String formatMultipleMessages(List messages) {
		StringBuffer buffer = new StringBuffer();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(
			buffer,
			HTMLPrinter.convertToHTMLContent(
				MessageFormat.format(
					"Multiple markers at this line ({0}):",
					new Object[] { new Integer(messages.size())})));
		HTMLPrinter.startBulletList(buffer);
		Iterator e = messages.iterator();
		while (e.hasNext())
			HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent((String) e.next()));
		HTMLPrinter.endBulletList(buffer);

		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	//////////////////////////////////////////////////////////////////////

}