package cg.editor;

import java.io.File;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.part.ViewPart;
import org.jdom.Document;
import org.jdom.Element;

import cg.base.util.FileWatcher;
import cg.base.util.IFileModificationListener;
import cg.base.util.IOUtils;
import cg.data.util.FileUtils;
import cg.editor.data.Category;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.EditorObject;
import cg.editor.data.TypeInfo;
import cg.editor.extend.DataObjectInput;
import cg.editor.extend.DataObjectLabelProvider;
import cg.editor.extend.ProjectDataListProvider;

public class View extends ViewPart implements IFileModificationListener {
	
	public static final String ID = "CrossGateEditor.view";

	/**
	 * ���������������͵����
	 */
	private CTabFolder dataTypeTabFolder;
	/**
	 * ���������͵�Tab
	 */
	private CTabItem[] dataTypeTabs;
	/**
	 * ���������͵Ĺ��������
	 */
	private Text[] dataListFilterText;
	/**
	 * ���������͵ı��
	 */
	private TreeViewer[] dataTreeViewers;
	/**
	 * ���������͵ı��
	 */
	private Tree[] dataListTree;
	/**
	 * ����½�����
	 */
	private Action newAction;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		// Ĭ��
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		
		dataTypeTabFolder = new CTabFolder(container, SWT.NONE); // �������������������͵����
		
		CrossGateEditor.launcher();
		
		createActions();
		
		getViewSite().getActionBars().getMenuManager();
		
		try {
			createDataTypeTabs(CrossGateEditor.getConfiguration());
		} catch (Exception e) {
			CrossGateEditor.getLog().error(getClass().getName(), e);
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
//		viewer.getControl().setFocus();
	}

	private void createActions() {
		newAction = new Action("�½�(&N)...") {
			
			@Override
			public void run() {
				try {
					int tabIndex = dataTypeTabFolder.getSelectionIndex();
					TypeInfo typeInfo = CrossGateEditor.getEditorPool().get(IOUtils.getClass(CrossGateEditor.getTypeKeys().get(tabIndex)));
					EditorObject unit = typeInfo.createWizard();
					if(unit != null){
						CrossGateEditor.getLog().info("Create a new Object : [" + unit.getName() + "].The class is " + unit.getClass() + ".");
					}
					refresh();
				} catch (Exception e) {
					CrossGateEditor.getLog().error(getClass().getName(), e);
					MessageDialog.openError(getSite().getShell(), "����", "��������ʧ�ܣ�ԭ��\n" + e.toString());
				}
			}
			
		};
	}

	/**
	 * �������������͵�Tab
	 * @param 	doc
	 *          jdomģ��
	 * @throws 	IllegalAccessException
	 * @throws 	InstantiationException
	 * @throws 	ClassNotFoundException
	 */
	private void createDataTypeTabs(Document doc) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		List<Element> list = doc.getRootElement().getChildren("extension");// �����չ��Ϣ����
		for (Element element : list) {
			@SuppressWarnings("unchecked")
			List<Element> sublist = element.getChildren("editor");// ��ñ༭����Ϣ����
			if (sublist.size() > 0) {
				list = sublist;
				break;
			}
		}
		dataTypeTabs = new CTabItem[list.size()];// �������������͵�Tab����
		dataListFilterText = new Text[list.size()];
		dataTreeViewers = new TreeViewer[list.size()];
		dataListTree = new Tree[list.size()];
		for (int i = 0; i < list.size(); i++) {// �������������͵�Tab
			// ����xml
			Element element = list.get(i);
			String className = element.getAttributeValue("class");
			String text = element.getAttributeValue("name");
			CrossGateEditor.getTypeKeys().add(className);
			CrossGateEditor.getLog().info("Load " + className + " and the text is " + text + ".");

			// �ؼ�
			dataTypeTabs[i] = new CTabItem(dataTypeTabFolder, SWT.NONE);
			dataTypeTabs[i].setText(text);

			Composite tabComp = new Composite(dataTypeTabFolder, SWT.NONE);
			GridLayout gd_comp = new GridLayout(1, true);
			gd_comp.marginBottom = 0;
			gd_comp.marginHeight = 0;
			gd_comp.marginLeft = 0;
			gd_comp.marginRight = 0;
			gd_comp.marginTop = 0;
			gd_comp.marginWidth = 0;
			gd_comp.verticalSpacing = 0;
			gd_comp.horizontalSpacing = 0;
			tabComp.setLayout(gd_comp);

			// �������������
			dataListFilterText[i] = new Text(tabComp, SWT.BORDER);
			GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, false);
			dataListFilterText[i].setLayoutData(gd_text);
			dataListFilterText[i].addKeyListener(new KeyAdapter() {
				
				@Override
				public void keyReleased(KeyEvent e) {
					if (e.character == '\r') {
						fireFilterText(e.widget);
					}
				}
				
			});

