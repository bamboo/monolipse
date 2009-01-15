package monolipse.core.launching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;

public class BoojaySourceLookupDirector extends JavaSourceLookupDirector {

	@Override
	public Object[] findSourceElements(Object object) throws CoreException {
		//...
		return super.findSourceElements(object);
	}
}
