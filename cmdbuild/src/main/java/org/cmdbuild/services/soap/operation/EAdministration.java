package org.cmdbuild.services.soap.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.MenuCard.MenuCodeType;
import org.cmdbuild.elements.wrappers.MenuCard.MenuType;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.types.Metadata;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;
import org.cmdbuild.workflow.WorkflowCache;
import org.cmdbuild.workflow.operation.ActivityDO;

public class EAdministration {
	
	private UserContext userCtx;

	public EAdministration(UserContext userCtx) {
		this.userCtx = userCtx;
	}
	
	public MenuSchema getClassMenuSchema()  {
		TableTree tree =  userCtx.tables().fullTree().displayable();
		return serializeDefaultTree(tree.exclude(ProcessType.BaseTable).getRootElement(), false, userCtx);
	}
	
	public MenuSchema getProcessMenuSchema()  {
		TableTree tree = userCtx.processTypes().tree();
		return serializeDefaultTree(tree.getRootElement(), true, userCtx);
	}
	
	public MenuSchema getMenuSchema()  {
		CTree<MenuCard> tree = MenuCard.loadTreeForGroup(userCtx.getDefaultGroup().getId());
		if (tree.getRootElement().getNumberOfChildren() > 0)
			return serializeTree(tree.getRootElement(), userCtx);
		else {
			CTree<MenuCard> defaulttree = MenuCard.loadTreeForGroup(0);
			return serializeTree(defaulttree.getRootElement(), userCtx);
		}
	}

	private MenuSchema serializeDefaultTree(CNode<ITable> rootElement, boolean isProcess, UserContext userCtx) {
		MenuSchema schema = new MenuSchema();

		ITable table = rootElement.getData();
		schema.setId(table.getId());
		schema.setDescription(table.getDescription());
		schema.setClassname(table.getName());
		setMenuTypeFromTypeAndChildren(schema, isProcess, table.isSuperClass());
		schema.setMetadata(serializeMetadata(table, null));
		addAccessPrivileges(schema, userCtx);
		Boolean isDefault = checkIsDefault(table, userCtx);
		schema.setDefaultToDisplay(isDefault);
		List<MenuSchema> children = new LinkedList<MenuSchema>();
		if(rootElement.getNumberOfChildren() > 0) {
			for(CNode<ITable> child : rootElement.getChildren()) {
				children.add(serializeDefaultTree(child,isProcess, userCtx));
			}
		}
		schema.setChildren(children.toArray(new MenuSchema[children.size()]));
		return schema;
	}

	private Boolean checkIsDefault(ITable table, UserContext userCtx) {
		return table.equals(userCtx.getDefaultGroup().getStartingClass());
	}

	private void setMenuTypeFromTypeAndChildren(MenuSchema schema, boolean isProcess, boolean isSuperclass) {
		MenuType type;
		if (isSuperclass) {
			type = isProcess ? MenuType.PROCESS_SUPERCLASS : MenuType.SUPERCLASS;
		} else {
			type = isProcess ? MenuType.PROCESS : MenuType.CLASS;
		}
		schema.setMenuType(type.getType());
	}

	private static void addAccessPrivileges(MenuSchema menuSchema, UserContext userCtx)  {
		ITable menuEntryClass = UserContext.systemContext().tables().get(menuSchema.getClassname());
		menuSchema.setClassname(menuEntryClass.getName());
		PrivilegeType privileges = userCtx.privileges().getPrivilege(menuEntryClass);
		if (!PrivilegeType.NONE.equals(privileges)){
			menuSchema.setPrivilege(privileges.toString());
		}
	}
	
	public AttributeSchema serialize(IAttribute attribute) {
		return serialize(attribute, null, attribute.getIndex());
	}

	public AttributeSchema serialize(IAttribute attribute, ActivityDO activity, int client_index) {
		AttributeSchema schema = new AttributeSchema();
		schema.setIdClass(attribute.getSchema().getId());
		schema.setName(attribute.getDBName());
		schema.setDescription(attribute.getDescription());
		schema.setType(attribute.getType().name());
		schema.setBaseDSP(attribute.isBaseDSP());
		schema.setUnique(attribute.isUnique());
		schema.setNotnull(attribute.isNotNull());
		schema.setInherited(!attribute.isLocal());
		schema.setIndex(client_index);
		schema.setLength(attribute.getLength());
		schema.setPrecision(attribute.getPrecision());
		schema.setScale(attribute.getScale());
		schema.setFieldmode(attribute.getFieldMode().getMode());
		schema.setDefaultValue(attribute.getDefaultValue());
		schema.setClassorder(attribute.getClassOrder());
		
		schema.setMetadata(serializeMetadata(attribute, activity));
		
		AttributeType atype = attribute.getType();
		switch (atype) {
		case LOOKUP:
			if (attribute.getLookupType() != null)
				schema.setLookupType(attribute.getLookupType().getType());
			break;
		case REFERENCE:
			ITable table;
			if (attribute.isReferenceDirect())
				table = attribute.getReferenceDomain().getTables()[1];
			else
				table = attribute.getReferenceDomain().getTables()[0];
			schema.setReferencedClassName(table.getName());
			schema.setReferencedIdClass(table.getId());
			schema.setIdDomain(attribute.getReferenceDomain().getId());
			break;
		}
		return schema;
	}

