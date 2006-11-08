package monolipse.ui.views;

import monolipse.core.BooCore;
import monolipse.core.IAssemblySource;
import monolipse.core.foundation.ArrayUtilities;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.model.WorkbenchContentProvider;


class BooExplorerContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {
	ITreeContentProvider _delegate = new WorkbenchContentProvider();

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		_delegate.inputChanged(v, oldInput, newInput);
	}

	public void dispose() {
		_delegate.dispose();
	}

	public Object getParent(Object element) {
		return _delegate.getParent(element);
	}

	public boolean hasChildren(Object parentElement) {
		if (BooCore.isAssemblySource(parentElement)) {
			return true;
		}
		if (parentElement instanceof ReferenceContainer) {
			try {
				return ((ReferenceContainer)parentElement).hasChildren();
			} catch (CoreException e) {
				e.printStackTrace();
				return false;
			}
		}
		return _delegate.hasChildren(parentElement);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ReferenceContainer) {
			return getReferenceContainerChildren(parentElement);
		}
		IAssemblySource source = getAssemblySource(parentElement);
		if (null != source) {
			return getAssemblySourceChildren(source);
		}
		return _delegate.getChildren(parentElement);
	}

	private IAssemblySource getAssemblySource(Object parentElement) {
		if (parentElement instanceof IFolder) {
			try {
				return BooCore.getAssemblySource((IFolder) parentElement);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private Object[] getAssemblySourceChildren(IAssemblySource assemblySource) {
		Object[] children = _delegate.getChildren(assemblySource.getFolder());		
		return ArrayUtilities.prepend(Object.class, children, new ReferenceContainer(assemblySource));
	}

	private Object[] getReferenceContainerChildren(Object parentElement) {
		try {
			return ((ReferenceContainer)parentElement).getChildren();
		} catch (CoreException e) {
			e.printStackTrace();
			return new Object[0];
		}
	}
}
