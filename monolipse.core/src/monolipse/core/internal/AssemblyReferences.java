package monolipse.core.internal;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;


public class AssemblyReferences {
	
	private static final QualifiedName SESSION_KEY = new QualifiedName("monolipse.core.resources", "BooAssemblyReference");
	
	private AssemblyReferences() {
	}
	
	public static IAssemblyReference assemblyReferenceFor(IFile file) throws CoreException {
		if (!file.exists())
			return new LocalAssemblyReference(file);
		
		IAssemblyReference reference = getCachedReference(file);
		if (null == reference) {
			reference = new LocalAssemblyReference(file);
			cacheReference(file, reference);
		}
		return reference;
	}

	public static IAssemblyReference assemblyReferenceFor(IAssemblySource source) throws CoreException {
		IFolder folder = source.getFolder();
		IAssemblyReference reference = getCachedReference(folder);
		if (null == reference) {
			reference = new AssemblySourceReference(source);
			cacheReference(folder, reference);
		}
		return reference;
	}
	
	private static IAssemblyReference getCachedReference(IResource resource) throws CoreException {
		return (IAssemblyReference)resource.getSessionProperty(SESSION_KEY);
	}
	
	private static void cacheReference(IResource resource, IAssemblyReference reference) throws CoreException {
		resource.setSessionProperty(SESSION_KEY, reference);
	}

	public static IAssemblyReference booAssemblyReference(String booAssemblyName) {
		return new BooAssemblyReference(booAssemblyName);
	}
}
