package monolipse.ui.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.*;

public class DerivedResourceFilter extends ViewerFilter {
	
	public static final DerivedResourceFilter DEFAULT = new DerivedResourceFilter();
	
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IResource) {
			return !((IResource)element).isDerived();
		}
		return true;
	}
}