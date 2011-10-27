package monolipse.ui.wizards;

import monolipse.core.BooCore;
import monolipse.core.IMonoProject;
import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class NewBooProjectWizard extends Wizard implements INewWizard {

	private WizardNewProjectCreationPage _projectPage;

	public boolean performFinish() {
		try {
			ResourcesPlugin.getWorkspace().run(projectCreation(), null);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private IWorkspaceRunnable projectCreation() {
		return new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IProject project = _projectPage.getProjectHandle();
				project.create(monitor);
				project.open(monitor);
				
				IMonoProject booProject = BooCore.createProject(project, monitor);
				booProject.addAssemblySource(new Path("src").append(project.getName()));
			}
		};
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setDefaultPageImageDescriptor(BooUI.getImageDescriptor(IBooUIConstants.PROJECT));
		setWindowTitle("New Boo Project");
	}
	
	public void addPages() {
		super.addPages();
		
		_projectPage = new WizardNewProjectCreationPage("basicNewProjectPage");
		_projectPage.setTitle("Create a Boo project");
		_projectPage.setDescription("Create a new Boo project in the workspace or in a external location");
		addPage(_projectPage);
	}

}
