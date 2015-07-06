package cg.editor.image.runLength;

import cg.base.image.ColorPalette;

class EditorColorPalette implements ColorPalette {
	
	private static final byte RED = 0;
	
	private static final byte GREEN = 1;
	
	private static final byte BLUE = 2;
	
	private static final byte ALPHA = 3;
	
	private int[] color;

	@Override
	public short getAlpha() {
		return (short) color[ALPHA];
	}

	@Override
	public short getBlue() {
		return (short) color[BLUE];
	}

	@Override
	public int getColor() {
		return (getAlpha() << 24) | (getRed() << 16) | (getGreen() << 8) | getBlue();
	}

	@Override
	public short getGreen() {
		return (short) color[GREEN];
	}

	@Override
	public int getRGBColor() {
		return (getRed() << 16) | (getGreen() << 8) | getBlue();
	}

	@Override
	public short getRed() {
		return (short) color[RED];
	}
	
	public void setColor(int[] color) {
		this.color = color;
	}
	
	public boolean compare(int[] color) {
		return getRed() == color[RED] && getGreen() == color[GREEN] && getBlue() == color[BLUE] && getAlpha() == color[ALPHA];
	}
	
	public boolean compare(ColorPalette color) {
		return getRed() == color.getRed() && getGreen() == color.getGreen() && getBlue() == color.getBlue() && getAlpha() == color.getAlpha();
	}

}
