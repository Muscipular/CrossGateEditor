package cg.editor.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cg.base.CrossGateBase;
import cg.base.image.ImageDictionary;
import cg.base.image.ImageManager;
import cg.base.image.ResourceListener;
import cg.base.io.ImageResource;
import cg.base.log.Log;
import cg.base.reader.CImageReader;
import cg.editor.image.EditorImageDictionary;
import cg.editor.image.EditorImageManager;

class EditorImageReader extends CImageReader implements ResourceListener {
	
	public EditorImageReader(ImageManager imageManager, Log log, String clientFilePath) {
		super(imageManager, log, clientFilePath);
		imageManager.addResourceListener(this, RESOURCE_TYPE);
	}

	@Override
	protected ImageDictionary[] load(int size, ImageResource resource, FileInputStream fis) {
		ImageDictionary[] imageDictionaries = super.load(size, resource, fis);
		for (ImageDictionary imageDictionary : imageDictionaries) {
			EditorImageManager.putUnAnimationImageInfo(imageDictionary);
		}
		return imageDictionaries;
	}

	@Override
	protected ImageDictionary createImageDictionary(ImageResource resource, FileInputStream fis) throws IOException {
		return new EditorImageDictionary(fis, resource, imageManager);
	}

	@Override
	public void addResource(ImageResource resource) {
		if (resource.getVersion() >= imageDictionaries.length) {
			ImageDictionary[][] imageDictionaries = new ImageDictionary[this.imageDictionaries.length + 1][];
			for (int i = 0;i < this.imageDictionaries.length;i++) {
				imageDictionaries[i] = this.imageDictionaries[i];
			}
			imageDictionaries[imageDictionaries.length - 1] = new ImageDictionary[0];
			this.imageDictionaries = imageDictionaries;
		}
	}

	@Override
	public void reload(ImageResource resource) throws Exception {
		File dir = new File(CrossGateBase.getClientFilePath());
		addRunLengthImageReader(resource.getVersion(), new File(dir, resource.getDataFile()));
		FileInputStream fis = new FileInputStream(new File(dir, resource.getInfoFile()));
		imageDictionaries[resource.getVersion()] = load((int) (fis.getChannel().size() / ImageDictionary.DATA_HEAD_LENGTH), resource, fis);
		CrossGateBase.getLog().info(getClass().getSimpleName() + " : load " + resource.getDataFile() + ".");
	}

}
