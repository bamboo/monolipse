package monolipse.ui.views;

import monolipse.core.IAssemblySource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;


public class ReferenceContainer implements IAdaptable {

	private IAssemblySource _source;

	ReferenceContainer(IAssemblySource source) {
		_source = source;
	}

	public Object getAdapter(Class adapter) {
		if (IAssemblySource.class == adapter) return _source;
		return null;
	}

	public boolean hasChildren() throws CoreException {
		return _source.getReferences().length > 0;
	}

	public Object[] getChildren() throws CoreException {
		return _source.getReferences();
	}

}
