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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import monolipse.ui.BooUI;
import monolipse.ui.editors.actions.ExpandCodeAction;
import monolipse.ui.editors.actions.ToggleCommentAction;

public class BooEditor extends TextEditor {
	
	public static final String ID_EDITOR = "monolipse.ui.editors.BooEditor";

	private BooContentOutlinePage _outlinePage;

	public BooEditor() {
		super();
		setSourceViewerConfiguration(new BooSourceViewerConfiguration(
				getSharedColors()));
		setDocumentProvider(new BooDocumentProvider());
		setKeyBindingScopes(new String[] { "monolipse.ui.booEditorScope", "org.eclipse.ui.textEditorScope" });
	}

	protected void createActions() {
		super.createActions();

		ToggleCommentAction action = new ToggleCommentAction(BooUI
				.getResourceBundle(), "ToggleComment.", this);
		action.setAccelerator(Action.convertAccelerator("CTRL+/"));
		setAction(ToggleCommentAction.ID, action);
		setActionActivationCode(ToggleCommentAction.ID, '/', -1, SWT.CTRL);
		markAsStateDependentAction(ToggleCommentAction.ID, true);
		action.configure(getSourceViewer(), getSourceViewerConfiguration());
		
		Action eca = new ExpandCodeAction(this);
		setAction(eca.getId(), eca);
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		IAction action = getAction(ToggleCommentAction.ID);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, action);
		menu.appendToGroup(ITextEditorActionConstants.MB_ADDITIONS, getAction(ExpandCodeAction.ID));
	}

	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (_outlinePage == null) {
				_outlinePage = new BooContentOutlinePage(getDocumentProvider(), this);
				_outlinePage.setInput(getEditorInput());
			}
			return _outlinePage;
		}
		return super.getAdapter(required);
	}

}
