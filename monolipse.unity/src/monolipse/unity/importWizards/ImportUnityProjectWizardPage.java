/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package monolipse.unity.importWizards;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ImportUnityProjectWizardPage extends WizardPage {
	
	protected DirectoryFieldEditor editor;

	public ImportUnityProjectWizardPage(String title) {
		super(title);
		setDescription("Import a Unity project into the workspace"); //NON-NLS-1
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#createAdvancedControls(org.eclipse.swt.widgets.Composite)
	 */	
	public void createControl(Composite parent) {
		Composite projectSelectionArea = new Composite(parent, SWT.NONE);
		
		GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
		projectSelectionArea.setLayoutData(fileSelectionData);

		GridLayout fileSelectionLayout = new GridLayout();
		fileSelectionLayout.numColumns = 3;
		fileSelectionLayout.makeColumnsEqualWidth = false;
		fileSelectionLayout.marginWidth = 0;
		fileSelectionLayout.marginHeight = 0;
		projectSelectionArea.setLayout(fileSelectionLayout);
		
		editor = new DirectoryFieldEditor("Unity Project", "Select Unity Project: ", projectSelectionArea); //NON-NLS-1 //NON-NLS-2
		editor.getTextControl(projectSelectionArea).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				IPath path = new Path(ImportUnityProjectWizardPage.this.editor.getStringValue());
			}
		});
		projectSelectionArea.moveAbove(null);
		
		setControl(projectSelectionArea);
	}
}
