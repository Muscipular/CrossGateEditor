package cg.editor.map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import cg.editor.View;
import cg.editor.data.Category;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.TypeInfo;
import cg.editor.data.Wizard;

/**
 * 地图创建向导
 * @author 	hyfu
 */
public class MapWizard implements Wizard<EditorMap> {

	@Override
	public EditorMap create(TypeInfo typeInfo) throws Exception {
		// 获得View
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        View view = (View) page.findView(View.ID);
        // 创建地图对话框
        NewMapDialog dlg = new NewMapDialog(shell);
        // 输入完成，获得输入的内容
        if (dlg.open() != Dialog.OK) {
            return null;
        }
        // 创建地图对象
        EditorMap editorObject = new EditorMap();
        editorObject.setImageReader(CrossGateEditor.getImageManager().getImageReader());
        editorObject.setTypeInfo(typeInfo);
        editorObject.setName(dlg.getName());
        editorObject.setSize(dlg.getEast(), dlg.getSouth());
        Object[] obj = view.getSelectedObjects();
        if (obj != null && obj.length > 0) {
        	Category cate = (Category) view.getSelectedObjects()[0];
        	if (!cate.name.equals(Category.NEW)) {
        		editorObject.setCategory(cate.name);
        	}
        }
        // 保存地图对象
		typeInfo.addUnit(editorObject);
		// 创建编辑界面
        view.refresh(MapEditor.class.getName());
        view.editObject(editorObject);
        // 保存本类型数据列表
        view.saveDataList(typeInfo);
        
        return editorObject;
	}
	
}
