package monolipse.nunit.views;

import java.util.ArrayList;
import java.util.HashMap;

import monolipse.core.IAssemblySource;
import monolipse.core.foundation.Strings;
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

	static class TestInfo {
		private String _testName;
		private IAssemblySource _assemblySource;
		private String _trace;
		private boolean _isFailure;

		public TestInfo() {
			this(null, "");
		}

		public TestInfo(IAssemblySource assemblySource, String testName) {
			this(assemblySource, testName, "", false);
		}
		
		public TestInfo(IAssemblySource assemblySource, String testName, String trace) {
			this(assemblySource, testName, trace, true);
		}
		
		protected TestInfo(IAssemblySource assemblySource, String testName, String trace, boolean isFailure) {
			_assemblySource = assemblySource;
			_testName = testName;
			_trace = trace;
			_isFailure = isFailure;
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

		public boolean isFailure() {
			return _isFailure;
		}
		
		public String[] splitName() {
			String[] result = new String[3];
			result[0] = result[1] = result[2] = "";
			
			String[] splitted = _testName.split("\\.");
			
			if (splitted.length > 2) {
				for (int i = 0; i < splitted.length - 2; i++) {
					result[0] = result[0] + splitted[i];
					if (i != splitted.length - 3) {
						result[0] += ".";
					}
				}
				result[1] = splitted[splitted.length-2];
				result[2] = splitted[splitted.length-1];
			}
			
			if (splitted.length == 2) {
				result[0] = splitted[0];
				result[1] = splitted[1];	
			}
			
			if (splitted.length == 1) {
				result[0] = splitted[0];
			}
			
			return result;
		}
	}
	
	static class ResultNode {
		private String _name;
		private TestInfo _info;
		private HashMap<String, ResultNode> _children;
		private ResultNode _parent; 

		public ResultNode(String name, ResultNode parent, TestInfo info) {
			_name = name;
			_parent = parent;
			_info = info;
			_children = new HashMap<String, ResultNode>();
		}

		public String getName() {
			return _name;
		}
		
		public ResultNode getParent() {
			return _parent;
		}

		public TestInfo getInfo() {
			return _info;
		}

		public HashMap<String, ResultNode> getChildren() {
			return _children;
		}

		public void reset() {
			_name = "";
			_info = null;
			_children = new HashMap<String, ResultNode>();
		}

		public IAssemblySource getAssemblySource() {
			if (_info != null) {
				return _info.getAssemblySource();
			}

			for(ResultNode node: _children.values()) {
				if (node.getInfo() != null) {
					return node.getInfo().getAssemblySource();
				}
			}
			
			return null;
		}

		public ArrayList<ResultNode> getFailedNodes() {
			return this.getFailedNodes(this);
		}
		
		private ArrayList<ResultNode> getFailedNodes(ResultNode root) {
			ArrayList<ResultNode> result = new ArrayList<ResultNode>();
			HashMap<String, ResultNode> target = root.getChildren();
			
			for(ResultNode node: target.values()) {
				if (node.hasFailures()) {
					if (node.isEmpty())
						result.add(node);
					else
						result.addAll(node.getFailedNodes());
				}
			}

			return result;
		}

		public void add(TestInfo info) {
			String[] names = info.splitName();
			
			ResultNode root = null;
			HashMap<String, ResultNode> target = _children;
			for(String name: names) {
				ResultNode next = null;
				if (!target.containsKey(name)) {
					next = new ResultNode(name, root, info);
					target.put(name, next);
				}
				else
					next = target.get(name);
				
				root = next;
				target = next.getChildren();
			}
		}

		public boolean isEmpty() {
			return _children.size() == 0;
		}

		public boolean hasFailures() {
			if (_info != null && _info._isFailure) 
				return true;
			
			for (ResultNode node: _children.values()) {
				if (node.hasFailures()) 
					return true;
			}

			return false;
		}
	}
	
	private NUnitProgressBar _progressBar;
	private CounterPanel _counterPanel;
	private SashForm _sashForm;
	private Composite _counterComposite;

	private final TestListener _listener = new TestListener();

	private final ResultNode _testRoot = new ResultNode("Root", null, null);
	private StackTraceViewer _traceView;
	private TreeViewer _testsView;

	public NUnitView() {
	}

	public void createPartControl(Composite parent) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		parent.setLayout(gridLayout);

		_counterComposite = createProgressCountPanel(parent);
		_counterComposite.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		SashForm sashForm = createSashForm(parent);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		NUnitPlugin.getDefault().addTestListener(_listener);
	}

	private SashForm createSashForm(Composite parent) {
		_sashForm = new SashForm(parent, SWT.HORIZONTAL);
		ViewForm left = new ViewForm(_sashForm, SWT.NONE);
		ViewForm right = new ViewForm(_sashForm, SWT.NONE);

		setFormLabel(left, "Tests");
		setFormLabel(right, "Failure Trace");

		createTestCasesView(left);
		createTraceView(right);

		_sashForm.setWeights(new int[] { 50, 50 });
		return _sashForm;
	}

	private void createTestCasesView(ViewForm left) {
		_testsView = new TreeViewer(left, SWT.FLAT);
		_testsView.setContentProvider(new TestsContentProvider());
		_testsView.setLabelProvider(new TestsLabelProvider());
		_testsView.setInput(_testRoot);
		_testsView.expandAll();
		
		_testsView.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				Object element = ((IStructuredSelection) event
						.getSelection()).getFirstElement();
				if (null == element)
					return;
				
				rerunChildrenTests(((ResultNode) element));
			}
		});
		
		_testsView.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						Object element = ((IStructuredSelection) event
								.getSelection()).getFirstElement();
						if (null == element)
							return;
						_traceView.setStackTrace(((ResultNode) element).getInfo()
								.getTrace());
					}
				});

		_testsView.getControl().setMenu(createFailureMenu(_testsView.getControl()));
		left.setContent(_testsView.getControl());
	}

	private void createTraceView(ViewForm right) {
		_traceView = new StackTraceViewer(right);
		_traceView.setBackground(_testsView.getControl().getBackground());
		right.setContent(_traceView.getControl());
	}

	private Menu createFailureMenu(final Control control) {
		final Menu menu = new Menu(control);
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
		if (!_testRoot.hasFailures())
			return;
		if (_testRoot.getAssemblySource() == null) {
			NUnitPlugin.logInfo("Cant get AssemblySource");
			return;
		}
		try {
			NUnitPlugin.logInfo(Strings.commaSeparatedList(failedTestNames()));
			NUnitLauncher.launch(_testRoot.getAssemblySource(),
					"run", failedTestNames());
		} catch (CoreException x) {
			NUnitPlugin.logException(x);
		}
	}

	protected void rerunChildrenTests(ResultNode resultNode) {
		try {
			ArrayList<String> childrenTestNames = childrenTestNames(resultNode);
			if (childrenTestNames.size() == 0)
				return;
			
			NUnitPlugin.logInfo(Strings.commaSeparatedList(childrenTestNames));
			NUnitLauncher.launch(_testRoot.getAssemblySource(),
					"run", childrenTestNames);
		} catch (CoreException x) {
			NUnitPlugin.logException(x);
		}
	}

	private ArrayList<String> childrenTestNames(ResultNode root) {
		ArrayList<String> result = new ArrayList<String>();
		if (root.isEmpty()) {
			result.add(root.getInfo().getTestName());
			return result;
		}

		for (ResultNode node: root.getChildren().values()) {
			if (node.isEmpty())
				result.add(node.getInfo().getTestName());
			else
				result.addAll(childrenTestNames(node));	
		}
		return result;
	}

	private ArrayList<String> failedTestNames() {
		ArrayList<String> result = new ArrayList<String>();
		
		for(ResultNode node: _testRoot.getFailedNodes()) {
			if (node.hasFailures() && node.isEmpty()) {
				result.add(node.getInfo().getTestName());
			}
		}
		
		return result;
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
		_counterPanel.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		_progressBar = new NUnitProgressBar(composite);
		_progressBar.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		return composite;
	}

	public void dispose() {
		super.dispose();
		NUnitPlugin.getDefault().removeTestListener(_listener);
	}

	public void setFocus() {
	}

	private void selectFirstFailure() {
		if (!_testRoot.hasFailures())
			return;

		for (ResultNode node: _testRoot.getFailedNodes()) {
			_testsView.expandToLevel(node, AbstractTreeViewer.ALL_LEVELS);
		}
		
		_testsView.setSelection(new StructuredSelection(_testRoot.getFailedNodes().get(0)));
	}

	private void activateNUnitView() {
		getSite().getPage().activate(NUnitView.this);
	}

	class TestListener implements ITestRunListener {

		private int _errors;
		private int _runs;
		private int _failures;

		private TestInfo _lastTest;

		public void testsStarted(IAssemblySource source, final int testCount) {
			updateUI(new Runnable() {
				public void run() {
					_testRoot.reset();
					_runs = 0;
					_errors = 0;
					_failures = 0;
					_lastTest = null;
					_counterPanel.setTotal(testCount);

					_progressBar.reset();
					_progressBar.setMaximum(testCount);

					_traceView.setStackTrace("");
				}
			});
		}

		public void testsFinished(IAssemblySource source) {
			updateUI(new Runnable() {
				public void run() {
					activateNUnitView();
					selectFirstFailure();
				}
			});
		}

		public void testStarted(final IAssemblySource source,
				final String fullName) {
			updateUI(new Runnable() {
				public void run() {
					if (_lastTest != null) {
						_testRoot.add(_lastTest);
					}

					_lastTest = new TestInfo(source, fullName);
					updateProgressBar();
					++_runs;
				}
			});
		}

		public void testFailed(final IAssemblySource source,
				final String fullName, final String trace) {
			updateUI(new Runnable() {
				public void run() {
					_testRoot.add(new TestInfo(source, fullName, trace));
					_failures++;
					_lastTest = null;
					_progressBar.refresh(failuresOccurred());
				}
			});
		}

		private void updateUI(final Runnable runnable) {
			getDisplay().asyncExec(new Runnable() {
				public void run() {
					synchronized (TestListener.this) {
						runnable.run();
						updateUI();
					}
				}
			});
		}

		private Display getDisplay() {
			return _counterComposite.getDisplay();
		}

		private void updateUI() {
			_counterPanel.setFailureValue(_failures);
			_counterPanel.setRunValue(_runs);
			_counterPanel.setErrorValue(_errors);
			_testsView.refresh();
		}

		private void updateProgressBar() {
			_progressBar.step(failuresOccurred());
		}

		private boolean failuresOccurred() {
			return _failures != 0;
		}
	}

	public class TestsContentProvider implements ITreeContentProvider {
		public Object[] getChildren(Object element) {
			return ((ResultNode)element).getChildren().values().toArray();
		}

		public Object[] getElements(Object element) {
			return ((ResultNode)element).getChildren().values().toArray();
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object getParent(Object element) {
			return ((ResultNode)element).getParent();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object old_input,
				Object new_input) {
		}
	}
	
	class TestsLabelProvider implements ILabelProvider {

		private final Image _warningIcon = BooUI.getImage(IBooUIConstants.WARNING);
		private final Image _successIcon = BooUI.getImage(IBooUIConstants.SUCCESS);

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
			return ((ResultNode)element).hasFailures() ? _warningIcon: _successIcon;
		}

		public String getText(Object element) {
			return ((ResultNode)element).getName();
		}
	}
}