	private Metadata[] serializeMetadata(IAttribute attribute, ActivityDO activity) {
		final Metadata[] commonMetadata = serializeMetadata(BaseSchema.class.cast(attribute), activity);		
		final List<Metadata> metadata = new ArrayList<Metadata>(Arrays.asList(commonMetadata));
		checkAttributeFilter(attribute, metadata);
		final Metadata[] array = new Metadata[metadata.size()];
		return metadata.toArray(array);
	}

	public Metadata[] serializeMetadata(BaseSchema baseSchema, ActivityDO activity) {
		final TreeMap<String, Object> metadata = baseSchema.getMetadata();
		List<Metadata> tmpList = serializeMetadata(metadata);
		if (activity != null){
			checkProcessIsStopable(activity, tmpList);
			checkProcessIsEditable(activity, tmpList);
		}
		
		Metadata[] metadataList = new Metadata[tmpList.size()];
		metadataList = tmpList.toArray(metadataList);
		
		return metadataList;
	}

	private List<Metadata> serializeMetadata(TreeMap<String, Object> metadata) {
		List<Metadata> tmpList = new LinkedList<Metadata>();
		for (String key : metadata.keySet()) {
			Metadata m = new Metadata();
			m.setKey(key);
			m.setValue(metadata.get(key).toString());
			tmpList.add(m);
		}
		return tmpList;
	}


	private void checkProcessIsStopable(ActivityDO activity, List<Metadata> tmpList) {
		boolean isStopable = activity.isUserStoppable();
		Metadata m = new Metadata();
		m.setKey(MetadataService.RUNTIME_PROCESS_ISSTOPPABLE);
		m.setValue(String.valueOf(isStopable));
		tmpList.add(m);
	}
	
	private void checkProcessIsEditable(ActivityDO activity, List<Metadata> tmpList) {
		Metadata m = new Metadata();
		m.setKey(MetadataService.RUNTIME_PRIVILEGES_KEY);
		if (activity.isEditable()){
			m.setValue("write");
		} else {
			m.setValue("read");
		}
		
		tmpList.add(m);
	}

	private void checkAttributeFilter(IAttribute attribute, List<Metadata> tmpList) {
		final String filter = attribute.getFilter();
		if (StringUtils.isNotBlank(filter)) {			
			Metadata m = new Metadata();
			m.setKey(MetadataService.SYSTEM_TEMPLATE_PREFIX);
			m.setValue(filter);
			tmpList.add(m);
		}		
	}

	private MenuSchema serializeTree(CNode<MenuCard> node, UserContext userCtx) {
		MenuSchema schema = new MenuSchema();
		MenuCard menu = node.getData();		
		if (checkIsReport(menu)) {
			schema.setId(menu.getElementObjId());
		} else {
			schema.setId(menu.getId());
		}
		if (menu.getCode() != null) {
			if (menu.getCode().equals(MenuCodeType.SYSTEM_FOLDER.getCodeType())) {
				schema.setMenuType(MenuCodeType.FOLDER.getCodeType());
			} else {
				try {
					ITable menuEntryClass = userCtx.tables().get(menu.getElementClassId());
					Boolean isDefault = checkIsDefault(menuEntryClass, userCtx);
					schema.setDefaultToDisplay(isDefault);
					schema.setClassname(menuEntryClass.getName());
					schema.setMetadata(serializeMetadata(menuEntryClass, null));
					PrivilegeType privileges = userCtx.privileges().getPrivilege(menuEntryClass);
					if (PrivilegeType.NONE.equals(privileges))
						return null;
					schema.setPrivilege(privileges.toString());
				} catch (Exception e) {
					// Who cares if it fails
				}
				schema.setMenuType(menu.getCode().toLowerCase());
			}
		} else {
			schema.setMenuType(MenuCodeType.CLASS.getCodeType());
		}
		schema.setDescription(menu.getDescription());
		
		
		List<MenuSchema> children = new LinkedList<MenuSchema>();
		for (CNode<MenuCard> child : node.getChildren()) {
			MenuSchema childMenuSchema = null;
			if (MenuType.PROCESS.equals(child.getData().getTypeEnum())) {
				int cid = child.getData().getElementClassId();
				String cname = UserContext.systemContext().tables().get(cid).getName();
				try {
					if (WorkflowService.getInstance().isEnabled() &&
						WorkflowCache.getInstance().hasProcessClass(cname)) {
						childMenuSchema = serializeTree(child, userCtx);
					}
				} catch (NullPointerException e) {
					// shark configured but connection failed
				}
			} else {
				childMenuSchema = serializeTree(child, userCtx);
			}
			
			if (childMenuSchema != null) { // no privileges
				children.add(childMenuSchema);
			}
		}
		schema.setChildren(children.toArray(new MenuSchema[children.size()]));
		return schema;
	}

	private boolean checkIsReport(MenuCard menu) {
		return (menu.getCode() != null &&
				(menu.getCode().equals(MenuCodeType.REPORT_CSV.getCodeType())||
						menu.getCode().equals(MenuCodeType.REPORT_PDF.getCodeType())||
						menu.getCode().equals(MenuCodeType.REPORT_ODT.getCodeType())||
						menu.getCode().equals(MenuCodeType.REPORT_XML.getCodeType())));
	}

}
