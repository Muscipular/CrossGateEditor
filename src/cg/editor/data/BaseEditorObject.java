package cg.editor.data;

import org.jdom.Element;

public abstract class BaseEditorObject implements EditorObject {
	
	protected int id;
	
	protected String name, cate;
	
	protected TypeInfo typeInfo;
	
	public BaseEditorObject() {
		cate = "";
	}

	protected void save(Element ret) {
        ret.addAttribute("id", getId() + "");
        ret.addAttribute("name", getName());
        ret.addAttribute("category", getCategory());
	}

	@Override
	public String getCategory() {
		return cate;
	}

	@Override
	public void setCategory(String cate) {
		this.cate = cate;
	}

	@Override
	public void load(Element element) {
		setId(Integer.parseInt(element.getAttributeValue("id")));
		setName(element.getAttributeValue("name"));
		String category = element.getAttributeValue("category");
		setCategory(category == null ? "" : category);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public TypeInfo getTypeInfo() {
		return typeInfo;
	}

	@Override
	public <T extends EditorObject> void update(T temp) {
		id = temp.getId();
		name = temp.getName();
		cate = temp.getCategory();
	}

	@Override
	public void setTypeInfo(TypeInfo typeInfo) {
		this.typeInfo = typeInfo;
	}

}
