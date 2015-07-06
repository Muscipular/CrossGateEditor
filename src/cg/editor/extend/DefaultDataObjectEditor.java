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
 * 编辑界面抽象类
 * @author 	hyfu
 */
public abstract class DefaultDataObjectEditor extends EditorPart implements ModifyListener {
	
    public static final int PROPERTY_CANUNDO = 1;
    
    public static final int PROPERTY_CANREDO = 2;

    public static final String D_ID = "fengEditor.DefaultDataObjectEditor"; //$NON-NLS-1$
    
    // 编辑目标对象
    protected EditorObject saveTarget;
    // 当前编辑的对象，这是目标对象的一个克隆，只有在保存时才把输入数据保存到saveTarget中
    public EditorObject editObject;
    // 当前编辑对象是否被修改标志
    private boolean dirty = false;
    // UNDO/REDO支持
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
			MessageDialog.openError(getSite().getShell(), "错误", e.toString());
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
     * 获取当前编辑对象。
     */
    public EditorObject getEditObject() {
        return editObject;
    }
    
    /**
     * 获取编辑的对象的原始对象。
     */
    public EditorObject getSaveTarget() {
        return saveTarget;
    }
    
    /**
     * 保存当前编辑数据。
     */
    protected abstract void saveData() throws Exception;

    /**
     * 保存当前编辑状态成一个对象。派生类应覆盖此方法。
     */
    protected Object saveState() {
        return editObject.save();
    }
    
    /**
     * 根据以前保存的编辑状态恢复当前编辑状态。派生类应覆盖此方法。
     */
    protected void loadState(Object stateObj) {
    	
    }
    
    /**
     * 保存当前编辑状态到UNDO缓冲区
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
     * 从UNDO缓冲区中恢复数据
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
     * 执行一次UNDO操作。
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
     * 执行一次REDO操作。
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
     * 设置修改标志。
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
     * 判断当前是否允许UNDO操作。
     * @return	是否允许UNDO操作
     */
    public boolean canUndo() {
        return undoCurrent > 0;
    }
    
    /**
     * 判断当前是否允许REDO操作。
     * @return	是否允许REDO操作
     */
    public boolean canRedo() {
        return undoCurrent < undoLast;
    }
    
    /**
     * 文本修改后设置修改标志。
     * @param	e
     * 			修改事件
     */
    public void modifyText(final ModifyEvent e) {
        setDirty(true);
    }
    
}
