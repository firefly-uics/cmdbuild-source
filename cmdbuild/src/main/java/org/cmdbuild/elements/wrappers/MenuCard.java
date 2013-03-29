package org.cmdbuild.elements.wrappers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.filters.OrderFilter.OrderFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.CardForwarder;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;

@OldDao
@Deprecated
public class MenuCard extends CardForwarder {

	protected static final long serialVersionUID = 2L;

	public static final String DEFAULT_GROUP = null;

	public static final String GROUP_NAME_ATTR = "GroupName";
	public static final String PARENT_ID_ATTR = "IdParent";
	public static final String TYPE_ATTR = "Type";
	public static final String ELEMENT_CLASS_ID_ATTR = "IdElementClass";
	public static final String ELEMENT_OBJECT_ID_ATTR = "IdElementObj";
	public static final String INDEX_ATTR = "Number";

	public static final String MENU_CLASS_NAME = "Menu";
	private static final ITable menuClass = UserOperations.from(UserContext.systemContext()).tables()
			.get(MENU_CLASS_NAME);

	public enum MenuCodeType {
		FOLDER("folder"), SYSTEM_FOLDER("system_folder"), CLASS("class"), PROCESS("processclass"), REPORT_PDF(
				"reportpdf"), REPORT_CSV("reportcsv"), REPORT_ODT("reportodt"), REPORT_XML("reportxml"), VIEW("view"), DASHBOARD(
				"dashboard"), ;

		private String ctype;

		MenuCodeType(final String type) {
			this.ctype = type;
		}

		public String getCodeType() {
			return this.ctype;
		}
	}

	public enum AllowedReportExtension {
		PDF(ReportFactory.ReportExtension.PDF.toString().toLowerCase()), CSV(ReportFactory.ReportExtension.CSV
				.toString().toLowerCase());
		private String extension;

		AllowedReportExtension(final String extension) {
			this.extension = extension;
		}

		public String getExtension() {
			return this.extension;
		}
	}

	public enum MenuType {
		FOLDER("folder"), CLASS("class"), SUPERCLASS("superclass"), PROCESS("processclass"), PROCESS_SUPERCLASS(
				"superprocessclass"), REPORT_NORMAL("normal"), REPORT_HISTORICAL("historical"), REPORT_SNAPSHOT(
				"snapshot"), REPORT_CUSTOM("custom"), REPORT_OPENOFFICE("openoffice"), VIEW("view"), ;

		private String type;

		MenuType(final String type) {
			this.type = type;
		}

		public String getType() {
			return this.type;
		}
	}

	public static MenuCodeType getCodeValueOf(final String type) {
		final MenuCodeType[] mtypes = MenuCodeType.values();
		try {
			for (final MenuCodeType mt : mtypes) {
				if (mt.getCodeType().equals(type)) {
					return MenuCodeType.valueOf(mt.toString());
				}
			}
			return MenuCodeType.CLASS;
		} catch (final IllegalArgumentException e) {
			return MenuCodeType.CLASS;
		}
	}

	public static MenuType getTypeValueOf(final String type) {
		final MenuType[] mtypes = MenuType.values();
		try {
			for (final MenuType mt : mtypes) {
				if (mt.getType().equals(type)) {
					return MenuType.valueOf(mt.toString());
				}
			}
			return MenuType.CLASS;
		} catch (final IllegalArgumentException e) {
			return MenuType.CLASS;
		}
	}

	private MenuCard() throws NotFoundException {
		super(menuClass.cards().create());
	}

	private MenuCard(final ICard card) throws NotFoundException {
		super(card);
	}

	public String getType() {
		if (getAttributeValue(TYPE_ATTR) != null && !getAttributeValue(TYPE_ATTR).isNull()) {
			return getAttributeValue(TYPE_ATTR).getString();
		} else {
			return "";
		}
	}

	public MenuType getTypeEnum() {
		return getTypeValueOf(getType());
	}

	public void setType(final String type) {
		getAttributeValue(TYPE_ATTR).setValue(type);
	}

	public int getParentId() {
		if (!getAttributeValue(PARENT_ID_ATTR).isNull()) {
			return getAttributeValue(PARENT_ID_ATTR).getInt();
		} else {
			return 0;
		}
	}

	public void setParentId(final Integer id) {
		getAttributeValue(PARENT_ID_ATTR).setValue(id);
	}

	public String getGroupName() {
		return getAttributeValue(GROUP_NAME_ATTR).getString();
	}

	public void setGroupName(final String name) {
		getAttributeValue(GROUP_NAME_ATTR).setValue(name);
	}

	public int getElementClassId() {
		if (!getAttributeValue(ELEMENT_CLASS_ID_ATTR).isNull()) {
			return getAttributeValue(ELEMENT_CLASS_ID_ATTR).getInt();
		} else {
			return 0;
		}
	}

