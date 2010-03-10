package monolipse.ui.editors;

import java.util.*;

import monolipse.core.compiler.OutlineNode;
import monolipse.ui.*;

import org.eclipse.jdt.internal.ui.text.AbstractInformationControl;
import org.eclipse.jface.action.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;


public class BooContentOutlinePage extends ContentOutlinePage {
	
	public class BooOutlineInformationControl extends AbstractInformationControl {

		public BooOutlineInformationControl(Shell parent, int shellStyle, int treeStyle) {
			super(parent, shellStyle, treeStyle);
			
		}

		protected TreeViewer createTreeViewer(Composite parent, int style) {
			
			final TreeViewer viewer = new TreeViewer(newTree(parent, style));
			viewer.setAutoExpandLevel(4);
			viewer.setContentProvider(new OutlineContentProvider());
			viewer.setLabelProvider(new OutlineLabelProvider());
			return viewer;
		}

		private Tree newTree(Composite parent, int style) {
			Tree tree = new Tree(parent, style);
			GridData gd= new GridData(GridData.FILL_BOTH);
			gd.heightHint= tree.getItemHeight() * 12;
			gd.widthHint = gd.heightHint * 2;
			tree.setLayoutData(gd);
			return tree;
		}
		
		protected Object getSelectedElement() {
			OutlineNode node = (OutlineNode)super.getSelectedElement();
			if (null != node) {
				goToNode(node);
			}
			return node;
		}
		
		protected String getId() {
			return getClass().getName();
		}

		public void setInput(Object information) {
			getTreeViewer().setInput(information);
		}
		
		protected void selectFirstMatch() {
			Tree tree= getTreeViewer().getTree();
			Object element= findElement(tree.getItems());
			if (element != null)
				getTreeViewer().setSelection(new StructuredSelection(element), true);
			else
				getTreeViewer().setSelection(StructuredSelection.EMPTY);
		}

		private Object findElement(TreeItem[] items) {
			ILabelProvider labelProvider= (ILabelProvider)getTreeViewer().getLabelProvider();
			for (int i= 0; i < items.length; i++) {
				Object element= items[i].getData();
				if (fStringMatcher == null) return element;
				if (element != null) {
					String label= labelProvider.getText(element);
					if (fStringMatcher.match(label)) {
						return element;
					}
				}

				element = findElement(items[i].getItems());
				if (element != null) return element;
			}
			return null;
		}
	}

	
	public static class OutlineLabelProvider extends LabelProvider {
		
		private final Map<String, Image> _imageMap = new HashMap<String, Image>();
		
		public OutlineLabelProvider() {
			setUpImageMap();
		}
		
		public String getText(Object element) {
			return ((OutlineNode)element).name();
		}
		
		public Image getImage(Object element) {
			OutlineNode node = (OutlineNode)element;
			String nodeImageKey = node.visibility().isEmpty() ? node.type() : node.visibility() + "_" + node.type();
			return _imageMap.containsKey(nodeImageKey) ? _imageMap.get(nodeImageKey) : _imageMap.get(node.type());
		}
		
