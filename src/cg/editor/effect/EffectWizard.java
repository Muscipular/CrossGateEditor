package cg.editor.effect;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import cg.editor.View;
import cg.editor.data.Category;
import cg.editor.data.TypeInfo;
import cg.editor.data.Wizard;

/**
 * 效果创建向导
 * @author 	hyfu
 */
public class EffectWizard implements Wizard<Effect> {

	@Override
	public Effect create(TypeInfo typeInfo) throws Exception {
		//获得View
		Shell shell=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        View view = (View) page.findView(View.ID);
        //创建效果对话框
        InputDialog dlg = new InputDialog(shell, "新建效果", "请输入效果名称:", "新效果", new IInputValidator(){
            public String isValid(String newText){
                if(newText.trim().length()==0){
                    return "效果为空";
                } else {
                    return null;
                }
            }
        });
        //输入完成，获得输入的内容
        if(dlg.open() != InputDialog.OK){
            return null;
        }
        String newname = dlg.getValue();
        //创建效果对象
        Effect effect = new Effect();
        effect.setTypeInfo(typeInfo);
        effect.setName(newname);
        Object[] obj = view.getSelectedObjects();
        if(obj != null && obj.length > 0){
        	Category cate = (Category) view.getSelectedObjects()[0];
            effect.setCategory(cate.name);
        }
        //保存效果对象
		typeInfo.addUnit(effect);
		//创建编辑界面
        view.refresh(EffectEditor.class.getName());
        view.editObject(effect);
        //保存本类型数据列表
        view.saveDataList(typeInfo);
        
        return effect;
	}
}
