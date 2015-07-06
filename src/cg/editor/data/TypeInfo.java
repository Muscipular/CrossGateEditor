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
 * 类型信息
 * @author 	hyfu
 */
public class TypeInfo {
	
	private static final File PATH;
	
	/**
	 * 编辑名称
	 */
	private String name;
	/**
	 * 编辑器类
	 */
	private Class<?> editor;
	/**
	 * 路径
	 */
	private String path;
	
	private String className;
	/**
	 * 分类集合
	 */
	private Hashtable<String, Category> categorys;
	/**
	 * 编辑单元集合
	 */
	private Hashtable<String, EditorObject> units;
	
	static {
		PATH = new File(CrossGateEditor.getClientFilePath() + "/editor");
	}
	
	/**
	 * 构造
	 */
	public TypeInfo() {
		categorys = new Hashtable<String, Category>();
		
		Category category = new Category(null);
		category.name = Category.NEW; // 新建
		categorys.put("new", category);

		category = new Category(null);
		category.name = Category.NOT; // 未分类
		categorys.put(Category.NOT, category);
		
		units = new Hashtable<String, EditorObject>();
	}
	
	/**
	 * 判断指定类的类名称是否为该类的编辑名称
	 * @param 	cls
	 * 			指定类
	 * @return	是否为该类的编辑名称
	 */
	public <T extends EditorObject> boolean isMine(Class<T> cls) {
		return name.equals(cls.getSimpleName().toLowerCase());
	}
	
	/**
	 * 获得编辑器id
	 * @return	编辑器id
	 */
	public String getEditorId() {
		return path + "." + StringUtils.firstUpper(name) + "Editor";
	}
	
	/**
	 * 获得编辑器id
	 * @return	编辑器id
	 */
	public Class<?> getEditor() {
		return editor;
	}
	
	/**
	 * 创建向导
	 * @throws 	Exception 
	 */
	@SuppressWarnings("rawtypes")
	public EditorObject createWizard() throws Exception {
		Wizard wiz = (Wizard) IOUtils.getObject(path + "." + StringUtils.firstUpper(name) + "Wizard");
		return wiz.create(this);
	}
	
	/**
	 * 获得配置文件对象
	 * @param 	base
	 * 			基础文件路径
	 * @return	配置文件对象
	 */
	public File getXML(File base) {
		return new File(base, name + ".xml");
	}
	
	/**
	 * 获得名称
	 * @return	名称
	 */
	public String getName() {
		return name;
	}
	
	/**
     * 从XML标签中载入对象属性。
     * @param 	elem
     * 			XML标签
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
	 * 解析单元类型
	 * @param 	doc
	 *          jdom模型
	 * @param 	name
	 *          文件名
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
	 * 解析编辑单元
	 * @param 	doc
	 *          jdom模型
	 * @param 	name
	 *          文件名
	 * @throws 	Exception
	 */
	protected void loadUnit(Document doc, String name) throws Exception {
		@SuppressWarnings("unchecked")
		List<Element> domList = doc.getRootElement().getChildren(className);
		for (Element element : domList) {
			EditorObject unit = (EditorObject) IOUtils.getObject("cg.editor." + name + "." + className);
			unit.setTypeInfo(this);
			unit.load(element); // 读取编辑单元信息
			save(unit);
		}
	}
	
	/**
	 * 获得分类数组
	 * @return	分类数组
	 */
	public Hashtable<String, Category> getCategorys() {
		return categorys;
	}
	
	/**
	 * 获得编辑单元集合
	 * @return	编辑单元集合
	 */
	public Hashtable<String, EditorObject> getUnits() {
		return units;
	}
	
	/**
	 * 添加新的编辑单元
	 * @param 	unit
	 * 			编辑单元
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
	 * 新建分类
	 * @param 	name
	 * 			分类名称
	 * @return	分类对象
	 * @throws 	Exception
	 */
	public Category newCategory(String name) throws Exception {
		Collection<Category> list = categorys.values();
		for (Category cate : list) {
			if (cate.name.equals(name)) {
				throw new Exception("分类名称不能重复。");
			}
		}
		Category cate = new Category(IOUtils.getClass(path + "." + className));
		cate.name = name;
		categorys.put(name, cate);
		return cate;
	}
	
	/**
	 * 保存到指定jdom模型中
	 * @param 	doc
	 * 			jdom模型
	 */
	public void save(Document doc) {
		Collection<Category> cates = getCategorys().values();
		for(Category cate : cates) {
			if (!cate.name.equals(Category.NEW)) {//新建不会被保存
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
	 * 保存一个编辑单元
	 * @param 	unit
	 * 			编辑单元
	 */
	protected void save(EditorObject unit) {
		units.put(unit.getName(), unit);
		
		Category category = categorys.get(unit.getCategory());
		category.objects.add(unit);
	}
	
}
