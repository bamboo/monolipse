package monolipse.core.foundation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class JavaModelUtilities {

	public static List<IClasspathEntry> sourceFoldersFor(IJavaProject javaProject)
			throws JavaModelException {
		List<IClasspathEntry> sourceFolders = new ArrayList<IClasspathEntry>();
		IClasspathEntry[] resolvedClasspath = javaProject.getResolvedClasspath(true);
		for (IClasspathEntry entry : resolvedClasspath) {
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
				sourceFolders.add(entry);
		}
		return sourceFolders;
	}

	public static boolean exclusionPatternsContains(String pattern, IPath[] exclusionPatterns) {
		for (IPath path : exclusionPatterns)
			if (path.equals(new Path(pattern)))
				return true;
		return false;
	}

}
