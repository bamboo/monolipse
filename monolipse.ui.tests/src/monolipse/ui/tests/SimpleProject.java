package monolipse.ui.tests;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Rodrigo B. de Oliveira
 *  
 */
public class SimpleProject {

	protected IProject _project;

	public SimpleProject(String name) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		_project = root.getProject(name);
		_project.create(null);
		_project.open(null);
	}

	public IFolder createFolder(String name) throws CoreException {
		IFolder folder = _project.getFolder(name);
		folder.create(false, true, null);
		return folder;
	}

	/**
	 * @return Returns the project.
	 */
	public IProject getProject() {
		return _project;
	}
	
	public void dispose() throws CoreException {
		_project.delete(true, true, null);
	}

}