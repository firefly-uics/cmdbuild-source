package org.cmdbuild.services.soap.operation;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.MenuCard.MenuCodeType;
import org.cmdbuild.elements.wrappers.MenuCard.MenuType;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.services.auth.PrivilegeManager.PrivilegeType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.structure.MenuSchema;
import org.cmdbuild.services.soap.types.Metadata;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.utils.tree.CTree;
import org.springframework.context.ApplicationContext;

public class EAdministration {

	private static ApplicationContext applicationContext = applicationContext();

	private final UserContext userCtx;
	private final WorkflowLogic workflowLogic;

	public EAdministration(final UserContext userCtx) {
		this.userCtx = userCtx;
		this.workflowLogic = applicationContext.getBean(WorkflowLogic.class);
	}

	public MenuSchema getClassMenuSchema() {
		final TableTree tree = UserOperations.from(userCtx).tables().fullTree().displayable();
		return serializeDefaultTree(tree.exclude(ProcessType.BaseTable).getRootElement(), false, userCtx);
	}

	public MenuSchema getProcessMenuSchema() {
		final TableTree tree = UserOperations.from(userCtx).processTypes().tree();
		return serializeDefaultTree(tree.getRootElement(), true, userCtx);
	}

	public MenuSchema getMenuSchema() {
		CTree<MenuCard> tree = MenuCard.loadTreeForGroup(userCtx.getDefaultGroup().getName());
		if (tree.getRootElement().getNumberOfChildren() == 0) {
			tree = MenuCard.loadTreeForGroup(MenuCard.DEFAULT_GROUP);
		}
		return serializeTree(tree.getRootElement(), userCtx);
	}

	private MenuSchema serializeDefaultTree(final CNode<ITable> rootElement, final boolean isProcess,
			final UserContext userCtx) {
		final MenuSchema schema = new MenuSchema();

		final ITable table = rootElement.getData();
		schema.setId(table.getId());
		schema.setDescription(table.getDescription());
		schema.setClassname(table.getName());
		setMenuTypeFromTypeAndChildren(schema, isProcess, table.isSuperClass());
		schema.setMetadata(serializeMetadata(table));
		addAccessPrivileges(schema, userCtx);
		final Boolean isDefault = checkIsDefault(table, userCtx);
		schema.setDefaultToDisplay(isDefault);
		final List<MenuSchema> children = new ArrayList<MenuSchema>();
		if (rootElement.getNumberOfChildren() > 0) {
			for (final CNode<ITable> child : rootElement.getChildren()) {
				children.add(serializeDefaultTree(child, isProcess, userCtx));
			}
		}
		schema.setChildren(children.toArray(new MenuSchema[children.size()]));
		return schema;
	}

	private Boolean checkIsDefault(final ITable table, final UserContext userCtx) {
		return table.equals(userCtx.getDefaultGroup().getStartingClass());
	}

	private void setMenuTypeFromTypeAndChildren(final MenuSchema schema, final boolean isProcess,
			final boolean isSuperclass) {
		MenuType type;
		if (isSuperclass) {
			type = isProcess ? MenuType.PROCESS_SUPERCLASS : MenuType.SUPERCLASS;
		} else {
			type = isProcess ? MenuType.PROCESS : MenuType.CLASS;
		}
		schema.setMenuType(type.getType());
	}

	private static void addAccessPrivileges(final MenuSchema menuSchema, final UserContext userCtx) {
		final ITable menuEntryClass = UserOperations.from(UserContext.systemContext()).tables()
				.get(menuSchema.getClassname());
		menuSchema.setClassname(menuEntryClass.getName());
		final PrivilegeType privileges = userCtx.privileges().getPrivilege(menuEntryClass);
		if (!PrivilegeType.NONE.equals(privileges)) {
			menuSchema.setPrivilege(privileges.toString());
		}
	}

	@Deprecated
	public AttributeSchema serialize(final IAttribute attribute) {
		return serialize(attribute, attribute.getIndex());
	}

