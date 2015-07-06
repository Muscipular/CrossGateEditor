package cg.editor.map;

import java.awt.image.BufferedImage;

import cg.base.image.ImageDictionary;
import cg.base.image.ImageReader;
import cg.base.map.MapCell;
import cg.editor.image.ImageUtils;

class EditorMapCell implements MapCell {
	
	public static final BufferedImage EMPTY_IMAGE = ImageUtils.createBufferedImage("icons/empty.gif");
	
	private int east, south, x, y;
	
	private byte mark;
	
	private ImageDictionary ground, object;
	
	private final ImageReader imageReader;
	
	public EditorMapCell(int east, int south, ImageReader imageReader) {
		this.east = east;
		this.south = south;
		this.imageReader = imageReader;
		setMark(MARK_NULL);
	}

	@Override
	public int getEast() {
		return east;
	}

	@Override
	public int getSouth() {
		return south;
	}

	@Override
	public int getImageGlobalId() {
		return ground == null ? 0 : ground.getGlobalId();
	}

	@Override
	public int getObjectId() {
		return object == null ? 0 : object.getGlobalId();
	}

	@Override
	public byte getMark() {
		return mark;
	}
	
	public void setImageGlobalId(int imageGlobalId) {
		ground = imageReader.getImageDictionary(imageGlobalId);
	}
	
	public void setObjectId(int objectId) {
		object = imageReader.getImageDictionary(objectId);
	}
	
	public void setMark(byte mark) {
		this.mark = mark;
	}
	
	public ImageDictionary getGroundImage() {
		return ground;
	}
	
	public ImageDictionary getObjectImage() {
		return object;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void setEast(int east) {
		this.east = east;
	}
	
	public void setSouth(int south) {
		this.south = south;
	}

}
