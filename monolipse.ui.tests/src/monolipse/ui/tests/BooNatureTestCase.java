package monolipse.ui.tests;

import java.util.List;

import monolipse.core.foundation.JavaModelUtilities;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class BooNatureTestCase extends AbstractBooTestCase {
	
	public void testImpliesJavaNature() throws CoreException {
		assertTrue(getProject().hasNature(JavaCore.NATURE_ID));
	}
	
	public void testBooAndMonolipseResourcesAreExcludedFromSourceFolder() throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(getProject());
		List<IClasspathEntry> sourceFolders = sourceFoldersFor(javaProject);
		assertEquals(1, sourceFolders.size());
		
		IPath[] exclusionPatterns = sourceFolders.get(0).getExclusionPatterns();
		assertTrue(exclusionPatternsContains("**/.monolipse", exclusionPatterns));
		assertTrue(exclusionPatternsContains("**/*.boo", exclusionPatterns));
	}

	private static boolean exclusionPatternsContains(String pattern, IPath[] exclusionPatterns) {
		return JavaModelUtilities.exclusionPatternsContains(pattern, exclusionPatterns);
	}

	private static List<IClasspathEntry> sourceFoldersFor(IJavaProject javaProject) throws JavaModelException {
		return JavaModelUtilities.sourceFoldersFor(javaProject);
	}
}
