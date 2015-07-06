package cg.editor.image;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jdom.Element;

import cg.base.image.ImageDictionary;
import cg.base.map.MapCell;
import cg.editor.data.CrossGateEditor;
import cg.editor.extend.DefaultDataObjectEditor;

public class ImageEditor extends DefaultDataObjectEditor {
	
	/**
	 * 编辑器id
	 */
    public static final String ID = "cg.editor.image.ImageEditor";
    
    private static String[] IMAGE_FILE_TYPES;
    
    private Composite container;
    
    private Label imageWidth, imageHeight, color, image;
    
    private Text useEast, useSouth, offsetX, offsetY, globalId;
    
    private Button isObstacle;
    
    private FileDialog imageDialog;
    
    private Spinner indexSpinner;
	
	static {
		try {
			@SuppressWarnings("unchecked")
			List<Element> list = CrossGateEditor.getConfiguration().getRootElement().getChildren("imageFileType");
			IMAGE_FILE_TYPES = new String[list.size()];
			for (int i = 0;i < list.size();i++) {
				IMAGE_FILE_TYPES[i] = list.get(i).getAttributeValue("name");
			}
		} catch (Exception e) {
			CrossGateEditor.getLog().error(EditorImageSet.class.getName(), e);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		EditorImageSet editorImage = (EditorImageSet) editObject;
		
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        CTabFolder tabFolder = new CTabFolder(container, SWT.BOTTOM);
        
        createCImageArea(tabFolder, editorImage);

        saveStateToUndoBuffer();
	}

	@Override
	protected void saveData() throws Exception {
		int index = indexSpinner.getSelection();
		EditorImageSet editorImage = (EditorImageSet) editObject;
		if (index < editorImage.getImageCount()) {
			EditorImageDictionary imageDictionary = (EditorImageDictionary) editorImage.select(index);
			try {
				imageDictionary.setGlobalId(Integer.parseInt(globalId.getText()));
				imageDictionary.setMark(isObstacle.getSelection() ? MapCell.MARK_OBSTACLE : MapCell.MARK_NOMARL);
				imageDictionary.setOffsetX(Integer.parseInt(offsetX.getText()));
				imageDictionary.setOffsetY(Integer.parseInt(offsetY.getText()));
				imageDictionary.setUseEast(Byte.parseByte(useEast.getText()));
				imageDictionary.setUseSouth(Byte.parseByte(useSouth.getText()));
				MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "完成", "保存成功：" + imageDictionary);
			} catch (Exception e) {
				CrossGateEditor.getLog().error(getClass().getName(), e);
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", "输入有误:" + e.getMessage());
			}
		}
	}
	
	private void createCImageArea(CTabFolder tabFolder, final EditorImageSet editorImage) {
		CTabItem cimageItem = new CTabItem(tabFolder, SWT.FILL);
        cimageItem.setText("图像");
        tabFolder.setSelection(cimageItem);
        // line
		Composite container = new Composite(tabFolder, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        container.setLayout(gridLayout);
        cimageItem.setControl(container);
        
        Label label = new Label(container, SWT.NONE);
        label.setText("物理编号");
        
        indexSpinner = new Spinner(container, SWT.BORDER);
        indexSpinner.setMinimum(0); 
        indexSpinner.setMaximum(editorImage.getImageCount());
        indexSpinner.addModifyListener(new IndexListener(editorImage));
        image = new Label(container, SWT.NONE);
        // line
        final Label countLabel = new Label(container, SWT.NONE);
        countLabel.setText("图片总数：" + editorImage.getImageCount());
        
        Button importButton = new Button(container, SWT.NONE);
        importButton.setText("导入");
        importButton.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
            public void widgetSelected(final SelectionEvent se) {
        		if (imageDialog == null) {
                    imageDialog = new FileDialog(getSite().getShell(), SWT.OPEN | SWT.MULTI);
                    String[] types = new String[IMAGE_FILE_TYPES.length];
                    for (int i = 0;i < IMAGE_FILE_TYPES.length;i++) {
                    	types[i] = "*." + IMAGE_FILE_TYPES[i];
                    }
                    imageDialog.setFilterExtensions(types);
                }
        		imageDialog.open();
        		String[] fileNames = imageDialog.getFileNames(); // 返回所有选择的文件名，不包括路径
                if (fileNames != null && fileNames.length > 0) {
                	String path = imageDialog.getFilterPath(); // 返回选择的路径
                	File parent = new File(path);
                	int index = editorImage.getImageCount();
            		try {
	                	for (int i = 0;i < fileNames.length;i++) {
	                		File file = new File(parent, fileNames[i]);
	            			editorImage.addBufferedImage(file);
	                	}
					} catch (Exception e) {
						CrossGateEditor.getLog().error(getClass().getName(), e);
					}
                    indexSpinner.setMaximum(editorImage.getImageCount());
                    indexSpinner.setSelection(index);
                    countLabel.setText("图片总数：" + editorImage.getImageCount());
                    indexSpinner.setSize(80, indexSpinner.getBounds().height);
                    countLabel.setSize(100, indexSpinner.getBounds().height);
                }
            }
            
        });
        
        label = new Label(container, SWT.NONE); // fill
        // line
        imageWidth = new Label(container, SWT.NONE);
        imageWidth.setText("图片宽度：		");
        label = new Label(container, SWT.NONE); // fill
        label = new Label(container, SWT.NONE); // fill
        
