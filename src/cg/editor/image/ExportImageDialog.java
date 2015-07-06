package cg.editor.image;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import cg.editor.extend.AutoSelectAll;

public class ExportImageDialog extends Dialog {
	
	private String path, suffix, fileTypes[];
	
	private int count;
	
	private Text textPath, textCount;
	
	private Combo fileType;

	protected ExportImageDialog(Shell parentShell, String[] fileTypes) {
		super(parentShell);
		this.fileTypes = fileTypes;
	}
	
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "确定", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "取消", false);
    }

    @Override
	protected void configureShell(Shell newShell) {
    	super.configureShell(newShell);
    	newShell.setText("导出图片");
	}

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	path = textPath.getText().trim();
        	suffix = fileType.getText();
            if (path.length() == 0) {
                MessageDialog.openError(getShell(), "错误", "相当路径不能为空。");
                return;
            } else {
            	try {
            		count = Integer.parseInt(textCount.getText());
            	} catch (Exception e) {
            		MessageDialog.openError(getShell(), "错误", e.getMessage());
                    return;
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
        label.setText("导出路径：");

        textPath = new Text(container, SWT.BORDER);
        textPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textPath.addFocusListener(AutoSelectAll.instance);

        label = new Label(container, SWT.NONE);
        label.setText("格式：");

        fileType = new Combo(container, SWT.READ_ONLY);
        fileType.setVisibleItemCount(fileTypes.length);
        fileType.setItems(fileTypes);

        label = new Label(container, SWT.NONE);
        label.setText("数量：");

        textCount = new Text(container, SWT.BORDER);
        textCount.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
    	
    	return container;
    }
	
	public String getPath() {
		return path;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public int getCount() {
		return count;
	}

}
