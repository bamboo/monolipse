package monolipse.ui.editors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.text.BadLocationException;

import monolipse.core.compiler.CompilerServices;
import monolipse.core.compiler.OutlineNode;
import monolipse.ui.BooUI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.swt.widgets.Display;


public class BooReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	
	private CompilerServices _builder;

	private BooDocument _document;
	private BooEditor _editor;
	
	public BooReconcilingStrategy() {
		try {
			_builder = CompilerServices.getInstance();
		} catch (CoreException e) {
			BooUI.logException(e);
		}
	}

	public void setDocument(IDocument document) {
		_document = (BooDocument) document;
	}

	public void setEditor(BooEditor editor) {
		_editor = editor;
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
	}

	public void reconcile(IRegion partition) {
		updateDocumentFolding(updateDocumentOutline());
	}
	
	ArrayList positions = new ArrayList();

	private void updateDocumentFolding(OutlineNode outline) {
        positions = calculatePositions(outline, positions);
 
        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                	if (_editor != null) 
                		_editor.updateFoldingStructure(positions);
                }
        });
	}

	private ArrayList calculatePositions(OutlineNode outline, ArrayList positions) {
        getOutlinePositions(outline, positions);
		return positions;
	}

	private void getOutlinePositions(OutlineNode root, ArrayList collection) {
		if (shouldFoldBlock(root)) {
			int start = getPostionForLine(root.startLine());
			int length = getPostionForLine(root.endLine() + 1) - start;			
			collection.add(new Position(start, length));
		}
		for (OutlineNode child: root.children()) {
			getOutlinePositions(child, collection);
		}
	}

	private boolean shouldFoldBlock(OutlineNode root) {
		if (root == null || root.type() == null) {
			return false;
		}
		
		String type = root.type();
		return type.equals("Method") || type.equals("Constructor") || type.equals("ClassDefinition") || type.equals("InterfaceDefinition");
	}

	private int getPostionForLine(int line) {
		if (null == _document) return 1;
		
		try {
			return _document.getLineOffset(line - 1);
		} catch (org.eclipse.jface.text.BadLocationException e) {
			return 1;
		}
	}

	private OutlineNode updateDocumentOutline() {
		if (null == _document) return null;
		OutlineNode outline = getOutline();
		if (null != outline) {
			_document.updateOutline(outline);
		}
		return outline;
	}

	private OutlineNode getOutline() {
		if (null == _builder) return null;
		try {
			return _builder.getOutline(_document.get());
		} catch (IOException e) {
			BooUI.logException(e);
		}
		return null;
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
	}

	public void initialReconcile() {
		updateDocumentFolding(updateDocumentOutline());
	}
}
