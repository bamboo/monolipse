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

package monolipse.ui.resources;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.AssemblySourceLanguage;
import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.ui.BooUI;
import monolipse.ui.views.BooExplorerLabelProvider;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.model.WorkbenchContentProvider;


public class BooAssemblySourcePropertyPage extends PreferencePage implements IWorkbenchPropertyPage {

	private IAdaptable _element;
	private Button[] _outputTypeButtons;
	private Button[] _languageButtons;
	private Text _outputPath;
	private Text _additionalOptions;

	public BooAssemblySourcePropertyPage() {
	}

	protected void performDefaults() {
	}
	
	public boolean performOk() {
		IAssemblySource source = getAssemblySource();
		if (null == source) return false;
		
		try {
			source.setOutputType(getSelectedOutputType());
			source.setLanguage(getSelectedLanguage());
			source.setOutputFolder(getOutputFolder());
			source.setAdditionalOptions(getAdditionalOptions());
			source.save(null);
		} catch (CoreException e) {
			BooUI.logException(e);
			return false;
		}
		
		return true;
	}

	private String getAdditionalOptions() {
		return _additionalOptions.getText();
	}

	private AssemblySourceLanguage getSelectedLanguage() {
		return (AssemblySourceLanguage) getSelectedButtonData(_languageButtons);
	}

	private String getSelectedOutputType() {
		return (String) getSelectedButtonData(_outputTypeButtons);
	}

	private Object getSelectedButtonData(Button[] buttons) {
		for (int i = 0; i < buttons.length; i++) {
			Button button = buttons[i];
			if (button.getSelection()) {
				return button.getData();
			}
		}
		return null;
	}

	private IAssemblySource getAssemblySource() {
		try {
			return BooCore.getAssemblySource((IFolder)_element);
		} catch (CoreException e) {
			BooUI.logException(e);
			setErrorMessage(e.getLocalizedMessage());
		}
		return null;
	}

	public IAdaptable getElement() {
		return _element;
	}

	public void setElement(IAdaptable element) {
		_element = element;
	}

	protected Control createContents(Composite parent) {
		IAssemblySource source = getAssemblySource();
		if (null == source) return null;
	    
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new RowLayout(SWT.VERTICAL));
		
		
		final String[] data = new String[] {
			    	IAssemblySource.OutputType.CONSOLE_APPLICATION,
			    	IAssemblySource.OutputType.WINDOWS_APPLICATION,
			    	IAssemblySource.OutputType.LIBRARY,
			    };
		final String[] labels = new String[] {
			    	"Console Application",
			    	"Windows Application",
			    	"Library",
			    };
		_outputTypeButtons = createButtonGroup(composite, "Output Type", labels, data, source.getOutputType());
		
		Object[] sourceLanguages = AssemblySourceLanguage.values();
		String[] languageLabels = new String[] { 
				"boojay",
				"boo",
				"c#",
				"c# 1.1 (no generics)",
		};
		_languageButtons = createButtonGroup(composite, "Language", languageLabels, sourceLanguages, source.getLanguage());
		
		createOutputFolderGroup(composite, source);
		
		createAdditionalOptionsGroup(composite, source);
		
		return composite;
	}

	private void createAdditionalOptionsGroup(Composite composite,
			IAssemblySource source) {
		
		Group group = createGroup(composite, "Additional Compiler Options: ");
		_additionalOptions = new Text(group, SWT.BORDER);
		_additionalOptions.setText(source.getAdditionalOptions());
		setTextSize(_additionalOptions, 20, 1);
	}

	private void setTextSize(Text text, int columns, int rows) {
		
	    GC gc = new GC(text);
	    FontMetrics fm = gc.getFontMetrics();
	    int width = columns * fm.getAverageCharWidth();
	    int height = rows * fm.getHeight();
	    gc.dispose();
	    text.setSize(text.computeSize(width, height));
	}

	private void createOutputFolderGroup(Composite composite, final IAssemblySource source) {
		
		Group group = createGroup(composite, "Output Folder: ");
	    
		_outputPath = new Text(group, SWT.NONE);
		_outputPath.setEditable(false);
		setOutputFolder(source.getOutputFile().getParent());
		
		Button button = new Button(group, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				FolderSelectionDialog dlg = new FolderSelectionDialog(getShell(), new BooExplorerLabelProvider(), new WorkbenchContentProvider());
				dlg.setTitle("Select Output Location: ");
				dlg.setInput(WorkspaceUtilities.getWorkspaceRoot());
				dlg.setInitialSelection(getOutputFolder());
				dlg.addFilter(new ViewerFilter() {
					public boolean select(Viewer viewer, Object parentElement, Object element) {
						return element instanceof IContainer;
					}
				});
				if (FolderSelectionDialog.OK == dlg.open())
				{
					setOutputFolder((IContainer) dlg.getFirstResult());
				}
			}
		});
	}

	private void setOutputFolder(IContainer outputFolder) {
		String path = outputFolder.getFullPath().toPortableString();
		_outputPath.setText(path);
		_outputPath.setData(outputFolder);
		_outputPath.setToolTipText(path);
		_outputPath.pack();
	}
	
	private IFolder getOutputFolder() {
		return (IFolder)_outputPath.getData();
	}

	private Button[] createButtonGroup(Composite parent, String groupLabel, String[] labels, Object[] data, Object selectedItem) {
		Group group = createGroup(parent, groupLabel);
	    
	    Button[] buttons = new Button[data.length];
	    for (int i=0; i<data.length; ++i) {
	    	Button button = new Button(group, SWT.RADIO);
	    	button.setText(labels[i]);
	    	button.setData(data[i]);
	    	if (data[i].equals(selectedItem)) {
	    		button.setSelection(true);
	    	}
	    	buttons[i] = button;
	    }
	    return buttons;
	}

	private Group createGroup(Composite parent, String groupLabel) {
		Group group = new Group(parent, SWT.SHADOW_IN);
	    group.setText(groupLabel);
	    group.setLayout(new RowLayout(SWT.VERTICAL));
		return group;
	}

}