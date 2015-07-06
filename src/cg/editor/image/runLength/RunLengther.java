package cg.editor.image.runLength;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import cg.base.image.ImageManager;
import cg.base.log.Log;
import cg.editor.image.EditorImageDictionary;

public class RunLengther extends RunLengthImage {
	
	private Map<Integer, RunLengthImage> runLengthImages;
	
	public RunLengther(Log log, ImageManager imageManager) {
		super(log);
		runLengthImages = new HashMap<Integer, RunLengthImage>();
		runLengthImages.put(BufferedImage.TYPE_4BYTE_ABGR, new RunLengthARGB(log, imageManager)); // png
		runLengthImages.put(BufferedImage.TYPE_BYTE_INDEXED, new RunLengthInLoad(log)); // bmp and gif
	}

	@Override
	public void outputImage(EditorImageDictionary imageDictionary, OutputStream os) throws Exception {
		runLengthImages.get(imageDictionary.bufferedImage().getType()).outputImage(imageDictionary, os);
	}

	@Deprecated
	@Override
	protected byte[] runLength(BufferedImage image) throws Exception {
		return null;
	}

	@Override
	public BufferedImage load(File file) throws Exception {
		return ImageIO.read(file);
	}

}
