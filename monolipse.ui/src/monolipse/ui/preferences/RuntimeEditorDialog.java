package monolipse.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class RuntimeEditorDialog extends org.eclipse.swt.widgets.Dialog {

	private Shell dialogShell;
	private Label _dialogLabel;
	private Button _cancelButton;
	private Button _okButton;
	private Label _nameLabel;
	private Text _name;
	private Button _browseButton;
	private Text _location;
	private Label _locationLabel;

	/**
	* Auto-generated main method to display this 
	* org.eclipse.swt.widgets.Dialog inside a new Shell.
	*/
	public static void main(String[] args) {
		try {
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			RuntimeEditorDialog inst = new RuntimeEditorDialog(shell, SWT.NULL);
			inst.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RuntimeEditorDialog(Shell parent, int style) {
		super(parent, style);
	}

	public void open() {
		try {
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

			FormLayout dialogShellLayout = new FormLayout();
			dialogShell.setLayout(dialogShellLayout);
			dialogShell.layout();
			dialogShell.pack();
			dialogShell.setSize(428, 143);
			{
				_nameLabel = new Label(dialogShell, SWT.NONE);
				FormData _nameLabelLData = new FormData();
				_nameLabelLData.width = 42;
				_nameLabelLData.height = 14;
				_nameLabelLData.left =  new FormAttachment(0, 1000, 30);
				_nameLabelLData.top =  new FormAttachment(0, 1000, 29);
				_nameLabel.setLayoutData(_nameLabelLData);
				_nameLabel.setText("Name:");
			}
			{
				FormData _nameLData = new FormData();
				_nameLData.width = 211;
				_nameLData.height = 21;
				_nameLData.left =  new FormAttachment(0, 1000, 77);
				_nameLData.top =  new FormAttachment(0, 1000, 29);
				_name = new Text(dialogShell, SWT.BORDER);
				_name.setLayoutData(_nameLData);
			}
			{
				_dialogLabel = new Label(dialogShell, SWT.NONE);
				FormData _dialogLabelLData = new FormData();
				_dialogLabelLData.width = 112;
				_dialogLabelLData.height = 14;
				_dialogLabelLData.left = new FormAttachment(0, 1000, 28);
				_dialogLabelLData.top = new FormAttachment(0, 1000, 7);
				_dialogLabel.setLayoutData(_dialogLabelLData);
				_dialogLabel.setText("Runtime properties:");
			}
			{
				_okButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				dialogShell.setDefaultButton(_okButton);
				FormData _okButtonLData = new FormData();
				_okButtonLData.width = 70;
				_okButtonLData.height = 21;
				_okButtonLData.left =  new FormAttachment(0, 1000, 343);
				_okButtonLData.top =  new FormAttachment(0, 1000, 14);
				_okButton.setLayoutData(_okButtonLData);
				_okButton.setText("OK");
			}
			{
				_locationLabel = new Label(dialogShell, SWT.NONE);
				FormData _locationLabelLData = new FormData();
				_locationLabelLData.width = 56;
				_locationLabelLData.height = 14;
				_locationLabelLData.left =  new FormAttachment(0, 1000, 16);
				_locationLabelLData.top =  new FormAttachment(0, 1000, 64);
				_locationLabel.setLayoutData(_locationLabelLData);
				_locationLabel.setText("Location: ");
			}
			{
				_browseButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				FormData _browseButtonLData = new FormData();
				_browseButtonLData.width = 63;
				_browseButtonLData.height = 21;
				_browseButtonLData.left =  new FormAttachment(0, 1000, 231);
				_browseButtonLData.top =  new FormAttachment(0, 1000, 60);
				_browseButton.setLayoutData(_browseButtonLData);
				_browseButton.setText("Browse...");
			}
			{
				FormData _locationLData = new FormData();
				_locationLData.width = 135;
				_locationLData.height = 15;
				_locationLData.left =  new FormAttachment(0, 1000, 77);
				_locationLData.top =  new FormAttachment(0, 1000, 60);
				_location = new Text(dialogShell, SWT.BORDER);
				_location.setLayoutData(_locationLData);
			}
			{
				_cancelButton = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				FormData button1LData = new FormData();
				button1LData.width = 70;
				button1LData.height = 21;
				button1LData.left =  new FormAttachment(0, 1000, 343);
				button1LData.top =  new FormAttachment(0, 1000, 49);
				_cancelButton.setLayoutData(button1LData);
				_cancelButton.setText("Cancel");
			}
			{
				FormData text2LData = new FormData();
				text2LData.width = 135;
				text2LData.height = 15;
				text2LData.left =  new FormAttachment(0, 1000, 77);
				text2LData.top =  new FormAttachment(0, 1000, 63);
				_location = new Text(dialogShell, SWT.BORDER);
				_location.setLayoutData(text2LData);
			}
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
