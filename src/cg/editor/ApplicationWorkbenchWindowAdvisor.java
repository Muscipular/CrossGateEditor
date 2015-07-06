package cg.editor;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.ide.IDE;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(
			IActionBarConfigurer configurer) {
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() {
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(1200, 800));
		configurer.setShowCoolBar(false);
		configurer.setShowStatusLine(false);
		configurer.setTitle("CrossGateEditor");
        configurer.addEditorAreaTransfer(FileTransfer.getInstance());
        configurer.configureEditorAreaDropListener(new FileDropTargetAdapter(configurer.getWindow()));
	}

    private static class FileDropTargetAdapter extends DropTargetAdapter {
    	
        IWorkbenchWindow window;
        
        public FileDropTargetAdapter(IWorkbenchWindow window) {
            this.window = window;
        }

        @Override
        public void dragOver(DropTargetEvent event) {
            event.feedback = DND.FEEDBACK_NONE | DND.FEEDBACK_SCROLL;
            event.detail = DND.DROP_NONE;
            FileTransfer transfer = FileTransfer.getInstance();
            Object data = transfer.nativeToJava(event.currentDataType);
            if (data != null) {
                event.detail = DND.DROP_COPY;
            }
        }

        @Override
        public void drop(DropTargetEvent event) {
            if (event.data == null) {
                return;
            }
            FileTransfer transfer = FileTransfer.getInstance();
            Object data = transfer.nativeToJava(event.currentDataType);
            if (data == null) {
                return;
            }
            String[] files = (String[])event.data;
            for (int i = 0; i < files.length; i++) {
                try {
                    IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(files[i]));
                    IDE.openEditorOnFileStore(window.getActivePage(), fileStore);
                } catch (Exception e) {
                }
            }
        }
    }
    
}
