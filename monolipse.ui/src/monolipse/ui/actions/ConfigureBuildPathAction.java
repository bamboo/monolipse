package monolipse.ui.actions;

import org.eclipse.ui.IViewPart;

import monolipse.ui.BooUI;
import monolipse.ui.IBooUIConstants;

public class ConfigureBuildPathAction extends FolderAction {
	public ConfigureBuildPathAction(IViewPart view) {
		super(view, "Add Reference...", BooUI.getImageDescriptor(IBooUIConstants.ASSEMBLY_REFERENCE));
	}
}
