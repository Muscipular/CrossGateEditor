package cg.editor.map;

import static cg.base.map.MapCellContainer.CELL_HALF_HEIGHT;
import static cg.base.map.MapCellContainer.CELL_HALF_WIDTH;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import cg.base.image.ImageDictionary;
import cg.base.image.ImageReader;
import cg.data.sprite.NpcTemplate;
import cg.editor.image.EditorImageManager;
import cg.editor.image.ImageUtils;
import cg.editor.swtdesigner.SWTResourceManager;

public class MapView extends Canvas implements PaintListener, DisposeListener {
	
	public static final boolean DEFAULT_SHOW_OBJECT = true;
	
	public static final byte MASK_SHOW_GROUND = 2 << 0;
	
	public static final byte MASK_SHOW_OBJECT = 2 << 1;
	
	public static final byte MASK_SHOW_NPC = 2 << 2;
	
	public static final byte MASK_SHOW_GRID = 2 << 3;
	
	private static RGB defaultBackgroundColor = new RGB(0xEE, 0xF2, 0xFB);
	
	private byte mask;

	private int middleX, middleY, curorX, curorY;
	
	private double ratio = 1.0;
	
	private Point paintOffset, previousPoint;
	
	private boolean showGrid, draggingOffset, redrawImage;
	
	private Image mapImage;
	
	private BufferedImage bufferedImage;
	
	private EditorMapCell mapCell;
	
	private CMapEditorTool tool;
	
	private final EditorMap editorMap;
	
	private final EditorImageManager imageManager;
	
	private final ImageReader imageReader;
	
