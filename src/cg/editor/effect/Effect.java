package cg.editor.effect;

import org.jdom.Element;

import cg.editor.data.BaseEditorObject;
import cg.editor.data.EditorObject;

/**
 * 效果编辑对象
 * @author 	hyfu
 */
public class Effect extends BaseEditorObject {

	@SuppressWarnings("hiding")
	@Override
	public <Effect extends EditorObject> void update(Effect temp) {
		super.update(temp);
	}

	@Override
	public Element save() {
		Element ret = new Element("effect");

		save(ret);
        
		return ret;
	}
	
}