package cg.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

import cg.editor.swtdesigner.ResourceManager;


/**
 * An action bar advisor is responsible for creating, adding, and disposing of
 * the actions added to a workbench window. Each window will be populated with
 * new actions.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	// Actions - important to allocate these only in makeActions, and then use
	// them
	// in the fill methods. This ensures that the actions aren't recreated
	// when fillActionBars is called with FILL_PROXY.
	/**
	 * 退出
	 */
	private IWorkbenchAction exitAction;
	/**
	 * 撤销
	 */
    private Action redoAction;
	/**
	 * 重复
	 */
    private Action undoAction;
	/**
	 * 关闭且保存全部
	 */
    private IWorkbenchAction closeAllSavedAction;
	/**
	 * 保存全部编辑内容
	 */
    private IWorkbenchAction saveAllAction;
	/**
	 * 保存当前编辑内容
	 */
    private IWorkbenchAction saveAction;
	/**
	 * 关闭所有编辑窗口
	 */
    private IWorkbenchAction closeAllAction;
	/**
	 * 关闭当前编辑窗口
	 */
    private IWorkbenchAction closeAction;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		// Creates the actions and registers them.
		// Registering is needed to ensure that key bindings work.
		// The corresponding commands keybindings are defined in the plugin.xml
		// file.
		// Registering also provides automatic disposal of the actions when
		// the window is closed.

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
        closeAction = ActionFactory.CLOSE.create(window);
        register(closeAction);
        
        closeAllAction = ActionFactory.CLOSE_ALL.create(window);
        register(closeAllAction);
        
        saveAction = ActionFactory.SAVE.create(window);
        register(saveAction);
        
        saveAllAction = ActionFactory.SAVE_ALL.create(window);
        register(saveAllAction);
        
        closeAllSavedAction = ActionFactory.CLOSE_ALL_SAVED.create(window);
        register(closeAllSavedAction);
		
        undoAction = new Action("&Undo") {
        	
        	@Override
            public void run() {
                this.firePropertyChange("chosen", this, this);
            }
        	
        };
        undoAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/undo_edit(1).gif"));
        undoAction.setDisabledImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/undo_edit.gif"));
        undoAction.setAccelerator(SWT.CTRL | 'z');

        redoAction = new Action("&Redo") {

        	@Override
            public void run() {
                this.firePropertyChange("chosen", this, this);
            }
        	
        };
        redoAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/redo_edit(1).gif"));
        redoAction.setDisabledImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.getDefault(), "icons/redo_edit.gif"));
        redoAction.setAccelerator(SWT.CTRL | 'y');
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File", IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(closeAllSavedAction);
		fileMenu.add(saveAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(closeAction);
		fileMenu.add(exitAction);
		
		MenuManager menuManager = new MenuManager("&Edit", IWorkbenchActionConstants.M_EDIT);
        menuBar.add(menuManager);
        menuManager.add(undoAction);
        menuManager.add(redoAction);
	}

	@Override
    protected void fillCoolBar(ICoolBarManager coolBar) {
    	ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
    	coolBar.add(toolBarManager);
    }

}
