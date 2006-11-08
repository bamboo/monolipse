package monolipse.core.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import monolipse.core.IAssemblyReference;
import monolipse.core.IAssemblySource;

public class BooAssemblyReference {
	
	private static final QualifiedName SESSION_KEY = new QualifiedName("monolipse.core.resources", "BooAssemblyReference");
	
	private BooAssemblyReference() {
	}
	
	public static IAssemblyReference get(IFile file) throws CoreException {
		IAssemblyReference reference = getCachedReference(file);
		if (null == reference) {
			reference = new LocalAssemblyReference(file);
			cacheReference(file, reference);
		}
		return reference;
	}

	public static IAssemblyReference get(IAssemblySource source) throws CoreException {
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
}
