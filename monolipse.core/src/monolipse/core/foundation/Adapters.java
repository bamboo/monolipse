package monolipse.core.foundation;

import org.eclipse.core.runtime.*;

public class Adapters {

	public static <T> T adapterFor(Object o, Class<T> type) {
		return type.cast(Platform.getAdapterManager().getAdapter(o, type));
	}
}
