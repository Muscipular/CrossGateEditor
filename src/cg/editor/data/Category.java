package cg.editor.data;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

/**
 * ���ݶ������Ŀ¼��
 * @author lighthu
 */
public class Category {
	
	/**
	 * �½�����
	 */
	public static final String NEW = "�½�����";
	/**
	 * δ����
	 */
	public static final String NOT = "";
	
    /**
     * ��Ӧ���ݶ����ࡣ
     */
    public Class<?> dataClass;
    /**
     * ��������ͬһ�������ݵķ����������ظ���
     */
    public String name;
    /**
     * ��������б�
     */
    public List<EditorObject> objects;
    /**
     * �����ٴΰ�������Ŀ¼
     */
    public List<Category> cates;
    /**
     * ������Ŀ¼
     */
    public String parent;
    
    /**
     * ����
     * @param 	cls
     * 			��Ӧ���ݶ����ࡣ
     */
    public Category(Class<?> cls) {
        dataClass = cls;
        objects = new ArrayList<EditorObject>();
        cates = new ArrayList<Category>();
    }
    
    @Override
    public String toString() {
        if (name == null || name.length() == 0) {
            return "<δ����>";
        }
        return name;
    }
    
    /**
     * �����xmlԪ��
     * @return	xmlԪ��
     */
    public Element save() {
    	Element ret = new Element("category");

        ret.addAttribute("name", name);
        ret.addAttribute("parent", parent == null ? "" : parent);
        
		return ret;
    }
}