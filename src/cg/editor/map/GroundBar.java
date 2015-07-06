package cg.editor.map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import cg.base.util.MathUtil;
import cg.editor.Activator;
import cg.editor.data.ContentListener;
import cg.editor.image.EditorImageManager;
import cg.editor.swtdesigner.ResourceManager;

public class GroundBar extends ToolBar implements CMapEditorTool {
	
	protected int page, imageSize, imageGlobalIds[], imageGlobalId;
	
	protected MapView mapView;
	
	protected EditorImageManager imageManager;
	
	protected Image curorImage;
	
	protected ContentListener contentListener;

	public GroundBar(Composite parent, int style) {
		super(parent, style);
	}
	
	@Override
	protected void checkSubclass() {}

	void init(MapView mapView) {
		this.mapView = mapView;
		imageManager = mapView.getEditorImageManager();
		imageSize = EditorImageManager.getUnAnimationImageInfos().size();
		
    	ToolItem[] items = new ToolItem[9];
    	imageGlobalIds = new int[items.length];

    	ToolItem pageUpItem = new ToolItem(this, SWT.PUSH);
    	for (int i = 0;i < items.length;i++) {
    		items[i] = new ToolItem(this, SWT.RADIO);
    		items[i].addSelectionListener(new Ground(items[i], i));
    	}
    	refreshItems(items);
        pageUpItem.addSelectionListener(new ImagePageUp(items));
        pageUpItem.setToolTipText("Ç°Ò³");
        pageUpItem.setSelection(false);
        pageUpItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/up.gif"));

    	ToolItem pageDownItem = new ToolItem(this, SWT.PUSH);
        pageDownItem.addSelectionListener(new ImagePageDown(items));
        pageDownItem.setToolTipText("ºóÒ³");
        pageDownItem.setSelection(false);
        pageDownItem.setImage(ResourceManager.getPluginImage(Activator.getDefault(), "icons/mapeditor/up.gif"));
        
        mapView.setTool(this);
	}
	
	private void refreshItems(ToolItem[] items) {
		int offset = items.length * page;
		int loop = (offset + items.length) < imageSize ? items.length : (imageSize - offset);
		for (int i = 0;i < loop;i++) {
			items[i].setSelection(false);
			imageGlobalIds[i] = EditorImageManager.getUnAnimationImageInfos().get(offset + i).getGlobalId();
			Image image = imageManager.getImage(imageGlobalIds[i], Display.getCurrent());
			items[i].setImage(image);
		}
	}
	
	private class Ground extends SelectionAdapter {
		
		private final ToolItem item;
		
		private final int index;
		
		public Ground(ToolItem item, int index) {
			this.item = item;
			this.index = index;
		}
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		curorImage = item.getImage();
    		imageGlobalId = imageGlobalIds[index];
    	}
		
	}
    
    private class ImagePageUp extends SelectionAdapter {
    	
    	private final ToolItem[] items;
    	
    	public ImagePageUp(ToolItem[] items) {
			this.items = items;
		}
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		if (page > 0) {
    			page--;
    			refreshItems(items);
    		}
    		curorImage = null;
    	}
    	
    }
    
    private class ImagePageDown extends SelectionAdapter {
    	
    	private final ToolItem[] items;
    	
    	private final int size;
    	
    	public ImagePageDown(ToolItem[] items) {
			this.items = items;
			size = MathUtil.divAddOne(imageSize, items.length);
		}
    	
    	@Override
        public void widgetSelected(SelectionEvent e) {
    		if (page < size) {
    			page++;
    			refreshItems(items);
    		}
    		curorImage = null;
    	}
    	
    }

	@Override
	public void mouseDown(MouseEvent e) {
		if (e.button == 1) {
			EditorMapCell mapCell = mapView.getMapCell();
			if (mapCell != null && curorImage != null) {
				replace(mapCell);
	    		contentListener.contentChanged(mapView);
	    		mapView.redrawAll();
				mapView.redraw();
			}
		}
	}
	
	protected void replace(EditorMapCell mapCell) {
		mapCell.setImageGlobalId(imageGlobalId);
	}

	@Override
	public void paint(GC g, int middleX, int middleY) {
		EditorMapCell mapCell = mapView.getMapCell();
		if (curorImage != null && mapCell != null) {
			int x = (mapView.getBounds().width >> 1) - (middleX - mapCell.getX()) - (curorImage.getBounds().width >> 1);
			int y = (mapView.getBounds().height >> 1) - (middleY - mapCell.getY()) - (curorImage.getBounds().height >> 1);
			g.drawImage(curorImage, x, y);
			g.drawString("Cell(" + x + ", " + y + ") EAST = " + mapCell.getEast() + " , SOUTH = " + mapCell.getSouth(), 10, getBounds().height - 90);
		}
	}
	
	public void setContentListener(ContentListener contentListener) {
		this.contentListener = contentListener;
	}

}
