/*
 * Boo Development Tools for the Eclipse IDE
 * Copyright (C) 2005 Rodrigo B. de Oliveira (rbo@acm.org)
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */
package monolipse.ui.editors;

import monolipse.ui.BooUI;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;


public class BooSourceViewerConfiguration extends SourceViewerConfiguration {
	private BooDoubleClickStrategy _doubleClickStrategy;
	private BooScanner _scanner;
	private BooScanner _codeLiteralScanner;
	private ISharedTextColors _colorManager;
	private MultiLineCommentScanner _multiLineCommentScanner;
	private StringScanner _tqsScanner;
	private StringScanner _dqsScanner;
	private SingleQuotedStringScanner _sqsScanner;
	private RegexScanner _regexScanner;
	private ContentAssistant _assistant;
	private BooEditor _editor;

	public BooSourceViewerConfiguration(ISharedTextColors colors, BooEditor booEditor) {
		this._colorManager = colors;
		this._editor = booEditor;
	}
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return BooPartitionScanner.PARTITION_TYPES;
	}
	
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		BooReconcilingStrategy strategy = new BooReconcilingStrategy();
		strategy.setEditor(_editor);
		return new MonoReconciler(strategy, false);
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (null == _assistant) {
			ContentAssistant assistant = new ContentAssistant();
			try {
				assistant.setContentAssistProcessor(
						new BooEditorContentAssistProcessor(),
						IDocument.DEFAULT_CONTENT_TYPE);
			} catch (CoreException e) {
				BooUI.logException(e);
				return null;
			}
			assistant.enableAutoActivation(true);
			_assistant = assistant;
		}
		_assistant.install(sourceViewer);
		return _assistant;
	}
	
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		if (IDocument.DEFAULT_CONTENT_TYPE == contentType
			|| BooPartitionScanner.CODE_LITERAL == contentType) {
			return new IAutoEditStrategy[] { new BooAutoEditStrategy() };
		}
		return super.getAutoEditStrategies(sourceViewer, contentType);
	}
	
	public ITextDoubleClickStrategy getDoubleClickStrategy(
		ISourceViewer sourceViewer,
		String contentType) {
		if (_doubleClickStrategy == null) {
			_doubleClickStrategy = new BooDoubleClickStrategy();
		}
		return _doubleClickStrategy;
	}

	protected BooScanner getBooScanner() {
		if (_scanner == null) {
			_scanner = new BooScanner(_colorManager);
		}
		return _scanner;
	}
	
	protected BooScanner getCodeLiteralScanner() {
		if (_codeLiteralScanner == null) {
			_codeLiteralScanner = new BooScanner(_colorManager, _colorManager.getColor(BooColorConstants.CODE_LITERAL_BACKGROUND));
		}
		return _codeLiteralScanner;
	}
	
	protected MultiLineCommentScanner getMultiLineCommentScanner() {
		if (_multiLineCommentScanner == null) {
			_multiLineCommentScanner = new MultiLineCommentScanner(_colorManager);
		}
		return _multiLineCommentScanner;
	}
	
	protected RegexScanner getRegexScanner() {
		if (_regexScanner == null) {
			_regexScanner = new RegexScanner(_colorManager);
		}
		return _regexScanner;
	}
	
	protected StringScanner getTripleQuotedStringScanner() {
		if (_tqsScanner == null) {
			_tqsScanner = new StringScanner(_colorManager.getColor(BooColorConstants.TRIPLE_QUOTED_STRING));
		}
		return _tqsScanner;
	}
	
	protected StringScanner getDoubleQuotedStringScanner() {
		if (_dqsScanner == null) {
			_dqsScanner = new StringScanner(_colorManager.getColor(BooColorConstants.STRING));
		}
		return _dqsScanner;
	}
	
	protected SingleQuotedStringScanner getSingleQuotedStringScanner() {
		if (_sqsScanner == null) {
			_sqsScanner = new SingleQuotedStringScanner(_colorManager.getColor(BooColorConstants.STRING));
		}
		return _sqsScanner;
	}
	
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new MarkerAnnotationHover();
	}
	
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return new BooSourceTextHover();
	}
	
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		if (IDocument.DEFAULT_CONTENT_TYPE == contentType
			|| BooPartitionScanner.SINGLELINE_COMMENT_TYPE == contentType) {
			return new String[] { "#", "//" };
		}
		return null;
	}
	
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, SWT.NONE, new org.eclipse.jface.internal.text.html.HTMLTextPresenter(true));
			}
		};
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		configureReconciler(reconciler, IDocument.DEFAULT_CONTENT_TYPE, getBooScanner());
		configureReconciler(reconciler, BooPartitionScanner.MULTILINE_COMMENT_TYPE, getMultiLineCommentScanner());		
		configureReconciler(reconciler, BooPartitionScanner.SINGLELINE_COMMENT_TYPE, getMultiLineCommentScanner());
		configureReconciler(reconciler, BooPartitionScanner.SINGLE_QUOTED_STRING, getSingleQuotedStringScanner());
		configureReconciler(reconciler, BooPartitionScanner.DOUBLE_QUOTED_STRING, getDoubleQuotedStringScanner());
		configureReconciler(reconciler, BooPartitionScanner.TRIPLE_QUOTED_STRING, getTripleQuotedStringScanner());
		configureReconciler(reconciler, BooPartitionScanner.REGEX_TYPE, getRegexScanner());
		configureReconciler(reconciler, BooPartitionScanner.CODE_LITERAL, getCodeLiteralScanner());

		return reconciler;
	}
	
	private void configureReconciler(PresentationReconciler reconciler, String partitionType, ITokenScanner scanner) {
		DefaultDamagerRepairer dr;
		dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, partitionType);
		reconciler.setRepairer(dr, partitionType);
	}

}