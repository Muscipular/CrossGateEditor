package cg.editor.data;

import java.io.File;
import java.util.Hashtable;

import cg.editor.extend.DefaultDataObjectEditor;

/**
 * ������Ϣ����
 * @author 	hyfu
 */
public class TypeMap {
	
	/**
	 * ����ʵ��
	 */
	private static TypeMap instance;

	/**
	 * ����Ϊ���ļ���
	 */
	private Hashtable<String, TypeInfo> nameKeys;
	/**
	 * �༭����Ϊ���ļ���
	 */
	private Hashtable<Class<?>, TypeInfo> editorKeys;
	/**
	 * ���༯��
	 */
	private Hashtable<String, Category> categorys;
	
	/**
	 * ��ø���ʵ��
	 * @return	����ʵ��
	 */
	public static TypeMap getInstance() {
		if(instance == null){
			instance = new TypeMap();
		}
		return instance;
	}
	
	/**
	 * ����
	 */
	private TypeMap() {
		nameKeys = new Hashtable<String, TypeInfo>();
		editorKeys = new Hashtable<Class<?>, TypeInfo>();
		categorys = new Hashtable<String, Category>();
	}

	/**
	 * ��ʹ�� {@link #save(TypeInfo)}
	 */
	@Deprecated
	public void save(String key, TypeInfo value) {
		save(value);
	}

	public TypeInfo delete(String key) {
		if(nameKeys.containsKey(key)){
			return editorKeys.remove(nameKeys.remove(key).getEditor());
		}
		return null;
	}

	public void deleteValue(TypeInfo value) {
		for(String key : nameKeys.keySet()){
			if(nameKeys.get(key).equals(value)){
				delete(key);
			}
		}
	}

	public TypeInfo get(String key) {
		return nameKeys.get(key);
	}

	@SuppressWarnings("unchecked")
	public <X extends TypeInfo> X get(String key, Class<X> cls) {
		TypeInfo value = get(key);
		return (X) value;
	}

	public void reset() {
		nameKeys.clear();
		editorKeys.clear();
	}

	public void save(TypeInfo value) {
		nameKeys.put(value.getName(), value);
		editorKeys.put(value.getEditor(), value);
	}

	public TypeInfo get(Class<?> key) {
		if (key.getSuperclass().equals(DefaultDataObjectEditor.class)) {
			return editorKeys.get(key);
		} else if(key.getInterfaces().length > 0 && key.getInterfaces()[0].equals(EditorObject.class)) {
			return get(key.getSimpleName());
		} else {
			return null;
		}
	}

	public TypeInfo delete(Class<?> key) {
		if(editorKeys.containsKey(key)){
			return delete(editorKeys.get(key).getName());
		}
		return null;
	}

	public TypeInfo[] toArray() {
		return nameKeys.values().toArray(new TypeInfo[size()]);
	}

	public int size() {
		return nameKeys.size();
	}

	public <T extends EditorObject> File getDataFile(Class<T> cls, File base) {
		return get(cls).getXML(base);
	}

	public Hashtable<String, Category> getCategorys() {
		return categorys;
	}
	
}
