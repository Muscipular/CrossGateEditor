package cg.editor.extend;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import cg.editor.Activator;
import cg.editor.data.EditorObject;

/**
 * ���ڱ༭һ�����ݶ�����������
 * @author lighthu
 */
public class DataObjectInput implements IEditorInput {
	
	/**
	 * �༭����
	 */
    protected EditorObject dataObject;;
    
    /**
     * ����
     * @param 	m
     * 			�༭����
     */
    public DataObjectInput(EditorObject m) {
        dataObject = m;
    }
    
    /**
     * ��ñ༭����
     * @return	�༭����
     */
    public EditorObject getDataObject() {
    	return dataObject;
    }
    
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }

    public boolean exists() {
        return false;
    }

    public ImageDescriptor getImageDescriptor() {
        return Activator.getDefault().getImageRegistry().getDescriptor("dataobj");
    }

    public String getName() {
        return dataObject.toString();
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public String getToolTipText() {
        return dataObject.toString();
    }
    
    public boolean equals(Object o) {
        if (o == null || !(o instanceof DataObjectInput)) {
            return false;
        }
        return dataObject.equals(((DataObjectInput) o).dataObject);
    }
}
