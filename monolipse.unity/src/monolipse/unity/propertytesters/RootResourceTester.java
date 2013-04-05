package monolipse.unity.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

public class RootResourceTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IResource resource = (IResource)receiver;
		boolean value = resource.getParent() == resource.getProject();
		return value == (Boolean)expectedValue;
	}
}
