package cg.editor.extend;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import cg.editor.effect.EffectEditor;

public class DefaultDataObjectEditorActionBarContributor extends EditorActionBarContributor implements IPartListener, IPropertyListener, IPropertyChangeListener {
	
	private EffectEditor activeEditor;
	
	private IAction undoAction, redoAction;
	
	private IWorkbenchPage workbench;
	
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		super.setActiveEditor(targetEditor);
		workbench = targetEditor.getSite().getWorkbenchWindow().getActivePage();
		workbench.addPartListener(this);
		targetEditor.addPropertyListener(this);
	}

	@Override
	public void init(IActionBars bars) {
		super.init(bars);
		IMenuManager menuMgr = bars.getMenuManager();
		IMenuManager editMenu = menuMgr.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			IContributionItem[] items = editMenu.getItems();
			undoAction = ((ActionContributionItem)items[0]).getAction();
			redoAction = ((ActionContributionItem)items[1]).getAction();
			undoAction.addPropertyChangeListener(this);
			redoAction.addPropertyChangeListener(this);
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof EffectEditor) {
			activeEditor = (EffectEditor)part;
			undoAction.setEnabled(activeEditor.canUndo());
			redoAction.setEnabled(activeEditor.canRedo());
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {}

	@Override
	public void partClosed(IWorkbenchPart part) {}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		if (part instanceof EffectEditor) {
			activeEditor = null;
		}
		undoAction.setEnabled(false);
		redoAction.setEnabled(false);
	}

	@Override
	public void partOpened(IWorkbenchPart part) {}

	@Override
	public void propertyChanged(Object source, int propId) {
		if (source == activeEditor) {
			if (propId == EffectEditor.PROPERTY_CANUNDO) {
				undoAction.setEnabled(activeEditor.canUndo());
			} else if (propId == EffectEditor.PROPERTY_CANREDO) {
				redoAction.setEnabled(activeEditor.canRedo());
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (activeEditor == null) {
			return;
		}
		if (event.getSource() == undoAction) {
			if (event.getProperty().equals("chosen")) {
				activeEditor.undo();
			}
		} else if (event.getSource() == redoAction) {
			if (event.getProperty().equals("chosen")) {
				activeEditor.redo();
			}
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		undoAction.removePropertyChangeListener(this);
		redoAction.removePropertyChangeListener(this);
		if (workbench != null) {
            workbench.removePartListener(this);
        }
	}
	
}
