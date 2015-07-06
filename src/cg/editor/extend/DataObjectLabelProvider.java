package cg.editor.extend;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import cg.editor.Activator;
import cg.editor.data.Category;
import cg.editor.data.EditorObject;

/**
 * ���ݶ������ı��ṩ�ߡ�ÿ��������һ�����ݣ�����ID���������С�
 * @author lighthu
 */
public class DataObjectLabelProvider extends LabelProvider implements ITableLabelProvider {
	
	@Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof EditorObject) {
        	EditorObject dobj = (EditorObject) element;
            if (columnIndex == 0) {
                return String.valueOf(dobj.getId());
            } else {
                return dobj.getName();
            }
        } else {
        	return element.toString();
        }
    }

	@Override
    public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
        	if(element instanceof Category) {
            	return Activator.getDefault().getImageRegistry().get("itemtype");
            } else {
            	return Activator.getDefault().getImageRegistry().get("item");
            }
		} 
		return null;
	}
}