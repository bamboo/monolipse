package monolipse.ui.tests;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;
import monolipse.core.AssemblySourceLanguage;

import org.eclipse.core.runtime.Path;


public class BooAssemblySourceTestCase extends AbstractBooTestCase {
	
	IAssemblySource _assemblySource;
	
	protected void setUp() throws Exception {
		super.setUp();
		_assemblySource = addAssemblySource(new Path("src/Test"));
	}
	
	public void testDefaultReferences() throws Exception {
		IAssemblyReference[] references = _assemblySource.getReferences();
		assertNotNull(references);
		assertEquals(0, references.length);
	}
	
	public void testDefaultOutputType() throws Exception {
		assertEquals(IAssemblySource.OutputType.CONSOLE_APPLICATION, _assemblySource.getOutputType());
	}
	
	public void testOutputFile() throws Exception {
		
		_assemblySource.setLanguage(AssemblySourceLanguage.BOO);
		
		assertEquals(getFile("bin/Test.exe"), _assemblySource.getOutputFile());
		
		_assemblySource.setOutputType(IAssemblySource.OutputType.LIBRARY);
		assertEquals(getFile("bin/Test.dll"), _assemblySource.getOutputFile());
	}
}
