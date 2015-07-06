package cg.editor.image.runLength;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.OutputStream;
import java.util.Queue;

import cg.base.log.Log;
import cg.base.reader.RunLength;
import cg.base.util.MathUtil;
import cg.editor.image.EditorImageDictionary;

public abstract class RunLengthImage implements RunLength {
	
	protected final Log log;
	
	public RunLengthImage(Log log) {
		this.log = log;
	}
	
	public abstract BufferedImage load(File file) throws Exception;
	
	public void outputImage(EditorImageDictionary imageDictionary, OutputStream os) throws Exception {
		BufferedImage image = imageDictionary.bufferedImage();
		byte[] data = new byte[HEAD_LENGTH_NO_COLOR];
		data[HEAD_HEAD_0] = 'N';
		data[HEAD_HEAD_1] = 'T';
		data[HEAD_VERSION] = imageDictionary.getHeadVersion();
		MathUtil.intToByte2(data, HEAD_WIDTH, 4, image.getWidth());
		MathUtil.intToByte2(data, HEAD_HEIGHT, 4, image.getHeight());
		byte[] runs = runLength(image);
		imageDictionary.setSize(runs.length + HEAD_LENGTH_NO_COLOR);
		MathUtil.intToByte2(data, HEAD_DATA_LENGTH, 4, imageDictionary.getSize());
		
		os.write(data);
		os.write(runs);
	}
	
	protected abstract byte[] runLength(BufferedImage image) throws Exception;
	
	protected int outputSame(Queue<Byte> queue, int length, int id) {
		if (length >= 0x10 * 0x100) {
			int o = length % 0x100;
			int m = length % (0x100 * 0x100) / 0x100;
			queue.add((byte) ((id == -1 ? FIRST_WORD_XYZ_COLOR : FIRST_WORD_XYZ) << 4 | length / (0x100 * 0x100)));
			if (id > -1) {
				queue.add((byte) id);
			}
			queue.add((byte) m);
			queue.add((byte) o);
		} else if (length >= 0x10) {
			int m = length % 0x100;
			queue.add((byte) ((id == -1 ? FIRST_WORD_NM_COLOR : FIRST_WORD_NMX) << 4 | length / 0x100));
			if (id > -1) {
				queue.add((byte) id);
			}
			queue.add((byte) m);
		} else { // 8n X Ìî³än¸öX
			queue.add((byte) ((id == -1 ? FIRST_WORD_N_COLOR : FIRST_WORD_NX) << 4 | length));
			if (id > -1) {
				queue.add((byte) id);
			}
		}
		
		return length;
	}

}
