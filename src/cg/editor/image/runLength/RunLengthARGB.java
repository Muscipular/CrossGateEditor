package cg.editor.image.runLength;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;

import cg.base.image.ColorPalette;
import cg.base.image.ImageManager;
import cg.base.log.Log;
import cg.editor.image.ImageUtils;

class RunLengthARGB extends RunLengthImage {
	
	private final ImageManager imageManager;
	
	public RunLengthARGB(Log log, ImageManager imageManager) {
		super(log);
		this.imageManager = imageManager;
	}

//	private static final ColorPalette[] defaultColorPalettes = CrossGateEditor.getColorPalettes("default");

	@Override
	protected byte[] runLength(BufferedImage image) throws Exception {
		int offset = 0, sameOffset = offset, colors[] = null, fillCount = 0;
		List<int[]> differentList = new LinkedList<int[]>();
		Queue<Byte> queue = new LinkedList<Byte>();
		Raster raster = image.getData();
		EditorColorPalette colorPalette = new EditorColorPalette();
		for (int y = image.getHeight() - 1;y >= 0;y--) {
			for (int x = 0;x < image.getWidth();x++) {
				colors = raster.getPixel(x, y, colors);
				if (offset == 0) { // first
					colorPalette.setColor(colors);
					differentList.add(colors);
				} else if (!colorPalette.compare(colors)) { // different with previous color
					int id = getId(colorPalette);
					int length = offset - sameOffset;
					if (length > 1) { // Before this there were many same color need to output.
						// run data
						fillCount += outputSame(queue, length, id);
					} else if (id == -1) { // Single no color output it.
						queue.add((byte) (FIRST_WORD_N_COLOR << 4 | 1));
						if (differentList.size() > 0) {
							throw new IllegalArgumentException("differentList.size() > 0");
						}
						fillCount += length;
					}
					colorPalette.setColor(colors);
					id = getId(colorPalette);
					if (id > -1) {
						differentList.add(colors);
					} else { // no color need output all
						fillCount += outputDifferentARGB(queue, differentList, image);
					}
					sameOffset = offset;
				} else { // same with previous color
					if (differentList.size() > 1) {
						differentList.remove(differentList.size() - 1);
						fillCount += outputDifferentARGB(queue, differentList, image);
					} else if (differentList.size() == 1) {
						differentList.clear();
					}
				}
				offset++;
				if (offset == image.getWidth() * image.getHeight()) {
					if (differentList.size() > 0) {
						fillCount += outputDifferentARGB(queue, differentList, image);
					} else {
						fillCount += outputSame(queue, offset - sameOffset, getId(colorPalette));
					}
				}
				colors = null;
			}
		}
		
		if (fillCount != image.getWidth() * image.getHeight()) {
			log.warning(ImageUtils.class.getName() + "::runLength() : fillCount is not enought.");
		}
		
		byte[] ret = new byte[queue.size()];
		for (int i = 0;i < ret.length;i++) {
			ret[i] = queue.poll();
		}
		
		return ret;
	}
	
	private int outputDifferentARGB(Queue<Byte> list, List<int[]> differentList, BufferedImage image) {
		int length = differentList.size();
		if (length >= 0x10 * 0x100) {
			int o = length % 0x100;
			int m = length % (0x100 * 0x100) / 0x100;
			list.add((byte) (FIRST_WORD_N_STRING << 4 | length / (0x100 * 0x100)));
			list.add((byte) m);
			list.add((byte) o);
		} else if (length >= 0x10) {
			int m = length % 0x100;
			list.add((byte) (FIRST_WORD_NM_STRING << 4 | length / 0x100));
			list.add((byte) m);
		} else {
			list.add((byte) (FIRST_WORD_N_STRING << 4 | length));
		}
		
		EditorColorPalette colorPalette = new EditorColorPalette();
		while (!differentList.isEmpty()) {
			colorPalette.setColor(differentList.remove(0));
			int id = getId(colorPalette);
			list.add((byte) id);
		}
		
		return length;
	}
	
	private int getId(EditorColorPalette color) {
		ColorPalette[] defaultColorPalettes = imageManager.getDefaultColorPalette();
		for (int i = 0;i < 16;i++) {
			if (color.compare(defaultColorPalettes[i])) {
				return i;
			}
		}
		for (int i = 0;i < 16;i++) {
			if (color.compare(defaultColorPalettes[i + 16])) {
				return i + 240;
			}
		}
		ColorPalette[] colorPalettes = imageManager.getGlobalColorPalette();
		for (int i = 0;i < 256 - defaultColorPalettes.length;i++) {
			if (color.compare(colorPalettes[i])) {
				return i + 16;
			}
		}
		return -1; // no color
	}

	@Override
	public BufferedImage load(File file) throws Exception {
		BufferedImage image = ImageIO.read(file);
		BufferedImage ret = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int w = 0;w < ret.getWidth();w++) {
			for (int h = 0;h < ret.getHeight();h++) {
				int[] array = null;
				array = image.getData().getPixel(w, h, array);
				ret.setRGB(w, h, array[0]);
			}
		}
		return ret;
	}

}
