package cg.editor.extend;

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Text;

public class AutoSelectAll implements FocusListener {
	
	public static AutoSelectAll instance = new AutoSelectAll();
	
	private AutoSelectAll() {}

	@Override
	public void focusGained(FocusEvent e) {
        if (e.getSource() instanceof Text) {
            ((Text) e.getSource()).selectAll();
        }
	}

	@Override
	public void focusLost(FocusEvent arg0) {}

}
