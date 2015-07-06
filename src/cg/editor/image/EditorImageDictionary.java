package cg.editor.image;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import cg.base.image.ImageData;
import cg.base.image.ImageDictionary;
import cg.base.image.ImageManager;
import cg.base.io.ImageResource;
import cg.base.reader.CImageDictionary;
import cg.base.util.MathUtil;
import cg.editor.data.CrossGateEditor;

public class EditorImageDictionary extends CImageDictionary {
	
	private byte headVersion;
	
	public EditorImageDictionary(BufferedImage image, byte version, int resourceId) {
		super();
		this.image = image;
		setVersion(version);
		setWidth(image.getWidth());
		setHeight(image.getHeight());
		setResourceId(resourceId);
		colorPalette = false;
		headVersion = HEAD_VERSION_RLE;
	}
	
	public EditorImageDictionary(ImageDictionary imageDictionary) {
		super();
		setAddress(imageDictionary.getAddress());
		setGlobalId(imageDictionary.getGlobalId());
		setHeight(imageDictionary.getHeight());
		setMark(imageDictionary.getMark());
		setOffsetX(imageDictionary.getOffsetX());
		setOffsetY(imageDictionary.getOffsetY());
		setResourceId(imageDictionary.getResourceId());
		setSize(imageDictionary.getSize());
		setUseEast(imageDictionary.getUseEast());
		setUseSouth(imageDictionary.getUseSouth());
		setVersion(imageDictionary.getVersion());
		setWidth(imageDictionary.getWidth());
		colorPalette = imageDictionary.hasColorPalettes();
	}
	
	public EditorImageDictionary(FileInputStream fis, ImageResource resource, ImageManager imageManager) throws IOException {
		super(fis, resource, imageManager);
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setUseEast(byte useEast) {
		this.useEast = useEast;
	}

	public void setUseSouth(byte useSouth) {
		this.useSouth = useSouth;
	}

	public void setMark(byte mark) {
		this.mark = mark;
	}

	public void setAddress(long address) {
		this.address = address;
	}

	public void setSize(int size) {
		this.size = size;
	}
	
	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public void setGlobalId(int globalId) {
		this.globalId = globalId;
	}
	
	public void output(OutputStream os) throws IOException {
		byte[] data = new byte[DATA_HEAD_LENGTH];
		MathUtil.intToByte2(data, STREAM_RESOURCE_ID, 4, getResourceId());
		MathUtil.intToByte2(data, STREAM_ADDRESS, 4, (int) getAddress());
		MathUtil.intToByte2(data, STREAM_SIZE, 4, getSize());
		MathUtil.intToByte2(data, STREAM_OFFSET_X, 4, getOffsetX());
		MathUtil.intToByte2(data, STREAM_OFFSET_Y, 4, getOffsetY());
		MathUtil.intToByte2(data, STREAM_WIDTH, 4, getWidth());
		MathUtil.intToByte2(data, STREAM_HEIGHT, 4, getHeight());
		data[STREAM_USE_EAST] = getUseEast();
		data[STREAM_USE_SOUTH] = getUseSouth();
		data[STREAM_MARK] = getMark();
		MathUtil.intToByte2(data, STREAM_GLOBAL_ID, 4, getGlobalId());
		os.write(data);
	}
	
	public byte getHeadVersion() {
		return headVersion;
	}

	@Override
	public BufferedImage bufferedImage() {
		if (image == null) {
			ImageData imageData = CrossGateEditor.getImageManager().getImageData(getVersion(), getResourceId());
			headVersion = imageData.getHeadVersion();
			image = imageData.getBufferedImage();
		}
		return image;
	}

}
