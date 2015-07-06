package cg.editor.data;

import org.jdom.Element;

/**
 * 编辑对象
 * @author 	hyfu
 */
public interface EditorObject {
    
    /**
     * 更新数据对象。
     * @param 	temp
     * 			编辑器当前输入内容
     * @return 	如果数据有变化，返回true
     */
    <T extends EditorObject> void update(T temp);
    /**
     * 保存成一个XML标签。
     * @return	XML标签
     * @throws 	Exception 
     */
    Element save();
    /**
     * 获得对象分类
     * @return	对象分类
     */
    String getCategory();
    /**
     * 获得对象分类
     * @return	对象分类
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
