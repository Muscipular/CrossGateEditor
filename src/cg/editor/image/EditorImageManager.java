package cg.editor.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import cg.base.image.ImageDictionary;
import cg.base.image.ImageReader;

public class EditorImageManager {
	
	private static final Map<Byte, Map<Integer, ImageDictionary>> unAnimationImageInfos = new HashMap<Byte, Map<Integer, ImageDictionary>>();
	
	private static final List<ImageDictionary> unAnimationImageInfoList = new ArrayList<ImageDictionary>();
	
	private final Map<Integer, Image> images;
	
	private final ImageReader imageReader;
	
	public EditorImageManager(ImageReader imageReader) {
		this.imageReader = imageReader;
		images = new HashMap<Integer, Image>();
	}
	
	public Image getImage(int globalId, Device device) {
		if (!images.containsKey(globalId)) {
			Image image = getImage(imageReader.getImageDictionary(globalId), device);
			images.put(globalId, image);
			return image;
		} else {
			return images.get(globalId);
		}
	}
	
	public void clearImage() {
		images.clear();
	}
	
	public Image getImage(ImageDictionary image, Device device) {
		int globalId = image.getGlobalId();
		if (!images.containsKey(globalId)) {
			Image ret = ImageUtils.createImage(device, image.bufferedImage());
			images.put(globalId, ret);
			return ret;
		} else {
			return images.get(globalId);
		}
	}
	
	public static void putUnAnimationImageInfo(ImageDictionary imageDictionary) {
		byte version = imageDictionary.getVersion();
		if (!unAnimationImageInfos.containsKey(version)) {
			unAnimationImageInfos.put(version, new HashMap<Integer, ImageDictionary>());
		}
		unAnimationImageInfos.get(version).put(imageDictionary.getResourceId(), imageDictionary);
	}
	
	public static List<ImageDictionary> getUnAnimationImageInfos() {
		if (unAnimationImageInfoList.size() == 0) {
			for (Map<Integer, ImageDictionary> map : unAnimationImageInfos.values()) {
				unAnimationImageInfoList.addAll(map.values());
			}
			unAnimationImageInfos.clear();
		}
		return unAnimationImageInfoList;
	}
	
	public static void removeUnAnimationImageInfo(byte version, int resourceId) {
		unAnimationImageInfos.get(version).remove(resourceId);
	}

}
