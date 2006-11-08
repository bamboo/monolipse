package monolipse.core.foundation;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

import monolipse.core.BooCore;


public class WorkspaceUtilities {
	public static void createTree(IFolder folder) throws CoreException {
		IContainer parent = folder.getParent();
		if (!parent.exists()) {
			createTree((IFolder)parent);
		}
		if (!folder.exists()) {
			folder.create(true, true, null);
		}
	}

	public static void ensureDerivedParentExists(IFile file) throws CoreException {
		IContainer parent = file.getParent();
		if (IResource.FOLDER == parent.getType()) {
			createTree((IFolder)parent);
			parent.setDerived(true);
		}
	}
	
	public static String getLocation(IResource resource) {
		return resource.getLocation().toOSString();
	}
	
	public static IFolder getFolder(String path) {
		return getWorkspaceRoot().getFolder(new Path(path));
	}
	
	public static IFile getFile(String path) {
		return getWorkspaceRoot().getFile(new Path(path));
	}
	
	public static IFile getFileForLocation(String path) {
		return getWorkspaceRoot().getFileForLocation(new Path(path));
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static void throwCoreException(IOException e) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, BooCore.ID_PLUGIN, -1, e.getLocalizedMessage(), e));
	}

	public static String getPortablePath(IResource resource) {
		return resource.getFullPath().toPortableString();
	}

	public static String getResourceLocalPath(Bundle bundle, String resourcePath) throws IOException {
		URL url = FileLocator.find(bundle, new Path(resourcePath), null);
		return new File(FileLocator.toFileURL(url).getFile()).getCanonicalPath();
	}
}
