package monolipse.ui.wizards;

import monolipse.core.AssemblySourceLanguage;
import monolipse.core.BooCore;
import monolipse.core.IMonoProject;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewAssemblySourceWizard extends Wizard implements INewWizard {

	private final AssemblySourceLanguage _language;
	private NewBooAssemblySourceWizardPage _mainPage;
	private IContainer _selection;

	public NewAssemblySourceWizard(AssemblySourceLanguage language) {
		_language = language;
	}

	public boolean performFinish() {
		IWorkspaceRunnable action = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IMonoProject booProject = BooCore.createProject(_selection.getProject(), monitor);
				
				IPath containerPath = _selection.getProjectRelativePath();
				booProject.addAssemblySource(containerPath.append(_mainPage.getName()), _language);
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(action, null);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Boo Source Folder");
		_selection = (IContainer)selection.getFirstElement();
	}
	
	public void addPages() {
		super.addPages();
		
		_mainPage = new NewBooAssemblySourceWizardPage();
		addPage(_mainPage);
	}

}