        imageHeight = new Label(container, SWT.NONE);
        imageHeight.setText("图片高度：		");
        label = new Label(container, SWT.NONE); // fill
        label = new Label(container, SWT.NONE); // fill
        
        label = new Label(container, SWT.NONE);
        label.setText("占东面积：");
        useEast = new Text(container, SWT.BORDER);
        useEast.addModifyListener(this);
        label = new Label(container, SWT.NONE); // fill
        
        label = new Label(container, SWT.NONE);
        label.setText("占南面积：");
        useSouth = new Text(container, SWT.BORDER);
        useSouth.addModifyListener(this);
        label = new Label(container, SWT.NONE); // fill
        
        label = new Label(container, SWT.NONE);
        label.setText("X偏移：");
        offsetX = new Text(container, SWT.BORDER);
        offsetX.addModifyListener(this);
        label = new Label(container, SWT.NONE); // fill
        
        label = new Label(container, SWT.NONE);
        label.setText("Y偏移：");
        offsetY = new Text(container, SWT.BORDER);
        offsetY.addModifyListener(this);
        label = new Label(container, SWT.NONE); // fill
        
        label = new Label(container, SWT.NONE);
        label.setText("全局索引：");
        globalId = new Text(container, SWT.BORDER);
        globalId.addModifyListener(this);
        label = new Label(container, SWT.NONE); // fill
        
        isObstacle = new Button(container, SWT.CHECK);
        isObstacle.setText("是否阻挡");
        isObstacle.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
            public void widgetSelected(final SelectionEvent e) {
            	setDirty(true);
            }
            
        });
        color = new Label(container, SWT.NONE);
        color.setText("调色板：		");
        label = new Label(container, SWT.NONE); // fill
        
        Button exportButton = new Button(container, SWT.NONE);
        exportButton.setText("导出bin");
        exportButton.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
            public void widgetSelected(final SelectionEvent se) {
        		try {
					editorImage.output(new File(CrossGateEditor.getClientFilePath()));
				} catch (Exception e) {
					CrossGateEditor.getLog().error(getClass().getName(), e);
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", e.getMessage());
				}
        	}
        	
		});
        exportButton.setEnabled(!editorImage.readOnly());
        
        Button exportImageButton = new Button(container, SWT.NONE);
        exportImageButton.setText("导出图片");
        exportImageButton.addSelectionListener(new SelectionAdapter() {
        	
        	@Override
            public void widgetSelected(final SelectionEvent se) {
        		ExportImageDialog dialog = new ExportImageDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), IMAGE_FILE_TYPES);
        		if (dialog.open() != Dialog.OK) {
                    return;
                }
        		
        		File path = new File(dialog.getPath());
        		String type = dialog.getSuffix();
        		int count = dialog.getCount(), imageCount = editorImage.getImageCount();
				try {
					if (!path.exists()) {
						path.mkdirs();
					}
	        		for (int i = 0;i < count;i++) {
	        			int index = indexSpinner.getSelection();
	        			if (index + i < imageCount) {
	        				ImageDictionary imageDictionary = editorImage.select(index + i);
	        				File file = new File(path, imageDictionary.getVersion() + "_" + imageDictionary.getResourceId() + "." + type);
	        				if (!file.exists()) {
	        					file.createNewFile();
	        				}
							ImageIO.write(imageDictionary.bufferedImage(), type, file);
	        			} else {
	        				break;
	        			}
	        		}
	        		MessageDialog.openInformation(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "完成", "导出成功。");
				} catch (IOException e) {
					CrossGateEditor.getLog().error(getClass().getName(), e);
					MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", "越界:" + e.getMessage());
				}
        	}
        	
		});
        
//        show(editorImage, 0);
	}
	
	private class IndexListener implements ModifyListener {
		
		private final EditorImageSet editorImage;
		
		public IndexListener(EditorImageSet editorImage) {
			this.editorImage = editorImage;
		}

		@Override
		public void modifyText(ModifyEvent e) {
			int index = ((Spinner) e.getSource()).getSelection();
			show(editorImage, index);
		}
		
	}
	
	private void show(EditorImageSet editorImage, int index) {
		if (index < editorImage.getImageCount()) {
			ImageDictionary imageDictionary = editorImage.select(index);
	        imageWidth.setText("图片宽度：" + imageDictionary.getWidth());
	        imageHeight.setText("图片高度：" + imageDictionary.getHeight());
	        useEast.setText("" + imageDictionary.getUseEast());
	        useSouth.setText("" + imageDictionary.getUseSouth());
	        offsetX.setText("" + imageDictionary.getOffsetX());
	        offsetY.setText("" + imageDictionary.getOffsetY());
	        globalId.setText("" + imageDictionary.getGlobalId());
	        isObstacle.setSelection(imageDictionary.getMark() == MapCell.MARK_OBSTACLE);
	        color.setText("调色板：" + (imageDictionary.hasColorPalettes() ? "自带" : "全局"));
	        ImageEditor.this.image.setImage(ImageUtils.createImage(ImageEditor.this.image.getDisplay(), imageDictionary.bufferedImage()));
	        Rectangle rect = ImageEditor.this.image.getBounds();
	        ImageEditor.this.image.setBounds(rect.x, rect.y, imageDictionary.getWidth(), imageDictionary.getHeight());
	        container.redraw();
		} else if (index != 0) {
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "错误", "越界:" + index);
		}
	}

}