	public MapView(Composite parent, int style, final EditorMap editorMap, ImageReader imageReader) {
		super(parent, style | SWT.NO_BACKGROUND);
		this.editorMap = editorMap;
		this.imageReader = imageReader;
		addPaintListener(this);
		setBackground(SWTResourceManager.getColor(defaultBackgroundColor));
		paintOffset = new Point(0, 0);
		middleX = editorMap.getWidth() >> 1;
		middleY = editorMap.getHeight() >> 1;
		imageManager = new EditorImageManager(imageReader);
		addMask((byte) (MASK_SHOW_GROUND | MASK_SHOW_GRID | MASK_SHOW_NPC | MASK_SHOW_OBJECT));

		addFocusListener(new FocusListener() {

			@Override
            public void focusGained(FocusEvent e) {
                redraw();
            }

			@Override
            public void focusLost(FocusEvent e) {
                redraw();
            }
			
        });
        addTraverseListener(new TraverseListener() {

        	@Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
                    e.doit = true;
                }
            };
            
        });
        addListener(SWT.KeyDown, new Listener() {

        	@Override
            public void handleEvent(Event event) {
//            	keyEventMask = event.stateMask;
            	onKeyDown(event.keyCode);
            }
        	
        });
        addListener(SWT.KeyUp, new Listener() {

        	@Override
            public void handleEvent(Event event) {
//            	keyEventMask = event.stateMask;
                onKeyUp(event.keyCode);
            }
        	
        });
		addMouseMoveListener(new MouseMoveListener() {

        	@Override
			public void mouseMove(MouseEvent e) {
				curorX = e.x;
				curorY = e.y;
				if (draggingOffset) {
//					if (oldMenu == null) {
//						oldMenu = AbstractImageViewer.this.getMenu();
//						AbstractImageViewer.this.setMenu(null);
//					}
					paintOffset.x += e.x - previousPoint.x;
					paintOffset.y += e.y - previousPoint.y;
					middleX -= e.x - previousPoint.x;
					middleY -= e.y - previousPoint.y;
					previousPoint = new Point(e.x, e.y);
				} else if (e.button != 1) {
					redrawImage = false;
					EditorMapCell mapCell = editorMap.getRectMapCell(middleX - ((getBounds().width >> 1) - curorX), middleY - ((getBounds().height >> 1) - curorY));
					if (MapView.this.mapCell != mapCell) {
						MapView.this.mapCell = mapCell;
						redraw();
					}
				}
			}
        	
		});
        addMouseListener(new MouseAdapter() {

        	@Override
			public void mouseDown(MouseEvent e) {
				if (tool != null) {
					tool.mouseDown(e);
				}
				if (e.button == 3) {
					if (draggingOffset == false) {
						draggingOffset = true;
						previousPoint = new Point(e.x, e.y);
					} else {
						draggingOffset = false;
					}
				}
			}

        	@Override
			public void mouseUp(MouseEvent e) {
				if (e.button == 3 && draggingOffset) {
					draggingOffset = false;
					redrawImage = true;
					redraw();
//					if (oldMenu != null) {
//						AbstractImageViewer.this.setMenu(oldMenu);
//						oldMenu = null;
//					}
				}
			}
        	
		});
        addMouseWheelListener(new MouseWheelListener() {

        	@Override
            public void mouseScrolled(MouseEvent e) {
                if (e.count > 0) {
                    zoomin();
                } else {
                    zoomout();
                }
            }
        	
        });
        addDisposeListener(this);
	}
	
	private void paintBackGround(Graphics g) {
		g.setColor(java.awt.Color.BLACK);
		g.setClip(0, 0, getBounds().width << 1, getBounds().height << 1);
		g.fillRect(0, 0, getBounds().width << 1, getBounds().height << 1);
	}
	
	private void paintGround(Graphics g, int left, int right, int top, int bottom) {
		if ((mask & MASK_SHOW_GROUND) > 0) {
			for (int east = 0;east < editorMap.getMaxEast();east++) {
				for (int south = 0;south < editorMap.getMaxSouth();south++) {
					EditorMapCell mapCell = editorMap.getMapCell(east, south);
					ImageDictionary image = mapCell.getGroundImage();
					if (image != null && canPaint(mapCell.getX(), mapCell.getY(), left, right, top, bottom)) {
						int x = getBounds().width - (middleX - mapCell.getX()) + (image == null ? -CELL_HALF_WIDTH : image.getOffsetX());
						int y = getBounds().height - (middleY - mapCell.getY()) + (image == null ? -CELL_HALF_HEIGHT : image.getOffsetY());
						g.setClip(x, y, image.getWidth(), image.getHeight());
						g.drawImage(image.bufferedImage(), x, y, null);
					}
	 			}
			}
		}
	}
	
	private void paintObject(Graphics g, int left, int right, int top, int bottom) {
		for (int east = editorMap.getMaxEast() - 1;east >= 0;east--) {
			for (int south = 0;south < editorMap.getMaxSouth();south++) {
				EditorMapCell mapCell = editorMap.getMapCell(east, south);
				if (canPaint(mapCell.getX(), mapCell.getY(), left, right, top, bottom)) {
					// object
					ImageDictionary image = mapCell.getObjectImage();
					if (image != null && (mask & MASK_SHOW_OBJECT) > 0) {
						int x = getBounds().width - (middleX - mapCell.getX() - image.getOffsetX());
						int y = getBounds().height - (middleY - mapCell.getY() - image.getOffsetY());
						g.setClip(x, y, image.getWidth(), image.getHeight());
						g.drawImage(image.bufferedImage(), x, y, null);
					}
					// npc
					NpcTemplate npcTemplate = editorMap.getNpc(east, south);
					if (npcTemplate != null && (mask & MASK_SHOW_NPC) > 0) {
						image = imageReader.getImageDictionary(npcTemplate.getResourcesId());
						int x = getBounds().width - (middleX - mapCell.getX() - image.getOffsetX());
						int y = getBounds().height - (middleY - mapCell.getY() - image.getOffsetY());
						g.setClip(x, y, image.getWidth(), image.getHeight());
						g.drawImage(image.bufferedImage(), x, y, null);
					}
				}
			}
		}
	}
	
	private void paintGrid(Graphics g, int left, int right, int top, int bottom) {
		if ((mask & MASK_SHOW_GRID) > 0) {
			g.setColor(java.awt.Color.GREEN);
			for (int east = 0;east < editorMap.getMaxEast();east++) {
				for (int south = 0;south < editorMap.getMaxSouth();south++) {
					EditorMapCell mapCell = editorMap.getMapCell(east, south);
					if (canPaint(mapCell.getX(), mapCell.getY(), left, right, top, bottom)) {
						int x = (bufferedImage.getWidth() >> 1) - (middleX - mapCell.getX());
						int y = (bufferedImage.getHeight() >> 1) - (middleY - mapCell.getY());
						g.setClip(x - CELL_HALF_WIDTH, y - CELL_HALF_HEIGHT, x + CELL_HALF_WIDTH, y + CELL_HALF_HEIGHT);
						g.drawLine(x - CELL_HALF_WIDTH, y, x, y - CELL_HALF_HEIGHT);
						g.drawLine(x, y - CELL_HALF_HEIGHT, x + CELL_HALF_WIDTH, y);
						g.drawLine(x + CELL_HALF_WIDTH, y, x, y + CELL_HALF_HEIGHT);
						g.drawLine(x, y + CELL_HALF_HEIGHT, x - CELL_HALF_WIDTH, y);
					}
	 			}
			}
		}
	}

	@Override
	public void widgetDisposed(DisposeEvent arg0) {
		removePaintListener(this);
	}

	@Override
	public void paintControl(PaintEvent e) {
//		paintBackGround(e.gc);
		int left = middleX - getBounds().width;
		int right = middleX + getBounds().width;
		int top = middleY - getBounds().height;
		int bottom = middleY + getBounds().height;
//		paintGround(e.gc, left, right, top, bottom);
//		paintObject(e.gc, left, right, top, bottom);
//		paintGrid(e.gc, left, right, top, bottom);
		
		if (redrawImage) {
			bufferedImage = new BufferedImage(getBounds().width << 1, getBounds().height << 1, BufferedImage.TYPE_INT_ARGB);
			Graphics g = bufferedImage.getGraphics();
			paintBackGround(g);
			paintGround(g, left, right, top, bottom);
			paintObject(g, left, right, top, bottom);
			paintGrid(g, left, right, top, bottom);
			mapImage = ImageUtils.createImage(e.gc.getDevice(), bufferedImage);
		} else {
			redrawImage = true;
		}

		if (mapImage != null) {
			e.gc.drawImage(mapImage, -(getBounds().width >> 1), -(getBounds().height >> 1));
		}
//		paintGrid(e.gc, left, right, top, bottom);
		if (tool != null) {
			tool.paint(e.gc, middleX, middleY);
		}
		e.gc.drawString("View(" + curorX + "/" + getBounds().width + ", " + curorY + "/" + getBounds().height + ")", 10, getBounds().height - 70);
		e.gc.drawString("Map(" + middleX + "/" + editorMap.getWidth() + ", " + middleY + "/" + editorMap.getHeight() + ")", 10, getBounds().height - 50);
		if (mapCell != null) {
			e.gc.drawString("Map(" + mapCell.getEast() + "/" + editorMap.getMaxEast() + ", " + mapCell.getSouth() + "/" + editorMap.getMaxSouth() + ")", 10, getBounds().height - 30);
		}
		e.gc.dispose();
	}
	
	@SuppressWarnings("unused")
	private void paintBackGround(GC g) {
		if (g.getBackground() == null) {
			g.setBackground(new Color(g.getDevice(), 0, 0, 0));
		} else {
			g.getBackground().getRGB().blue = 0x00;
			g.getBackground().getRGB().red = 0x00;
			g.getBackground().getRGB().green = 0x00;
		}
		g.fillRectangle(0, 0, getBounds().width, getBounds().height);
	}
	
	@SuppressWarnings("unused")
	private void paintGround(GC g, int left, int right, int top, int bottom) {
		for (int east = 0;east < editorMap.getMaxEast();east++) {
			for (int south = 0;south < editorMap.getMaxSouth();south++) {
				EditorMapCell mapCell = editorMap.getMapCell(east, south);
				ImageDictionary image = mapCell.getGroundImage();
				if (image != null && canPaint(mapCell.getX(), mapCell.getY(), left, right, top, bottom)) {
					int x = (getBounds().width >> 1) - (middleX - mapCell.getX()) + (image == null ? -CELL_HALF_WIDTH : image.getOffsetX());
					int y = (getBounds().height >> 1) - (middleY - mapCell.getY()) + (image == null ? -CELL_HALF_HEIGHT : image.getOffsetY());
					g.drawImage(imageManager.getImage(image, g.getDevice()), x, y);
				}
 			}
		}
	}
	
	private boolean canPaint(int x, int y, int left, int right, int top, int bottom) {
		return x >= middleX - getBounds().width && x <= middleX + getBounds().width
				&& y >= middleY - getBounds().height && y <= middleY + getBounds().height;
	}
	
	@SuppressWarnings("unused")
	private void paintObject(GC g, int left, int right, int top, int bottom) {
		for (int east = editorMap.getMaxEast() - 1;east >= 0;east--) {
			for (int south = 0;south < editorMap.getMaxSouth();south++) {
				EditorMapCell mapCell = editorMap.getMapCell(east, south);
				ImageDictionary image = mapCell.getObjectImage();
				if (image != null && canPaint(mapCell.getX(), mapCell.getY(), left, right, top, bottom)) {
					int x = (getBounds().width >> 1) - (middleX - mapCell.getX() - image.getOffsetX());
					int y = (getBounds().height >> 1) - (middleY - mapCell.getY() - image.getOffsetY());
					g.drawImage(imageManager.getImage(image, g.getDevice()), x, y);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void paintGrid(GC g, int left, int right, int top, int bottom) {
		g.getBackground().getRGB().blue = 0x00;
		g.getBackground().getRGB().red = 0x00;
		g.getBackground().getRGB().green = 0xFF;
		for (int east = 0;east < editorMap.getMaxEast();east++) {
			for (int south = 0;south < editorMap.getMaxSouth();south++) {
				EditorMapCell mapCell = editorMap.getMapCell(east, south);
				if (canPaint(mapCell.getX(), mapCell.getY(), left, right, top, bottom)) {
					int x = (getBounds().width >> 1) - (middleX - mapCell.getX());
					int y = (getBounds().height >> 1) - (middleY - mapCell.getY());
					g.drawLine(x - CELL_HALF_WIDTH, y, x, y - CELL_HALF_HEIGHT);
					g.drawLine(x, y - CELL_HALF_HEIGHT, x + CELL_HALF_WIDTH, y);
					g.drawLine(x + CELL_HALF_WIDTH, y, x, y + CELL_HALF_HEIGHT);
					g.drawLine(x, y + CELL_HALF_HEIGHT, x - CELL_HALF_WIDTH, y);
				}
 			}
		}
	}
	
	private void onKeyDown(int keyCode) {
		if (keyCode == 'w') {
			zoomin();
		} else if (keyCode == 's') {
			zoomout();
		} else if (keyCode == 'g') {
			switchShowGrid();
		}
	}
	
	private void onKeyUp(int keyCode) {
		
	}
	
	private void zoomin() {
		if (ratio < 64) {
			ratio *= 2;
			paintOffset.x *= 2;
			paintOffset.y *= 2;
			redraw();
		}
	}
	
	private void zoomout() {
		if (ratio > 0.125) {
			ratio /= 2;
			paintOffset.x /= 2;
			paintOffset.y /= 2;
			redraw();
		}
	}
	
	private void switchShowGrid() {
		showGrid = !showGrid;
		redraw();
	}
	
	public EditorImageManager getEditorImageManager() {
		return imageManager;
	}

	public void setTool(CMapEditorTool tool) {
		this.tool = tool;
	}
	
	public EditorMapCell getMapCell() {
		return mapCell;
	}
	
	public void redrawAll() {
		redrawImage = true;
	}
	
	public void addMask(byte mask) {
		this.mask |= mask;
	}
	
	public byte getMask() {
		return mask;
	}
	
	public void removeMask(byte mask) {
		this.mask -= mask;
	}

}
