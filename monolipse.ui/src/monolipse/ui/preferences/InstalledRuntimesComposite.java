package monolipse.ui.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

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
public class InstalledRuntimesComposite extends org.eclipse.swt.widgets.Composite {
	private Table _runtimesTable;
	private TableColumn _nameColumn;
	private TableColumn _locationColumn;
	private Label _header;
	private Button _buttonRemove;
	private TableColumn _typeColumn;
	private Button _buttonAdd;

	/**
	* Auto-generated main method to display this 
	* org.eclipse.swt.widgets.Composite inside a new Shell.
	*/
	public static void main(String[] args) {
		showGUI();
	}
		
	/**
	* Auto-generated method to display this 
	* org.eclipse.swt.widgets.Composite inside a new Shell.
	*/
	public static void showGUI() {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		InstalledRuntimesComposite inst = new InstalledRuntimesComposite(shell, SWT.NULL);
		Point size = inst.getSize();
		shell.setLayout(new FillLayout());
		shell.layout();
		if(size.x == 0 && size.y == 0) {
			inst.pack();
			shell.pack();
		} else {
			Rectangle shellBounds = shell.computeTrim(0, 0, size.x, size.y);
			shell.setSize(shellBounds.width, shellBounds.height);
		}
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	public InstalledRuntimesComposite(org.eclipse.swt.widgets.Composite parent, int style) {
		super(parent, style);
		initGUI();
	}

	private void initGUI() {
		try {
			this.setLayout(new FormLayout());
			this.setSize(413, 301);
			{
				_buttonAdd = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData _buttonAddLData = new FormData();
				_buttonAddLData.width = 70;
				_buttonAddLData.height = 28;
				_buttonAddLData.left =  new FormAttachment(0, 1000, 336);
				_buttonAddLData.top =  new FormAttachment(0, 1000, 35);
				_buttonAdd.setLayoutData(_buttonAddLData);
				_buttonAdd.setText("Add...");
				_buttonAdd.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						_buttonAddWidgetSelected(evt);
					}
				});
			}
			{
				_header = new Label(this, SWT.NONE);
				FormData _headerLData = new FormData();
				_headerLData.width = 210;
				_headerLData.height = 14;
				_headerLData.left =  new FormAttachment(0, 1000, 0);
				_headerLData.top =  new FormAttachment(0, 1000, 14);
				_header.setLayoutData(_headerLData);
				_header.setText("The following runtimes are available:");
			}
			{
				FormData _runtimesTableLData = new FormData();
				_runtimesTableLData.width = 310;
				_runtimesTableLData.height = 247;
				_runtimesTableLData.left =  new FormAttachment(0, 1000, 0);
				_runtimesTableLData.top =  new FormAttachment(0, 1000, 35);
				_runtimesTableLData.bottom =  new FormAttachment(1000, 1000, 0);
				_runtimesTable = new Table(this, SWT.BORDER);
				_runtimesTable.setLayoutData(_runtimesTableLData);
				_runtimesTable.setLayoutDeferred(true);
				_runtimesTable.setHeaderVisible(true);
				_runtimesTable.setLinesVisible(true);
				{
					_nameColumn = new TableColumn(_runtimesTable, SWT.NONE);
					_nameColumn.setText("Name");
					_nameColumn.setWidth(101);
				}
				{
					_locationColumn = new TableColumn(_runtimesTable, SWT.NONE);
					_locationColumn.setText("Location");
					_locationColumn.setWidth(142);
				}
				{
					_typeColumn = new TableColumn(_runtimesTable, SWT.NONE);
					_typeColumn.setText("Type");
					_typeColumn.setWidth(78);
				}
			}
			{
				_buttonRemove = new Button(this, SWT.PUSH | SWT.CENTER);
				FormData button1LData = new FormData();
				button1LData.width = 70;
				button1LData.height = 28;
				button1LData.left =  new FormAttachment(0, 1000, 336);
				button1LData.top =  new FormAttachment(0, 1000, 70);
				_buttonRemove.setLayoutData(button1LData);
				_buttonRemove.setText("Remove");
			}
			this.layout();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void _buttonAddWidgetSelected(SelectionEvent evt) {
		RuntimeEditorDialog dlg = new RuntimeEditorDialog(getShell(), SWT.NONE);
		dlg.open();
	}

}
