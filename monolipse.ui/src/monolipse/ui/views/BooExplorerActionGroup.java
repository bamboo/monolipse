package monolipse.ui.views;

import monolipse.core.BooCore;
import monolipse.ui.actions.ConfigureBuildPathAction;
import monolipse.ui.actions.UseAsAssemblySourceAction;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.ui.IContextMenuConstants;
import org.eclipse.jdt.ui.actions.OpenEditorActionGroup;
import org.eclipse.jdt.ui.actions.OpenViewActionGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.RefreshAction;


public class BooExplorerActionGroup extends ActionGroup {
	
	private OpenEditorActionGroup _openEditorActionGroup;
	
	private OpenViewActionGroup _openViewActionGroup;
	
	private RefreshAction _refreshAction;

	private BooExplorerView _view;

	private Action _configureBuildPath;

	private UseAsAssemblySourceAction _useAsAssemblySource; 
	
	public BooExplorerActionGroup(BooExplorerView view) {
		_openEditorActionGroup = new OpenEditorActionGroup(view);
		_openViewActionGroup = new OpenViewActionGroup(view);
		
		_view = view;
		_refreshAction = new RefreshAction(_view.getSite());
		
		_configureBuildPath = new ConfigureBuildPathAction(view);
		_useAsAssemblySource = new UseAsAssemblySourceAction(view);
	}

	private IWorkbenchWindow getActiveWorkbenchWindow() {
		return _view.getWorkbench().getActiveWorkbenchWindow();
	}
	
	public void setContext(ActionContext context) {
		super.setContext(context);
		_openEditorActionGroup.setContext(context);
		_openViewActionGroup.setContext(context);
	}
	
	public void fillContextMenu(IMenuManager menu) {
		addNewWizards(menu);
		
		_openEditorActionGroup.fillContextMenu(menu);
		_openViewActionGroup.fillContextMenu(menu);
		menu.insertAfter(IContextMenuConstants.GROUP_REORGANIZE, _refreshAction);
		
		addBuildPathActions(menu);
	}
	
	private void addBuildPathActions(IMenuManager menu) {
		Object selectedElement = getSingleSelectedElement();
		boolean isFolder = selectedElement instanceof IFolder;
		boolean isReference = isReferenceContainer(selectedElement);
		boolean isSource = isAssemblySource(selectedElement);
		
		IMenuManager buildPath = new MenuManager("Build Path");
		
		if (isSource || isReference) buildPath.add(_configureBuildPath);
		if (isFolder && !isSource) buildPath.add(_useAsAssemblySource);
		
		menu.insertAfter(IContextMenuConstants.GROUP_BUILD, buildPath);
	}

	private boolean isAssemblySource(Object selectedElement) {
		return BooCore.isAssemblySource(selectedElement);
	}

	private boolean isReferenceContainer(Object selectedElement) {
		return (selectedElement instanceof ReferenceContainer);
	}

	static class NewWizard extends NewWizardMenu {

		public NewWizard(IWorkbenchWindow window) {
			super(window);
		}
		
		public void fill(IMenuManager menu) {
			IContributionItem[] items = super.getContributionItems();
			for (int i = 0; i < items.length; i++) {
				menu.add(items[i]);
			}
		}
	}

	private void addNewWizards(IMenuManager menu) {
		
		int count = getCurrentSelection().size();
		boolean isNewProjectTarget = count >= 0 && count <=1;
		boolean isNewExampleTarget = getSingleSelectedElement() instanceof IContainer;
		
		if (!isNewProjectTarget && !isNewExampleTarget) return;
		
		IMenuManager newMenu = new MenuManager("New");
		NewWizard wizard = new NewWizard(getActiveWorkbenchWindow());
		wizard.fill(newMenu);
		 
		menu.appendToGroup(IContextMenuConstants.GROUP_NEW, newMenu);
		
	}

	private Object getSingleSelectedElement() {
		IStructuredSelection selection = getCurrentSelection();
		return selection.size() > 1 ? null : selection.getFirstElement();
	}

	private IStructuredSelection getCurrentSelection() {
		return (IStructuredSelection) getContext().getSelection();
	}


}
