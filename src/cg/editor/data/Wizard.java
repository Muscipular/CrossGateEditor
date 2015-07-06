package cg.editor.data;

/**
 * 创建向导
 * @author 	hyfu
 */
public interface Wizard<T extends EditorObject> {

	/**
	 * 创建
	 */
	T create(TypeInfo typeInfo) throws Exception;
	
}
