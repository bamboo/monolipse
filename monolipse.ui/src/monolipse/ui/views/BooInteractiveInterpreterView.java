package monolipse.ui.views;



import monolipse.core.interpreter.IInterpreterListener;
import monolipse.core.interpreter.InteractiveInterpreter;
import monolipse.ui.BooUI;
import monolipse.ui.TextViewerUtilities;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;


public class BooInteractiveInterpreterView extends ViewPart {
	
	public static final String ID_VIEW = "monolipse.ui.views.BooInteractiveInterpreterView";
	
	Action _actionReset;
	Action action2;
	
	private TextViewer _text;
	
	InteractiveInterpreter _interpreter;
		
	/**
	 * The constructor.
	 */
	public BooInteractiveInterpreterView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		try {
			_interpreter = new InteractiveInterpreter();
		} catch (CoreException x) {
			BooUI.logException(x);
			return;
		}
		
		_text = new TextViewer(parent, SWT.FULL_SELECTION|SWT.MULTI/*|SWT.BORDER*/|SWT.V_SCROLL);
		_text.setDocument(new Document());
		
		final StyledText textWidget = _text.getTextWidget();
		textWidget.setFont(JFaceResources.getTextFont());
		_text.prependVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent e) {
				if (e.character == '\r') {
					e.doit = false;
					try {
						final String currentLine = getCurrentLine();
						if (currentLine.equals("cls")) {
							textWidget.getContent().setText("");
							return;
						}
						_interpreter.eval(currentLine);
						_text.setEditable(false);
					} catch (Exception x) {
						BooUI.logException(x);
					}					
				}
			}
		});
		
		_interpreter.addListener(new IInterpreterListener() {
			public void evalFinished(final String result) {
				textWidget.getDisplay().asyncExec(new Runnable() {
					public void run() {
						prompt(result);
					}
				});
			}
		});
		
		final ContentAssistant assistant = new ContentAssistant();
		assistant.setContentAssistProcessor(
				new BooContentAssistProcessor(_interpreter),
				IDocument.DEFAULT_CONTENT_TYPE);

		assistant.install(_text);
		assistant.enableAutoActivation(true);
		
		/*
		_text.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case SWT.F1:
					assistant.showPossibleCompletions();
				break;
				default:
					//ignore everything else
					}
				}
			});*/
		
		makeActions();
		hookContextMenu();
		contributeToActionBars();
	}
	
	protected String getCurrentLine() {
		return getLineAtOffset(_text.getTextWidget().getCaretOffset());
	}

	private String getLineAtOffset(final int offset) {
		return TextViewerUtilities.getLineAtOffset(_text, offset);
	}

	public void dispose() {
		if (null != _interpreter) {
			_interpreter.dispose();
			_interpreter = null;
		}
		super.dispose();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BooInteractiveInterpreterView.this.fillContextMenu(manager);
			}
		});
		//Menu menu = menuMgr.createContextMenu(viewer.getControl());
		//viewer.getControl().setMenu(menu);
		//getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(_actionReset);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(_actionReset);
		manager.add(action2);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(_actionReset);
		manager.add(action2);
	}

	private void makeActions() {
		_actionReset = new Action() {
			public void run() {
				if (null != _interpreter) {
					_interpreter.unload();
				}
				prompt("interpreter reset");
			}
		};
		_actionReset.setText("Reset Interpreter");
		_actionReset.setToolTipText("resets the interpreter");
		_actionReset.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_WARN_TSK));
		
		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			getSite().getShell(),
			"Boo Interactive Interpreter",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		_text.getControl().setFocus();
	}

	private void prompt(String message) {
		StyledText widget = _text.getTextWidget();
		widget.setSelection(0, 0);
		widget.insert(message);
		widget.insert("\n");
		widget.setCaretOffset(0);
		widget.setEditable(true);
	}
}