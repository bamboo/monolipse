package monolipse.ui.tests;


import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Path;


public class BooBuilderTestCase extends AbstractBooTestCase {
	
	private IAssemblySource _assemblySource;

	protected void setUp() throws Exception {
		super.setUp();
		
		IFile file = copyResourceTo("TestClass.dll", "lib");
		IAssemblyReference reference = BooCore.createAssemblyReference(file);
		_assemblySource = _booProject.addAssemblySource(new Path("src/Test"));
		_assemblySource.setReferences(reference);
		copyResourceTo("Program.boo", "src/Test");
		build();
	}
	
	public void testBuild() throws Exception {
		
		assertTrue(_assemblySource.getOutputFile().exists());
		assertTrue(_assemblySource.getOutputFile().isDerived());
		
		IFile copiedRef = getFile("bin/TestClass.dll");
		assertTrue("referenced local files must be copied to the output folder", copiedRef.exists());
		assertTrue("copied references must be marked as derived files", copiedRef.isDerived());
	}
	
	public void testClean() throws Exception {
		_project.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		
		assertFalse(_assemblySource.getOutputFile().exists());
		assertFalse(getFile("bin/TestClass.dll").exists());
	}
	
	public void testAssemblySourceBuildOrder() throws Exception {
		IAssemblySource bar = addAssemblySource("src/Bar");
		bar.setOutputType(IAssemblySource.OutputType.LIBRARY);
		
		IAssemblySource foo = addAssemblySource("src/Foo");

		foo.setReferences(new IAssemblyReference[] { BooCore.createAssemblyReference(bar) });
		
		copyResourceTo("Bar.boo", "src/Bar");
		copyResourceTo("Foo.boo", "src/Foo");
		
		build();
		assertTrue(foo.getOutputFile().exists());
		assertTrue(bar.getOutputFile().exists());
	}
	
	public void testConfigFilesAreCopiedToOutputFolder() throws Exception {
		copyResourceTo("Test.exe.config", "src/Test");
		build();
		
		assertTrue(_assemblySource.getOutputFile().getParent().exists(new Path("./Test.exe.config")));
	}


}
