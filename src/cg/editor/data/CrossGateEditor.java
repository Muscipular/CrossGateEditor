package cg.editor.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;

import cg.base.LoadCall;
import cg.base.animation.AnimationReader;
import cg.base.image.ImageManager;
import cg.base.io.PacketFactory;
import cg.base.util.IOUtils;
import cg.base.util.Updatable;
import cg.base.util.Updater;
import cg.data.CrossGateData;
import cg.data.gmsvReader.CFileMapReader;
import cg.data.gmsvReader.CGarbledReader;
import cg.data.gmsvReader.CGatherAreaReader;
import cg.data.gmsvReader.CMessageReader;
import cg.data.gmsvReader.CNPCReader;
import cg.data.gmsvReader.CWarpReader;
import cg.data.map.CWarpManager;
import cg.data.map.MapReader;
import cg.data.map.WarpManager;
import cg.data.resource.BaseProjectData;
import cg.data.resource.ProjectData;
import cg.data.resource.Reloadable;
import cg.data.title.TitleManager;
import cg.data.util.FileUtils;
import cg.editor.data.npc.NpcManager;
import cg.editor.reader.EditorAnimationReader;
import cg.editor.reader.EditorImageResourceManager;

public class CrossGateEditor extends CrossGateData {
	
	private static Document configuration;
	
	private static Vector<String> typeKeys;
	
	private static TypeMap typeInfos;
	
	private static Map<String, Object> pool;
	
	private static NpcManager npcManager;
	
	public static void launcher() {
		Runnable runnable = new EditorLoader(null);
		runnable.run();
	}
	
	private static class EditorLoader extends DataLoader {

		public EditorLoader(LoadCall call) {
			super(call);
		}

		@Override
		protected void createSimpleObject() {
			try {
				configuration = FileUtils.loadDOM(new File(new File("."), "config.xml"));
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			super.createSimpleObject();
		}

		@Override
		public void run() {
			super.run();

			// ���ͼ���
			typeKeys = new Vector<String>();
			typeInfos = TypeMap.getInstance();
			pool = new HashMap<String, Object>();
			
			npcManager = new NpcManager();
			
			try {
				CrossGateEditor.load();
			} catch (Exception e) {
				getLog().error(CrossGateEditor.class.getName(), e);
			}
		}

		@Override
		protected void registerReload() {
			reloadManager.register(ProjectData.class.getName(), projectData);
			reloadManager.register(ImageManager.class.getName(), (Reloadable) imageManager);
		}

		@Override
		protected void loadData() {
			projectData.addObjectReader(new CWarpReader());
			projectData.addObjectReader(new CGatherAreaReader());
			projectData.addObjectReader(new CNPCReader());
			projectData.addObjectReader(new CMessageReader());
			projectData.addObjectReader(new CGarbledReader(getLog()));
		}

		@Override
		protected byte getModel() {
			return MODEL_EDITOR;
		}

		@Override
		protected ProjectData createProjectData() {
			return new BaseProjectData(getLog(), getClientFilePath());
		}

		@Override
		protected WarpManager createWarpManager() {
			return new CWarpManager(getProjectData());
		}

		@Override
		protected MapReader createMapReader() {
			return new CFileMapReader(getWarpManager(), "newMap", getImageManager().getImageReader(), getProjectData(), getLog());
		}

		@Override
		protected URI loadHostPath() throws Exception {
			return new URI(IOUtils.encode(getConfiguration().getRootElement().getChild("resource").getText()));
		}

		@Deprecated
		@Override
		protected PacketFactory createPacketFactory() {
			return null;
		}

		@Deprecated
		@Override
		protected TitleManager createTitleManager() {
			return null;
		}

		@Override
		protected ImageManager createImageManager() {
			return new EditorImageResourceManager(getLog(), getClientFilePath());
		}

		@Override
		protected void logStart() {
			log.info("CrossGateEditor start finish.");
		}

		@Override
		protected Updater createUpdater() {
			return new Updater() {
				
				@Override
				public void update() {}
				
				@Override
				public void remove(Updatable updatable) {}
				
				@Override
				public void add(Updatable updatable) {}
				
			};
		}

		@Override
		protected AnimationReader createAnimationReader() {
			return new EditorAnimationReader(getLog(), getClientFilePath(), getImageManager(), getTimer());
		}
		
	}
	
