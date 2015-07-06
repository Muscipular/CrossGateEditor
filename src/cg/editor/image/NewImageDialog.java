package cg.editor.image;

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

public class NewImageDialog extends Dialog {
	
	private String path, suffix, name;
	
	private Text textPath, textSuffix, textName;

	protected NewImageDialog(Shell parentShell) {
		super(parentShell);
	}
	
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "ȷ��", true);
        createButton(parent, IDialogConstants.CANCEL_ID, "ȡ��", false);
    }

    @Override
	protected void configureShell(Shell newShell) {
    	super.configureShell(newShell);
    	newShell.setText("�½�ͼƬ");
	}

    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
        	path = textPath.getText().trim();
        	suffix = textSuffix.getText().trim();
        	name = textName.getText().trim();
            if (path.length() == 0) {
                MessageDialog.openError(getShell(), "����", "�൱·������Ϊ�ա�");
                return;
            } else if (suffix.length() == 0) {
                MessageDialog.openError(getShell(), "����", "��׺����Ϊ�ա�");
                return;
            } else if (name.length() == 0) {
                MessageDialog.openError(getShell(), "����", "���Ʋ���Ϊ�ա�");
                return;
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
        label.setText("���ƣ�");

        textName = new Text(container, SWT.BORDER);
        textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textName.addFocusListener(AutoSelectAll.instance);

        label = new Label(container, SWT.NONE);
        label.setText("���·����");

        textPath = new Text(container, SWT.BORDER);
        textPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textPath.addFocusListener(AutoSelectAll.instance);

        label = new Label(container, SWT.NONE);
        label.setText("��׺��");

        textSuffix = new Text(container, SWT.BORDER);
        textSuffix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        textSuffix.addFocusListener(AutoSelectAll.instance);
    	
    	return container;
    }
	
	public String getPath() {
		return path;
	}
	
	public String getSuffix() {
		return suffix;
	}

	public String getName() {
		return name;
	}

}
