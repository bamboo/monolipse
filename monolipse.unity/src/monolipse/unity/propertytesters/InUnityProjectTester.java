package monolipse.unity.propertytesters;

import monolipse.unity.builder.UnityNature;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class InUnityProjectTester extends PropertyTester {
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return inUnityProject((IResource)receiver) == (Boolean)expectedValue;
	}

	private boolean inUnityProject(IResource resource) {
		try {
			return resource.getProject().hasNature(UnityNature.NATURE_ID);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
	}
}
