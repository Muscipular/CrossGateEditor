package cg.editor.data;

/**
 * ������
 * @author 	hyfu
 */
public interface Wizard<T extends EditorObject> {

	/**
	 * ����
	 */
	T create(TypeInfo typeInfo) throws Exception;
	
}
