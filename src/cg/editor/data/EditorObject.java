package cg.editor.data;

import org.jdom.Element;

/**
 * �༭����
 * @author 	hyfu
 */
public interface EditorObject {
    
    /**
     * �������ݶ���
     * @param 	temp
     * 			�༭����ǰ��������
     * @return 	��������б仯������true
     */
    <T extends EditorObject> void update(T temp);
    /**
     * �����һ��XML��ǩ��
     * @return	XML��ǩ
     * @throws 	Exception 
     */
    Element save();
    /**
     * ��ö������
     * @return	�������
     */
    String getCategory();
    /**
     * ��ö������
     * @return	�������
     */
    void setCategory(String cate);
    
    void load(Element element);
    
    int getId();
    
    void setId(int id);
    
    String getName();
    
    void setName(String name);
    
    TypeInfo getTypeInfo();
    
    void setTypeInfo(TypeInfo typeInfo);
    
}
