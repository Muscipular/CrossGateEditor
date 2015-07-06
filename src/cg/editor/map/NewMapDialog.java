package cg.editor.map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cg.editor.extend.AutoSelectAll;

public class NewMapDialog extends Dialog {
	
	private String name;
	
	private int east, south;
	
	private Text textName, textEast, textSouth;

	protected NewMapDialog(Shell parentShell) {
		super(parentShell);
	}
	
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    @Override
	protected void configureShell(Shell newShell) {
    	super.configureShell(newShell);
    	newShell.setText("新建地图");
	}

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            name = textName.getText().trim();
            if (name.length() == 0) {
                MessageDialog.openError(getShell(), "错误", "名称不能为空。");
                return;
            } else {
            	try {
            		east = Integer.parseInt(textEast.getText().trim());
            		south = Integer.parseInt(textSouth.getText().trim());
            	} catch (Exception e) {
            		MessageDialog.openError(getShell(), "错误", "非数值。");
				}
            }
        }
        super.buttonPressed(buttonId);
    }

	@Override
    protected Control createDialogArea(Composite parent) {
    	Composite container = (Composite) super.createDialogArea(parent);

        GridLayout gridLayout = new GridLayout();
        container.setLayout(gridLayout);

        Label label = new Label(container, SWT.NONE);
        label.setText("地图名称：");

        textName = new Text(container, SWT.BORDER);
        textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textName.setText("新地图");
        textName.addFocusListener(AutoSelectAll.instance);

        label = new Label(container, SWT.NONE);
        label.setText("EAST：");

        textEast = new Text(container, SWT.BORDER);
        textEast.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textEast.setText("10");
        textEast.addFocusListener(AutoSelectAll.instance);

        label = new Label(container, SWT.NONE);
        label.setText("SOUTH：");

        textSouth = new Text(container, SWT.BORDER);
        textSouth.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textSouth.setText("10");
        textSouth.addFocusListener(AutoSelectAll.instance);
    	
    	return container;
    }

	public String getName() {
		return name;
	}

	public int getEast() {
		return east;
	}

	public int getSouth() {
		return south;
	}

}
