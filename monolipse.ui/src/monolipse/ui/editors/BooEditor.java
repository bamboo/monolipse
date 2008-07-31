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

import monolipse.ui.BooUI;
import monolipse.ui.editors.actions.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.*;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


public class BooEditor extends TextEditor {
	
	private final class BooOutlineInformationProvider implements
			IInformationProvider, IInformationProviderExtension {
		public String getInformation(ITextViewer textViewer, IRegion subject) {
			return null;
		}

		public IRegion getSubject(ITextViewer textViewer, int offset) {
			return new Region(offset, 1);
		}

		public IInformationControlCreator getInformationPresenterControlCreator() {
			return quickOutlineCreator();
		}

		public Object getInformation2(ITextViewer textViewer, IRegion subject) {
			return outlinePage().outline();
		}
	}

	public static final String ID_EDITOR = "monolipse.ui.editors.BooEditor";

	private BooContentOutlinePage _outlinePage;

	public BooEditor() {
		super();
		setSourceViewerConfiguration(new BooSourceViewerConfiguration(getSharedColors()));
		setDocumentProvider(new BooDocumentProvider());
		setKeyBindingScopes(new String[] { "monolipse.ui.booEditorScope", "org.eclipse.ui.textEditorScope" });
	}

	protected void createActions() {
		super.createActions();

		ToggleCommentAction action = new ToggleCommentAction(BooUI.getResourceBundle(), "ToggleComment.", this);
		action.setAccelerator(Action.convertAccelerator("M1+/"));
		setAction(ToggleCommentAction.ID, action);
		setActionActivationCode(ToggleCommentAction.ID, '/', -1, SWT.MOD1);
		markAsStateDependentAction(ToggleCommentAction.ID, true);
		action.configure(getSourceViewer(), getSourceViewerConfiguration());
		
		Action eca = new ExpandCodeAction(this);
		setAction(eca.getId(), eca);
		
		final InformationPresenter quickOutline = new InformationPresenter(quickOutlineCreator()) {
			public IInformationProvider getInformationProvider(String contentType) {
				return new BooOutlineInformationProvider();
			}
		};
		quickOutline.install(getSourceViewer());
		final Action outlineAction = new Action("QuickOutline") {
			public void run() {
				BusyIndicator.showWhile(getSite().getShell().getDisplay(), new Runnable() {
					public void run() {
						quickOutline.showInformation();
					}
				});
				
			}
		};
		outlineAction.setImageDescriptor(BooUI.sharedImage(ISharedImages.IMG_DEF_VIEW));
		outlineAction.setId("monolipse.ui.editors.actions.BooQuickOutlineAction");
		outlineAction.setActionDefinitionId(outlineAction.getId());
		setAction(outlineAction.getId(), outlineAction);
	}

	private IInformationControlCreator quickOutlineCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int shellStyle= SWT.RESIZE;
				int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
				return outlinePage().createQuickOutline(parent, shellStyle, treeStyle);
			}
		};
	}

	protected void editorContextMenuAboutToShow(IMenuManager menu) {
		super.editorContextMenuAboutToShow(menu);
		IAction action = getAction(ToggleCommentAction.ID);
		menu.appendToGroup(ITextEditorActionConstants.GROUP_EDIT, action);
		menu.appendToGroup(ITextEditorActionConstants.MB_ADDITIONS, getAction(ExpandCodeAction.ID));
	}

	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return outlinePage();
		}
		return super.getAdapter(required);
	}

	private BooContentOutlinePage outlinePage() {
		if (_outlinePage == null) {
			_outlinePage = new BooContentOutlinePage(getDocumentProvider(), this);
			_outlinePage.setInput(getEditorInput());
		}
		return _outlinePage;
	}


}
