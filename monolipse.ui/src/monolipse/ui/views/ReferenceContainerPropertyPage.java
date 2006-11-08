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

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;

import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;
import monolipse.core.IGlobalAssemblyCacheReference;
import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

public class ReferenceContainerPropertyPage extends PreferencePage
	implements IWorkbenchPropertyPage {
	
	abstract class ReferenceLabelProvider implements ITableLabelProvider {
		private Image _image = BooUI.getImage(IBooUIConstants.ASSEMBLY_REFERENCE);

		public Image getColumnImage(Object element, int columnIndex) {
			return 0 == columnIndex
				? _image
				: null;
		}
		
		public void dispose() {
		}
		
		public void addListener(ILabelProviderListener listener) {
		}
		
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	class GACContentProvider implements IStructuredContentProvider {
		
		IAssemblyReference[] _gac;
		
		public Object[] getElements(Object inputElement) {
			if (null == _gac) {
				try {
					_gac = BooCore.listGlobalAssemblyCache();
				} catch (IOException e) {
					dumpError(e);
				}
			}
			return _gac;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class GACLabelProvider extends ReferenceLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			IGlobalAssemblyCacheReference reference = (IGlobalAssemblyCacheReference)element;
			switch (columnIndex) {
			case 0: return reference.getAssemblyName();
			case 1: return reference.getVersion();
			case 2: return reference.getToken();
			}	
			return "";
		}
	}
	
	class SelectedReferencesContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return _references.toArray();
		}

		public void dispose() {			
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	class SelectedReferencesLabelProvider extends ReferenceLabelProvider {		
		public String getColumnText(Object element, int columnIndex) {
			IAssemblyReference reference = (IAssemblyReference)element;
			return columnIndex == 0 ? reference.getAssemblyName() : reference.getType();
		}		
	}

	private IAdaptable _element;
	
	private java.util.Set _references = new LinkedHashSet();

	private TableViewer _selectedViewer;

	public ReferenceContainerPropertyPage() {
	}

	protected void performDefaults() {
	}
	
	public boolean performOk() {
		try {
			IAssemblyReference[] references = new IAssemblyReference[_references.size()];
			_references.toArray(references);
			IAssemblySource assemblySource = getAssemblySource();
			assemblySource.setReferences(references);
			assemblySource.save(null);
			return true;
		} catch (CoreException e) {
			dumpError(e);
		}
		return false;
	}

	public IAdaptable getElement() {
		return _element;
	}

	public void setElement(IAdaptable element) {
		_element = element;
	}

	protected Control createContents(Composite parent) {
		
		initializeReferences();
		
		Composite control = new Composite(parent, SWT.NONE);
		
		CTabFolder libraryBrowser = createTabbedLibraryBrowser(control);
		_selectedViewer = createSelectedLibrariesViewer(control);
		
		GridLayout layout = new GridLayout(1, true);		
		control.setLayout(layout);
		libraryBrowser.setLayoutData(new GridData(GridData.FILL_BOTH));
		_selectedViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return control;
	}

	private void initializeReferences() {
		_references.addAll(Arrays.asList(getAssemblySource().getReferences()));
	}

	private void dumpError(Exception e) {
		e.printStackTrace();
		setErrorMessage(e.getLocalizedMessage());
	}

	private TableViewer createSelectedLibrariesViewer(Composite control) {
		final TableViewer viewer = new TableViewer(control, SWT.BORDER);
		Table table = viewer.getTable();
		
		addColumn(table, "Assembly", 250);		
		addColumn(table, "Type", 150);
		
		table.setHeaderVisible(true);
		
		viewer.setContentProvider(new SelectedReferencesContentProvider());
		viewer.setLabelProvider(new SelectedReferencesLabelProvider());
		viewer.setInput(getAssemblySource());
		table.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.DEL) {
					List selected = ((IStructuredSelection)_selectedViewer.getSelection()).toList();
					_references.removeAll(selected);
					_selectedViewer.refresh();
				}
			}
			
		});
		return viewer;
	}

	private CTabFolder createTabbedLibraryBrowser(Composite control) {
		final CTabFolder tabFolder = new CTabFolder(control, SWT.BORDER);
		tabFolder.setBorderVisible(true);
	    
		CTabItem localReferencesPage = new CTabItem(tabFolder, SWT.NONE, 0);
		localReferencesPage.setText("Libraries");
		localReferencesPage.setControl(createLocalReferenceBrowser(tabFolder));
		
		CTabItem sourceReferencesPage = new CTabItem(tabFolder, SWT.NONE, 1);
		sourceReferencesPage.setText("Assembly Sources");
		sourceReferencesPage.setControl(createAssemblySourceBrowser(tabFolder));
		
		final CTabItem gacReferencesPage = new CTabItem(tabFolder, SWT.NONE, 2);
		gacReferencesPage.setText("Global Assembly Cache");
		final TableViewer gacBrowser = createGACBrowser(tabFolder);
		gacReferencesPage.setControl(gacBrowser.getControl());
		
		tabFolder.setSelection(localReferencesPage);
		
		final Object dummyInput = new Object();
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (gacReferencesPage == tabFolder.getSelection()) {
					if (gacBrowser.getInput() != dummyInput) {
						gacBrowser.setInput(dummyInput);
					}
				}
			}
		});
		
		return tabFolder;
	}

	private TableViewer createGACBrowser(Composite tabFolder) {
		TableViewer viewer = new TableViewer(tabFolder, SWT.FULL_SELECTION);
		
		Table table = viewer.getTable();
		
		addColumn(table, "Name", 150);
		addColumn(table, "Version", 150);
		addColumn(table, "Public Key", 100);
		
		table.setHeaderVisible(true);
		
		viewer.setContentProvider(new GACContentProvider());
		viewer.setLabelProvider(new GACLabelProvider());
		viewer.addDoubleClickListener(_doubleClickListener);

		return viewer;
	}

	private void addColumn(Table table, String text, int width) {
		TableColumn columnName = new TableColumn(table, SWT.LEFT);
		columnName.setText(text);		
		columnName.setWidth(width);
	}
	
	IDoubleClickListener _doubleClickListener = new IDoubleClickListener() {
		public void doubleClick(DoubleClickEvent event) {
			Object element = ((IStructuredSelection)event.getSelection()).getFirstElement();			
			IAssemblyReference reference = getReference(element);
			if (null == reference) return;
			
			_references.add(reference);
			_selectedViewer.refresh(true);
		}

		private IAssemblyReference getReference(Object element) {
			if (element instanceof IAssemblyReference) return (IAssemblyReference)element;
			return (IAssemblyReference) Platform.getAdapterManager().getAdapter(element, IAssemblyReference.class);
		}
	};
	
	private Control createAssemblySourceBrowser(Composite parent) {
		TreeViewer viewer = createResourceBrowser(parent);
		viewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				try {
					if (element instanceof IFolder) {
						return isOrContainsAssemblySource((IFolder)element);
					}
					
				} catch (CoreException e) {
					dumpError(e);
				}
				return false;
			}
		});
		viewer.expandAll();
		return viewer.getControl();
	}

	private Control createLocalReferenceBrowser(Composite parent) {
		TreeViewer viewer = createResourceBrowser(parent);
		
		viewer.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				try {
					if (element instanceof IFolder) {
						return containsLibrary((IFolder)element);
					}
					if (element instanceof IFile) {
						return isLibrary((IFile)element);
					}
				} catch (CoreException e) {
					dumpError(e);
				}
				return false;
			}
		});
		viewer.expandAll();
		return viewer.getControl();
	}

	private TreeViewer createResourceBrowser(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.FILL);
		viewer.setContentProvider(new WorkbenchContentProvider());
		viewer.setLabelProvider(new BooExplorerLabelProvider());
		viewer.setInput(getAssemblySource().getFolder().getProject());
		
		viewer.addDoubleClickListener(_doubleClickListener);
		
		ResourcePatternFilter resourcePatternFilter = new ResourcePatternFilter();
		resourcePatternFilter.setPatterns(new String[] { ".*" });
		viewer.addFilter(resourcePatternFilter);
		viewer.addFilter(DerivedResourceFilter.DEFAULT);
		return viewer;
	}

	protected boolean isLibrary(IFile file) {
		String extension = file.getFileExtension();
		return null == extension
			? false
			: (extension.equals("dll") || extension.equals("exe"));
	}

	protected boolean containsLibrary(IFolder folder) throws CoreException {
		final boolean[] result = new boolean[] { false };
		folder.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (IResource.FILE == resource.getType()
					&& isLibrary((IFile)resource)) {
					result[0] = true;
					return false;
				}
				return true;
			}
		});
		
		return result[0];
	}
	
	protected boolean isOrContainsAssemblySource(IFolder folder) throws CoreException {
		if (isValidAssemblySourceReference(folder)) return true;
		final boolean[] result = new boolean[] { false };
		folder.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (IResource.FOLDER == resource.getType()
					&& isValidAssemblySourceReference((IFolder) resource)) {
					result[0] = true;
					return false;
				}
				return true;
			}
		});
		
		return result[0];
	}

	private boolean isValidAssemblySourceReference(IFolder folder) throws CoreException {
		IAssemblySource source = BooCore.getAssemblySource(folder);
		return null == source
			? false
			: source != getAssemblySource(); 
	}

	private IAssemblySource getAssemblySource() {
		return (IAssemblySource)getElement().getAdapter(IAssemblySource.class);
	}

}