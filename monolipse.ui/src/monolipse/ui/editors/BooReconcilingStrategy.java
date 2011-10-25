package monolipse.ui.editors;

import java.io.*;
import java.util.*;

import monolipse.core.compiler.*;
import monolipse.ui.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.reconciler.*;
import org.eclipse.swt.widgets.*;


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
	
	ArrayList<Position> positions = new ArrayList<Position>();

	private void updateDocumentFolding(OutlineNode outline) {
        positions = calculatePositions(outline, positions);
 
        Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                	if (_editor != null) 
                		_editor.updateFoldingStructure(positions);
                }
        });
	}

	private ArrayList<Position> calculatePositions(OutlineNode outline, ArrayList<Position> positions) {
        getOutlinePositions(outline, positions);
		return positions;
	}

	private void getOutlinePositions(OutlineNode root, ArrayList<Position> collection) {
		if (shouldFoldBlock(root)) {
			int start = getPositionForLine(root.startLine());
			int length = getPositionForLine(root.endLine() + 1) - start;			
			collection.add(new Position(start, Math.max(1, length)));
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
		return type.equals("ImportCollection") || type.equals("Method") || type.equals("Constructor") || type.equals("ClassDefinition") || type.equals("InterfaceDefinition");
	}

	private int getPositionForLine(int line) {
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
