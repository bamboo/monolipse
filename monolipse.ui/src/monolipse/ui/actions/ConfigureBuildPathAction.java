package monolipse.ui.actions;

import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

import org.eclipse.ui.IViewPart;


public class ConfigureBuildPathAction extends FolderAction {
	public ConfigureBuildPathAction(IViewPart view) {
		super(view, "Add Reference...", BooUI.getImageDescriptor(IBooUIConstants.ASSEMBLY_REFERENCE));
	}
}
