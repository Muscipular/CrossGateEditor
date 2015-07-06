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
 * Ч��������
 * @author 	hyfu
 */
public class EffectWizard implements Wizard<Effect> {

	@Override
	public Effect create(TypeInfo typeInfo) throws Exception {
		//���View
		Shell shell=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        View view = (View) page.findView(View.ID);
        //����Ч���Ի���
        InputDialog dlg = new InputDialog(shell, "�½�Ч��", "������Ч������:", "��Ч��", new IInputValidator(){
            public String isValid(String newText){
                if(newText.trim().length()==0){
                    return "Ч��Ϊ��";
                } else {
                    return null;
                }
            }
        });
        //������ɣ�������������
        if(dlg.open() != InputDialog.OK){
            return null;
        }
        String newname = dlg.getValue();
        //����Ч������
        Effect effect = new Effect();
        effect.setTypeInfo(typeInfo);
        effect.setName(newname);
        Object[] obj = view.getSelectedObjects();
        if(obj != null && obj.length > 0){
        	Category cate = (Category) view.getSelectedObjects()[0];
            effect.setCategory(cate.name);
        }
        //����Ч������
		typeInfo.addUnit(effect);
		//�����༭����
        view.refresh(EffectEditor.class.getName());
        view.editObject(effect);
        //���汾���������б�
        view.saveDataList(typeInfo);
        
        return effect;
	}
}
