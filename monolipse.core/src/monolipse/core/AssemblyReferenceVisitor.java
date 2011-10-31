package monolipse.core;

import org.eclipse.core.runtime.CoreException;


public class AssemblyReferenceVisitor implements
		IAssemblyReferenceVisitor {
	
	@Override
	public boolean visit(ILocalAssemblyReference reference) throws CoreException {
		return true;
	}

	@Override
	public boolean visit(IGlobalAssemblyCacheReference reference) throws CoreException {
		return true;
	}

	@Override
	public boolean visit(IAssemblySourceReference reference) throws CoreException {
		return true;
	}

	@Override
	public boolean visit(IBooAssemblyReference reference) throws CoreException {
		return true;
	}

}
