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

import java.lang.reflect.InvocationTargetException;

import monolipse.unity.Activator;
import monolipse.unity.builder.UnityNature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ImportUnityProjectWizardPage extends WizardPage {
	
	protected DirectoryFieldEditor editor;

	public ImportUnityProjectWizardPage(String title) {
		super(title);
		setDescription("Import a Unity project into the workspace");
	}

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
		
		editor = new DirectoryFieldEditor("Unity Project", "Select Unity Project: ", projectSelectionArea);
		editor.getTextControl(projectSelectionArea).addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				boolean isUnityProject = getSelectedProjectPath().append("Assets").toFile().exists();
				setPageComplete(isUnityProject);
			}
		});
		projectSelectionArea.moveAbove(null);
		
		setControl(projectSelectionArea);
		
		setPageComplete(false);
	}

	public boolean importProject() {
		
		final Path selectedProjectPath = getSelectedProjectPath();
		
		return runImportOperation(new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException, CoreException {
				try {
					monitor.beginTask("", 1);
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					importProject(selectedProjectPath, monitor);
				} finally {
					monitor.done();
				}
			}
		});
	}

	protected void importProject(Path projectPath, IProgressMonitor monitor) throws CoreException {
		String projectName = projectPath.lastSegment();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		
		final IProjectDescription description = workspace.newProjectDescription(projectName);
		description.setLocation(projectPath);
		description.setNatureIds(new String[] { UnityNature.NATURE_ID });
		
		try {
			monitor.beginTask("Importing project", 100);
			project.create(description, new SubProgressMonitor(monitor, 30));
			project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 70));
		} finally {
			monitor.done();
		}
	}
	
	private boolean runImportOperation(WorkspaceModifyOperation op) {
		try {
			getContainer().run(true, true, op);
			return true;
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			String message = "Failed to import Unity project";
			ErrorDialog.openError(getShell(), message, null, errorStatusFor(message, e.getTargetException()));
			return false;
		}
	}

	private IStatus errorStatusFor(String message, Throwable t) {
		return t instanceof CoreException
			? ((CoreException) t).getStatus()
			: new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, message, t);
	}

	private Path getSelectedProjectPath() {
		return new Path(editor.getStringValue());
	}
}
