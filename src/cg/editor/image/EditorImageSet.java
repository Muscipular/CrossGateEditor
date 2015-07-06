package cg.editor.image;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.jdom.Element;

import cg.base.image.ImageDictionary;
import cg.base.image.ImageManager;
import cg.base.image.ImageReader;
import cg.base.io.ImageResource;
import cg.editor.data.BaseEditorObject;
import cg.editor.data.CrossGateEditor;
import cg.editor.image.runLength.RunLengthImage;

public class EditorImageSet extends BaseEditorObject implements ImageResource {
	
	private List<EditorImageDictionary> importImages;
	
	private String path, suffix;
	
	private boolean colorPalette, readOnly;
	
	private final ImageManager imageManager;
    
    private final RunLengthImage runLengthImage;
    
    public EditorImageSet() {
		this(CrossGateEditor.getImageManager(), ImageWizard.runLengther);
	}
    
	public EditorImageSet(ImageManager imageManager, RunLengthImage runLengthImage) {
		this.imageManager = imageManager;
		this.runLengthImage = runLengthImage;
		importImages = new LinkedList<EditorImageDictionary>();
	}
	
	public EditorImageSet(ImageResource resource, ImageManager imageManager, RunLengthImage runLengthImage) {
		this(imageManager, runLengthImage);
		
		setId(resource.getVersion());
		setName(resource.getDecription());
		path = resource.getPath();
		suffix = resource.getSuffix();
		colorPalette = resource.isColorPalette();
//		readOnly = resource.readOnly();
	}
	
	public EditorImageSet(String path, String suffix, ImageManager imageManager, RunLengthImage runLengthImage) {
		this(imageManager, runLengthImage);
		
		this.path = path;
		this.suffix = suffix;
		colorPalette = false;
	}

	@Override
	public Element save() {
		Element ret = new Element(getClass().getSimpleName());

		save(ret);
		
		ret.addAttribute("type", ImageReader.RESOURCE_TYPE);
		ret.addAttribute("path", path);
		ret.addAttribute("suffix", suffix);
		ret.addAttribute("colorPalette", String.valueOf(colorPalette));
		ret.addAttribute("readOnly", String.valueOf(readOnly));
        
		return ret;
	}

	@Override
	public void load(Element element) {
		super.load(element);
		
		path = element.getAttributeValue("path");
		suffix = element.getAttributeValue("suffix");
		colorPalette = Boolean.parseBoolean(element.getAttributeValue("colorPalette"));
		readOnly = Boolean.parseBoolean(element.getAttributeValue("readOnly"));
		
		try {
			imageManager.addResource(this);
			imageManager.reloadResource(this);
		} catch (Exception e) {
			CrossGateEditor.getLog().error(getClass().getName(), e);
		}
	}
	
	public int getImageCount() {
		return imageManager.getImageReader().getCount(getVersion()) + importImages.size();
	}
	
	public ImageDictionary select(int index) {
		int readerSize = imageManager.getImageReader().getCount(getVersion());
		return index < readerSize ? imageManager.getImageReader().getImageDictionary(getVersion(), index) : importImages.get(index - readerSize);
	}
	
	@Override
	public byte getVersion() {
		return (byte) getId();
	}
	
	public void addBufferedImage(File file) throws Exception {
		importImages.add(new EditorImageDictionary(runLengthImage.load(file), getVersion(), getImageCount()));
	}

	@Override
	public String getDecription() {
		return name;
	}

	@Override
	public String getInfoFile() {
		return getPath() + "/" + getType() + "Info" + getSuffix() + ".bin";
	}

	@Override
	public String getDataFile() {
		return getPath() + "/" + getType() + getSuffix() + ".bin";
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getSuffix() {
		return suffix;
	}

	@Override
	public String getType() {
		return ImageReader.RESOURCE_TYPE;
	}

	@Override
	public boolean isColorPalette() {
		return colorPalette;
	}
	
	public void output(File path) throws Exception {
		File file = new File(path, getDataFile());
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream dataFos = new FileOutputStream(file, true); // tail write
		
		file = new File(path, getInfoFile());
		if (!file.exists()) {
			file.createNewFile();
		}
		FileOutputStream infoFos = new FileOutputStream(file);
		
		int readerSize = imageManager.getImageReader().getCount(getVersion());
		long address = 0;
		for (int i = 0;i < readerSize;i++) { // old data write info only
			ImageDictionary imageDictionary = imageManager.getImageReader().getImageDictionary(getVersion(), i);
			((EditorImageDictionary) imageDictionary).output(infoFos);
			address += imageDictionary.getSize();
		}
		for (int i = 0;i < importImages.size();i++) { // new data write info and image
			EditorImageDictionary imageDictionary = importImages.get(i);
			imageDictionary.setAddress(address);
			runLengthImage.outputImage(imageDictionary, dataFos);
			address += imageDictionary.getSize();
			imageDictionary.output(infoFos);
		}
		
		dataFos.flush();
		dataFos.close();
		infoFos.flush();
		infoFos.close();
		
		importImages.clear();
		imageManager.reloadResource(this);
		
		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "完成", "导出");
	}

	public boolean readOnly() {
		return readOnly;
	}

}
