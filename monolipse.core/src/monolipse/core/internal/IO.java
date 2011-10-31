package monolipse.core.internal;

import java.io.FileInputStream;
import java.io.IOException;

import monolipse.core.BooCore;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class IO {

	static void copyFileToFolder(IFile sourceFile, IFolder folder, IProgressMonitor monitor) throws CoreException {
		IPath fullPath = sourceFile.getFullPath();
		
		String name = fullPath.lastSegment();
		IFile targetFile = folder.getFile(name);
		
		BooCore.logInfo("Copying {0} to {1}...", fullPath, targetFile.getFullPath());
		
		if (targetFile.exists()) {
			if (!isNewer(sourceFile, targetFile)) return;
			targetFile.delete(true, monitor);
		}
		sourceFile.copy(targetFile.getFullPath(), true, monitor);
		targetFile.setDerived(true, monitor);
	}

	static boolean isNewer(IFile sourceFile, IFile targetFile) {
		return sourceFile.getModificationStamp() > targetFile.getModificationStamp();
	}

	public static boolean existsFile(String file) {
		return new java.io.File(file).exists();
	}

	public static void copyFile(String srcFile, IFile targetFile, IProgressMonitor monitor) throws IOException, CoreException {
		FileInputStream fis = new FileInputStream(srcFile);
		try {
			if (targetFile.exists())
				targetFile.setContents(fis, true, false, monitor);
			else
				targetFile.create(fis, true, monitor);
			targetFile.setDerived(true, monitor);
		} finally {
			fis.close();
		}
	}

}
