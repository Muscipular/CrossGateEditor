package cg.editor.image;

import java.util.List;

import org.jdom.Document;

import cg.base.image.ImageReader;
import cg.base.io.ImageResource;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.TypeInfo;

@Deprecated
public class ImageTypeInfo extends TypeInfo {
	
	public ImageTypeInfo() {
		super();
	}
	
	@Override
	protected void loadUnit(Document doc, String name) throws Exception {
		List<ImageResource> list = CrossGateEditor.getImageManager().getImageResources(ImageReader.RESOURCE_TYPE);
		for (ImageResource resource : list) {
			EditorImageSet editorImage = new EditorImageSet(resource, null, null);
			editorImage.setTypeInfo(this);
			save(editorImage);
		}
	}

}
