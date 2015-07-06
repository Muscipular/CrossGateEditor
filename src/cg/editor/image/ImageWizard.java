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
		// 获得View
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        View view = (View) page.findView(View.ID);
        // 创建图片对话框
        NewImageDialog dialog = new NewImageDialog(shell);
        // 输入完成，获得输入的内容
        if (dialog.open() != Dialog.OK) {
            return null;
        }
        // 创建图片对象
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
        // 保存图片对象
		typeInfo.addUnit(editorImageSet);
		// 创建编辑界面
        view.refresh(ImageEditor.class.getName());
        view.editObject(editorImageSet);
        // 保存本类型数据列表
        view.saveDataList(typeInfo);
        
        imageManager.addResource(editorImageSet);
        
        return editorImageSet;
	}

}
