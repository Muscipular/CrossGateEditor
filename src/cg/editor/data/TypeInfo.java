package cg.editor.data;

import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import cg.base.util.IOUtils;
import cg.base.util.StringUtils;
import cg.data.util.FileUtils;

/**
 * ������Ϣ
 * @author 	hyfu
 */
public class TypeInfo {
	
	private static final File PATH;
	
	/**
	 * �༭����
	 */
	private String name;
	/**
	 * �༭����
	 */
	private Class<?> editor;
	/**
	 * ·��
	 */
	private String path;
	
	private String className;
	/**
	 * ���༯��
	 */
	private Hashtable<String, Category> categorys;
	/**
	 * �༭��Ԫ����
	 */
	private Hashtable<String, EditorObject> units;
	
	static {
		PATH = new File(CrossGateEditor.getClientFilePath() + "/editor");
	}
	
	/**
	 * ����
	 */
	public TypeInfo() {
		categorys = new Hashtable<String, Category>();
		
		Category category = new Category(null);
		category.name = Category.NEW; // �½�
		categorys.put("new", category);

		category = new Category(null);
		category.name = Category.NOT; // δ����
		categorys.put(Category.NOT, category);
		
		units = new Hashtable<String, EditorObject>();
	}
	
	/**
	 * �ж�ָ������������Ƿ�Ϊ����ı༭����
	 * @param 	cls
	 * 			ָ����
	 * @return	�Ƿ�Ϊ����ı༭����
	 */
	public <T extends EditorObject> boolean isMine(Class<T> cls) {
		return name.equals(cls.getSimpleName().toLowerCase());
	}
	
	/**
	 * ��ñ༭��id
	 * @return	�༭��id
	 */
	public String getEditorId() {
		return path + "." + StringUtils.firstUpper(name) + "Editor";
	}
	
	/**
	 * ��ñ༭��id
	 * @return	�༭��id
	 */
	public Class<?> getEditor() {
		return editor;
	}
	
	/**
	 * ������
	 * @throws 	Exception 
	 */
	@SuppressWarnings("rawtypes")
	public EditorObject createWizard() throws Exception {
		Wizard wiz = (Wizard) IOUtils.getObject(path + "." + StringUtils.firstUpper(name) + "Wizard");
		return wiz.create(this);
	}
	
	/**
	 * ��������ļ�����
	 * @param 	base
	 * 			�����ļ�·��
	 * @return	�����ļ�����
	 */
	public File getXML(File base) {
		return new File(base, name + ".xml");
	}
	
	/**
	 * �������
	 * @return	����
	 */
	public String getName() {
		return name;
	}
	
	/**
     * ��XML��ǩ������������ԡ�
     * @param 	elem
     * 			XML��ǩ
	 * @throws 	Exception 
     */
	void load(Element elem) throws Exception {
		name = elem.getAttributeValue("name");
		path = elem.getAttributeValue("path");
		className = elem.getAttributeValue("class");
		editor = IOUtils.getClass(getEditorId());
		
		Document doc = FileUtils.loadDOM(new File(PATH, name + ".xml"));
		Category category = new Category(IOUtils.getClass(path + "." + className));
		category.name = "";
		loadCategory(doc, name);
		loadUnit(doc, name);
	}

	/**
	 * ������Ԫ����
	 * @param 	doc
	 *          jdomģ��
	 * @param 	name
	 *          �ļ���
	 * @throws 	IllegalAccessException
	 * @throws 	InstantiationException
	 * @throws 	ClassNotFoundException
	 */
	private void loadCategory(Document doc, String name) throws ClassNotFoundException, InstantiationException,IllegalAccessException {
		if (doc.getRootElement().getChildren("category").size() > 0) {
			@SuppressWarnings("unchecked")
			List<Element> domList = doc.getRootElement().getChildren("category");
			for (Element element : domList) {
				String cate = element.getAttributeValue("name");
				String parent = element.getAttributeValue("parent");
				
				Category category = new Category(IOUtils.getClass(path + "." + className));
				category.name = cate;
				category.parent = parent;
				
				categorys.put(cate, category);
			}
		}
	}

	/**
	 * �����༭��Ԫ
	 * @param 	doc
	 *          jdomģ��
	 * @param 	name
	 *          �ļ���
	 * @throws 	Exception
	 */
	protected void loadUnit(Document doc, String name) throws Exception {
		@SuppressWarnings("unchecked")
		List<Element> domList = doc.getRootElement().getChildren(className);
		for (Element element : domList) {
			EditorObject unit = (EditorObject) IOUtils.getObject("cg.editor." + name + "." + className);
			unit.setTypeInfo(this);
			unit.load(element); // ��ȡ�༭��Ԫ��Ϣ
			save(unit);
		}
	}
	
	/**
	 * ��÷�������
	 * @return	��������
	 */
	public Hashtable<String, Category> getCategorys() {
		return categorys;
	}
	
	/**
	 * ��ñ༭��Ԫ����
	 * @return	�༭��Ԫ����
	 */
	public Hashtable<String, EditorObject> getUnits() {
		return units;
	}
	
	/**
	 * ����µı༭��Ԫ
	 * @param 	unit
	 * 			�༭��Ԫ
	 */
	public void addUnit(EditorObject unit) {
		Collection<EditorObject> eos = units.values();
    	int max = 1;
    	for (EditorObject eo : eos) {
    		if (max <= eo.getId()) {
    			max = eo.getId() + 1;
    		}
    	}
    	unit.setId(max);
    	
		units.put(unit.getName(), unit);
		categorys.get(unit.getCategory()).objects.add(unit);
	}
	
	/**
	 * �½�����
	 * @param 	name
	 * 			��������
	 * @return	�������
	 * @throws 	Exception
	 */
	public Category newCategory(String name) throws Exception {
		Collection<Category> list = categorys.values();
		for (Category cate : list) {
			if (cate.name.equals(name)) {
				throw new Exception("�������Ʋ����ظ���");
			}
		}
		Category cate = new Category(IOUtils.getClass(path + "." + className));
		cate.name = name;
		categorys.put(name, cate);
		return cate;
	}
	
	/**
	 * ���浽ָ��jdomģ����
	 * @param 	doc
	 * 			jdomģ��
	 */
	public void save(Document doc) {
		Collection<Category> cates = getCategorys().values();
		for(Category cate : cates) {
			if (!cate.name.equals(Category.NEW)) {//�½����ᱻ����
				Element element = cate.save();
				doc.getRootElement().addContent(element);
			}
		}
		
		Collection<EditorObject> units = getUnits().values();
		for(EditorObject unit : units) {
			Element element = unit.save();
			doc.getRootElement().addContent(element);
		}
	}
	
	/**
	 * ����һ���༭��Ԫ
	 * @param 	unit
	 * 			�༭��Ԫ
	 */
	protected void save(EditorObject unit) {
		units.put(unit.getName(), unit);
		
		Category category = categorys.get(unit.getCategory());
		category.objects.add(unit);
	}
	
}
