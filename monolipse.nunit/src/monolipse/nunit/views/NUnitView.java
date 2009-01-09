package monolipse.nunit.views;

import java.util.ArrayList;

import monolipse.core.IAssemblySource;
import monolipse.nunit.*;
import monolipse.nunit.launching.NUnitLauncher;
import monolipse.ui.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;



public class NUnitView extends ViewPart {
	
	static class FailureInfo {
		private final String _testName;
		private final String _trace;
		private final IAssemblySource _assemblySource;

		public FailureInfo(IAssemblySource assemblySource, String testName, String trace) {
			_assemblySource = assemblySource;
			_testName = testName;
			_trace = trace;
		}
		
		public IAssemblySource getAssemblySource() {
			return _assemblySource;
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
	
	private final ArrayList<FailureInfo> _failures = new ArrayList();
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
		final Table failureTableWidget = _failureView.getTable();
		failureTableWidget.setMenu(createFailureMenu(failureTableWidget));

		left.setContent(_failureView.getControl());
	}

	private Menu createFailureMenu(final Table failureTableWidget) {
		final Menu menu = new Menu(failureTableWidget);
		final MenuItem rerunFailures = new MenuItem(menu, SWT.PUSH);
		rerunFailures.setText("Rerun failed tests");
		rerunFailures.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				rerunLastFailures();
			}
		
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		return menu;
	}

	protected void rerunLastFailures() {
		if (_failures.isEmpty())
			return;
		try {
			NUnitLauncher.launch(_failures.get(0).getAssemblySource(), "run", failedTestNames());
		} catch (CoreException x) {
			NUnitPlugin.logException(x);
		}
	}

	private ArrayList<String> failedTestNames() {
		final ArrayList<String> failedTests = new ArrayList<String>();
		for (FailureInfo failure : _failures)
			failedTests.add(failure.getTestName());
		return failedTests;
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
	
	private void selectFirstFailure() {
		if (_failures.isEmpty())
			return;
		
		_failureView.setSelection(new StructuredSelection(_failures.get(0)), true);
	}

	private void activateNUnitView() {
		getSite().getPage().activate(NUnitView.this);
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
		
		public void testsStarted(IAssemblySource source, final int testCount) {
			updateUI(new Runnable() { public void run() {
				_failures.clear();
				_runs = 0;
				_errors = 0;
				_counterPanel.setTotal(testCount);
				
				_progressBar.reset();
				_progressBar.setMaximum(testCount);

				_traceView.setStackTrace("");
			}});
		}

		public void testsFinished(IAssemblySource source) {
			updateUI(new Runnable() { public void run() {
				activateNUnitView();
				selectFirstFailure();
			}});
		}

		public void testStarted(IAssemblySource source, final String fullName) {
			updateUI(new Runnable() { public void run() {
				updateProgressBar();
				++_runs;
			}});
		}

		public void testFailed(final IAssemblySource source, final String fullName, final String trace) {
			updateUI(new Runnable() { public void run() {
				_failures.add(new FailureInfo(source, fullName, trace));
				_progressBar.refresh(failuresOccurred());
			}});
		}
		
		private void updateUI(final Runnable runnable) {
			getDisplay().asyncExec(new Runnable() { public void run() {
				synchronized(TestListener.this) {
					runnable.run();
					updateUI();
				}
			}});
		}
		
		private Display getDisplay() {
			return _counterComposite.getDisplay();
		}

		private void updateUI() {
			_counterPanel.setFailureValue(_failures.size());
			_counterPanel.setRunValue(_runs);
			_counterPanel.setErrorValue(_errors);
			_failureView.refresh();
		}

		private void updateProgressBar() {
			_progressBar.step(failuresOccurred());
		}

		private boolean failuresOccurred() {
			return !_failures.isEmpty();
		}
		
	}

}