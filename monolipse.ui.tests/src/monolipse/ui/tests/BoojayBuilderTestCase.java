package monolipse.ui.tests;

import monolipse.core.IAssemblySource;
import monolipse.core.IAssemblySourceLanguage;
import monolipse.core.internal.BooBuilder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;


public class BoojayBuilderTestCase extends AbstractBooTestCase {

	private IAssemblySource _assemblySource;

	protected void setUp() throws Exception {
		super.setUp();
		_assemblySource = _booProject.addAssemblySource(new Path("src/Java"));
		_assemblySource.setLanguage(IAssemblySourceLanguage.BOOJAY);
	}

	public void testBuild() throws Exception {
		addResourceAndBuild("Boojay.boo");
		assertOutputFile("BoojayModule.class");
	}
	
	public void testBuildErrorsCauseMarkers() throws Exception {
		addResourceAndBuild("BoojayWithErrors.boo");
		
		final IFile[] sourceFiles = _assemblySource.getSourceFiles();
		assertEquals(1, sourceFiles.length);
		
		final IMarker[] problems = booProblemsOn(sourceFiles[0]);
		assertEquals(1, problems.length);
		assertProblemLineNumberAndMessage(problems[0], 1, "Unknown identifier: 'p'.");
	}

	private void assertProblemLineNumberAndMessage(IMarker marker, int lineNumber, String message) {
		assertEquals(lineNumber, marker.getAttribute(IMarker.LINE_NUMBER, -1));
		assertEquals(message, marker.getAttribute(IMarker.MESSAGE, ""));
	}

	private IMarker[] booProblemsOn(final IFile file) throws CoreException {
		return file.findMarkers(BooBuilder.BOO_PROBLEM_MARKER_TYPE, true, IResource.DEPTH_ZERO);
	}

	private void assertOutputFile(final String expectedFile) {
		final IFile classFile = outputFolder().getFile(new Path(expectedFile));
		assertTrue(classFile.exists());
	}
	
	private void addResourceAndBuild(final String resource) throws Exception,
			CoreException {
		copyResourceTo(resource, "src/Java");
		build();
	}

	private IContainer outputFolder() {
		return _assemblySource.getOutputFile().getParent();
	}
}
