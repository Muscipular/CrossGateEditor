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
 * ���ݶ������α�������ṩ�ߡ�ÿ��������һ�����ݡ�
 * @author 	hyfu
 */
public class ProjectDataListProvider implements ITreeContentProvider {
	
	/**
	 * ��������
	 */
    private Class<?> dataClass;
    /**
     * ��������
     */
    private String filterText;

    /**
     * ����
     * @param 	cls
     * 			��������
     */
    public ProjectDataListProvider(Class<?> cls) {
        dataClass = cls;
    }
    
    /**
     * ���ù�������
     * @param 	text
     * 			��������
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
    
    //�ݹ���ӹ��˵���Ŀ
    private void findFilterObject(Collection<Category> cates, Collection<Category> list) {
        // ��������˹��ˣ���ֻ��ʾ���˺������ݵķ���
        String ft = filterText.toLowerCase();
        Iterator<Category> itor = cates.iterator();
        while (itor.hasNext()) {
            Category cate = (Category) itor.next();
            for (EditorObject dobj : cate.objects) {
                if (dobj.toString().toLowerCase().contains(ft)) {
                    list.add(cate);//��ʱ��ӵĶ��Ƿ������
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
	        	//�ϲ���������
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