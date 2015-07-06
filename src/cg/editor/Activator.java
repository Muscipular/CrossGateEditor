package cg.editor;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import cg.editor.data.CrossGateEditor;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CrossGateEditor"; //$NON-NLS-1$

    // The image file need to be shared.
    private static final String[] imageNames = { 
        "dataobj", "npcicon", "itemtype", "item", "flag", "rootnode", 
        "dropgroup", "dropitem", "newattribute", "empty", "delete"
    };

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		getLog().addLogListener(new LogListener());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    /** 
     * Initializes an image registry with images which are frequently used by the 
     * plugin.
     */
	@Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        try {
            for (int i = 0; i < imageNames.length; i++) {
                ImageDescriptor desc = getImageDescriptor("icons/" + imageNames[i] + ".gif");
                getImageRegistry().put(imageNames[i], desc);
            }
        } catch (Exception e) {
        	CrossGateEditor.getLog().error(getClass().getName(), e);
        }
    }
	
	private static class LogListener implements ILogListener {

		@Override
		public void logging(IStatus paramIStatus, String paramString) {
			CrossGateEditor.getLog().info(paramString);
		}
		
	}
    
}
