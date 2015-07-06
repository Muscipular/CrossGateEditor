package cg.editor.map;

import org.eclipse.swt.widgets.Composite;

public class ObjectBar extends GroundBar {

	public ObjectBar(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	protected void replace(EditorMapCell mapCell) {
		mapCell.setObjectId(imageGlobalId);
	}

}
