package cg.editor.effect;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import cg.editor.extend.DefaultDataObjectEditor;

/**
 * 效果编辑器
 * @author 	hyfu
 */
public class EffectEditor extends DefaultDataObjectEditor {
	
	/**
	 * 编辑器id
	 */
    public static final String ID = "feng.editor.effect.EffectEditor";
    
    /**
     * id输入框
     */
    private Text textID;
    /**
     * 名称输入框
     */
    private Text textTitle;

	@Override
	public void createPartControl(Composite parent) {
		setPartName(editObject.getName());
		saveStateToUndoBuffer();
		
		SashForm container = new SashForm (parent, SWT.VERTICAL);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Group rowExt = new Group(container, SWT.NONE);
		rowExt.setText("效果信息");
		GridData extLayoutData = new GridData(GridData.FILL_BOTH);
		rowExt.setLayoutData(extLayoutData);
		
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 5;
		rowExt.setLayout(groupLayout);
		GridData data = new GridData(200, -1);
		GridData data1 = new GridData(300, -1);
		
		Label label = new Label(rowExt, SWT.NONE);
		label.setText("ID：");
		textID = new Text(rowExt, SWT.BORDER);
		textID.setLayoutData(data);
		textID.setText(editObject.getId() + "");
		textID.addModifyListener(this);
		
		Label labelTitle = new Label(rowExt, SWT.NONE);
		labelTitle.setText("名称： ");
		textTitle = new Text(rowExt, SWT.BORDER);
		textTitle.setSize(textTitle.computeSize(300, -1));
		textTitle.setLayoutData(data1);
		textTitle.setText(editObject.getName());
		textTitle.addModifyListener(this);
	}

	@Override
	protected void saveData() throws Exception {
		editObject.setId(Integer.parseInt(textID.getText().trim()));
		editObject.setName(textTitle.getText().trim());

		setPartName(editObject.getName());
	}
}