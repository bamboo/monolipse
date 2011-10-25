package monolipse.ui.debug;

import monolipse.ui.editors.BooEditor;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.*;

public class BreakpointAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof BooEditor && IToggleBreakpointsTarget.class.isAssignableFrom(adapterType))
			return adapt((BooEditor)adaptableObject);
		return null;
	}

	private Object adapt(BooEditor editor) {
		return new BooEditorToggleBreakpointsTarget(editor);
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IToggleBreakpointsTarget.class, IToggleBreakpointsTargetExtension.class };
	}

}
