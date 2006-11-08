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

import java.text.MessageFormat;

import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


/**
 * A panel with counters for the number of Runs, Errors and Failures.
 */
public class CounterPanel extends Composite {
	protected Text _numberOfErrors;
	protected Text _numberOfFailures;
	protected Text _numberOfRuns;
	protected int _total;
	
	private final Image _errorIcon= BooUI.getImage(IBooUIConstants.ERROR);
	private final Image _failureIcon= BooUI.getImage(IBooUIConstants.WARNING);
			
	public CounterPanel(Composite parent) {
		super(parent, SWT.WRAP);
		GridLayout gridLayout= new GridLayout();
		gridLayout.numColumns= 9;
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth= 0;
		setLayout(gridLayout);
		
		_numberOfRuns= createLabel("Runs", null, " 0/0  "); //$NON-NLS-1$
		_numberOfErrors= createLabel("Errors", _errorIcon, " 0 "); //$NON-NLS-1$
		_numberOfFailures= createLabel("Failures", _failureIcon, " 0 "); //$NON-NLS-1$
	}
	
	private Text createLabel(String name, Image image, String init) {
		Label label= new Label(this, SWT.NONE);
		if (image != null) {
			image.setBackground(label.getBackground());
			label.setImage(image);
		}
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		
		label= new Label(this, SWT.NONE);
		label.setText(name);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		//label.setFont(JFaceResources.getBannerFont());
		
		Text value= new Text(this, SWT.READ_ONLY);
		value.setText(init);
		// bug: 39661 Junit test counters do not repaint correctly [JUnit] 
		value.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		value.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.HORIZONTAL_ALIGN_BEGINNING));
		return value;
	}

	public void reset() {
		setErrorValue(0);
		setFailureValue(0);
		setRunValue(0);
		_total= 0;
	}
	
	public void setTotal(int value) {
		_total= value;
	}
	
	public int getTotal(){
		return _total;
	}
	
	public void setRunValue(int value) {
		String runString= MessageFormat.format("{0}/{1}", new Object[] { Integer.toString(value), Integer.toString(_total) }); 
		_numberOfRuns.setText(runString);

		_numberOfRuns.redraw();
		redraw();
	}
	
	public void setErrorValue(int value) {
		_numberOfErrors.setText(Integer.toString(value));
		redraw();
	}
	
	public void setFailureValue(int value) {
		_numberOfFailures.setText(Integer.toString(value));
		redraw();
	}
}
