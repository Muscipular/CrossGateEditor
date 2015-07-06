package cg.editor.map;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.jdom.Element;

import cg.base.image.ImageReader;
import cg.base.map.MapCell;
import cg.base.map.MapCellContainer;
import cg.base.sprite.Unit;
import cg.base.util.MathUtil;
import cg.data.map.MapInfo;
import cg.data.map.Warp;
import cg.data.sprite.NpcTemplate;
import cg.data.time.GameTime;
import cg.editor.data.BaseEditorObject;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.EditorObject;

public class EditorMap extends BaseEditorObject implements MapInfo, MapCellContainer {
	
	private byte dayState;
	
	private int maxEast, maxSouth, mapId, width, height;
	
	private short enemyLevel;
	
	private EditorMapCell[] mapCells;
	
	private Map<Integer, Integer> warpIds;
	
	private MapInfo source;
	
	private Rect[] rects;
	
	private Map<Integer, NpcTemplate> npcs;
	
	private ImageReader imageReader;

	public EditorMap() {
		super();
		warpIds = new HashMap<Integer, Integer>();
		npcs = new HashMap<Integer, NpcTemplate>();
	}
	
	public void setImageReader(ImageReader imageReader) {
		this.imageReader = imageReader;
	}
	
	protected void setSource(MapInfo source) {
		this.source = source;
	}
	
	protected void load() {
		if (source != null) {
			setSize(source.getMaxEast(), source.getMaxSouth());
			Map<Integer, Warp> warps = CrossGateEditor.getWarpManager().getWarps(source.getMapId());
			for (int east = 0;east < getMaxEast();east++) {
				for (int south = 0;south < getMaxSouth();south++) {
					int index = calcIndex(east, south);
					mapCells[index].setImageGlobalId(source.getImageGlobalId(east, south));
					mapCells[index].setObjectId(source.getObjectId(east, south));
					mapCells[index].setMark(source.getMark(east, south));
					if (mapCells[index].getMark() == MapCell.MARK_WARP) {
						addWarp(warps.get(source.getWarpId(east, south)));
					}
				}
			}
			setDayState(GameTime.TIME_ALL);
			source = null;
		}
		setRects();
	}

	@SuppressWarnings("hiding")
	@Override
	public <EditorMap extends EditorObject> void update(EditorMap temp) {
		super.update(temp);
	}

	@Override
	public Element save() {
		Element ret = new Element(getClass().getSimpleName());

		save(ret);
        
		return ret;
	}

	@Override
	public int getMapId() {
		return mapId;
	}

	@Override
	public int getMaxEast() {
		return maxEast;
	}

	@Override
	public int getMaxSouth() {
		return maxSouth;
	}

	@Override
	public int getImageGlobalId(int east, int south) {
		return getMapCell(east, south).getImageGlobalId();
	}

	@Override
	public int getObjectId(int east, int south) {
		return getMapCell(east, south).getObjectId();
	}

	@Override
	public byte getMark(int east, int south) {
		return getMapCell(east, south).getMark();
	}

	@Override
	public int getWarpId(int east, int south) {
		int key = calcIndex(east, south);
		return warpIds.containsKey(key) ? warpIds.get(key) : NO_WARP_ID;
	}

	@Override
	public void addWarp(Warp warp) {
		int key = calcIndex(warp.getSourceMapEast(), warp.getSourceMapSouth());
		warpIds.put(key, warp.getId());
		getMapCell(warp.getSourceMapEast(), warp.getSourceMapSouth()).setMark(EditorMapCell.MARK_WARP);
	}

	@Override
	public short getEnemyLevel() {
		return enemyLevel;
	}

	@Override
	public void setObject(int east, int south, int resourceId) {
		getMapCell(east, south).setObjectId(resourceId);
	}
	
