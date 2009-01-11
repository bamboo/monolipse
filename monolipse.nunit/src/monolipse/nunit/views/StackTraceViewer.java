package monolipse.nunit.views;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import monolipse.core.foundation.WorkspaceUtilities;
import monolipse.nunit.NUnitPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class StackTraceViewer {
	
	private static final Pattern MONO_STACK_PATTERN = Pattern.compile("in\\s(.+\\.(boo|cs|js)):(\\d+)");
	
	private static final Pattern JAVA_STACK_PATTERN = Pattern.compile("\\((.+\\.(boo|cs|js)):(\\d+)\\)");
	
	private static final Pattern COMPILER_OUTPUT_PATTERN = Pattern.compile("\\b(.+\\.(boo|cs|js))\\((\\d+),\\d+\\)");
	
	private Link _traceView;

	private ScrolledComposite _composite;

	public StackTraceViewer(Composite parent) {
		_composite = new ScrolledComposite(parent, SWT.V_SCROLL|SWT.H_SCROLL|SWT.FLAT);
		_composite.setExpandHorizontal(true);
		_composite.setExpandVertical(true);
		_composite.setAlwaysShowScrollBars(true);
		
		_traceView = new Link(_composite, SWT.MULTI|SWT.FLAT);
		//_traceView.setEditable(false);
		_traceView.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				String[] parts = event.text.split(":");
				String path = parts[0];
				String line = parts[1];
				revealInEditor(WorkspaceUtilities.getFile(path), Integer.parseInt(line));
			}
		});
		
		_composite.setContent(_traceView);
	}
	
	private void revealInEditor(IFile file, int lineNumber) {
		
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (null != page) {
			try {
				IEditorPart editorPart = IDE.openEditor(page, file, true);
				selectLine(editorPart, lineNumber);
			} catch (PartInitException e) {
				NUnitPlugin.logException(e);
			}
		}
	}
	
	private void selectLine(IEditorPart editorPart, int lineNumber) {
		if (lineNumber > 0 && editorPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor)editorPart;
			IEditorInput input = editorPart.getEditorInput();
			
			int offset = 0;
			int length = 0;
			IDocumentProvider provider = textEditor.getDocumentProvider();
			try {
				provider.connect(input);
			} catch (CoreException e) {
				// unable to link
				NUnitPlugin.logException(e);
				return;
			}
			IDocument document = provider.getDocument(input);
			try {
				IRegion region= document.getLineInformation(lineNumber - 1);
				offset = region.getOffset();
				length = region.getLength();
			} catch (BadLocationException e) {
				// unable to link
				NUnitPlugin.logException(e);
			}
			provider.disconnect(input);
			if (offset >= 0 && length >=0) {
				textEditor.selectAndReveal(offset, length);
			}
		}
	}
	
	public void setStackTrace(String trace) {
		_traceView.setText(createHyperLinks(trace));
		_composite.setMinSize(_traceView.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}
	
	public Control getControl() {
		return _composite;
	}
	
	public void setBackground(Color background) {
		_traceView.setBackground(background);
	}
	
	private String createHyperLinks(String trace) {
		if (0 == trace.length()) return trace;
		return createHyperLinks(COMPILER_OUTPUT_PATTERN,
				createHyperLinks(MONO_STACK_PATTERN,
					createHyperLinks(JAVA_STACK_PATTERN, trace)));
	}

	private String createHyperLinks(Pattern pattern, String trace) {
		final Matcher matcher = pattern.matcher(trace);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			String fname = matcher.group(1);
			final IFile file = WorkspaceUtilities.getFileForLocation(fname);
			String lineNumber = matcher.group(3);
			String replacement = null == file
				? ""
				: "<a href=\"" + file.getFullPath().toString() + ":" + lineNumber + "\">" + file.getName() + ":" + lineNumber + "</a>";
			matcher.appendReplacement(buffer, replacement);
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}
}
