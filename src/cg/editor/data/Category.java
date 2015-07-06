package cg.editor.data;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

/**
 * 数据对象分类目录。
 * @author lighthu
 */
public class Category {
	
	/**
	 * 新建分类
	 */
	public static final String NEW = "新建分类";
	/**
	 * 未分类
	 */
	public static final String NOT = "";
	
    /**
     * 对应数据对象类。
     */
    public Class<?> dataClass;
    /**
     * 分类名。同一类型数据的分类名不能重复。
     */
    public String name;
    /**
     * 分类对象列表。
     */
    public List<EditorObject> objects;
    /**
     * 允许再次包含分类目录
     */
    public List<Category> cates;
    /**
     * 父分类目录
     */
    public String parent;
    
    /**
     * 构造
     * @param 	cls
     * 			对应数据对象类。
     */
    public Category(Class<?> cls) {
        dataClass = cls;
        objects = new ArrayList<EditorObject>();
        cates = new ArrayList<Category>();
    }
    
    @Override
    public String toString() {
        if (name == null || name.length() == 0) {
            return "<未分类>";
        }
        return name;
    }
    
    /**
     * 保存成xml元素
     * @return	xml元素
     */
    public Element save() {
    	Element ret = new Element("category");

        ret.addAttribute("name", name);
        ret.addAttribute("parent", parent == null ? "" : parent);
        
		return ret;
    }
}