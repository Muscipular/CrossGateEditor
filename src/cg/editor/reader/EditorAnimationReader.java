package cg.editor.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import cg.base.image.ImageManager;
import cg.base.io.ResourceInfo;
import cg.base.log.Log;
import cg.base.reader.CAnimationReader;
import cg.base.time.Timer;
import cg.base.util.MathUtil;
import cg.editor.data.CrossGateEditor;
import cg.editor.image.EditorImageManager;

public class EditorAnimationReader extends CAnimationReader {

	private EditorAnimationInfos editorAnimationInfos;
	
	public EditorAnimationReader(Log log, String clientFilePath, ImageManager imageManager, Timer timer) {
		super(log, clientFilePath, imageManager, timer);
		editorAnimationInfos = null;
	}
	
	@Override
	protected void saveAnimationDictionary(ResourceInfo resourceInfo) {
		super.saveAnimationDictionary(resourceInfo);
		if (editorAnimationInfos == null) {
			editorAnimationInfos = new EditorAnimationInfos();
		}
		byte version = resourceInfo.getVersion();
		if (version <= VERSION_EX_4) {
			int resourceId = resourceInfo.getResourceId();
			resourceId = version == VERSION_20 ? resourceId + ANIMATION_TURE_INDEX : resourceId;
			try {
				editorAnimationInfos.read(fis[version], version, fis0[version]);
			} catch (IOException e) {
				CrossGateEditor.getLog().error(getClass().getName(), e);
			}
		}
	}
	
	private static class EditorAnimationInfos extends CAnimationInfos {
		
		public EditorAnimationInfos() {
			super();
		}
		
		public void read(FileInputStream fin, byte version, FileInputStream fis0) throws IOException {
			fin.read(bytes);
			id = MathUtil.bytesToInt(bytes, 0);
			address = MathUtil.bytesToInt(bytes, 4);
			actionCount = MathUtil.bytesToShort(bytes, 8);
			
			animationInfos = new HashMap<Integer, AnimationInfo>();
			fis0.getChannel().position(address);
			for (int i = 0;i < actionCount;i++) {
				if (!animationInfos.containsKey(i)) {
					animationInfos.put(i, new EditorAnimationInfo());
				}
				((EditorAnimationInfo) animationInfos.get(i)).read(fis0, version);
			}
		}
		
	}
	
	private static class EditorAnimationInfo extends SpriteAnimationInfo {
		
		public EditorAnimationInfo() {
			super();
		}
		
		public void read(FileInputStream fin, byte version) throws IOException {
			byte[] bytes = new byte[SIZES[version]];
			fin.read(bytes);
			dir = (byte) MathUtil.bytesToShort(bytes, 0);
			actionId = (byte) MathUtil.bytesToShort(bytes, 2);
			time = MathUtil.bytesToInt(bytes, 4);
			frame = MathUtil.bytesToInt(bytes, 8);
			byte[] data = new byte[10];
			for (int i = 0;i < frame;i++) {
				fin.read(data);
				EditorImageManager.removeUnAnimationImageInfo(version, MathUtil.bytesToInt(data, 0));
			}
		}
		
	}

}
