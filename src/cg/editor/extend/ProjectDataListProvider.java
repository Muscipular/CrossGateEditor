package cg.editor.extend;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import cg.editor.data.Category;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.EditorObject;

/**
 * 数据对象树形表格数据提供者。每个表格包含一类数据。
 * @author 	hyfu
 */
public class ProjectDataListProvider implements ITreeContentProvider {
	
	/**
	 * 数据类型
	 */
    private Class<?> dataClass;
    /**
     * 过滤内容
     */
    private String filterText;

    /**
     * 构造
     * @param 	cls
     * 			数据类型
     */
    public ProjectDataListProvider(Class<?> cls) {
        dataClass = cls;
    }
    
    /**
     * 设置过滤内容
     * @param 	text
     * 			过滤内容
     */
    public void setFilterText(String text) {
        filterText = text;
    }
    
	public Object[] getElements(Object inputElement) {
    	Collection<Category> list;
    	if (filterText == null || filterText.length() == 0) {
			list = CrossGateEditor.getCategoryList(dataClass);
    	} else {
    		list = new ArrayList<Category>();
            findFilterObject(CrossGateEditor.getCategoryList(dataClass), (List<Category>) list);
    	}
    	return list.toArray();
    }
    
    //递归添加过滤的项目
    private void findFilterObject(Collection<Category> cates, Collection<Category> list) {
        // 如果设置了过滤，则只显示过滤后又内容的分类
        String ft = filterText.toLowerCase();
        Iterator<Category> itor = cates.iterator();
        while (itor.hasNext()) {
            Category cate = (Category) itor.next();
            for (EditorObject dobj : cate.objects) {
                if (dobj.toString().toLowerCase().contains(ft)) {
                    list.add(cate);//暂时添加的都是分类对象
                    break;
                }
            }
            
            if(cate.cates != null) {
                findFilterObject(cate.cates, list);
            }
        }
    }
    
    public void dispose() {}
    
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	public Object[] getChildren(Object parentElement) {
	    if (parentElement instanceof Category) {
	        Category cate = (Category) parentElement;
	        if (filterText == null || filterText.length() == 0) {
	        	//合并两类数据
	            Object[] ret = new Object[cate.objects.size() + cate.cates.size()];
	            System.arraycopy(cate.objects.toArray(), 0, ret, 0, cate.objects.size());
	            System.arraycopy(cate.cates.toArray(), 0, ret, cate.objects.size(), cate.cates.size());
	            return ret;
	        } else {
	            String ft = filterText.toLowerCase();
	            List<EditorObject> matchList = new ArrayList<EditorObject>();
	            for (EditorObject dobj : cate.objects) {
	                if (dobj.toString().toLowerCase().contains(ft)) {
                        matchList.add(dobj);
                    }
	            }
	            return matchList.toArray();
	        }
	    }
		return null;
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
	    return getChildren(element) != null;
	}
}