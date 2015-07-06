package cg.editor.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;

import cg.base.reader.RunLength;
import cg.editor.Activator;
import cg.editor.data.CrossGateEditor;
import cg.editor.swtdesigner.ResourceManager;

public class ImageUtils implements RunLength {
	
	private static final Image EMPTY_IMAGE = ResourceManager.getPluginImage(Activator.getDefault(), "icons/empty.gif");
	
	public static InputStream createImageInputStream(BufferedImage image) {
		InputStream is = null;
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ImageOutputStream imOut; 
        try { 
            imOut = ImageIO.createImageOutputStream(bs); 
            ImageIO.write(image, "png", imOut); 
            is = new ByteArrayInputStream(bs.toByteArray()); 
        } catch (IOException e) { 
            CrossGateEditor.getLog().error(ImageUtils.class.getName(), e);
        }
        return is;
	}
	
	public static Image createImage(Device device, BufferedImage image) {
		return image == null ? EMPTY_IMAGE : new Image(device, createImageInputStream(image));
	}
	
	public static BufferedImage createBufferedImage(String name) {
		try {
			return ImageIO.read(ResourceManager.getInputStream(Activator.getDefault(), name));
		} catch (IOException e) {
			CrossGateEditor.getLog().error(ImageUtils.class.getName(), e);
			return null;
		}
	}

}
