package monolipse.core.internal;

import monolipse.core.BooCore;
import monolipse.core.IAssemblyReference;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;


public class BooAssemblyReferenceAdapterFactory implements IAdapterFactory {

	public Object getAdapter(Object adaptableObject, @SuppressWarnings("rawtypes") Class adapterType) {
		IFile file = (IFile)adaptableObject;
		if (!file.exists()) return null;
		try {
			return AssemblyReferences.assemblyReferenceFor(file);
		} catch (CoreException e) {
			BooCore.logException(e);
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IAssemblyReference.class };
	}

}
