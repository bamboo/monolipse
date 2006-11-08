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
package monolipse.ui.preferences;

import monolipse.ui.BooUI;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


public class BooPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	static class RuntimeDirectoryFieldEditor extends DirectoryFieldEditor {
		RuntimeDirectoryFieldEditor(Composite parent) {
			super("notset", "&Mono runtime location:", parent);
			setErrorMessage("Value must point to a valid runtime prefix.");
		}
		
		protected boolean doCheckState() {
			boolean valid = super.doCheckState();
//			if (valid) {
//				try {
////					int status = MonoRuntime.validateRuntimePath(getTextControl().getText());
////					return MonoRuntime.RuntimePathStatus.OK == status;
//				} catch (IOException e) {
//					e.printStackTrace();
//					setErrorMessage(e.getLocalizedMessage());
//					return false;
//				}
//				
//			}
			return valid;
		}
	}

	public BooPreferencePage() {
		super(GRID);
		
		setPreferenceStore(BooUI.getCorePreferenceStore());
		setDescription("General Boo Preferences");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new RuntimeDirectoryFieldEditor(getFieldEditorParent()));
	}
	
	public boolean performOk() {
		boolean result = super.performOk();
		if (result) {
			BooUI.saveCorePreferences();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}