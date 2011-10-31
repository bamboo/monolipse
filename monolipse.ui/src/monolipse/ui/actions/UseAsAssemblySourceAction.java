package monolipse.ui.actions;

import monolipse.core.AssemblySourceLanguage;
import monolipse.core.BooCore;
import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IViewPart;

public class UseAsAssemblySourceAction extends FolderAction {
	
	public UseAsAssemblySourceAction(IViewPart view) {
		super(view, "Use as Assembly Source...", BooUI.getImageDescriptor(IBooUIConstants.ASSEMBLY_SOURCE_DECORATOR));
	}
	
	public void run() {
		IFolder folder = getSelectedFolder();
		try {
			BooCore.createAssemblySource(folder, AssemblySourceLanguage.BOO);
		} catch (CoreException x) {
			x.printStackTrace();
		}
	}
}