	public static Document getConfiguration() {
		return configuration;
	}
	
	/**
	 * ���������Ƽ���
	 * @return	������Ƽ���
	 */
	public static Vector<String> getTypeKeys() {
		return typeKeys;
	}

	/**
	 * ���������Ϣ����
	 * @return 	������Ϣ����
	 */
	public static TypeMap getEditorPool() {
		return typeInfos;
	}

	/**
	 * ����б�Ԫ�ؼ���
	 * @param 	key
	 *          ��
	 * @return 	�б�Ԫ�ؼ���
	 */
	public static Collection<Category> getCategoryList(String key) {
		return getEditorPool().get(key).getCategorys().values();
	}

	/**
	 * ����б�Ԫ�ؼ���
	 * @param 	key
	 *          ��
	 * @return 	�б�Ԫ�ؼ���
	 */
	public static Collection<Category> getCategoryList(Class<?> key) {
		return getEditorPool().get(key).getCategorys().values();
	}

	/**
	 * ��ö����
	 * @return 	�����
	 */
	public static Map<String, Object> getObjectPool() {
		return pool;
	}

	/**
	 * ��һ����ݶ����һ�������ƶ�������һ�����ࡣ
	 * @param 	obj
	 *          ��ݶ���
	 * @param 	newCate
	 *          �������
	 */
	public static void changeObjectCategory(EditorObject obj, Category newCate) {
		TypeInfo info = getEditorPool().get(obj.getClass());
		Category oldCate = (Category) info.getCategorys().get(obj.getCategory());
		oldCate.objects.remove(obj);
		newCate.objects.add(obj);
		obj.setCategory(newCate.name);
	}
	
	/**
	 * ��û��ļ�·��
	 * @return	���ļ�·��
	 */
	public static File getBaseFile() {
		return (File) pool.get("baseDir");
	}
    
    /**
     * ������Ŀ����Ŀ֧�ֵ�������ݶ��󶼻ᱻ���롣
     * @throws 	Exception
     */
    private static void load() throws Exception {
    	Document doc = loadXML("game.conf", "conf");
    	if (doc.getRootElement().getChildren("dataInfos").size() > 0) {
    		Element firstElement = (Element) doc.getRootElement().getChildren("dataInfos").get(0);
			@SuppressWarnings("unchecked")
			List<Element> domList = firstElement.getChildren("dataInfo");
			for (Element element : domList) {
				String typeInfoClass = element.getAttributeValue("typeInfoClass");
				TypeInfo typeInfo = typeInfoClass == null ? new TypeInfo() : (TypeInfo) Class.forName(typeInfoClass).newInstance();
				typeInfo.load(element);
				typeInfos.save(typeInfo);
			}
    	} else {
    		throw new Exception("Load game.conf error.");
    	}
    }

	/**
	 * ��ȡxml�ļ�
	 * @param 	name
	 *          �ļ���
	 * @param 	root
	 *          ��ڵ���ƣ������ļ�������ʱ����ʹ��
	 * @return	jdomģ��
	 */
	public static Document loadXML(String name, String root) {
		File path = new File(getClientFilePath());
		try {
			return FileUtils.loadDOM(new File(path, name + ".xml"));// ���Զ�ȡ����xml�ļ�
		} catch (Exception e) {
			getLog().error(CrossGateEditor.class.getName(), e);
		}

		// �����쳣������½��ļ�����д�����
		File file = new File(path, name + ".xml");
		try {
			FileOutputStream os = new FileOutputStream(file, true);
			os.write(("<?xml version=\"1.0\" encoding=\"GBK\"?>\r\n<" + root + "s>\r\n</" + root + "s>").getBytes());
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			getLog().error(CrossGateEditor.class.getName(), e);
		} catch (IOException e) {
			getLog().error(CrossGateEditor.class.getName(), e);
		}
		getLog().info("Load file : " + name + ".xml again.");
		return loadXML(name, root);
	}
	
	public static NpcManager getNpcManager() {
		return npcManager;
	}

}
