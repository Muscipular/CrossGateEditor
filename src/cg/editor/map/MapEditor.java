package cg.editor.map;

import java.io.File;
import java.io.FileOutputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;

import cg.base.util.IOUtils;
import cg.base.util.MathUtil;
import cg.data.gmsvReader.CFileMapReader.FileMapInfo;
import cg.data.map.MapInfo;
import cg.editor.Activator;
import cg.editor.data.ContentListener;
import cg.editor.data.CrossGateEditor;
import cg.editor.extend.DefaultDataObjectEditor;
import cg.editor.propertysheet.PropertySheetEntry;
import cg.editor.propertysheet.PropertySheetViewer;
import cg.editor.swtdesigner.ResourceManager;

public class MapEditor extends DefaultDataObjectEditor implements ContentListener {
	
	/**
	 * 编辑器id
	 */
    public static final String ID = "cg.editor.map.MapEditor";
    
    private Composite propertyContainer;
    
    private PropertySheetViewer propEditor;
    
    private MapView mapView;
    
    private GroundBar groundBar, objectBar;
    
    private Composite container;

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		EditorMap editorMap = (EditorMap) editObject;
		editorMap.load();
		
        container = new Composite(parent, SWT.NONE);
        container.setLayout(new FillLayout());

        CTabFolder tabFolder = new CTabFolder(container, SWT.BOTTOM);
        
        createMapItem(tabFolder, editorMap);
        createInfoItem(tabFolder, editorMap);
        
