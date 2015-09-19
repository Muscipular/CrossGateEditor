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
	 * 包含所有数据类型的面板
	 */
	private CTabFolder dataTypeTabFolder;
	/**
	 * 各数据类型的Tab
	 */
	private CTabItem[] dataTypeTabs;
	/**
	 * 各数据类型的过滤输入框
	 */
	private Text[] dataListFilterText;
	/**
	 * 各数据类型的表格
	 */
	private TreeViewer[] dataTreeViewers;
	/**
	 * 各数据类型的表格
	 */
	private Tree[] dataListTree;
	/**
	 * 命令：新建对象
	 */
	private Action newAction;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		// 默认
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		
		dataTypeTabFolder = new CTabFolder(container, SWT.NONE); // 创建包含所有数据类型的面板
		
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
		newAction = new Action("新建(&N)...") {
			
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
					MessageDialog.openError(getSite().getShell(), "错误", "创建对象失败，原因：\n" + e.toString());
				}
			}
			
		};
	}

	/**
	 * 创建各数据类型的Tab
	 * @param 	doc
	 *          jdom模型
	 * @throws 	IllegalAccessException
	 * @throws 	InstantiationException
	 * @throws 	ClassNotFoundException
	 */
	private void createDataTypeTabs(Document doc) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		@SuppressWarnings("unchecked")
		List<Element> list = doc.getRootElement().getChildren("extension");// 获得扩展信息集合
		for (Element element : list) {
			@SuppressWarnings("unchecked")
			List<Element> sublist = element.getChildren("editor");// 获得编辑器信息集合
			if (sublist.size() > 0) {
				list = sublist;
				break;
			}
		}
		dataTypeTabs = new CTabItem[list.size()];// 创建各数据类型的Tab数组
		dataListFilterText = new Text[list.size()];
		dataTreeViewers = new TreeViewer[list.size()];
		dataListTree = new Tree[list.size()];
		for (int i = 0; i < list.size(); i++) {// 创建各数据类型的Tab
			// 解析xml
			Element element = list.get(i);
			String className = element.getAttributeValue("class");
			String text = element.getAttributeValue("name");
			CrossGateEditor.getTypeKeys().add(className);
			CrossGateEditor.getLog().info("Load " + className + " and the text is " + text + ".");

			// 控件
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

			// 创建过滤输入框
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

			// 树
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
							// 新建分类
							InputDialog dlg = new InputDialog(getSite().getShell(), "新建分类", "请输入新分类的名称：", "新分类", new IInputValidator() {
								
								@Override
								public String isValid(String newText) {
									return newText.trim().length() == 0 ? "分类名称不能为空。" : null;
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
								MessageDialog.openError(getSite().getShell(), "错误", e.toString());
							}
						}
					}
				}
				
			});
			dataListTree[i] = dataTreeViewers[i].getTree();
			dataListTree[i].setHeaderVisible(true);
			GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true);
			dataListTree[i].setLayoutData(gd_tree);

			// 设置DND
			setupDragAndDrop(dataTreeViewers[i]);

			// 设置树表格列
			final TreeColumn column1 = new TreeColumn(dataListTree[i], SWT.LEFT);
			column1.setWidth(200);
			column1.setText("ID");

			final TreeColumn column2 = new TreeColumn(dataListTree[i], SWT.LEFT);
			column2.setWidth(200);
			column2.setText("名称");

			// 创建右键菜单
			MenuManager mgr = new MenuManager();
			mgr.add(newAction);
			// mgr.add(deleteAction);
			// mgr.add(newCategory);
			// mgr.add(copyAction);
			Menu menu = mgr.createContextMenu(dataListTree[i]);
			dataListTree[i].setMenu(menu);

			// 设置表格内容
			dataTreeViewers[i].setInput(new Object());

			// 把表格加入Tab
			dataTypeTabs[i].setControl(tabComp);

			CrossGateEditor.getLog().info("Create editor [" + text + "] by class " + className);
		}
		CrossGateEditor.getLog().info("CreateDataTypeTabs finish.");
	}

	/**
	 * 过滤器文本内容修改。
	 */
	private void fireFilterText(Widget widget) {
		
	}

	/**
	 * 打开一个数据对象的编辑器。
	 * @param 	
	 * 			基础对象的衍生类
	 * @param 	obj
	 * 			被编辑的对象
	 */
	public void editObject(EditorObject obj) {
		TypeInfo typeInfo = obj.getTypeInfo();
		try {
			getSite().getWorkbenchWindow().getActivePage().openEditor(new DataObjectInput(obj), typeInfo.getEditorId());
		} catch (Exception e) {
			CrossGateEditor.getLog().error(getClass().getName() + "::editObject() : editorID = " + typeInfo.getEditorId(), e);
//			CrossGateEditor.catchException(e, false);
			MessageDialog.openError(getSite().getShell(), "错误", "打开编辑器失败，原因：\n" + e.toString());
		}
	}

	/**
	 * 展开/合上节点
	 * @param 	viewer
	 * 			树型列表控件
	 * @param 	node
	 * 			节点
	 */
	private void expandOrCollapseNode(TreeViewer viewer, Object node) {
		if (viewer.getExpandedState(node)) {
			viewer.collapseToLevel(node, 1);
		} else {
			viewer.expandToLevel(node, 1);
		}
	}

	/**
	 * 创建一个新的数据分类。
	 * @param 	cls
	 *          数据对象类型
	 * @param 	name
	 *          类型名称
	 * @return 	新的数据分类
	 */
	private Category newCategory(Class<?> cls, String name) throws Exception {
		return CrossGateEditor.getEditorPool().get(cls).newCategory(name);
	}

	/**
	 * 设置拖动支持。
	 * @param 	viewer
	 * 			树型列表控件
	 */
	private void setupDragAndDrop(TreeViewer viewer) {
		Tree tree = viewer.getTree();
		final DragSource treeDragSource = new DragSource(tree, DND.DROP_MOVE);
		treeDragSource.addDragListener(new DragSourceAdapter() {
			
			/**
			 * 判断是否允许拖动。任意对象都可以拖动。分类不允许拖动。
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
			 * 设置拖动数据，一行一个对象，格式为：类名:id。
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
			 * 检查当前目标是否允许拖放。允许拖动到一个新分类中（加到最后），或者拖动到一个指定对象（插入到前面）。
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
			 * 拖动结束。
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
						// 拖动到一个分类的最后
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
						// 拖动到一个对象的前面
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
	 * 取得选中的对象。
	 * @return 	选中的对象
	 */
	public Object[] getSelectedObjects() {
		int tabIndex = dataTypeTabFolder.getSelectionIndex();
		StructuredSelection sel = (StructuredSelection) dataTreeViewers[tabIndex].getSelection();
		return sel.toArray();
	}

	/**
	 * 保存一个类型的所有数据对象。
	 * @param 	cls
	 *          数据对象类型
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
	 * 保存结束
	 * @param 	cls
	 * 			保存类型
	 */
    public void saveEnd(Class<?> cls) {
    	File f = CrossGateEditor.getEditorPool().get(cls).getXML(CrossGateEditor.getBaseFile());
        if (f.exists()) {
        	FileWatcher.watch(f, this);
        }
    }

	/**
	 * 当编辑器保存数据时，暂时取消监听，以避免没有必要的警告。
	 * @param	cls
	 * 			数据类
	 */
    public void saveStart(Class<?> cls) {
		File f = CrossGateEditor.getEditorPool().get(cls).getXML(CrossGateEditor.getBaseFile());
		if (f.exists()) {
			FileWatcher.unwatch(f, this);
		}
	}

	/**
	 * 刷新数据列表。
	 */
	public void refresh() {
		for (ContentViewer viewer : dataTreeViewers) {
			viewer.refresh();
		}
	}

	/**
	 * 刷新某一类型的数据列表。
	 * @param 	name
	 *          编辑类名称
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
	 * 刷新一个对象的显示。
	 * @param	obj
	 * 			编辑器对象
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
	 * 数据变化处理类
	 * @author 	hyfu
	 */
	private class DataChangedHandler implements Runnable {
    	
    	/**
    	 * 变化的类名称
    	 */
        private String name;

        /**
         * 构造
         * @param 	name
         * 			变化的类名称
         */
        public DataChangedHandler(String name) {
        	this.name = name;
        }

        public void run() {
            String msg = name + "数据被外部程序改变，是否重载？\n" + "注：如果选择是，所有已打开的编辑窗口将被关闭，数据不会保存；如果选择否，"
                    + "那么在你继续编辑并保存数据时，将有可能覆盖别人的修改！";
            if (MessageDialog.openConfirm(getSite().getShell(), "警告", msg) == false) {
                return;
            }
            try {
                // 重载
            	CrossGateEditor.getReloadManager().reload();

                // 刷新列表
                for (TreeViewer tv : dataTreeViewers) {
                    tv.refresh();
                }

                // 关闭所有编辑器
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
                MessageDialog.openError(getSite().getShell(), "错误", e.toString());
            }
        }
    }
	
}
