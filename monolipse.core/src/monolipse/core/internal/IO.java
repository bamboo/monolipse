package monolipse.core.internal;

import monolipse.core.BooCore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class IO {

	static void copyFileToFolder(IFile sourceFile, IFolder folder, IProgressMonitor monitor) throws CoreException {
		String name = sourceFile.getName();
		IFile targetFile = folder.getFile(name);
		
		BooCore.logInfo("Copying {0} to {1}...", sourceFile.getFullPath(), targetFile.getFullPath());
		
		if (targetFile.exists()) {
			if (!isNewer(sourceFile, targetFile)) return;
			targetFile.delete(true, monitor);
		}
		sourceFile.copy(targetFile.getFullPath(), true, monitor);
		targetFile.setDerived(true);
	}

	static boolean isNewer(IFile sourceFile, IFile targetFile) {
		return sourceFile.getModificationStamp() > targetFile.getModificationStamp();
	}

}
