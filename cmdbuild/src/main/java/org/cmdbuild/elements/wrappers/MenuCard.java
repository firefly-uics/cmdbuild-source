package org.cmdbuild.elements.wrappers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

public class MenuCard extends CardForwarder {

	protected static final long serialVersionUID = 2L;

	public static final String DEFAULT_GROUP = null;

	public static final String GROUP_NAME_ATTR = "IdGroup";
	public static final String PARENT_ID_ATTR = "IdParent";
	public static final String TYPE_ATTR = "Type";
	public static final String ELEMENT_CLASS_ID_ATTR = "IdElementClass";
	public static final String ELEMENT_OBJECT_ID_ATTR = "IdElementObj";
	public static final String INDEX_ATTR = "Number";

	public static final String MENU_CLASS_NAME = "Menu";
	private static final ITable menuClass = UserContext.systemContext().tables().get(MENU_CLASS_NAME);

	public enum MenuCodeType {
		FOLDER("folder"),
		SYSTEM_FOLDER("system_folder"),
		CLASS("class"),
		PROCESS("processclass"),
		REPORT_PDF("reportpdf"),
		REPORT_CSV("reportcsv"),
		REPORT_ODT("reportodt"),
		REPORT_XML("reportxml"),
		VIEW("view");

		private String ctype;

		MenuCodeType(String type) {
			this.ctype = type;
		}

		public String getCodeType(){
			return this.ctype;
		}
	}

	public enum AllowedReportExtension {
		PDF(ReportFactory.ReportExtension.PDF.toString().toLowerCase()),
		CSV(ReportFactory.ReportExtension.CSV.toString().toLowerCase());
		private String extension;

		AllowedReportExtension(String extension) {
			this.extension = extension;
		}

		public String getExtension(){
			return this.extension;
		}
	}

	public enum MenuType {
		FOLDER("folder"),
		CLASS("class"),
		SUPERCLASS("superclass"),
		PROCESS("processclass"),
		PROCESS_SUPERCLASS("superprocessclass"),
		REPORT_NORMAL("normal"),
		REPORT_HISTORICAL("historical"),
		REPORT_SNAPSHOT("snapshot"),
		REPORT_CUSTOM("custom"),
		REPORT_OPENOFFICE("openoffice"),
		VIEW("view");

		private String type;

		MenuType(String type) {
			this.type = type;
		}

		public String getType(){
			return this.type;
		}
	}

	public static MenuCodeType getCodeValueOf (String type){
		MenuCodeType[] mtypes = MenuCodeType.values();
		try{
			for(MenuCodeType mt: mtypes){
				if(mt.getCodeType().equals(type)){
					return MenuCodeType.valueOf(mt.toString());
				}
			}
			return MenuCodeType.CLASS;
		} catch(IllegalArgumentException e){
			return MenuCodeType.CLASS;
		}
	}

	public static MenuType getTypeValueOf (String type){
		MenuType[] mtypes = MenuType.values();
		try{
			for(MenuType mt: mtypes){
				if(mt.getType().equals(type)){
					return MenuType.valueOf(mt.toString());
				}
			}
			return MenuType.CLASS;
		} catch(IllegalArgumentException e){
			return MenuType.CLASS;
		}
	}

	public MenuCard() throws NotFoundException {
		super(menuClass.cards().create());
	}

	private MenuCard(ICard card) throws NotFoundException {
		super(card);
	}

	public String getType() {
		if (getAttributeValue(TYPE_ATTR)!=null &&!getAttributeValue(TYPE_ATTR).isNull())
		    return getAttributeValue(TYPE_ATTR).getString();
		else
		    return "";
	}

	public MenuType getTypeEnum() {
		return getTypeValueOf(getType());
	}

	public void setType(String type) {
		getAttributeValue(TYPE_ATTR).setValue(type);
	}

	public int getParentId(){
		if (!getAttributeValue(PARENT_ID_ATTR).isNull())
			return getAttributeValue(PARENT_ID_ATTR).getInt();
		else
			return 0;
	}

	public void setParentId(Integer id){
		getAttributeValue(PARENT_ID_ATTR).setValue(id);
	}

	public String getGroupName(){
		return getAttributeValue(GROUP_NAME_ATTR).getString();
	}

	public void setGroupName(String name){
		getAttributeValue(GROUP_NAME_ATTR).setValue(name);
	}

	public int getElementClassId(){
		if (!getAttributeValue(ELEMENT_CLASS_ID_ATTR).isNull())
			return getAttributeValue(ELEMENT_CLASS_ID_ATTR).getInt();
		else
			return 0;
	}

	public void setElementClassId(Integer id){
		getAttributeValue(ELEMENT_CLASS_ID_ATTR).setValue(id);
	}

