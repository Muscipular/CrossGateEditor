package cg.editor.extend;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import cg.editor.View;
import cg.editor.data.CrossGateEditor;
import cg.editor.data.EditorObject;

/**
 * �༭���������
 * @author 	hyfu
 */
public abstract class DefaultDataObjectEditor extends EditorPart implements ModifyListener {
	
    public static final int PROPERTY_CANUNDO = 1;
    
    public static final int PROPERTY_CANREDO = 2;

    public static final String D_ID = "fengEditor.DefaultDataObjectEditor"; //$NON-NLS-1$
    
    // �༭Ŀ�����
    protected EditorObject saveTarget;
    // ��ǰ�༭�Ķ�������Ŀ������һ����¡��ֻ���ڱ���ʱ�Ű��������ݱ��浽saveTarget��
    public EditorObject editObject;
    // ��ǰ�༭�����Ƿ��޸ı�־
    private boolean dirty = false;
    // UNDO/REDO֧��
    protected Object[] undoBuffer;
    
    protected int undoCurrent, undoLast;
    
    protected boolean lockUndoBuffer = false;

    /**
     * Create contents of the editor part
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        setPartName(editObject.getName());
        saveStateToUndoBuffer();
    }

    @Override
    public void setFocus() {
        // Set the focus
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    	// Do the Save operation
		try {
			saveData();
			setDirty(false);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			View view = (View) page.findView(View.ID);
			view.refresh(saveTarget);
			view.saveDataList(editObject.getTypeInfo());
		} catch (Exception e) {
			CrossGateEditor.getLog().error(getClass().getName(), e);
			MessageDialog.openError(getSite().getShell(), "����", e.toString());
//			cache.catchException(e, false);
			monitor.setCanceled(true);
		}
    }

    @Override
    public void doSaveAs() {
        // Do the Save As operation
    }

	@Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        // Initialize the editor part
        setSite(site);
        setInput(input);
        DataObjectInput dinput = (DataObjectInput)input;
        saveTarget = (EditorObject) dinput.dataObject;
        editObject = saveTarget;
        undoBuffer = new Object[1000];
        undoCurrent = undoLast = -1;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    
    /**
     * ��ȡ��ǰ�༭����
     */
    public EditorObject getEditObject() {
        return editObject;
    }
    
    /**
     * ��ȡ�༭�Ķ����ԭʼ����
     */
    public EditorObject getSaveTarget() {
        return saveTarget;
    }
    
    /**
     * ���浱ǰ�༭���ݡ�
     */
    protected abstract void saveData() throws Exception;

    /**
     * ���浱ǰ�༭״̬��һ������������Ӧ���Ǵ˷�����
     */
    protected Object saveState() {
        return editObject.save();
    }
    
    /**
     * ������ǰ����ı༭״̬�ָ���ǰ�༭״̬��������Ӧ���Ǵ˷�����
     */
    protected void loadState(Object stateObj) {
    	
    }
    
    /**
     * ���浱ǰ�༭״̬��UNDO������
     */
    protected void saveStateToUndoBuffer() {
        try {
            Object stateObj = saveState();
            if (stateObj == null) {
                return;
            }
            if (undoCurrent >= undoBuffer.length - 2) {
                byte[][] newBuf = new byte[undoBuffer.length + 1000][];
                System.arraycopy(undoBuffer, 0, newBuf, 0, undoBuffer.length);
            }
            undoBuffer[++undoCurrent] = stateObj;
            undoLast = undoCurrent;
        } catch (Exception e) {
            CrossGateEditor.getLog().error(getClass().getName(), e);
        }
    }
    
    /**
     * ��UNDO�������лָ�����
     * @param 	stateObj
     * 			
     */
    protected void loadStateFromUndoBuffer(Object stateObj) {
        try {
            loadState(stateObj);
            dirty = true;
            firePropertyChange(PROP_DIRTY);
        } catch (Exception e) {
        	CrossGateEditor.getLog().error(getClass().getName(), e);
        }
    }
    
    /**
     * ִ��һ��UNDO������
     */
    public void undo() {
        if (!canUndo()) {
            return;
        }
        lockUndoBuffer = true;
        try {
            loadStateFromUndoBuffer(undoBuffer[--undoCurrent]);
        } finally {
            lockUndoBuffer = false;
        }
        firePropertyChange(PROPERTY_CANUNDO);
        firePropertyChange(PROPERTY_CANREDO);
    }
    
    /**
     * ִ��һ��REDO������
     */
    public void redo() {
        if (!canRedo()) {
            return;
        }
        lockUndoBuffer = true;
        try {
            loadStateFromUndoBuffer(undoBuffer[++undoCurrent]);
        } finally {
            lockUndoBuffer = false;
        }
        firePropertyChange(PROPERTY_CANUNDO);
        firePropertyChange(PROPERTY_CANREDO);
    }
    
    /**
     * �����޸ı�־��
     * @param 	value
     * 			
     */
    public void setDirty(boolean value) {
        dirty = value;
        firePropertyChange(PROP_DIRTY);
        if (value && !lockUndoBuffer) {
            saveStateToUndoBuffer();
            firePropertyChange(PROPERTY_CANUNDO);
            firePropertyChange(PROPERTY_CANREDO);
        }
    }
    
    /**
     * �жϵ�ǰ�Ƿ�����UNDO������
     * @return	�Ƿ�����UNDO����
     */
    public boolean canUndo() {
        return undoCurrent > 0;
    }
    
    /**
     * �жϵ�ǰ�Ƿ�����REDO������
     * @return	�Ƿ�����REDO����
     */
    public boolean canRedo() {
        return undoCurrent < undoLast;
    }
    
    /**
     * �ı��޸ĺ������޸ı�־��
     * @param	e
     * 			�޸��¼�
     */
    public void modifyText(final ModifyEvent e) {
        setDirty(true);
    }
    
}
