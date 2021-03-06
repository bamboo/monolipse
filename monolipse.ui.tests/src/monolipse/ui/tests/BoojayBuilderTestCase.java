package monolipse.ui.tests;

import monolipse.core.*;
import monolipse.core.internal.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.junit.*;

public class BoojayBuilderTestCase extends AbstractBooTestCase {

	private static final String SRC_FOLDER = "src/BoojayTest";
	
	private IAssemblySource _assemblySource;

	protected void setUp() throws Exception {
		super.setUp();
		_assemblySource = _booProject.addAssemblySource(new Path(SRC_FOLDER), AssemblySourceLanguage.BOOJAY);
	}
	
	public void testBuildClassesToTheProjectOutputFolder() throws Exception {
		addResourceAndBuild("Boojay.boo");
		assertNoErrorsOn(_assemblySource);
		assertEquals(javaOutputFolderPath(), _assemblySource.getOutputFolder().getFullPath());
	}
	
	private IPath javaOutputFolderPath() throws CoreException {
		return javaProject().getOutputLocation();
	}

	private IJavaProject javaProject() {
		return JavaCore.create(getProject());
	}

	public void testBuildErrorsCauseMarkers() throws Exception {
		addResourceAndBuild("BoojayWithErrors.boo");
		
		final IFile[] sourceFiles = _assemblySource.getSourceFiles();
		assertEquals(1, sourceFiles.length);
		
		final IMarker[] problems = BooMarkers.booProblemsOn(sourceFiles[0]);
		assertEquals(1, problems.length);
		assertProblemLineNumberAndMessage(problems[0], 1, "Unknown identifier: 'p'.");
	}

	private void assertProblemLineNumberAndMessage(IMarker marker, int lineNumber, String message) {
		assertEquals(lineNumber, marker.getAttribute(IMarker.LINE_NUMBER, -1));
		assertEquals(message, marker.getAttribute(IMarker.MESSAGE, ""));
	}

	private void assertOutputFile(final String expectedFileName) throws CoreException {
		assertEquals(expectedFileName, _assemblySource.getOutputFile().getName());
		
		IContainer outputFolder = outputFolder();
		outputFolder.refreshLocal(IResource.DEPTH_ONE, null);
		final IFile expectedFile = outputFolder.getFile(new Path(expectedFileName));
		assertTrue(expectedFile.exists());
	}
	
	private void addResourceAndBuild(final String resource) throws Exception,
			CoreException {
		copyResourceTo(resource, SRC_FOLDER);
		build();
	}

	private IContainer outputFolder() throws CoreException {
		return _assemblySource.getOutputFolder();
	}
}
