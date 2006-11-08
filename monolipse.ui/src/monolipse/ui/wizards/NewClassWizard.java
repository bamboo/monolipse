package monolipse.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;

public class NewClassWizard extends Wizard implements INewWizard {
	private NewClassWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for NewClassWizard.
	 */
	public NewClassWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new NewClassWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String className = page.getClassName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, className, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(
		String containerName,
		String className,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + className, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(className + ".boo"));
		try {
			InputStream stream = openContentStream(calcNamespace(file), className);
			try {
				file.create(stream, true, monitor);
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
					e.printStackTrace();
				}
			}
		});
		monitor.worked(1);
	}
	
	private String calcNamespace(IResource resource) {
		IAssemblySource source = BooCore.getAssemblySourceContainer(resource);
		if (null != source) {
			IFolder folder = source.getFolder();
			String parentFullPath = folder.getParent().getFullPath().toPortableString();
			String resourceFullPath = resource.getParent().getFullPath().toPortableString();
			return resourceFullPath.substring(parentFullPath.length()+1).replace('/', '.');
		}
		return null;
	}

	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream(String ns, String className) {
		StringBuffer buffer = new StringBuffer();
		if (null != ns && ns.length() > 0) {
			buffer.append("namespace ");
			buffer.append(ns);
			buffer.append("\n\n");
		}
		buffer.append("class ");
		buffer.append(className);
		buffer.append(":\n\tpass");
		return new ByteArrayInputStream(buffer.toString().getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "monolipse.ui", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}