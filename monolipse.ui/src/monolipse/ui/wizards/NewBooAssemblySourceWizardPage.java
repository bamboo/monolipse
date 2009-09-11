package monolipse.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewBooAssemblySourceWizardPage extends WizardPage {
	
	private Text _name;

	public NewBooAssemblySourceWizardPage() {
		super("General");
		setDescription("Type the name of the new source folder");
	}

	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.FILL);
		
		Label label = new Label(container, SWT.NONE);
		label.setText("Name: ");
		
		GridData nameData = new GridData();
		nameData.grabExcessHorizontalSpace = true;
		nameData.horizontalAlignment = GridData.FILL;
		_name = new Text(container, SWT.BORDER|SWT.FILL);
		_name.setLayoutData(nameData);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		
		container.setLayout(layout);
		setControl(container);
	}
	
	public String getName() {
		return _name.getText();
	}

}
