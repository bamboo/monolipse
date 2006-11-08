package monolipse.ui.editors;

import org.eclipse.jface.text.Document;

import monolipse.core.compiler.OutlineNode;

public class BooDocument extends Document {
	
	public interface OutlineListener {
		void outlineChanged(OutlineNode newOutline);
	}

	private OutlineNode _outline = new OutlineNode();
	private OutlineListener _listener;
	
	public BooDocument() {
	}
	
	public OutlineNode getOutline() {
		return _outline;
	}

	public void updateOutline(OutlineNode outline) {
		_outline = outline;
		if (null != _listener) {
			_listener.outlineChanged(_outline);
		}
	}

	public void addOutlineListener(OutlineListener listener) {
		_listener = listener;
	}
}
