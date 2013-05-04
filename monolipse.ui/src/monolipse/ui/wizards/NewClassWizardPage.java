package monolipse.ui.wizards;

import monolipse.core.foundation.Adapters;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewClassWizardPage extends WizardPage {
	private Text _containerText;

	private Text _classNameText;

	private ISelection _selection;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewClassWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Boo Class");
		setDescription("This wizard creates a new boo class.");
		this._selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("&Container:");

		_containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		_containerText.setLayoutData(gd);
		_containerText.setEditable(false);

		label = new Label(container, SWT.NULL);
		label.setText("Class &name:");

		_classNameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		_classNameText.setLayoutData(gd);
		_classNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		_classNameText.setFocus();
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (_selection != null && _selection.isEmpty() == false
				&& _selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) _selection;
			if (ssel.size() > 1)
				return;
			IResource resource = Adapters.adapterFor(ssel.getFirstElement(), IResource.class);
			if (resource != null) {
				IContainer container;
				if (resource instanceof IContainer)
					container = (IContainer) resource;
				else
					container = ((IResource) resource).getParent();
				_containerText.setText(container.getFullPath().toString());
			}
		}
		_classNameText.setText("NewClass");
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		String className = getClassName();

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (className.length() == 0) {
			updateStatus("Class name must be specified");
			return;
		}
		if (!className.matches("\\w+")) {
			updateStatus("Class name must be valid");
			return;
		}
		
		final String fname = className + ".boo";
		final IProject project = container.getProject();
		final IPath newFilePath = container.getProjectRelativePath().append(fname);
		if (project.getFile(newFilePath).exists()) {
			updateStatus("File '" + fname + "' already exists.");
			return;
		}
		
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return _containerText.getText();
	}

	public String getClassName() {
		return _classNameText.getText();
	}
}