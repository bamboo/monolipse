package monolipse.core;

import org.eclipse.core.runtime.CoreException;


public class AssemblyReferenceVisitor implements
		IAssemblyReferenceVisitor {
	
	public boolean visit(ILocalAssemblyReference reference) throws CoreException {
		return true;
	}

	public boolean visit(IGlobalAssemblyCacheReference reference) throws CoreException {
		return true;
	}

	public boolean visit(IAssemblySourceReference reference) throws CoreException {
		return true;
	}

}