	@Deprecated
	public static AttributeSchema serialize(final IAttribute attribute, final int client_index) {
		final AttributeSchema schema = new AttributeSchema();
		schema.setIdClass(attribute.getSchema().getId());
		schema.setName(attribute.getDBName());
		schema.setDescription(attribute.getDescription());
		schema.setType(attribute.getType().wsName());
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

		schema.setMetadata(serializeMetadata(attribute));

		final AttributeType atype = attribute.getType();
		switch (atype) {
		case LOOKUP:
			if (attribute.getLookupType() != null) {
				schema.setLookupType(attribute.getLookupType().getType());
			}
			break;
		case REFERENCE:
			ITable table;
			if (attribute.isReferenceDirect()) {
				table = attribute.getReferenceDomain().getTables()[1];
			} else {
				table = attribute.getReferenceDomain().getTables()[0];
			}
			schema.setReferencedClassName(table.getName());
			schema.setReferencedIdClass(table.getId());
			schema.setIdDomain(attribute.getReferenceDomain().getId());
			break;
		}
		return schema;
	}

	private static Metadata[] serializeMetadata(final IAttribute attribute) {
		final List<Metadata> metadata = serializeMetadata(attribute.getMetadata());
		addAttributeFilterMetadata(attribute, metadata);
		return metadata.toArray(new Metadata[metadata.size()]);
	}

	private Metadata[] serializeMetadata(final BaseSchema baseSchema) {
		final List<Metadata> meta = serializeMetadata(baseSchema.getMetadata());
		return meta.toArray(new Metadata[meta.size()]);
	}

	private static List<Metadata> serializeMetadata(final TreeMap<String, Object> metadata) {
		final List<Metadata> tmpList = new ArrayList<Metadata>();
		for (final String key : metadata.keySet()) {
			final Metadata m = new Metadata();
			m.setKey(key);
			m.setValue(metadata.get(key).toString());
			tmpList.add(m);
		}
		return tmpList;
	}

	private static void addAttributeFilterMetadata(final IAttribute attribute, final List<Metadata> tmpList) {
		final String filter = attribute.getFilter();
		if (StringUtils.isNotBlank(filter)) {
			final Metadata m = new Metadata();
			m.setKey(MetadataService.SYSTEM_TEMPLATE_PREFIX);
			m.setValue(filter);
			tmpList.add(m);
		}
	}

	private MenuSchema serializeTree(final CNode<MenuCard> node, final UserContext userCtx) {
		final MenuSchema schema = new MenuSchema();
		final MenuCard menu = node.getData();
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
					final ITable menuEntryClass = UserOperations.from(userCtx).tables().get(menu.getElementClassId());
					final Boolean isDefault = checkIsDefault(menuEntryClass, userCtx);
					schema.setDefaultToDisplay(isDefault);
					schema.setClassname(menuEntryClass.getName());
					schema.setMetadata(serializeMetadata(menuEntryClass));
					final PrivilegeType privileges = userCtx.privileges().getPrivilege(menuEntryClass);
					if (PrivilegeType.NONE.equals(privileges)) {
						return null;
					}
					schema.setPrivilege(privileges.toString());
				} catch (final Exception e) {
					// Who cares if it fails
				}
				schema.setMenuType(menu.getCode().toLowerCase());
			}
		} else {
			schema.setMenuType(MenuCodeType.CLASS.getCodeType());
		}
		schema.setDescription(menu.getDescription());

		final List<MenuSchema> children = new ArrayList<MenuSchema>();
		for (final CNode<MenuCard> child : node.getChildren()) {
			MenuSchema childMenuSchema = null;
			if (MenuType.PROCESS.equals(child.getData().getTypeEnum())) {
				final int cid = child.getData().getElementClassId();
				final String cname = UserOperations.from(UserContext.systemContext()).tables().get(cid).getName();
				if (workflowLogic.isProcessUsable(cname)) {
					childMenuSchema = serializeTree(child, userCtx);
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

	private boolean checkIsReport(final MenuCard menu) {
		return (menu.getCode() != null && (menu.getCode().equals(MenuCodeType.REPORT_CSV.getCodeType())
				|| menu.getCode().equals(MenuCodeType.REPORT_PDF.getCodeType())
				|| menu.getCode().equals(MenuCodeType.REPORT_ODT.getCodeType()) || menu.getCode().equals(
				MenuCodeType.REPORT_XML.getCodeType())));
	}

}