	public int getElementObjId(){
		if (!getAttributeValue(ELEMENT_OBJECT_ID_ATTR).isNull())
			return getAttributeValue(ELEMENT_OBJECT_ID_ATTR).getInt();
		else
			return 0;
	}

	public void setElementObjId(Integer id){
		getAttributeValue(ELEMENT_OBJECT_ID_ATTR).setValue(id);
	}

	public int getNumber(){
		if (!getAttributeValue(INDEX_ATTR).isNull())
			return getAttributeValue(INDEX_ATTR).getInt();
		else
			return 0;
	}

	public void setNumber(Integer number){
		getAttributeValue(INDEX_ATTR).setValue(number);
	}

	public boolean isReport() {
		return getCode() != null && (
				getCode().equals(MenuCodeType.REPORT_CSV.getCodeType()) ||
				getCode().equals(MenuCodeType.REPORT_PDF.getCodeType()) ||
				getCode().equals(MenuCodeType.REPORT_ODT.getCodeType()) ||
				getCode().equals(MenuCodeType.REPORT_XML.getCodeType()) );
	}

	public static void saveTree(CTree<MenuCard> tree) throws ORMException {
		deleteTree(tree.getRootElement().getData().getGroupName());
		saveAllItems(tree.getRootElement().getChildren(), 0);
	}

	public static void deleteTree(final String groupName) throws ORMException {
		ICard template = menuClass.cards().create();
		template.setStatus(ElementStatus.INACTIVE);
		menuClass.cards().list().filter(GROUP_NAME_ATTR, AttributeFilterType.EQUALS, groupName).update(template);
	}

	public static void saveAllItems(List<CNode<MenuCard>> childrenList, int parentId) {
		int pnum = 0;
		for(CNode<MenuCard> child : childrenList){
			MenuCard menu = child.getData();
			int childId = 0;
			if (menu != null){
				menu.setParentId(parentId);
				menu.setNumber(pnum);
				menu.setStatus(ElementStatus.ACTIVE);
				menu.save();
				childId = menu.getId();
			}
			saveAllItems(child.getChildren(), childId);
			pnum++;
		}
	}

	public static CTree<MenuCard> loadTreeForGroup(final String groupName) throws ORMException {
		try {
			Iterable<ICard> list = getGroupMenuItems(groupName);
			return buildTree(list);
		} catch (NotFoundException e) {
			Log.PERSISTENCE.fatal("Table "+MENU_CLASS_NAME+" does not exist !!!", e);
			return null;
		}
	}

	public static CTree<MenuCard> loadAvailableItemsTreeForGroup(
			String groupName,
			ITableFactory tf) throws ORMException {
		try {
			Iterable<ICard> list = new AvailableMenuItemsView().cards().list().filter(GROUP_NAME_ATTR, AttributeFilterType.EQUALS, groupName).ignoreStatus();
			return buildAvailableItemsTree(list, tf);
		} catch (NotFoundException e) {
			Log.PERSISTENCE.fatal("View "+AvailableMenuItemsView.AvailableMenuView+" does not exist !!!", e);
			return null;
		}
	}

	private static CTree<MenuCard> buildTree(Iterable<ICard> list) throws NotFoundException {
		// prepare hash
		TreeMap<Integer, CNode<MenuCard>> tempHash = new TreeMap<Integer, CNode<MenuCard>>();
		for(ICard card: list) {
			CNode<MenuCard> node = new CNode<MenuCard>();
			MenuCard menu = new MenuCard(card);
			node.setData(menu);
			tempHash.put(card.getId(), node);
		}

		// build tree
		MenuCard root = new MenuCard();
		CNode<MenuCard> rootNode = new CNode<MenuCard>();
		rootNode.setData(root);
		CTree<MenuCard> tree = new CTree<MenuCard>();
		tree.setRootElement(rootNode);

		for(CNode<MenuCard> childNode: tempHash.values()) {
			CNode<MenuCard> parentNode = null;
			int parentId = childNode.getData().getParentId();
			if(tempHash.containsKey(parentId))
				parentNode = tempHash.get(parentId);
			if(parentNode == null)
				rootNode.addChild(childNode);
			else
				parentNode.addChild(childNode);
		}
		return tree;
	}

	public static Iterable<MenuCard> loadListForGroup(final String groupName) {
		try {
			Iterable<ICard> list = getGroupMenuItems(groupName);
			return buildMenuCardList(list);
		} catch (NotFoundException e) {
			Log.PERSISTENCE.fatal("Table " + MENU_CLASS_NAME
					+ " does not exist !!!", e);
			return null;
		}
	}

