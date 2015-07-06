package cg.editor.image.runLength;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.imageio.ImageIO;

import cg.base.log.Log;
import cg.editor.image.ImageUtils;

class RunLengthInLoad extends RunLengthImage {

	public RunLengthInLoad(Log log) {
		super(log);
	}

	@Override
	protected byte[] runLength(BufferedImage image) throws Exception {
		int offset = 0, sameOffset = offset, colors[] = null, fillCount = 0, color = -1;
		List<Integer> differentList = new LinkedList<Integer>();
		Queue<Byte> queue = new LinkedList<Byte>();
		Raster raster = image.getData();
		for (int y = image.getHeight() - 1;y >= 0;y--) {
			for (int x = 0;x < image.getWidth();x++) {
				colors = raster.getPixel(x, y, colors);
				if (offset == 0) { // first
					color = colors[0];
					differentList.add(color);
				} else if (color != colors[0]) { // different with previous color
					int length = offset - sameOffset;
					if (length > 1) { // Before this there were many same color need to output.
						// run data
						fillCount += outputSame(queue, length, color);
					} else if (color == -1) { // Single no color output it.
						queue.add((byte) (FIRST_WORD_N_COLOR << 4 | 1));
						if (differentList.size() > 0) {
							throw new IllegalArgumentException("differentList.size() > 0");
						}
						fillCount += length;
					}
					color = colors[0];
					if (color > -1) {
						differentList.add(color);
					} else { // no color need output all
						fillCount += outputDifferent(queue, differentList, image);
					}
					sameOffset = offset;
				} else { // same with previous color
					if (differentList.size() > 1) {
						differentList.remove(differentList.size() - 1);
						fillCount += outputDifferent(queue, differentList, image);
					} else if (differentList.size() == 1) {
						differentList.clear();
					}
				}
				offset++;
				if (offset == image.getWidth() * image.getHeight()) {
					if (differentList.size() > 0) {
						fillCount += outputDifferent(queue, differentList, image);
					} else {
						fillCount += outputSame(queue, offset - sameOffset, color);
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
	
	private int outputDifferent(Queue<Byte> list, List<Integer> differentList, BufferedImage image) {
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
		
		while (!differentList.isEmpty()) {
			int id = differentList.remove(0);
			list.add((byte) id);
		}
		
		return length;
	}

	@Override
	public BufferedImage load(File file) throws Exception {
		return ImageIO.read(file);
	}

}
