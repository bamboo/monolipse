package monolipse.ui.tests;

import monolipse.core.IAssemblySource;
import monolipse.core.AssemblySourceLanguage;

import org.eclipse.core.runtime.Path;

public class CSharpBuilderTestCase extends AbstractBooTestCase {

	private IAssemblySource _assemblySource;

	protected void setUp() throws Exception {
		super.setUp();
		
		_assemblySource = _booProject.addAssemblySource(new Path("src/CSharp"), AssemblySourceLanguage.CSHARP);
		_assemblySource.setOutputType(IAssemblySource.OutputType.LIBRARY);
	}
	
	public void testBuild() throws Exception {
		
		copyResourceTo("TestClass.cs", "src/CSharp");
		build();
		
		assertTrue(_assemblySource.getOutputFile().exists());
		assertTrue(_assemblySource.getOutputFile().isDerived());
	}
}
