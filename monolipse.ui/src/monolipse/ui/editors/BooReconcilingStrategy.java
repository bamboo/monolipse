package monolipse.ui.editors;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;

import monolipse.core.compiler.CompilerServices;
import monolipse.core.compiler.OutlineNode;
import monolipse.ui.BooUI;

public class BooReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	
	private CompilerServices _builder;

	private BooDocument _document;
	
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

	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
	}

	public void reconcile(IRegion partition) {
		updateDocumentOutline();
	}

	private void updateDocumentOutline() {
		if (null == _document) return;
		OutlineNode outline = getOutline();
		if (null != outline) {
			_document.updateOutline(outline);
		}
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
		updateDocumentOutline();
	}

}