	protected void setSize(int maxEast, int maxSouth) {
		if (mapCells == null) {
			this.maxEast = maxEast;
			this.maxSouth = maxSouth;
			mapCells = new EditorMapCell[maxEast * maxSouth];
			for (int i = 0;i < mapCells.length;i++) {
				mapCells[i] = new EditorMapCell(i % getMaxEast(), i / getMaxEast(), imageReader);
				calcMapCellXY(mapCells[i]);
			}
			width = (getMaxEast() + getMaxSouth()) * CELL_HALF_WIDTH + CELL_HALF_WIDTH;
			height = (getMaxEast() + getMaxSouth()) * CELL_HALF_HEIGHT + CELL_HALF_HEIGHT;
		} else if (getMaxEast() != maxEast || getMaxSouth() != maxSouth) {
			this.maxEast = maxEast;
			this.maxSouth = maxSouth;
			width = (getMaxEast() + getMaxSouth()) * CELL_HALF_WIDTH + CELL_HALF_WIDTH;
			height = (getMaxEast() + getMaxSouth()) * CELL_HALF_HEIGHT + CELL_HALF_HEIGHT;
		}
	}
	
	private void setRects() {
		rects = new Rect[(getWidth() / CELL_HALF_WIDTH) * (getHeight() / CELL_HALF_HEIGHT)];
		for (int i = 0;i < rects.length;i++) {
			rects[i] = new Rect();
		}
		int[] dirs = new int[]{0, 0, 0, -1, -1, -1, -1, 0};
		for (int i = 0;i < mapCells.length;i++) {
			int column = mapCells[i].getX() / CELL_HALF_WIDTH, row = mapCells[i].getY() / CELL_HALF_HEIGHT;
			for (int dir = 0;dir < (dirs.length >> 1);dir++) {
				int index = calcRectIndex(column + dirs[dir << 1], row + dirs[(dir << 1) + 1]);
				rects[index].indexs[rects[index].indexs[0] == Rect.NONE_INDEX ? 0 : 1] = i;
			}
		}
	}
	
	private int calcRectIndex(int column, int row) {
		return row * (getWidth() / CELL_HALF_WIDTH) + column;
	}
	
	@Override
	public EditorMapCell getMapCell(int east, int south) {
		return mapCells[calcIndex(east, south)];
	}
	
