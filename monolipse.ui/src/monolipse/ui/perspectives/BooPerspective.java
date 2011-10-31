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
package monolipse.ui.perspectives;

import monolipse.ui.views.BooExplorerView;
import monolipse.ui.views.BooInteractiveInterpreterView;

import org.eclipse.ui.*;


public class BooPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		
		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
		layout.addActionSet("org.eclipse.debug.ui.launchActionSet");
		
		layout.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective");
		layout.addNewWizardShortcut("monolipse.ui.wizards.NewClassWizard");
		layout.addNewWizardShortcut("monolipse.ui.wizards.NewBooAssemblySourceWizard");
		layout.addNewWizardShortcut("monolipse.ui.wizards.NewBoojayAssemblySourceWizard");
		layout.addNewWizardShortcut("monolipse.ui.wizards.NewBooProjectWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
		
		String editorArea = layout.getEditorArea();

		// Place navigator and outline to left of
		// editor area.
		IFolderLayout upperLeft = layout.createFolder("upperLeft",
				IPageLayout.LEFT, (float) 0.26, editorArea);
		upperLeft.addView(BooExplorerView.ID_VIEW);
		upperLeft.addView(IPageLayout.ID_RES_NAV);

		IFolderLayout bottomLeft = layout.createFolder("bottomLeft",
				IPageLayout.BOTTOM, (float) 0.5, "upperLeft");
		bottomLeft.addView(IPageLayout.ID_OUTLINE);

		IFolderLayout bottom = layout.createFolder("bottom",
				IPageLayout.BOTTOM, (float) 0.70, editorArea);
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IPageLayout.ID_TASK_LIST);
		bottom.addView(BooInteractiveInterpreterView.ID_VIEW);
	}
}
