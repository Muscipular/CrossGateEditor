package cg.editor.image;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import cg.base.image.ImageManager;
import cg.editor.View;
import cg.editor.data.Category;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.TypeInfo;
import cg.editor.data.Wizard;
import cg.editor.image.runLength.RunLengther;

public class ImageWizard implements Wizard<EditorImageSet> {
	
	public static RunLengther runLengther = new RunLengther(CrossGateEditor.getLog(), CrossGateEditor.getImageManager());

	@Override
	public EditorImageSet create(TypeInfo typeInfo) throws Exception {
		// ���View
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        View view = (View) page.findView(View.ID);
        // ����ͼƬ�Ի���
        NewImageDialog dialog = new NewImageDialog(shell);
        // ������ɣ�������������
        if (dialog.open() != Dialog.OK) {
            return null;
        }
        // ����ͼƬ����
        ImageManager imageManager = CrossGateEditor.getImageManager();
        EditorImageSet editorImageSet = new EditorImageSet(dialog.getPath(), dialog.getSuffix(), imageManager, runLengther);
        editorImageSet.setTypeInfo(typeInfo);
        editorImageSet.setName(dialog.getName());
        
        Object[] obj = view.getSelectedObjects();
        if (obj != null && obj.length > 0) {
        	Category cate = (Category) view.getSelectedObjects()[0];
        	if (!cate.name.equals(Category.NEW)) {
        		editorImageSet.setCategory(cate.name);
        	}
        }
        // ����ͼƬ����
		typeInfo.addUnit(editorImageSet);
		// �����༭����
        view.refresh(ImageEditor.class.getName());
        view.editObject(editorImageSet);
        // ���汾���������б�
        view.saveDataList(typeInfo);
        
        imageManager.addResource(editorImageSet);
        
        return editorImageSet;
	}

}
