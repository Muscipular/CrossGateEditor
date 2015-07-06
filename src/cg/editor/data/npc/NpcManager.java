package cg.editor.data.npc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cg.data.sprite.NpcTemplate;
import cg.editor.data.CrossGateEditor;

public class NpcManager {
	
	private Map<Integer, List<NpcTemplate>> npcInfos;
	
	public NpcManager() {
		npcInfos = new HashMap<Integer, List<NpcTemplate>>();
		List<NpcTemplate> list = CrossGateEditor.getProjectData().getReader(NpcTemplate.class).read(CrossGateEditor.getProjectData());
		for (int i = 0;i < list.size();i++) {
			NpcTemplate npcInfo = list.get(i);
			if (!npcInfos.containsKey(npcInfo.getMapId())) {
				List<NpcTemplate> npcInfoList = new ArrayList<NpcTemplate>();
				npcInfoList.add(npcInfo);
				npcInfos.put(npcInfo.getMapId(), npcInfoList);
			} else {
				npcInfos.get(npcInfo.getMapId()).add(npcInfo);
			}
		}
	}

	public List<NpcTemplate> getNpcInfos(int mapId) {
		List<NpcTemplate> npcList = npcInfos.get(mapId);
		return npcList == null ? new ArrayList<NpcTemplate>(0) : npcList;
	}

}