			// ��
			dataTreeViewers[i] = new TreeViewer(tabComp, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
			dataTreeViewers[i].getTree().setLinesVisible(true);
			dataTreeViewers[i].setLabelProvider(new DataObjectLabelProvider());
			dataTreeViewers[i].setContentProvider(new ProjectDataListProvider(IOUtils.getClass(className)));
			dataTreeViewers[i].addDoubleClickListener(new IDoubleClickListener() {
				
				@Override
				public void doubleClick(DoubleClickEvent event) {
					StructuredSelection sel = (StructuredSelection) event.getSelection();
					if (sel.isEmpty()) {
						return;
					}
					Object obj = sel.getFirstElement();
					if (obj instanceof EditorObject) {
						editObject((EditorObject) obj);
					} else if (obj instanceof Category) {
						if (!((Category) obj).name.equals(Category.NEW)) {
							expandOrCollapseNode((TreeViewer) event.getViewer(), obj);
						} else {
							// �½�����
							InputDialog dlg = new InputDialog(getSite().getShell(), "�½�����", "�������·�������ƣ�", "�·���", new IInputValidator() {
								
								@Override
								public String isValid(String newText) {
									return newText.trim().length() == 0 ? "�������Ʋ���Ϊ�ա�" : null;
								}
								
							});
	
							if (dlg.open() != InputDialog.OK) {
								return;
							}
	
							String newname = dlg.getValue();
							int clsIndex = dataTypeTabFolder.getSelectionIndex();
							try {
								Class<?> cls = IOUtils.getClass(CrossGateEditor.getTypeKeys().get(clsIndex));
								newCategory(cls, newname);
								CrossGateEditor.getLog().info("Create a new category : [" + newname + "] in class [" + cls + "].");
								dataTreeViewers[clsIndex].refresh();
							} catch (Exception e) {
								CrossGateEditor.getLog().error(getClass().getName(), e);
								MessageDialog.openError(getSite().getShell(), "����", e.toString());
							}
						}
					}
				}
				
			});
			dataListTree[i] = dataTreeViewers[i].getTree();
			dataListTree[i].setHeaderVisible(true);
			GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true);
			dataListTree[i].setLayoutData(gd_tree);

			// ����DND
			setupDragAndDrop(dataTreeViewers[i]);

			// �����������
			final TreeColumn column1 = new TreeColumn(dataListTree[i], SWT.LEFT);
			column1.setWidth(200);
			column1.setText("ID");

			final TreeColumn column2 = new TreeColumn(dataListTree[i], SWT.LEFT);
			column2.setWidth(200);
			column2.setText("����");

			// �����Ҽ��˵�
			MenuManager mgr = new MenuManager();
			mgr.add(newAction);
			// mgr.add(deleteAction);
			// mgr.add(newCategory);
			// mgr.add(copyAction);
			Menu menu = mgr.createContextMenu(dataListTree[i]);
			dataListTree[i].setMenu(menu);

			// ���ñ������
			dataTreeViewers[i].setInput(new Object());

