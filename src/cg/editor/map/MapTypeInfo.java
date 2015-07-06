package cg.editor.map;

import org.jdom.Document;

import cg.data.map.MapInfo;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.TypeInfo;

public class MapTypeInfo extends TypeInfo {
	
	public MapTypeInfo() {
		super();
	}
	
	@Override
	protected void loadUnit(Document doc, String name) throws Exception {
		MapInfo[] mapInfos = CrossGateEditor.getMapReader().load();
		super.loadUnit(doc, name);
		for (MapInfo mapInfo : mapInfos) {
			EditorMap editorMap = (EditorMap) getUnits().get(mapInfo.getName());
			editorMap.setImageReader(CrossGateEditor.getImageManager().getImageReader());
			editorMap.setSource(mapInfo);
		}
	}
	

}
