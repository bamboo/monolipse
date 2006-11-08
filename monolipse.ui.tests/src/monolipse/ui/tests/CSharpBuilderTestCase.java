package monolipse.ui.tests;

import monolipse.core.IAssemblySource;
import monolipse.core.IAssemblySourceLanguage;

import org.eclipse.core.runtime.Path;


public class CSharpBuilderTestCase extends AbstractBooTestCase {

	private IAssemblySource _assemblySource;

	protected void setUp() throws Exception {
		super.setUp();
		
		_assemblySource = _booProject.addAssemblySource(new Path("src/CSharp"));
		_assemblySource.setOutputType(IAssemblySource.OutputType.LIBRARY);
		_assemblySource.setLanguage(IAssemblySourceLanguage.CSHARP);
		copyResourceTo("TestClass.cs", "src/CSharp");
		build();
	}
	
	public void testBuild() throws Exception {
		
		assertTrue(_assemblySource.getOutputFile().exists());
		assertTrue(_assemblySource.getOutputFile().isDerived());
		
	}
}