	private int calcIndex(int east, int south) {
		return south * getMaxEast() + east;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public EditorMapCell getRectMapCell(int x, int y) {
		if (x < 0 || y < 0) {
			return null;
		}
		int column = x / CELL_HALF_WIDTH, row = y / CELL_HALF_HEIGHT;
		int index = calcRectIndex(column, row);
		if (index < rects.length) {
			int[] indexs = rects[calcRectIndex(column, row)].indexs;
			if (indexs[0] == Rect.NONE_INDEX) {
				return null;
			} else if (indexs[1] == Rect.NONE_INDEX) {
				return mapCells[indexs[0]];
			} else {
				double range_1 = MathUtil.getRange(x, y, mapCells[indexs[0]].getX(), mapCells[indexs[0]].getY());
				double range_2 = MathUtil.getRange(x, y, mapCells[indexs[1]].getX(), mapCells[indexs[1]].getY());
				return range_1 > range_2 ? mapCells[indexs[1]] : mapCells[indexs[0]];
			}
		} else {
			return null;
		}
	}
	
	public void write(OutputStream os) throws Exception {
		int length = MapInfo.DATA_LENGTH;
		byte[] datas = new byte[getMaxEast() * getMaxSouth() << 2];
		for (int i = 0;i < mapCells.length;i++) {
			MathUtil.intToByte(datas, i * length, length, mapCells[i].getImageGlobalId());
		}
		int offset = getMaxEast() * getMaxSouth() << 1;
		for (int i = 0;i < mapCells.length;i++) {
			MathUtil.intToByte(datas, offset + i * length, length, mapCells[i].getObjectId());
		}
		os.write(datas);
	}
	
	public void northLarge(int n) {
		setSize(getMaxEast(), n + getMaxSouth());
		int sizeOffset = n * getMaxEast();
		EditorMapCell[] ret = new EditorMapCell[mapCells.length + sizeOffset];
		for (int i = 0;i < sizeOffset;i++) {
			ret[i] = new EditorMapCell(i % getMaxEast(), i / getMaxEast(), imageReader);
			calcMapCellXY(ret[i]);
		}
		for (int i = sizeOffset;i < ret.length;i++) {
			ret[i] = mapCells[i - sizeOffset];
			ret[i].setEast(i % getMaxEast());
			ret[i].setSouth(i / getMaxEast());
			calcMapCellXY(ret[i]);
		}
		mapCells = ret;
		setRects();
	}
	
	public void southLarge(int n) {
		setSize(getMaxEast(), n + getMaxSouth());
		int sizeOffset = n * getMaxEast();
		EditorMapCell[] ret = new EditorMapCell[mapCells.length + sizeOffset];
		for (int i = 0;i < mapCells.length;i++) {
			ret[i] = mapCells[i];
		}
		for (int i = mapCells.length;i < ret.length;i++) {
			ret[i] = new EditorMapCell(i % getMaxEast(), i / getMaxEast(), imageReader);
			calcMapCellXY(ret[i]);
		}
		mapCells = ret;
		setRects();
	}
	
	public void westLarge(int n) {
		int maxEast = getMaxEast();
		setSize(n + maxEast, getMaxSouth());
		int sizeOffset = n * getMaxSouth();
		EditorMapCell[] ret = new EditorMapCell[mapCells.length + sizeOffset];
		for (int east = 0;east < n;east++) {
			for (int south = 0;south < getMaxSouth();south++) {
				int index = calcIndex(east, south);
				ret[index] = new EditorMapCell(east, south, imageReader);
				calcMapCellXY(ret[index]);
			}
		}
		for (int east = n;east < getMaxEast();east++) {
			for (int south = 0;south < getMaxSouth();south++) {
				int index = calcIndex(east, south);
				ret[index] = mapCells[south * maxEast + (east - n)];
				ret[index].setEast(east);
				ret[index].setSouth(south);
				calcMapCellXY(ret[index]);
			}
		}
		mapCells = ret;
		setRects();
	}
	
	public void eastLarge(int n) {
		int maxEast = getMaxEast();
		setSize(n + maxEast, getMaxSouth());
		int sizeOffset = n * getMaxSouth();
		EditorMapCell[] ret = new EditorMapCell[mapCells.length + sizeOffset];
		for (int east = 0;east < maxEast;east++) {
			for (int south = 0;south < getMaxSouth();south++) {
				int index = calcIndex(east, south);
				ret[index] = mapCells[south * maxEast + east];
				calcMapCellXY(ret[index]);
			}
		}
		for (int east = maxEast;east < getMaxEast();east++) {
			for (int south = 0;south < getMaxSouth();south++) {
				int index = calcIndex(east, south);
				ret[index] = new EditorMapCell(east, south, imageReader);
				calcMapCellXY(ret[index]);
			}
		}
		mapCells = ret;
		setRects();
	}
	
	public void large(byte dir, int n) {
		switch (dir) {
		case Unit.DIR_NORTH : 
			northLarge(n);
			break;
		case Unit.DIR_EAST : 
			eastLarge(n);
			break;
		case Unit.DIR_SOUTH : 
			southLarge(n);
			break;
		case Unit.DIR_WEST : 
			westLarge(n);
			break;
		default : 
			throw new IllegalArgumentException("Unsupport dir : " + dir);
		}
	}
	
	public void northSmall(int n) {
		if (getMaxSouth() > n) {
			setSize(getMaxEast(), getMaxSouth() - n);
			int sizeOffset = n * getMaxEast();
			EditorMapCell[] ret = new EditorMapCell[mapCells.length - sizeOffset];
			for (int i = 0;i < ret.length;i++) {
				ret[i] = mapCells[i + sizeOffset];
				ret[i].setEast(i % getMaxEast());
				ret[i].setSouth(i / getMaxEast());
				calcMapCellXY(ret[i]);
			}
			mapCells = ret;
			setRects();
		}
	}
	
	public void southSmall(int n) {
		if (getMaxSouth() > n) {
			setSize(getMaxEast(), getMaxSouth() - n);
			int sizeOffset = n * getMaxEast();
			EditorMapCell[] ret = new EditorMapCell[mapCells.length - sizeOffset];
			for (int i = 0;i < ret.length;i++) {
				ret[i] = mapCells[i];
				calcMapCellXY(ret[i]);
			}
			mapCells = ret;
			setRects();
		}
	}
	
	public void westSmall(int n) {
		int maxEast = getMaxEast();
		if (maxEast > n) {
			setSize(maxEast - n, getMaxSouth());
			int sizeOffset = n * getMaxSouth();
			EditorMapCell[] ret = new EditorMapCell[mapCells.length - sizeOffset];
			for (int east = 0;east < getMaxEast();east++) {
				for (int south = 0;south < getMaxSouth();south++) {
					int index = calcIndex(east, south);
					ret[index] = mapCells[south * maxEast + (east + n)];
					ret[index].setEast(east);
					ret[index].setSouth(south);
					calcMapCellXY(ret[index]);
				}
			}
			mapCells = ret;
			setRects();
		}
	}
	
	public void eastSmall(int n) {
		int maxEast = getMaxEast();
		if (maxEast > n) {
			setSize(maxEast - n, getMaxSouth());
			int sizeOffset = n * getMaxSouth();
			EditorMapCell[] ret = new EditorMapCell[mapCells.length - sizeOffset];
			for (int east = 0;east < getMaxEast();east++) {
				for (int south = 0;south < getMaxSouth();south++) {
					int index = calcIndex(east, south);
					ret[index] = mapCells[south * maxEast + east];
					calcMapCellXY(ret[index]);
				}
			}
			mapCells = ret;
			setRects();
		}
	}
	
	public void small(byte dir, int n) {
		switch (dir) {
		case Unit.DIR_NORTH : 
			northSmall(n);
			break;
		case Unit.DIR_EAST : 
			eastSmall(n);
			break;
		case Unit.DIR_SOUTH : 
			southSmall(n);
			break;
		case Unit.DIR_WEST : 
			westSmall(n);
			break;
		default : 
			throw new IllegalArgumentException("Unsupport dir : " + dir);
		}
	}
	
	private void calcMapCellXY(EditorMapCell mapCell) {
		int eastX = mapCell.getEast() * EditorMap.CELL_HALF_WIDTH;
		int southX = mapCell.getSouth() * EditorMap.CELL_HALF_WIDTH;
		int x = eastX + southX + EditorMap.CELL_HALF_WIDTH;
		int eastY = (getMaxEast() - mapCell.getEast()) * EditorMap.CELL_HALF_HEIGHT;
		int southY = mapCell.getSouth() * EditorMap.CELL_HALF_HEIGHT;
		int y = eastY + southY + EditorMap.CELL_HALF_HEIGHT;
		
		mapCell.setX(x);
		mapCell.setY(y);
	}
	
	public byte getDayState() {
		return dayState;
	}

	public void setDayState(byte dayState) {
		this.dayState = dayState;
		npcs.clear();
		List<NpcTemplate> npcList = CrossGateEditor.getNpcManager().getNpcInfos(getId());
		for (int i = 0;i < npcList.size();i++) {
			NpcTemplate npcTemplate = npcList.get(i);
			if (dayState == GameTime.TIME_ALL || npcTemplate.getAppearTime() == dayState) {
				int[] coordinates = npcTemplate.getCoordinates();
				for (int j = 0;j < coordinates.length >> 1;j++) {
					int key = calcIndex(coordinates[j << 1], coordinates[(j << 1) + 1]);
					NpcTemplate temp = npcs.get(key);
					if (temp == null) {
						npcs.put(calcIndex(coordinates[j << 1], coordinates[(j << 1) + 1]), npcTemplate);
					} else if (temp != npcTemplate) {
						MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "´íÎó", "NPCÎ»ÖÃÖØ¸´(" + temp + ") (" + npcTemplate + ")¡£");
					}
				}
			}
		}
	}
	
	public NpcTemplate getNpc(int east, int south) {
		int key = calcIndex(east, south);
		return npcs.get(key);
	}

	private static class Rect {
		
		public static final int NONE_INDEX = -1;
		
		private int[] indexs = new int[2];
		
		public Rect() {
			indexs[0] = NONE_INDEX;
			indexs[1] = NONE_INDEX;
		}
		
	}

}
