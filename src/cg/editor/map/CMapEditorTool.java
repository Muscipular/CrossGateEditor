package cg.editor.map;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;

public interface CMapEditorTool {
	
	void mouseDown(MouseEvent e);
	
	void paint(GC g, int middleX, int middleY);

}
