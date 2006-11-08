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
package monolipse.ui.views;

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.*;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;

public class BooExplorerView extends ViewPart implements ISetSelectionTarget {

	public final static String ID_VIEW = "monolipse.ui.views.BooExplorerView";

	private TreeViewer _viewer;

	private DrillDownAdapter _drillDownAdapter;
	
	BooExplorerActionGroup _actionGroup;
	
	/**
	 * The constructor.
	 */
	public BooExplorerView() {
	}
	
	class RefreshOnProjectChange implements IResourceChangeListener {
		
		public void resourceChanged(IResourceChangeEvent event) {
			IResource resource = event.getResource();
			if (null == resource
				|| IResource.PROJECT == resource.getType()) {
				getDisplay().asyncExec(new Runnable() {
					public void run() {
						refresh();
					}
				});
			}
		}
	};
	
	class RefreshOnBuild implements IResourceChangeListener {
		
		public void resourceChanged(IResourceChangeEvent event) {
			try {
				final Set projects = new HashSet();
				event.getDelta().accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						projects.add(delta.getResource().getProject());
						return false;
					}
				});
				for (Iterator i = projects.iterator(); i.hasNext();) {
					final Object project = (Object) i.next();
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (!isDisposed()) {
								_viewer.refresh(project, true);
							}
						}
					});
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}		
	}

	/**
	 * This is a callback that will allow us to create the _viewer and
	 * initialize it.
	 */
	public void createPartControl(Composite parent) {
		_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		_drillDownAdapter = new DrillDownAdapter(_viewer);		
		_viewer.setUseHashlookup(true);
		
		_viewer.setSorter(new ViewerSorter() {
			public int category(Object element) {
				if (element instanceof IResource) {
					return 10-((IResource)element).getType();
				}
				return 0;
			}
		});
		
		configureViewFilters();
		
		_viewer.setContentProvider(new BooExplorerContentProvider());
		_viewer.setLabelProvider(new BooExplorerLabelProvider());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		_viewer.setInput(workspace);
		getSite().setSelectionProvider(_viewer);
		
		workspace.addResourceChangeListener(new RefreshOnProjectChange(), IResourceChangeEvent.POST_CHANGE);
		workspace.addResourceChangeListener(new RefreshOnBuild(), IResourceChangeEvent.POST_BUILD);

		createActionGroups();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void configureViewFilters() {
		ResourcePatternFilter resourcePatternFilter = new ResourcePatternFilter();
		resourcePatternFilter.setPatterns(new String[] { ".*" });
		_viewer.addFilter(resourcePatternFilter);
		
		_viewer.addFilter(DerivedResourceFilter.DEFAULT);
	}

	private void createActionGroups() {
		_actionGroup = new BooExplorerActionGroup(this);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BooExplorerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(_viewer.getControl());
		_viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, _viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void fillContextMenu(IMenuManager menu) {
		
		createStandardMenuGroups(menu);
		
		_drillDownAdapter.addNavigationActions(menu);
		menu.add(new Separator());
		
		_actionGroup.setContext(new ActionContext(getSelection()));
		_actionGroup.fillContextMenu(menu);
		_actionGroup.setContext(null);
		
	}

	private void createStandardMenuGroups(IMenuManager menu) {
		menu.add(new Separator(IContextMenuConstants.GROUP_NEW));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_GOTO));
		menu.add(new Separator(IContextMenuConstants.GROUP_OPEN));
		menu.add(new GroupMarker(IContextMenuConstants.GROUP_SHOW));
		menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
		menu.add(new Separator(IContextMenuConstants.GROUP_GENERATE));
		menu.add(new Separator(IContextMenuConstants.GROUP_SEARCH));
		menu.add(new Separator(IContextMenuConstants.GROUP_BUILD));
		menu.add(new Separator(IContextMenuConstants.GROUP_ADDITIONS));
		menu.add(new Separator(IContextMenuConstants.GROUP_VIEWER_SETUP));
		menu.add(new Separator(IContextMenuConstants.GROUP_PROPERTIES));
	}

	private IStructuredSelection getSelection() {
		return (IStructuredSelection)_viewer.getSelection();
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		_drillDownAdapter.addNavigationActions(manager);
	}

	private void hookDoubleClickAction() {
		_viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					Object selectedElement = ((IStructuredSelection) event
							.getSelection()).getFirstElement();					
					if (selectedElement instanceof IResource){
						openResource((IResource) selectedElement);
					} else if (selectedElement instanceof IAdaptable) {
						PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(getSite().getShell(), (IAdaptable) selectedElement, null, null, null);
						dialog.setBlockOnOpen(true);
						dialog.open();
					}
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}

			private void openResource(IResource resource) throws PartInitException {
				if (IResource.FILE == resource.getType()) {
					// TODO: use openAction here
					IDE.openEditor(getWorkbench()
							.getActiveWorkbenchWindow().getActivePage(),
							(IFile) resource);
				} else if (IResource.FOLDER == resource.getType()) {
					_drillDownAdapter.goInto(resource);
				}
			}
		});
	}
	
	/**
	 * Passing the focus request to the _viewer's control.
	 */
	public void setFocus() {
		_viewer.getControl().setFocus();
	}

	IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	public void selectReveal(ISelection selection) {
		_viewer.setSelection(selection, true);
	}

	private Display getDisplay() {
		return _viewer.getControl().getDisplay();
	}

	private void refresh() {
		if (!isDisposed()) {
			Object[] elements = _viewer.getExpandedElements();
			_viewer.refresh();
			_viewer.setExpandedElements(elements);
		}
	}

	private boolean isDisposed() {
		return _viewer.getControl().isDisposed();
	}
}