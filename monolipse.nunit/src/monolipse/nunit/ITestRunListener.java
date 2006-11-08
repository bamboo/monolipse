package monolipse.nunit;

import monolipse.core.IAssemblySource;

public interface ITestRunListener {
	
	void testsStarted(IAssemblySource source, int testCount);
	
	void testsFinished(IAssemblySource source);
	
	void testStarted(IAssemblySource source, String fullName);
	
	void testFailed(IAssemblySource source, String fullName, String trace);
}