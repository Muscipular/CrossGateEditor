package cg.editor.reader;

import cg.base.image.ImageReader;
import cg.base.log.Log;
import cg.data.resource.ReadImageResourceManager;

public class EditorImageResourceManager extends ReadImageResourceManager {

	public EditorImageResourceManager(Log log, String clientFilePath) {
		super(log, clientFilePath);
	}
	
	@Override
	protected ImageReader createImageReader() {
		return new EditorImageReader(this, log, clientFilePath);
	}

}
