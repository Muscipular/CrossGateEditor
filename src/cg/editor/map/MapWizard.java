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
 * ��ͼ������
 * @author 	hyfu
 */
public class MapWizard implements Wizard<EditorMap> {

	@Override
	public EditorMap create(TypeInfo typeInfo) throws Exception {
		// ���View
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        View view = (View) page.findView(View.ID);
        // ������ͼ�Ի���
        NewMapDialog dlg = new NewMapDialog(shell);
        // ������ɣ�������������
        if (dlg.open() != Dialog.OK) {
            return null;
        }
        // ������ͼ����
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
        // �����ͼ����
		typeInfo.addUnit(editorObject);
		// �����༭����
        view.refresh(MapEditor.class.getName());
        view.editObject(editorObject);
        // ���汾���������б�
        view.saveDataList(typeInfo);
        
        return editorObject;
	}
	
}
