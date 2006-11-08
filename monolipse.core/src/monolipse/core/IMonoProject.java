package monolipse.core;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public interface IMonoProject {

	IAssemblySource addAssemblySource(IPath path)
			throws CoreException;

	IAssemblySource[] getAssemblySources()
			throws CoreException;

	IAssemblySource[] getAffectedAssemblySources(
			IResourceDelta delta) throws CoreException;

	IAssemblySource[] getAssemblySourceOrder(IAssemblySource[] sources) throws CoreException;

}