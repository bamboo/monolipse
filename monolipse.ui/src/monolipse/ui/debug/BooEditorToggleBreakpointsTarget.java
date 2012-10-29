package monolipse.ui.debug;

import java.util.HashMap;

import monolipse.core.compiler.OutlineNode;
import monolipse.ui.editors.BooEditor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public class BooEditorToggleBreakpointsTarget implements
		IToggleBreakpointsTargetExtension {

	private final class TypeLocator implements OutlineNode.Visitor {
		private final int line;
		private OutlineNode type;

		private TypeLocator(int line) {
			this.line = line;
		}
		
		public OutlineNode type() {
			return type;
		}

		public boolean visit(OutlineNode node) {
			if (node.startLine() > line)
				return false;
			if (OutlineNode.CLASS.equals(node.type()))
				type = node;
			return true;
		}
	}

	private final BooEditor _editor;

	public BooEditorToggleBreakpointsTarget(BooEditor editor) {
		_editor = editor;
	}
	
	public boolean canToggleBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return true;
	}

	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {
		toggleLineBreakpoints(part, selection);
	}

	public boolean canToggleLineBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return true;
	}

	public boolean canToggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) {
		return false;
	}

	public boolean canToggleWatchpoints(IWorkbenchPart part,
			ISelection selection) {
		return false;
	}

	public void toggleLineBreakpoints(final IWorkbenchPart part, final ISelection selection)
			throws CoreException {
		
		if (!(selection instanceof ITextSelection))
			return;
		
		final ITextSelection textSelection = (ITextSelection) selection;
		final int lineNumber = textSelection.getStartLine() + 1;
		final String typeName = typeNameFor(lineNumber);
		IBreakpoint existing = JDIDebugModel.lineBreakpointExists(resource(), typeName, lineNumber);
		if (existing != null) {
			DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(existing, true);
			return;
		}
			
		JDIDebugModel.createLineBreakpoint(
				resource(), typeName, lineNumber, -1, -1, 0, true, new HashMap<String, Object>());
	}

	private String typeNameFor(final int line) {
		final OutlineNode outline = getOutline();
		if (null == outline)
			return moduleClassName();
		
		final TypeLocator locator = new TypeLocator(line);
		outline.accept(locator);
		final OutlineNode type = locator.type();
		if (type == null)
			return moduleClassName();
		
		return type.name();
	}

	private OutlineNode getOutline() {
		return _editor.getDocument().getOutline();
	}

	private String moduleClassName() {
		return resource().getFullPath().removeFileExtension().lastSegment() + "Module";
	}

	private IResource resource() {
		return (IResource)_editor.getEditorInput().getAdapter(IResource.class);
	}

	public void toggleMethodBreakpoints(IWorkbenchPart part,
			ISelection selection) throws CoreException {

	}

	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection)
			throws CoreException {

	}

}
