/* based on eclipse code */
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package monolipse.nunit.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * A progress bar with a red/green indication for success or failure.
 */
public class NUnitProgressBar extends Canvas {
	private static final int DEFAULT_WIDTH = 160;
	private static final int DEFAULT_HEIGHT = 18;

	private int _currentTickCount= 0;
	private int _maxTickCount= 0;	
	private int _colorBarWidth= 0;
	private Color _oKColor;
	private Color _failureColor;
	private Color _stoppedColor;
	private boolean _error;
	private boolean _stopped= false;
	
	public NUnitProgressBar(Composite parent) {
		super(parent, SWT.NONE);
		
		addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				_colorBarWidth= scale(_currentTickCount);
				redraw();
			}
		});	
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				_failureColor.dispose();
				_oKColor.dispose();
				_stoppedColor.dispose();
			}
		});
		Display display= parent.getDisplay();
		_failureColor= new Color(display, 159, 63, 63);
		_oKColor= new Color(display, 95, 191, 95);
		_stoppedColor= new Color(display, 120, 120, 120);
	}

	public void setMaximum(int max) {
		_maxTickCount= max;
	}
		
	public void reset() {
		_error= false;
		_stopped= false;
		_currentTickCount= 0;
		_colorBarWidth= 0;
		_maxTickCount= 0;
		redraw();
	}
	
	private void paintStep(int startX, int endX) {
		GC gc = new GC(this);	
		setStatusColor(gc);
		Rectangle rect= getClientArea();
		startX= Math.max(1, startX);
		gc.fillRectangle(startX, 1, endX-startX, rect.height-2);
		gc.dispose();		
	}

	private void setStatusColor(GC gc) {
		if (_stopped)
			gc.setBackground(_stoppedColor);
		else if (_error)
			gc.setBackground(_failureColor);
		else if (_stopped)
			gc.setBackground(_stoppedColor);
		else
			gc.setBackground(_oKColor);
	}

	public void stopped() {
		_stopped= true;
		redraw();
	}
	
	private int scale(int value) {
		if (_maxTickCount > 0) {
			Rectangle r= getClientArea();
			if (r.width != 0)
				return Math.max(0, value*(r.width-2)/_maxTickCount);
		}
		return value; 
	}
	
	private void drawBevelRect(GC gc, int x, int y, int w, int h, Color topleft, Color bottomright) {
		gc.setForeground(topleft);
		gc.drawLine(x, y, x+w-1, y);
		gc.drawLine(x, y, x, y+h-1);
		
		gc.setForeground(bottomright);
		gc.drawLine(x+w, y, x+w, y+h);
		gc.drawLine(x, y+h, x+w, y+h);
	}
	
	private void paint(PaintEvent event) {
		GC gc = event.gc;
		Display disp= getDisplay();
			
		Rectangle rect= getClientArea();
		gc.fillRectangle(rect);
		drawBevelRect(gc, rect.x, rect.y, rect.width-1, rect.height-1,
			disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
			disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		
		setStatusColor(gc);
		_colorBarWidth= Math.min(rect.width-2, _colorBarWidth);
		gc.fillRectangle(1, 1, _colorBarWidth, rect.height-2);
	}	
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point size= new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		if (wHint != SWT.DEFAULT) size.x= wHint;
		if (hHint != SWT.DEFAULT) size.y= hHint;
		return size;
	}
	
	public void step(boolean hasFailures) {
		_currentTickCount++;
		int x= _colorBarWidth;

		_colorBarWidth= scale(_currentTickCount);

		if (!_error && hasFailures) {
			_error= true;
			x= 1;
		}
		if (_currentTickCount == _maxTickCount)
			_colorBarWidth= getClientArea().width-1;
		paintStep(x, _colorBarWidth);
	}

	public void refresh(boolean hasErrors) {
		_error= hasErrors;
		redraw();
	}
	
}