			// �ѱ�����Tab
			dataTypeTabs[i].setControl(tabComp);

			CrossGateEditor.getLog().info("Create editor [" + text + "] by class " + className);
		}
		CrossGateEditor.getLog().info("CreateDataTypeTabs finish.");
	}

	/**
	 * �������ı������޸ġ�
	 */
	private void fireFilterText(Widget widget) {
		
	}

	/**
	 * ��һ�����ݶ���ı༭����
	 * @param 	
	 * 			���������������
	 * @param 	obj
	 * 			���༭�Ķ���
	 */
	public void editObject(EditorObject obj) {
		TypeInfo typeInfo = obj.getTypeInfo();
		try {
			getSite().getWorkbenchWindow().getActivePage().openEditor(new DataObjectInput(obj), typeInfo.getEditorId());
		} catch (Exception e) {
			CrossGateEditor.getLog().error(getClass().getName() + "::editObject() : editorID = " + typeInfo.getEditorId(), e);
//			CrossGateEditor.catchException(e, false);
			MessageDialog.openError(getSite().getShell(), "����", "�򿪱༭��ʧ�ܣ�ԭ��\n" + e.toString());
		}
	}

	/**
	 * չ��/���Ͻڵ�
	 * @param 	viewer
	 * 			�����б�ؼ�
	 * @param 	node
	 * 			�ڵ�
	 */
	private void expandOrCollapseNode(TreeViewer viewer, Object node) {
		if (viewer.getExpandedState(node)) {
			viewer.collapseToLevel(node, 1);
		} else {
			viewer.expandToLevel(node, 1);
		}
	}

	/**
	 * ����һ���µ����ݷ��ࡣ
	 * @param 	cls
	 *          ���ݶ�������
	 * @param 	name
	 *          ��������
	 * @return 	�µ����ݷ���
	 */
	private Category newCategory(Class<?> cls, String name) throws Exception {
		return CrossGateEditor.getEditorPool().get(cls).newCategory(name);
	}

	/**
	 * �����϶�֧�֡�
	 * @param 	viewer
	 * 			�����б�ؼ�
	 */
	private void setupDragAndDrop(TreeViewer viewer) {
		Tree tree = viewer.getTree();
		final DragSource treeDragSource = new DragSource(tree, DND.DROP_MOVE);
		treeDragSource.addDragListener(new DragSourceAdapter() {
			
			/**
			 * �ж��Ƿ������϶���������󶼿����϶������಻�����϶���
			 */
			@Override
			public void dragStart(DragSourceEvent event) {
				Object[] sels = getSelectedObjects();
				if (sels.length == 0) {
					event.doit = false;
				} else {
					event.doit = false;
					for (int i = 0; i < sels.length; i++) {
						if (sels[i] instanceof EditorObject) {
							event.doit = true;
							break;
						}
					}
				}
			}

			/**
			 * �����϶����ݣ�һ��һ�����󣬸�ʽΪ������:id��
			 */
			@Override
			public void dragSetData(DragSourceEvent event) {
				Object[] sels = getSelectedObjects();
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < sels.length; i++) {
					if (i > 0) {
						buf.append("\n");
					}
//					if (sels[i] instanceof BaseUnit) {
//						buf.append(sels[i].getClass().getName() + ":"
//								+ ((BaseUnit) sels[i]).getId());
//					}
				}
				event.data = buf.toString();
			}

			@Override
			public void dragFinished(DragSourceEvent event) {}
			
		});
		treeDragSource.setTransfer(new Transfer[] {TextTransfer.getInstance()});

		final DropTarget treeDropTarget = new DropTarget(tree, DND.DROP_MOVE);
		treeDropTarget.addDropListener(new DropTargetAdapter() {
			
			@Override
			public void dragEnter(DropTargetEvent event) {}

			@Override
			public void dragLeave(DropTargetEvent event) {}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {}

			/**
			 * ��鵱ǰĿ���Ƿ������Ϸš������϶���һ���·����У��ӵ���󣩣������϶���һ��ָ�����󣨲��뵽ǰ�棩��
			 */
			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_SCROLL;
				event.detail = DND.DROP_NONE;
				if (event.item != null) {
					TextTransfer textTransfer = TextTransfer.getInstance();
					String data = (String) textTransfer
							.nativeToJava(event.currentDataType);
					if (data == null) {
						return;
					}
					TreeItem titem = (TreeItem) event.item;
					Object targetObj = titem.getData();
					if (targetObj instanceof Category
							|| targetObj instanceof EditorObject) {
						event.detail = DND.DROP_MOVE;
					}
				}
			}

			/**
			 * �϶�������
			 */
			@Override
			public void drop(DropTargetEvent event) {
				if (event.data == null || event.item == null) {
					return;
				}
				TreeItem titem = (TreeItem) event.item;
				Object targetObj = titem.getData();
				String data = (String) event.data;
				String[] items = data.split("\n");
				try {
					boolean changed = false;
					TypeInfo typeInfo = null;
					if (targetObj instanceof Category) {
						// �϶���һ����������
						Category targetCate = (Category) targetObj;
						for (String line : items) {
							String[] sec = line.split(":");
							Class<?> cls = IOUtils.getClass(sec[0]);
							if (cls == null) {
								return;
							}
							EditorObject dobj = (EditorObject) CrossGateEditor.getObjectPool().get(sec[0]);
							if (dobj != null && !targetCate.equals(dobj.getCategory())) {
								CrossGateEditor.changeObjectCategory(dobj, targetCate);
								changed = true;
								typeInfo = dobj.getTypeInfo();
							}
						}
					} else if (targetObj instanceof EditorObject) {
						// �϶���һ�������ǰ��
						EditorObject tobj = (EditorObject) targetObj;
						typeInfo = tobj.getTypeInfo();
						TypeInfo info = CrossGateEditor.getEditorPool().get(tobj.getClass());
						Category targetCate = (Category) CrossGateEditor.getObjectPool().get(((Category) info.getCategorys().get(tobj.getCategory())).name);
						int index = targetCate.objects.indexOf(tobj);
						for (String line : items) {
							String[] sec = line.split(":");
							Class<?> cls = IOUtils.getClass(sec[0]);
							if (cls == null) {
								return;
							}
							EditorObject dobj = (EditorObject) CrossGateEditor.getObjectPool().get(sec[0]);
							if (dobj == null || dobj == tobj) {
								continue;
							}
							if (!targetCate.equals(dobj.getCategory())) {
								CrossGateEditor.changeObjectCategory(dobj, targetCate);
							}
							int oldIndex = targetCate.objects.indexOf(dobj);
							if (oldIndex < index) {
								targetCate.objects.remove(dobj);
								index--;
								targetCate.objects.add(index, dobj);
							} else {
								targetCate.objects.remove(dobj);
								targetCate.objects.add(index, dobj);
							}
							index++;
							changed = true;
						}
					}
					if (changed) {
						saveDataList(typeInfo);
						refresh();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void dropAccept(DropTargetEvent event) {}
			
		});
		treeDropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });
	}

	/**
	 * ȡ��ѡ�еĶ���
	 * @return 	ѡ�еĶ���
	 */
	public Object[] getSelectedObjects() {
		int tabIndex = dataTypeTabFolder.getSelectionIndex();
		StructuredSelection sel = (StructuredSelection) dataTreeViewers[tabIndex].getSelection();
		return sel.toArray();
	}

	/**
	 * ����һ�����͵��������ݶ���
	 * @param 	cls
	 *          ���ݶ�������
	 * @throws 	Exception
	 */
	public void saveDataList(TypeInfo typeInfo) throws Exception {
//		saveStart(cls);
		Element root = new Element(typeInfo.getName() + "s");
		Document doc = new Document(root);
		typeInfo.save(doc);
		File path = new File(CrossGateEditor.getClientFilePath());
		FileUtils.saveDOM(doc, new File(path, "editor/" + typeInfo.getName() + ".xml"));
		CrossGateEditor.getLog().info(typeInfo.getName() + " info has been save.");
//		saveEnd(cls);
	}

	/**
	 * �������
	 * @param 	cls
	 * 			��������
	 */
    public void saveEnd(Class<?> cls) {
    	File f = CrossGateEditor.getEditorPool().get(cls).getXML(CrossGateEditor.getBaseFile());
        if (f.exists()) {
        	FileWatcher.watch(f, this);
        }
    }

	/**
	 * ���༭����������ʱ����ʱȡ���������Ա���û�б�Ҫ�ľ��档
	 * @param	cls
	 * 			������
	 */
    public void saveStart(Class<?> cls) {
		File f = CrossGateEditor.getEditorPool().get(cls).getXML(CrossGateEditor.getBaseFile());
		if (f.exists()) {
			FileWatcher.unwatch(f, this);
		}
	}

	/**
	 * ˢ�������б�
	 */
	public void refresh() {
		for (ContentViewer viewer : dataTreeViewers) {
			viewer.refresh();
		}
	}

	/**
	 * ˢ��ĳһ���͵������б�
	 * @param 	name
	 *          �༭������
	 */
	public void refresh(String name) {
		for (int i = 0; i < CrossGateEditor.getTypeKeys().size(); i++) {
			if (CrossGateEditor.getTypeKeys().get(i).equals(name)) {
				dataTreeViewers[i].refresh();
				break;
			}
		}
	}

	/**
	 * ˢ��һ���������ʾ��
	 * @param	obj
	 * 			�༭������
	 */
	public  void refresh(EditorObject obj) {
		TypeInfo[] infos = CrossGateEditor.getEditorPool().toArray();
		for (int i = 0; i < infos.length; i++) {
			if (infos[i].isMine(obj.getClass())) {
				dataTreeViewers[i].refresh(obj);
				break;
			}
		}
	}

	@Override
	public void fileModified(File f) {
		TypeInfo[] infos = CrossGateEditor.getEditorPool().toArray();
		File base = CrossGateEditor.getBaseFile();
		for (TypeInfo info : infos) {
			if(info.getXML(base).equals(f)){
				getSite().getShell().getDisplay().asyncExec(new DataChangedHandler(info.getName()));
			}
		}
	}

	/**
	 * ���ݱ仯������
	 * @author 	hyfu
	 */
	private class DataChangedHandler implements Runnable {
    	
    	/**
    	 * �仯��������
    	 */
        private String name;

        /**
         * ����
         * @param 	name
         * 			�仯��������
         */
        public DataChangedHandler(String name) {
        	this.name = name;
        }

        public void run() {
            String msg = name + "���ݱ��ⲿ����ı䣬�Ƿ����أ�\n" + "ע�����ѡ���ǣ������Ѵ򿪵ı༭���ڽ����رգ����ݲ��ᱣ�棻���ѡ���"
                    + "��ô��������༭����������ʱ�����п��ܸ��Ǳ��˵��޸ģ�";
            if (MessageDialog.openConfirm(getSite().getShell(), "����", msg) == false) {
                return;
            }
            try {
                // ����
            	CrossGateEditor.getReloadManager().reload();

                // ˢ���б�
                for (TreeViewer tv : dataTreeViewers) {
                    tv.refresh();
                }

                // �ر����б༭��
                IEditorReference[] refs = getSite().getWorkbenchWindow().getActivePage().getEditorReferences();
                for (IEditorReference ref : refs) {
                    IEditorPart editor = ref.getEditor(false);
                    if (editor != null) {
                        getSite().getWorkbenchWindow().getActivePage().closeEditor(editor, false);
                    }
                }
            } catch (Exception e) {
            	CrossGateEditor.getLog().error(getClass().getName(), e);
//            	CrossGateEditor.catchException(e, false);
                MessageDialog.openError(getSite().getShell(), "����", e.toString());
            }
        }
    }
	
}
