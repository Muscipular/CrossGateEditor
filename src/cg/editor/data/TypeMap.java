package cg.editor.data;

import java.io.File;
import java.util.Hashtable;

import cg.editor.extend.DefaultDataObjectEditor;

/**
 * 类型信息集合
 * @author 	hyfu
 */
public class TypeMap {
	
	/**
	 * 该类实例
	 */
	private static TypeMap instance;

	/**
	 * 名称为键的集合
	 */
	private Hashtable<String, TypeInfo> nameKeys;
	/**
	 * 编辑类型为键的集合
	 */
	private Hashtable<Class<?>, TypeInfo> editorKeys;
	/**
	 * 分类集合
	 */
	private Hashtable<String, Category> categorys;
	
	/**
	 * 获得该类实例
	 * @return	该类实例
	 */
	public static TypeMap getInstance() {
		if(instance == null){
			instance = new TypeMap();
		}
		return instance;
	}
	
	/**
	 * 构造
	 */
	private TypeMap() {
		nameKeys = new Hashtable<String, TypeInfo>();
		editorKeys = new Hashtable<Class<?>, TypeInfo>();
		categorys = new Hashtable<String, Category>();
	}

	/**
	 * 请使用 {@link #save(TypeInfo)}
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