	public void setElementClassId(final Integer id) {
		getAttributeValue(ELEMENT_CLASS_ID_ATTR).setValue(id);
	}

	public int getElementObjId() {
		if (!getAttributeValue(ELEMENT_OBJECT_ID_ATTR).isNull()) {
			return getAttributeValue(ELEMENT_OBJECT_ID_ATTR).getInt();
		} else {
			return 0;
		}
	}

	public void setElementObjId(final Long id) {
		getAttributeValue(ELEMENT_OBJECT_ID_ATTR).setValue(id);
	}

	public int getNumber() {
		if (!getAttributeValue(INDEX_ATTR).isNull()) {
			return getAttributeValue(INDEX_ATTR).getInt();
		} else {
			return 0;
		}
	}

	public void setNumber(final Integer number) {
		getAttributeValue(INDEX_ATTR).setValue(number);
	}

	public boolean isReport() {
		return getCode() != null
				&& (getCode().equals(MenuCodeType.REPORT_CSV.getCodeType())
						|| getCode().equals(MenuCodeType.REPORT_PDF.getCodeType())
						|| getCode().equals(MenuCodeType.REPORT_ODT.getCodeType()) || getCode().equals(
						MenuCodeType.REPORT_XML.getCodeType()));
	}

	public static void saveTree(final CTree<MenuCard> tree) throws ORMException {
		deleteTree(tree.getRootElement().getData().getGroupName());
		saveAllItems(tree.getRootElement().getChildren(), 0);
	}

	public static void deleteTree(final String groupName) throws ORMException {
		final ICard template = menuClass.cards().create();
		template.setStatus(ElementStatus.INACTIVE);
		menuClass.cards().list().filter(GROUP_NAME_ATTR, AttributeFilterType.EQUALS, groupName).update(template);
	}

	public static void saveAllItems(final List<CNode<MenuCard>> childrenList, final int parentId) {
		int pnum = 0;
		for (final CNode<MenuCard> child : childrenList) {
			final MenuCard menu = child.getData();
			int childId = 0;
			if (menu != null) {
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
			final Iterable<ICard> list = getGroupMenuItems(groupName);
			return buildTree(list);
		} catch (final NotFoundException e) {
			Log.PERSISTENCE.error("Table " + MENU_CLASS_NAME + " does not exist !!!", e);
			return null;
		}
	}

	private static CTree<MenuCard> buildTree(final Iterable<ICard> list) throws NotFoundException {
		// prepare hash
		final TreeMap<Integer, CNode<MenuCard>> tempHash = new TreeMap<Integer, CNode<MenuCard>>();
		for (final ICard card : list) {
			final CNode<MenuCard> node = new CNode<MenuCard>();
			final MenuCard menu = new MenuCard(card);
			node.setData(menu);
			tempHash.put(card.getId(), node);
		}

		// build tree
		final MenuCard root = new MenuCard();
		final CNode<MenuCard> rootNode = new CNode<MenuCard>();
		rootNode.setData(root);
		final CTree<MenuCard> tree = new CTree<MenuCard>();
		tree.setRootElement(rootNode);

		for (final CNode<MenuCard> childNode : tempHash.values()) {
			CNode<MenuCard> parentNode = null;
			final int parentId = childNode.getData().getParentId();
			if (tempHash.containsKey(parentId)) {
				parentNode = tempHash.get(parentId);
			}
			if (parentNode == null) {
				rootNode.addChild(childNode);
			} else {
				parentNode.addChild(childNode);
			}
		}
		return tree;
	}

	public static Iterable<MenuCard> loadListForGroup(final String groupName) {
		try {
			final Iterable<ICard> list = getGroupMenuItems(groupName);
			return buildMenuCardList(list);
		} catch (final NotFoundException e) {
			Log.PERSISTENCE.error("Table " + MENU_CLASS_NAME + " does not exist !!!", e);
			return new LinkedList<MenuCard>();
		}
	}

	@SuppressWarnings("deprecation")
	private static Iterable<ICard> getGroupMenuItems(final String groupName) {
		final Iterable<ICard> list = UserOperations.from(UserContext.systemContext()).tables().get(MENU_CLASS_NAME)
				.cards().list().filter(GROUP_NAME_ATTR, AttributeFilterType.EQUALS, groupName)
				.order(PARENT_ID_ATTR, OrderFilterType.ASC).order(INDEX_ATTR, OrderFilterType.ASC);
		return list;
	}

	private static Iterable<MenuCard> buildMenuCardList(final Iterable<ICard> list) throws NotFoundException {
		final List<MenuCard> menuCardList = new ArrayList<MenuCard>();
		for (final ICard card : list) {
			menuCardList.add(new MenuCard(card));
		}
		return menuCardList;
	}

}