		void setUpImageMap() {
			mapImage(OutlineNode.INTERFACE, 		IBooUIConstants.INTERFACE);
			mapImage(OutlineNode.PROTECTED_INTERFACE, IBooUIConstants.PROTECTED_INTERFACE);
			mapImage(OutlineNode.PRIVATE_INTERFACE, IBooUIConstants.PRIVATE_INTERFACE);
			mapImage(OutlineNode.INTERNAL_INTERFACE, IBooUIConstants.INTERNAL_INTERFACE);

			mapImage(OutlineNode.CLASS, 			IBooUIConstants.CLASS);
			mapImage(OutlineNode.PROTECTED_CLASS, 	IBooUIConstants.PROTECTED_CLASS);
			mapImage(OutlineNode.PRIVATE_CLASS, 	IBooUIConstants.PRIVATE_CLASS);
			mapImage(OutlineNode.INTERNAL_CLASS, 	IBooUIConstants.INTERNAL_CLASS);
			
			mapImage(OutlineNode.METHOD, 			IBooUIConstants.METHOD);
			mapImage(OutlineNode.PROTECTED_METHOD, 	IBooUIConstants.PROTECTED_METHOD);
			mapImage(OutlineNode.PRIVATE_METHOD, 	IBooUIConstants.PRIVATE_METHOD);
			mapImage(OutlineNode.INTERNAL_METHOD, 	IBooUIConstants.INTERNAL_METHOD);
			
			mapImage(OutlineNode.PROPERTY, 			IBooUIConstants.PROPERTY);
			mapImage(OutlineNode.PROTECTED_PROPERTY, IBooUIConstants.PROTECTED_PROPERTY);
			mapImage(OutlineNode.PRIVATE_PROPERTY, 	IBooUIConstants.PRIVATE_PROPERTY);
			mapImage(OutlineNode.INTERNAL_PROPERTY, IBooUIConstants.INTERNAL_PROPERTY);

			mapImage(OutlineNode.FIELD, 			IBooUIConstants.FIELD);
			mapImage(OutlineNode.PROTECTED_FIELD, 	IBooUIConstants.PROTECTED_FIELD);
			mapImage(OutlineNode.PRIVATE_FIELD, 	IBooUIConstants.PRIVATE_FIELD);
			mapImage(OutlineNode.INTERNAL_FIELD, 	IBooUIConstants.INTERNAL_FIELD);

			mapImage(OutlineNode.ENUM, 				IBooUIConstants.ENUM);
			mapImage(OutlineNode.PROTECTED_ENUM, 	IBooUIConstants.PROTECTED_ENUM);
			mapImage(OutlineNode.PRIVATE_ENUM, 		IBooUIConstants.PRIVATE_ENUM);
			mapImage(OutlineNode.INTERNAL_ENUM, 	IBooUIConstants.INTERNAL_ENUM);

			mapImage(OutlineNode.CONSTRUCTOR, IBooUIConstants.METHOD);
			mapImage(OutlineNode.PROPERTY, IBooUIConstants.PROPERTY);
			mapImage(OutlineNode.EVENT, IBooUIConstants.EVENT);
			mapImage(OutlineNode.CALLABLE, IBooUIConstants.CALLABLE);
			mapImage(OutlineNode.STRUCT, IBooUIConstants.STRUCT);
		}
		
		void mapImage(String entityType, String key) {
			_imageMap.put(entityType, BooUI.getImage(key));
		}
	}

	public static class OutlineContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			return ((OutlineNode)parentElement).children();
		}

		public Object getParent(Object element) {
			return ((OutlineNode)element).parent();
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}		
	}

	private IDocumentProvider _documentProvider;
	private IEditorInput _editorInput;
	private BooEditor _editor;

	public BooContentOutlinePage(IDocumentProvider documentProvider, BooEditor editor) {
		_documentProvider = documentProvider;
		_editor = editor;
	}

	public void setInput(IEditorInput editorInput) {
		_editorInput = editorInput;
	}
	
	private void gotoLine(int line) {
		try {
			BooDocument document = getDocument();
			IRegion info = document.getLineInformation(line);
			_editor.selectAndReveal(info.getOffset(), info.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		setUpOutline();
		
		setUpTreeViewer();
		
		toolBarManager().add(createSortAction());
		
	}

	private void setUpTreeViewer() {
		final TreeViewer tree = getTreeViewer();
		setUpTreeViewer(tree);
	}

	void setUpTreeViewer(final TreeViewer tree) {
		tree.setAutoExpandLevel(4);
		tree.setContentProvider(new OutlineContentProvider());
		tree.setLabelProvider(new OutlineLabelProvider());
		tree.setInput(outline());
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (null == selected) return;
				int line = ((OutlineNode)selected).startLine()-1;
				gotoLine(line);
			}
		});
	}

	public OutlineNode outline() {
		return getDocument().getOutline();
	}

	private IToolBarManager toolBarManager() {
		return getSite().getActionBars().getToolBarManager();
	}

	private Action createSortAction() {
		Action sortAction = new Action("Sort", Action.AS_CHECK_BOX) {
			public void run() {
				getTreeViewer().setComparator(isChecked() ? new ViewerComparator() : null);
			}
		};
		sortAction.setToolTipText("sorts by name");
		sortAction.setImageDescriptor(BooUI.sharedImage(ISharedImages.IMG_DEF_VIEW));
		return sortAction;
	}

	void setUpOutline() {
		final BooDocument document = getDocument();
		document.addOutlineListener(new BooDocument.OutlineListener() {
			public void outlineChanged(OutlineNode node) {
				final TreeViewer tree = getTreeViewer();
				tree.getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						tree.setInput(document.getOutline());
					};
				});
			}
		});
	}

	private BooDocument getDocument() {
		return (BooDocument) _documentProvider.getDocument(_editorInput);
	}

	public IInformationControl createQuickOutline(Shell parent, int shellStyle,
			int treeStyle) {
		return new BooOutlineInformationControl(parent, shellStyle, treeStyle);
	}

	private void goToNode(final OutlineNode node) {
		int line = node.startLine()-1;
		gotoLine(line);
	}
}
