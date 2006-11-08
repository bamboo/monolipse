package monolipse.core;

import org.eclipse.core.runtime.CoreException;

public interface IAssemblyReferenceVisitor {
	boolean visit(ILocalAssemblyReference reference) throws CoreException;
	boolean visit(IGlobalAssemblyCacheReference reference) throws CoreException;
	boolean visit(IAssemblySourceReference reference) throws CoreException;
}