	private static Iterable<ICard> getGroupMenuItems(final String groupName) {
		Iterable<ICard> list = UserContext.systemContext().tables().get(MENU_CLASS_NAME).cards().list()
				.filter(GROUP_NAME_ATTR,AttributeFilterType.EQUALS, groupName)
				.order(PARENT_ID_ATTR, OrderFilterType.ASC).order(INDEX_ATTR, OrderFilterType.ASC);
		return list;
	}

	private static Iterable<MenuCard> buildMenuCardList(Iterable<ICard> list) throws NotFoundException {
		List<MenuCard> menuCardList = new ArrayList<MenuCard>();
		for(ICard card: list) {
			menuCardList.add(new MenuCard(card));
		}
		return menuCardList;
	}

	private static CTree<MenuCard> buildAvailableItemsTree(
			Iterable<ICard> list,
			ITableFactory tf
		) throws NotFoundException {
		// prepare hash
		HashMap<String, CNode<MenuCard>> classMap = new HashMap<String, CNode<MenuCard>>();
		HashMap<String, CNode<MenuCard>> processMap = new HashMap<String, CNode<MenuCard>>();
		HashMap<String, CNode<MenuCard>> viewMap = new HashMap<String, CNode<MenuCard>>();
		HashMap<String, CNode<MenuCard>> reportMap = new HashMap<String, CNode<MenuCard>>();

		for (ICard item: list) {
			CNode<MenuCard> node = new CNode<MenuCard>();
			MenuCard menu = new MenuCard(item);
			node.setData(menu);
			switch (getCodeValueOf(menu.getCode())) {
			case CLASS:
				if (tf.fullTree().branch(ProcessType.BaseTable).contains(menu.getElementClassId())) {
					menu.setCode(MenuCodeType.PROCESS.getCodeType());
					if(getTypeValueOf(menu.getCode()).equals(MenuType.SUPERCLASS))
						menu.setType(MenuType.PROCESS_SUPERCLASS.getType());
					else
						menu.setType(MenuType.PROCESS.getType());
					processMap.put(item.getDescription(), node);
				} else {
					classMap.put(item.getDescription(), node);
				}
				break;
			case PROCESS:
				processMap.put(item.getDescription(), node);
				break;
			case REPORT_CSV:
			case REPORT_PDF:
			case REPORT_ODT:
			case REPORT_XML:
				reportMap.put(item.getCode()+item.getDescription(), node);
				break;
			case VIEW:
				viewMap.put(item.getDescription(), node);
				break;
			}
		}

		// build tree
		MenuCard root = new MenuCard();
		CNode<MenuCard> rootNode = new CNode<MenuCard>();
		rootNode.setData(root);

		MenuCard classes = new MenuCard();
		classes.setDescription(MenuType.CLASS.getType());
		classes.setCode(MenuCodeType.SYSTEM_FOLDER.getCodeType());
		classes.setType(MenuType.FOLDER.getType());
		CNode<MenuCard> classesNode = new CNode<MenuCard>();
		classesNode.setData(classes);

		MenuCard processes = new MenuCard();
		processes.setDescription(MenuType.PROCESS.getType());
		processes.setCode(MenuCodeType.SYSTEM_FOLDER.getCodeType());
		processes.setType(MenuType.FOLDER.getType());
		CNode<MenuCard> processesNode = new CNode<MenuCard>();
		processesNode.setData(processes);

		MenuCard reports = new MenuCard();
		reports.setCode(MenuCodeType.SYSTEM_FOLDER.getCodeType());
		reports.setType(MenuType.FOLDER.getType());
		reports.setDescription("report");
		CNode<MenuCard> reportsNode = new CNode<MenuCard>();
		reportsNode.setData(reports);

		MenuCard views = new MenuCard();
		views.setCode(MenuCodeType.SYSTEM_FOLDER.getCodeType());
		views.setType(MenuType.FOLDER.getType());
		views.setDescription(MenuType.VIEW.getType());
		CNode<MenuCard> viewsNode = new CNode<MenuCard>();
		viewsNode.setData(views);

		rootNode.addChild(classesNode);
		rootNode.addChild(processesNode);
		rootNode.addChild(reportsNode);
		rootNode.addChild(viewsNode);

		for(CNode<MenuCard> childNode: classMap.values()){
			classesNode.addChild(childNode);
		}
		for(CNode<MenuCard> childNode: processMap.values()){
			processesNode.addChild(childNode);
		}
		for(CNode<MenuCard> childNode: reportMap.values()){
			reportsNode.addChild(childNode);
		}
		for(CNode<MenuCard> childNode: viewMap.values()){
			viewsNode.addChild(childNode);
		}
		CTree<MenuCard> tree = new CTree<MenuCard>();
		tree.setRootElement(rootNode);
		return tree;
	}


}