        saveStateToUndoBuffer();
	}

	@Override
	protected void saveData() throws Exception {
		EditorMap editorMap = (EditorMap) editObject;
		File file = new File(CrossGateEditor.getClientFilePath() + "/newMap/" + editorMap.getId());
		FileOutputStream fos = new FileOutputStream(file);
		writeHead(fos, editorMap);
		editorMap.write(fos);
		fos.flush();
		fos.close();
	}
	
	private void writeHead(FileOutputStream fos, EditorMap editorMap) throws Exception {
		byte[] datas = new byte[FileMapInfo.HEAD_LENGTH];
		// head
		byte[] bytes = MapInfo.SNAIL_HEAD_0.getBytes();
		for (int i = 0;i < bytes.length;i++) {
			datas[i] = bytes[i];
		}
		// id
		MathUtil.intToByte(datas, bytes.length, MapInfo.DATA_LENGTH, editorMap.getId());
		// name
		bytes = editorMap.getName().getBytes(IOUtils.ENCODING);
		for (int i = 0;i < bytes.length;i++) {
			datas[8 + i] = bytes[i];
		}
		// east
		MathUtil.intToByte(datas, 40, 2, editorMap.getMaxEast());
		// south
		MathUtil.intToByte(datas, 42, 2, editorMap.getMaxSouth());
		fos.write(datas);
	}
	
	private void createMapItem(CTabFolder tabFolder, EditorMap editorMap) {
		CTabItem mapItem = new CTabItem(tabFolder, SWT.NONE);
        mapItem.setText("场景编辑");
        tabFolder.setSelection(mapItem);
        
		Composite container = new Composite(tabFolder, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        container.setLayout(gridLayout);
        mapItem.setControl(container);

        createLeftBar(container);
        createGroundBar(container);
		
		SashForm sashForm = new SashForm(container, SWT.NONE);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        
        SashForm sashForm2 = new SashForm(sashForm, SWT.VERTICAL);
        sashForm2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mapView = new MapView(sashForm, SWT.NONE, editorMap, CrossGateEditor.getImageManager().getImageReader());
        
        groundBar.init(mapView);
        objectBar.init(mapView);
        
        propertyContainer = new Composite(sashForm2, SWT.NONE);
        
        propEditor = new PropertySheetViewer(propertyContainer, SWT.BORDER, false);
        PropertySheetEntry rootEntry = new PropertySheetEntry();
        propEditor.setRootEntry(rootEntry);
        propEditor.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        ((GridData) propEditor.getControl().getLayoutData()).exclude = false;
        ((Tree) propEditor.getControl()).setHeaderVisible(true);
        
        propertyContainer.layout();
        
        sashForm.setWeights(new int[]{3, 1});
	}
	
	private void createInfoItem(CTabFolder tabFolder, final EditorMap editorMap) {
        CTabItem infoItem = new CTabItem(tabFolder, SWT.NONE);
        infoItem.setText("基本信息");

        Composite composite = new Composite(tabFolder, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 6;
        composite.setLayout(gridLayout);
        infoItem.setControl(composite);
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("扩大：");
        
        final Text textLarge = new Text(composite, SWT.BORDER);
        GridData gd_textLarge = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        textLarge.setLayoutData(gd_textLarge);
//        textLarge.addFocusListener(AutoSelectAll.instance);
        textLarge.addModifyListener(this);
        
        final Button[] largeButtons = new Button[4];
        String[] texts = new String[]{"北", "东", "南", "西", };
        for (byte i = 0;i < largeButtons.length;i++) {
        	largeButtons[i] = new Button(composite, SWT.NONE);
        	largeButtons[i].setText(texts[i]);
        	final byte dir = (byte) (i << 1);
        	largeButtons[i].addSelectionListener(new SelectionAdapter() {
        		
        		@Override
                public void widgetSelected(final SelectionEvent se) {
        			try {
                        editorMap.large(dir, Integer.parseInt(textLarge.getText()));
                        setDirty(true);
        			} catch (Exception e) {
						CrossGateEditor.getLog().error(getClass().getName(), e);
					}
                }
                
            });
        }
        
        label = new Label(composite, SWT.NONE);
        label.setText("缩小：");
        
        final Text textSmall = new Text(composite, SWT.BORDER);
        GridData gd_textSmall = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        textSmall.setLayoutData(gd_textSmall);
//        textSmall.addFocusListener(AutoSelectAll.instance);
        textSmall.addModifyListener(this);
        
        final Button[] smallButtons = new Button[4];
        for (byte i = 0;i < smallButtons.length;i++) {
        	smallButtons[i] = new Button(composite, SWT.NONE);
        	smallButtons[i].setText(texts[i]);
        	final byte dir = (byte) (i << 1);
        	smallButtons[i].addSelectionListener(new SelectionAdapter() {
        		
        		@Override
                public void widgetSelected(final SelectionEvent se) {
        			try {
                        editorMap.small(dir, Integer.parseInt(textSmall.getText()));
                        setDirty(true);
        			} catch (Exception e) {
						CrossGateEditor.getLog().error(getClass().getName(), e);
					}
                }
                
            });
        }
	}
	
    private void createLeftBar(Composite container) {
    	ToolBar toolBar = new ToolBar(container, SWT.VERTICAL);
        toolBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        
        ToolItem pickupItem = new ToolItem(toolBar, SWT.RADIO);
        pickupItem.addSelectionListener(new Pickup());
        pickupItem.setToolTipText("选择工具");
        pickupItem.setSelection(true);
        pickupItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/pickup.gif"));
        
        ToolItem groundItem = new ToolItem(toolBar, SWT.RADIO);
        groundItem.addSelectionListener(new Ground());
        groundItem.setToolTipText("地表层");
        groundItem.setSelection(false);
        groundItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/landform.gif"));
        
        ToolItem objectItem = new ToolItem(toolBar, SWT.RADIO);
        objectItem.addSelectionListener(new Object());
        objectItem.setToolTipText("物体层");
        objectItem.setSelection(false);
        objectItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/npc.gif"));

        ToolItem showObjectItem = new ToolItem(toolBar, SWT.CHECK);
        showObjectItem.addSelectionListener(new ShowObject(MapView.MASK_SHOW_GROUND));
        showObjectItem.setToolTipText("显示地表");
        showObjectItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/relive.gif"));
        showObjectItem.setSelection(mapView == null ? MapView.DEFAULT_SHOW_OBJECT : (mapView.getMask() & MapView.MASK_SHOW_GROUND) > 0);

        showObjectItem = new ToolItem(toolBar, SWT.CHECK);
        showObjectItem.addSelectionListener(new ShowObject(MapView.MASK_SHOW_OBJECT));
        showObjectItem.setToolTipText("显示物体");
        showObjectItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/relive.gif"));
        showObjectItem.setSelection(mapView == null ? MapView.DEFAULT_SHOW_OBJECT : (mapView.getMask() & MapView.MASK_SHOW_OBJECT) > 0);

        showObjectItem = new ToolItem(toolBar, SWT.CHECK);
        showObjectItem.addSelectionListener(new ShowObject(MapView.MASK_SHOW_NPC));
        showObjectItem.setToolTipText("显示NPC");
        showObjectItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/relive.gif"));
        showObjectItem.setSelection(mapView == null ? MapView.DEFAULT_SHOW_OBJECT : (mapView.getMask() & MapView.MASK_SHOW_NPC) > 0);

        showObjectItem = new ToolItem(toolBar, SWT.CHECK);
        showObjectItem.addSelectionListener(new ShowObject(MapView.MASK_SHOW_GRID));
        showObjectItem.setToolTipText("显示网格");
        showObjectItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/relive.gif"));
        showObjectItem.setSelection(mapView == null ? MapView.DEFAULT_SHOW_OBJECT : (mapView.getMask() & MapView.MASK_SHOW_GRID) > 0);
    }
    
    private void createGroundBar(Composite container) {
    	groundBar = new GroundBar(container, SWT.VERTICAL);
    	groundBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    	groundBar.setVisible(false);
    	groundBar.setContentListener(this);
    	
    	objectBar = new ObjectBar(container, SWT.VERTICAL);
    	objectBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    	objectBar.setVisible(false);
    	objectBar.setContentListener(this);
    }
    
    private class Pickup extends SelectionAdapter {
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		objectBar.setVisible(false);
    		groundBar.setVisible(false);
    		mapView.setTool(null);
    		container.redraw();
        }
    	
    }
    
    private class Ground extends SelectionAdapter {
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		objectBar.setVisible(false);
    		groundBar.setVisible(true);
    		mapView.setTool(groundBar);
            container.redraw();
        }
    	
    }
    
    private class Object extends SelectionAdapter {
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		groundBar.setVisible(false);
    		objectBar.setVisible(true);
    		mapView.setTool(objectBar);
            container.redraw();
        }
    	
    }
    
    private class ShowObject extends SelectionAdapter {
    	
    	private final byte mask;
    	
    	public ShowObject(byte mask) {
			this.mask = mask;
		}
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		if ((mapView.getMask() & mask) > 0) {
    			mapView.removeMask(mask);
    		} else {
    			mapView.addMask(mask);
    		}
           	mapView.redrawAll();
           	mapView.redraw();
        }
    	
    }

	@Override
	public void contentChanged(java.lang.Object source) {
		setDirty(true);
	}

    /**
     * 文本修改后设置修改标志。
     */
	@Override
    public void modifyText(ModifyEvent e) {
//        if (e.widget == npcSearch) {
//            npcSearchText = npcSearch.getText();
//            StructuredSelection sel = (StructuredSelection) npcTemplateListViewer.getSelection();
//            Object selObj = sel.isEmpty() ? null : sel.getFirstElement();
//            npcTemplateListViewer.refresh();
//            if (selObj != null) {
//                sel = new StructuredSelection(selObj);
//                npcTemplateListViewer.setSelection(sel);
//            }
//        } else {
		setDirty(true);
//        }
    }

}
