package monolipse.nunit.views;

import java.util.ArrayList;

import monolipse.core.IAssemblySource;
import monolipse.nunit.ITestRunListener;
import monolipse.nunit.NUnitPlugin;
import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;



public class NUnitView extends ViewPart {
	
	static class FailureInfo {
		private String _testName;
		private String _trace;

		public FailureInfo(String testName, String trace) {
			_testName = testName;
			_trace = trace;
		}
		
		public String getTestName() {
			return _testName;
		}

		public String getTrace() {
			return _trace;
		}
	}
	
	private NUnitProgressBar _progressBar;
	private CounterPanel _counterPanel;
	private SashForm _sashForm;
	private Composite _counterComposite;
	
	private final TestListener _listener = new TestListener();
	
	ArrayList _failures = new ArrayList();
	private TableViewer _failureView;
	private StackTraceViewer _traceView;

	public NUnitView() {
	}

	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout(); 
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);
		
		_counterComposite = createProgressCountPanel(parent);
		_counterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		SashForm sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		NUnitPlugin.getDefault().addTestListener(_listener);
	}
	
	private SashForm createSashForm(Composite parent) {
		_sashForm = new SashForm(parent, SWT.HORIZONTAL);
		ViewForm left = new ViewForm(_sashForm, SWT.NONE);		
		ViewForm right = new ViewForm(_sashForm, SWT.NONE);
		
		setFormLabel(left, "Failures");
		setFormLabel(right, "Failure Trace");
		
		createFailureView(left);
		
		createTraceView(right);

		_sashForm.setWeights(new int[]{50, 50});
		return _sashForm;
	}

	private void createTraceView(ViewForm right) {
		_traceView = new StackTraceViewer(right);
		_traceView.setBackground(_failureView.getControl().getBackground());
		right.setContent(_traceView.getControl());
	}

	private void createFailureView(ViewForm left) {
		_failureView = new TableViewer(left, SWT.FLAT);
		_failureView.setContentProvider(new FailureContentProvider());
		_failureView.setLabelProvider(new FailureLabelProvider());
		_failureView.setInput(_failures);
		_failureView.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((IStructuredSelection)event.getSelection()).getFirstElement();
				if (null == element) return;
				_traceView.setStackTrace(((FailureInfo)element).getTrace());
			}
		});

		left.setContent(_failureView.getControl());
	}

	private void setFormLabel(ViewForm form, final String labelText) {
		CLabel label = new CLabel(form, SWT.NONE);
		label.setText(labelText);
		form.setTopLeft(label);
	}

	
	protected Composite createProgressCountPanel(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2; 
		
		_counterPanel = new CounterPanel(composite);
		_counterPanel.setLayoutData(
			new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		_progressBar = new NUnitProgressBar(composite);
		_progressBar.setLayoutData(
				new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	
	public void dispose() {
		super.dispose();
		NUnitPlugin.getDefault().removeTestListener(_listener);
	}

	public void setFocus() {
	}
	
	class FailureContentProvider implements IStructuredContentProvider {

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public Object[] getElements(Object inputElement) {
			return _failures.toArray();
		}		
	}
	
	class FailureLabelProvider implements ILabelProvider {
		
		private final Image _errorIcon= BooUI.getImage(IBooUIConstants.ERROR);
		//private final Image _failureIcon= BooUI.getImage(IBooUIConstants.WARNING);

		public void addListener(ILabelProviderListener listener) {
		}

		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}

		public Image getImage(Object element) {
			return _errorIcon;
		}

		public String getText(Object element) {
			return ((FailureInfo)element).getTestName();
		}
		
	}
		
	class TestListener implements ITestRunListener {
		
		private int _errors;

		private int _runs;
		
		public synchronized void testsStarted(IAssemblySource source, final int testCount) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					_failures.clear();
					_runs = _errors = 0;
					_counterPanel.setTotal(testCount);
					
					_progressBar.reset();
					_progressBar.setMaximum(testCount);

					_traceView.setStackTrace("nothing");
					
					updateUI();
				}
			});
		}

		private Display getDisplay() {
			return _counterComposite.getDisplay();
		}

		public synchronized void testsFinished(IAssemblySource source) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					getSite().getPage().activate(NUnitView.this);
					if (!_failures.isEmpty()) {
						_failureView.setSelection(new StructuredSelection(_failures.get(0)), true);
					}
				}
			});
		}

		public synchronized void testStarted(IAssemblySource source, final String fullName) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					_progressBar.step(_failures.size());
					++_runs;
					updateUI();
				}
			});
		}

		public synchronized void testFailed(IAssemblySource source, final String fullName, final String trace) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					_failures.add(new FailureInfo(fullName, trace));
					updateUI();
				}
			});
		}

		private synchronized void updateUI() {
			_counterPanel.setFailureValue(_failures.size());
			_counterPanel.setRunValue(_runs);
			_counterPanel.setErrorValue(_errors);
			_failureView.refresh();
		}
		
	}

}