package org.cmdbuild.servlets.json.serializers;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.ArrayUtils;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.common.Constants;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.GeometryAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dms.Metadata;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.dms.StoredDocument;
import org.cmdbuild.elements.AttributeValue;
import org.cmdbuild.elements.DirectedDomain;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.elements.TableImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.IRelation.RelationAttributes;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.Process.ProcessAttributes;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.utils.CountedValue;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.MenuCard;
import org.cmdbuild.elements.wrappers.MenuCard.MenuCodeType;
import org.cmdbuild.elements.wrappers.MenuCard.MenuType;
import org.cmdbuild.elements.wrappers.PrivilegeCard.PrivilegeType;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.exception.DmsException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.GroupInfo;
import org.cmdbuild.logic.privileges.SecurityLogic.PrivilegeInfo;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.gis.GeoFeatureType;
import org.cmdbuild.services.gis.GeoLayer;
import org.cmdbuild.services.gis.GeoTable;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.servlets.json.management.ActivityIdentifier;
import org.cmdbuild.servlets.json.schema.ModClass.JsonModeMapper;
import org.cmdbuild.servlets.json.serializers.JsonHistory.HistoryItem;
import org.cmdbuild.servlets.json.serializers.JsonHistory.ValueAndDescription;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class Serializer {

	// TODO use constants
	private static final SimpleDateFormat ATTACHMENT_DATE_FOMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static final String AVAILABLE_CLASS = "availableclass";
	public static final String AVAILABLE_PROCESS_CLASS = "availableprocessclass";
	public static final String AVAILABLE_REPORT = "availablereport";
	public static final String AVAILABLE_DASHBOARDS = "availabledashboards";

	public static JSONObject serializeCard(final ICard card, final boolean printReserved) {
		return serializeCard(card, printReserved, false, false);
	}

	public static JSONObject serializeCardNormalized(final ICard card) {
		return serializeCard(card, false, false, true);
	}

	public static JSONObject serializeCardWithPrivileges(final ICard card, final boolean printReserved) {
		return serializeCard(card, printReserved, true, false);
	}

	private static JSONObject serializeCard(final ICard card, final boolean printReserved,
			final boolean printPrivileges, final boolean normalize) {
		final JSONObject jsoncard = new JSONObject();
		try {
			for (final String attributeName : card.getAttributeValueMap().keySet()) {
				final AttributeValue value = card.getAttributeValue(attributeName);
				if (value != null) {
					final IAttribute attribute = value.getSchema();
					if (!printReserved
							&& attribute.getMode().equals(Mode.RESERVED)
							&& !(attributeName.equals(ICard.CardAttributes.Id.toString())
									|| !attribute.getStatus().isActive() || // skip
							// inactive
							// attributes
							attributeName.equals(ICard.CardAttributes.Notes.toString()) // Notes
							// is
							// reserved!
							))
						continue;
					final Integer id = value.getId();
					String valueString = value.toString();
					if (normalize) {
						valueString = valueString.replace("\n", " ");
					}
					if (id != null) {
						// jsoncard.put(attributeName, id);
						// jsoncard.put(attributeName+"_value", valueString);
						final JSONObject a = new JSONObject();
						a.put("id", id);
						a.put("description", valueString);
						jsoncard.put(attributeName, a);
					} else {
						jsoncard.put(attributeName, valueString);
					}
				}
			}
			jsoncard.put(ICard.CardAttributes.ClassId.toString(), card.getSchema().getId()); // put
			// classId
			jsoncard.put(ICard.CardAttributes.ClassId.toString() + "_value", card.getSchema().getDescription());
			if (printPrivileges) {
				addMetadataAndAccessPrivileges(jsoncard, card.getSchema());
			}
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing card", e);
		}
		return jsoncard;
	}

	public static JSONObject serializeRelation(final CountedValue<IRelation> countedRelation) {
		return serializeRelation(countedRelation.getValue(), countedRelation.getCount());
	}

	public static JSONObject serializeRelation(final IRelation relation) {
		return serializeRelation(relation, 0);
	}

	public static JSONObject serializeRelation(final IRelation relation, final int count) {
		final JSONObject serializer = new JSONObject();
		ICard destCard, card1, card2;
		try {
			final DirectedDomain directedDomain = relation.getDirectedDomain();
			serializer.put("Domain", directedDomain.toString());
			serializer.put("DomainDesc", directedDomain.getDescription());
			serializer.put("DomainDir", directedDomain.getDirectionValue());
			if (count != 0)
				serializer.put("DomainCount", count);
			destCard = relation.getCard2();
			if (relation.isReversed()) {
				serializer.put("DomainDestClassId", relation.getSchema().getClass1().getId());
				card1 = relation.getCard2();
				card2 = relation.getCard1();
			} else {
				serializer.put("DomainDestClassId", relation.getSchema().getClass2().getId());
				card1 = relation.getCard1();
				card2 = relation.getCard2();
			}
			if (destCard != null) {
				final ITable destTable = destCard.getSchema();
				// relation key
				serializer.put("Id", relation.getId());
				serializer.put("DomainId", relation.getSchema().getId());
				serializer.put("Class1Id", card1.getIdClass());
				serializer.put("Card1Id", card1.getId());
				serializer.put("Class2Id", card2.getIdClass());
				serializer.put("Card2Id", card2.getId());
				serializer.put("BeginDate", relation.getAttributeValue(RelationAttributes.BeginDate.toString()));
				serializer.put("EndDate", relation.getAttributeValue("EndDate"));
				serializer.put("User", relation.getAttributeValue("User"));

				serializer.put("Class", destCard.getSchema().toString());
				serializer.put("ClassType", getClassType(destCard.getSchema().getName()));
				serializer.put("ClassId", destTable.getId());
				addMetadataAndAccessPrivileges(serializer, destTable);

				serializer.put("CardId", destCard.getId());
				serializer.put("CardCode", destCard.getCode());
				serializer.put("CardDescription", destCard.getDescription());
			}
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing relation", e);
		}
		return serializer;
	}

	/**
	 * @deprecated This is awful: a Table should know it is in a tree!
	 */
	@Deprecated
	private static String getClassType(final String className) {
		// TODO This is awful: a Table should know it is in a tree!
		if (TableImpl.tree().branch(ProcessType.BaseTable).contains(className))
			return "processclass";
		else
			return "class";
	}

	public static JSONObject serializeAttachment(final StoredDocument attachment) {
		final JSONObject serializer = new JSONObject();
		try {
			serializer.put("Category", attachment.getCategory());
			serializer.put("CreationDate", ATTACHMENT_DATE_FOMAT.format(attachment.getCreated()));
			serializer.put("ModificationDate", ATTACHMENT_DATE_FOMAT.format(attachment.getModified()));
			serializer.put("Author", attachment.getAuthor());
			serializer.put("Version", attachment.getVersion());
			serializer.put("Filename", attachment.getName());
			serializer.put("Description", attachment.getDescription());
			serializer.put("Metadata", serialize(attachment.getMetadataGroups()));
		} catch (final JSONException e) {
			Log.JSONRPC.error("Error serializing attachment", e);
		}
		return serializer;
	}

	private static JSONObject serialize(final Iterable<MetadataGroup> metadataGroups) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		for (final MetadataGroup metadataGroup : metadataGroups) {
			final JSONObject jsonAllMetadata = new JSONObject();
			for (final Metadata metadata : metadataGroup.getMetadata()) {
				jsonAllMetadata.put(metadata.getName(), metadata.getValue());
			}
			jsonMetadata.put(metadataGroup.getName(), jsonAllMetadata);
		}
		return jsonMetadata;
	}

	public static JSONObject serializeLookup(final Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public static JSONObject serializeLookup(final Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("Id", lookup.getId());
			serializer.put("Description", lookup.getDescription());

			if (!shortForm) {
				serializer.put("Type", lookup.getType());
				serializer.put("Code", lookup.getCode() != null ? lookup.getCode() : "");
				serializer.put("Number", lookup.getNumber());
				serializer.put("Notes", lookup.getNotes());
				serializer.put("Default", lookup.getIsDefault());
				serializer.put("Active", lookup.getStatus().isActive());
			}

			final Lookup parent = lookup.getParent();
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {
					serializer.put("ParentDescription", parent.getDescription());
					serializer.put("ParentType", parent.getType());
				}
			}
		}
		return serializer;
	}

	public static JSONObject serializeLookupType(final LookupType lookupType) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put("description", lookupType.getType());
		row.put("parent", lookupType.getParentTypeName());
		row.put("orig_type", lookupType.getType()); // used if someone want to
		// modify the type name
		return row;
	}

	public static JSONObject serializeLookupParent(final Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("ParentId", lookup.getId());
			serializer.put("ParentDescription", lookup.getDescription());
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupType lookupType) throws JSONException {
		final JSONObject serializer = new JSONObject();
		serializer.put("id", lookupType.getType());
		serializer.put("text", lookupType.getType());
		serializer.put("type", "lookuptype");
		serializer.put("selectable", true);

		if (lookupType.getParentTypeName() != null) {
			serializer.put("parent", lookupType.getParentTypeName());
		}
		return serializer;
	}

	/*
	 * Administration
	 */

	public static JSONObject serialize(final CMAttribute attribute) throws JSONException {
		final JSONObject jsonObject = new JSONObject();

		final Map<String, Object> attributes = new CMAttributeTypeVisitor() {

			private final Map<String, Object> attributes = Maps.newHashMap();

			@Override
			public void visit(final BooleanAttributeType attributeType) {
			}

			@Override
			public void visit(final EntryTypeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateTimeAttributeType attributeType) {
			}

			@Override
			public void visit(final DateAttributeType attributeType) {
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				attributes.put("precision", attributeType.precision);
				attributes.put("scale", attributeType.scale);
			}

			@Override
			public void visit(final DoubleAttributeType attributeType) {
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				// jattr.put("fkDestination",
				// attribute.getFKTargetClass().getId());
			}

			@Override
			public void visit(final GeometryAttributeType attributeType) {
			}

			@Override
			public void visit(final IntegerAttributeType attributeType) {
			}

			@Override
			public void visit(final IpAddressAttributeType attributeType) {
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				// // NdPaolo: PLEASE, LET ME REFACTOR THE LOOKUPS
				// LookupType lt = attribute.getLookupType();
				// JSONArray lookupChain = new JSONArray();
				// while (lt != null) {
				// if (lookupChain.length() == 0) {
				// jattr.put("lookup", lt.getType());
				// }
				// lookupChain.put(lt.getType());
				// lt = lt.getParentType();
				// }
				// jattr.put("lookupchain", lookupChain);
			}

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				// ITable reftable = attribute.getReferenceTarget();
				// jattr.put("referencedClassName", reftable.getName());
				// jattr.put("referencedIdClass", reftable.getId());
				// jattr.put("fieldFilter", attribute.getFilter());
				// jattr.put("domainDirection", attribute.isReferenceDirect());
				// jattr.put("idDomain",
				// attribute.getReferenceDomain().getId());
			}

			@Override
			public void visit(final StringAttributeType attributeType) {
				attributes.put("len", attributeType.length);
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				attributes.put("editorType", attribute.getEditorType());
			}

			@Override
			public void visit(final TimeAttributeType attributeType) {
			}

			public Map<String, Object> fill(final CMAttribute attribute) {
				// type specific
				attribute.getType().accept(this);

				// commons
				attributes.put("idClass", attribute.getOwner().getId());
				attributes.put("name", attribute.getName());
				attributes.put("description", attribute.getDescription());
				attributes.put("type",
						new JsonDashboardDTO.JsonDataSourceParameter.TypeConverter(attribute.getType()).getTypeName());
				attributes.put("isbasedsp", attribute.isDisplayableInList());
				attributes.put("isunique", attribute.isUnique());
				attributes.put("isnotnull", attribute.isMandatory());
				attributes.put("inherited", attribute.isInherited());
				attributes.put("isactive", attribute.isActive());
				attributes.put("fieldmode", JsonModeMapper.textFrom(attribute.getMode()));
				attributes.put("index", attribute.getIndex());
				attributes.put("defaultvalue", attribute.getDefaultValue());
				attributes.put("group", attribute.getGroup());
				// addMetadata(jattr, attribute);

				int absoluteClassOrder = attribute.getClassOrder();
				int classOrderSign;
				if (absoluteClassOrder == 0) {
					classOrderSign = 0;
					// to manage the sorting in the AttributeGridForSorting
					absoluteClassOrder = 10000;
				} else if (absoluteClassOrder > 0) {
					classOrderSign = 1;
				} else {
					classOrderSign = -1;
					absoluteClassOrder *= -1;
				}
				attributes.put("classOrderSign", classOrderSign);
				attributes.put("absoluteClassOrder", absoluteClassOrder);
				return attributes;
			}

		}.fill(attribute);
		for (final Entry<String, Object> entry : attributes.entrySet()) {
			jsonObject.put(entry.getKey(), entry.getValue());
		}
		return jsonObject;
	}

	/**
	 * @deprecated use serialize(CMAttribute) instead.
	 */
	@Deprecated
	public static JSONObject serializeAttribute(final IAttribute attribute) throws JSONException {
		final JSONObject jattr = new JSONObject();
		jattr.put("idClass", attribute.getSchema().getId());
		jattr.put("name", attribute.getName());
		jattr.put("description", attribute.getDescription());
		jattr.put("type", attribute.getType());
		jattr.put("isbasedsp", attribute.isBaseDSP());
		jattr.put("isunique", attribute.isUnique());
		jattr.put("isnotnull", attribute.isNotNull());
		jattr.put("inherited", !attribute.isLocal());
		jattr.put("index", attribute.getIndex());
		jattr.put("group", attribute.getGroup());

		int absoluteClassOrder = attribute.getClassOrder();
		int classOrderSign;
		if (absoluteClassOrder == 0) {
			classOrderSign = 0;
			// to manage the sorting in the AttributeGridForSorting
			absoluteClassOrder = 10000;
		} else if (absoluteClassOrder > 0) {
			classOrderSign = 1;
		} else {
			classOrderSign = -1;
			absoluteClassOrder *= -1;
		}
		jattr.put("classOrderSign", classOrderSign);
		jattr.put("absoluteClassOrder", absoluteClassOrder);
		jattr.put("len", attribute.getLength());
		jattr.put("precision", attribute.getPrecision());
		jattr.put("scale", attribute.getScale());
		jattr.put("defaultvalue", attribute.getDefaultValue());
		jattr.put("isactive", attribute.getStatus().isActive());
		jattr.put("isactive", attribute.getStatus().isActive());
		jattr.put("fieldmode", attribute.getFieldMode().getMode());
		jattr.put("editorType", attribute.getEditorType());
		switch (attribute.getType()) {
		case LOOKUP:
			// NdPaolo: PLEASE, LET ME REFACTOR THE LOOKUPS
			LookupType lt = attribute.getLookupType();
			final JSONArray lookupChain = new JSONArray();
			while (lt != null) {
				if (lookupChain.length() == 0) {
					jattr.put("lookup", lt.getType());
				}
				lookupChain.put(lt.getType());
				lt = lt.getParentType();
			}
			jattr.put("lookupchain", lookupChain);
			break;
		case REFERENCE:
			final ITable reftable = attribute.getReferenceTarget();
			jattr.put("referencedClassName", reftable.getName());
			jattr.put("referencedIdClass", reftable.getId());
			jattr.put("fieldFilter", attribute.getFilter());
			jattr.put("domainDirection", attribute.isReferenceDirect());
			jattr.put("idDomain", attribute.getReferenceDomain().getId());
			break;

		case FOREIGNKEY:
			jattr.put("fkDestination", attribute.getFKTargetClass().getId());
			break;
		}
		addMetadata(jattr, attribute);
		return jattr;
	}

	/**
	 * @deprecated use serialize(CMDomain) instead.
	 */
	@Deprecated
	public static JSONObject serializeDomain(final IDomain domain, final boolean activeOnly) throws JSONException {
		final JSONObject jsonobj = new JSONObject();
		jsonobj.put("idDomain", domain.getId());
		jsonobj.put("name", domain.getName());
		jsonobj.put("origName", domain.getName());
		jsonobj.put("description", domain.getDescription());
		jsonobj.put("descrdir", domain.getDescriptionDirect());
		jsonobj.put("descrinv", domain.getDescriptionInverse());
		jsonobj.put("class1", domain.getTables()[0].toString());
		jsonobj.put("class1id", domain.getTables()[0].getId());
		jsonobj.put("class2", domain.getTables()[1].toString());
		jsonobj.put("class2id", domain.getTables()[1].getId());
		jsonobj.put("md", domain.isMasterDetail());
		jsonobj.put("md_label", domain.getMDLabel());
		jsonobj.put("classType", getClassType(domain.getTables()[0].getName()));
		jsonobj.put("active", domain.getStatus().isActive());
		jsonobj.put("cardinality", domain.getCardinality());
		jsonobj.put("attributes", serializeAttributeList(domain, activeOnly));
		addMetadataAndAccessPrivileges(jsonobj, domain);
		return jsonobj;
	}

	public static JSONObject serialize(final CMDomain domain, final boolean activeOnly) throws JSONException {
		final JSONObject jsonDomain = new JSONObject();
		jsonDomain.put("idDomain", domain.getId());
		jsonDomain.put("name", domain.getName());
		jsonDomain.put("origName", domain.getName());
		jsonDomain.put("description", domain.getDescription());
		jsonDomain.put("descrdir", domain.getDescription1());
		jsonDomain.put("descrinv", domain.getDescription2());
		jsonDomain.put("class1", domain.getClass1().getName());
		jsonDomain.put("class1id", domain.getClass1().getId());
		jsonDomain.put("class2", domain.getClass2().getName());
		jsonDomain.put("class2id", domain.getClass2().getId());
		jsonDomain.put("md", domain.isMasterDetail());
		jsonDomain.put("md_label", domain.getMasterDetailDescription());
		jsonDomain.put("classType", getClassType(domain.getName()));
		jsonDomain.put("active", domain.isActive());
		jsonDomain.put("cardinality", domain.getCardinality());
		jsonDomain.put("attributes", serialize(domain.getAllAttributes(), activeOnly));
		// TODO complete
		// addMetadataAndAccessPrivileges(jsonDomain, domain);
		return jsonDomain;
	}

	public static JSONObject serialize(final CMDomain domain, final Long classId) throws JSONException {
		final JSONObject jsonDomain = serialize(domain, false);
		jsonDomain.put("inherited", !isDomainDefinedForClass(domain, classId));
		return jsonDomain;
	}

	/**
	 * @return true if the domain is defined for the class with provided
	 *         classId, false otherwise (it is defined for a superclass)
	 */
	private static boolean isDomainDefinedForClass(final CMDomain domain, final Long classId) {
		final CMClass class1 = domain.getClass1();
		final CMClass class2 = domain.getClass2();
		if (!class1.getId().equals(classId) && !class2.getId().equals(classId)) {
			return false;
		}
		return true;
	}

	public static JSONObject serializeTableTree(final CNode<ITable> node) throws JSONException {
		final ITable table = node.getData();
		final JSONObject jsonTableTree = serializeTable(table);
		if (jsonTableTree != null) {
			if (node.getNumberOfChildren() > 0) {
				for (final CNode<ITable> child : node.getChildren()) {
					final JSONObject jsonChild = serializeTableTree(child);
					if (jsonChild != null) {
						jsonTableTree.append("children", jsonChild);
					}
				}
			}

			final boolean hasChildren = jsonTableTree.has("children"); // children
			// might be
			// without
			// xpdl
			jsonTableTree.put("leaf", !hasChildren);
		}
		return jsonTableTree;
	}

	public static JSONObject serializeTable(final ITable table, final UserProcessClass pc) throws JSONException {
		final JSONObject jsonProcess = serializeTable(table);
		boolean isStartable = !pc.isSuperclass();
		if (isStartable) {
			try {
				isStartable = pc.isStartable();
			} catch (final CMWorkflowException e) {
				isStartable = false;
			}
		}

		// add this to look in the XPDL if the current user has
		// the privileges to start the process and ignore the table privileges
		// (priv_create)
		jsonProcess.put("startable", isStartable);
		return jsonProcess;
	}

	public static JSONObject serialize(final CMClass cmClass) throws JSONException {
		final JSONObject jsonTable = new JSONObject();

		jsonTable.put("type", getClassType(cmClass.getName()));
		// TODO complete
		// if (table.isActivity()) {
		// jsonTable.put("userstoppable", table.isUserStoppable());
		// } else {
		// jsonTable.put("type", "class");
		// }

		jsonTable.put("id", cmClass.getId());
		jsonTable.put("name", cmClass.getName());
		jsonTable.put("text", cmClass.getDescription());
		jsonTable.put("superclass", cmClass.isSuperclass());
		jsonTable.put("active", cmClass.isActive());

		jsonTable.put("tableType", cmClass.holdsHistory() ? "standard" : "simpletable");
		jsonTable.put("selectable", !cmClass.getName().equals(Constants.BASE_CLASS_NAME));

		// TODO complete
		// addMetadataAndAccessPrivileges(jsonTable, table);
		// addGeoFeatureTypes(jsonTable, table);
		addParent(cmClass, jsonTable);
		return jsonTable;
	}

	/**
	 * @deprecated use serialize(CMClass) instead.
	 */
	@Deprecated
	public static JSONObject serializeTable(final ITable table) throws JSONException {
		final JSONObject jsonTable = new JSONObject();

		if (table.isActivity()) {
			jsonTable.put("type", "processclass");
			jsonTable.put("userstoppable", table.isUserStoppable());
		} else {
			jsonTable.put("type", "class");
		}

		jsonTable.put("id", table.getId());
		jsonTable.put("name", table.getName());
		jsonTable.put("text", table.getDescription());
		jsonTable.put("superclass", table.isSuperClass());
		jsonTable.put("active", table.getStatus().isActive());

		if (table.getTableType() == CMTableType.SIMPLECLASS) {
			jsonTable.put("tableType", "simpletable");
		} else {
			jsonTable.put("tableType", "standard");
		}

		if (table.isTheTableClass()) {
			jsonTable.put("selectable", false);
		} else {
			jsonTable.put("selectable", true);
		}

		addMetadataAndAccessPrivileges(jsonTable, table);
		addGeoFeatureTypes(jsonTable, table);
		addParent(table, jsonTable);
		return jsonTable;
	}

	private static void addGeoFeatureTypes(final JSONObject jsonTable, final ITable table) throws JSONException {
		final JSONArray jsonFeatureTypes = new JSONArray();
		final GeoTable geoMasterClass = new GeoTable(table);
		for (final GeoLayer layer : geoMasterClass.getVisibleOrOwnLayers()) {
			jsonFeatureTypes.put(serializeGeoLayer(layer, table));
		}
		final JSONObject jsonMeta = (JSONObject) jsonTable.get("meta");
		jsonMeta.put("geoAttributes", jsonFeatureTypes);
	}

	public static JSONArray serializeGeoLayers(final List<? extends GeoLayer> geoLayers) throws JSONException {
		return serializeGeoLayers(geoLayers, null);
	}

	public static JSONArray serializeGeoLayers(final List<? extends GeoLayer> geoLayers, final ITable tableForVisibility)
			throws JSONException {
		final JSONArray jsonLayers = new JSONArray();
		for (final GeoLayer geoLayer : geoLayers) {
			jsonLayers.put(serializeGeoLayer(geoLayer, tableForVisibility));
		}
		return jsonLayers;
	}

	public static JSONObject serializeGeoLayer(final GeoLayer geoLayer) throws JSONException {
		return serializeGeoLayer(geoLayer, null);
	}

	public static JSONObject serializeGeoLayer(final GeoLayer geoLayer, final ITable tableForVisibility)
			throws JSONException {
		final JSONObject jsonGeoLayer = new JSONObject();
		jsonGeoLayer.put("name", geoLayer.getName());
		jsonGeoLayer.put("description", geoLayer.getDescription());
		jsonGeoLayer.put("type", geoLayer.getTypeName());
		jsonGeoLayer.put("maxZoom", geoLayer.getMaxZoom());
		jsonGeoLayer.put("minZoom", geoLayer.getMinZoom());
		jsonGeoLayer.put("index", geoLayer.getIndex());
		if (tableForVisibility != null) {
			jsonGeoLayer.put("isvisible", geoLayer.isVisible(tableForVisibility));
		}
		if (geoLayer instanceof GeoFeatureType) {
			final GeoFeatureType featureType = (GeoFeatureType) geoLayer;
			jsonGeoLayer.put("style", featureType.getStyle());
			jsonGeoLayer.put("masterTableId", featureType.getMasterTable().getId());
			jsonGeoLayer.put("masterTableName", featureType.getMasterTable().getName());
		}
		return jsonGeoLayer;
	}

	// FIXME really needed in this way?
	private static void addParent(final CMClass target, final JSONObject jsonTable) throws JSONException {
		final boolean isSimpleClass = target.holdsHistory();
		final boolean isActivityClass = target.getName().equals(Constants.BASE_PROCESS_CLASS_NAME);
		final CMClass parent = target.getParent();
		if (!isSimpleClass && !isActivityClass && (parent != null)) {
			jsonTable.put("parent", parent.getId());
		}
	}

	/**
	 * @deprecated use addParent(CMClass, JSONObject) instead.
	 */
	@Deprecated
	private static void addParent(final ITable table, final JSONObject jsonTable) throws JSONException {
		try {
			if (table.getTableType() != CMTableType.SIMPLECLASS && !table.isTheTableActivity()) {
				jsonTable.put("parent", table.getParent().getId());
			}
		} catch (final NullPointerException e) {
			// If the table has no parent
		}
	}

	private static void addMetadataAndAccessPrivileges(final JSONObject serializer, final BaseSchema schema)
			throws JSONException {
		addMetadata(serializer, schema);
		addAccessPrivileges(serializer, schema);
	}

	private static void addMetadata(final JSONObject serializer, final BaseSchema schema) throws JSONException {
		final JSONObject jsonMetadata = new JSONObject();
		final TreeMap<String, Object> metadata = schema.getMetadata();
		for (final String key : metadata.keySet()) {
			jsonMetadata.put(key, metadata.get(key));
		}
		serializer.put("meta", jsonMetadata);
	}

	private static void addAccessPrivileges(final JSONObject serializer, final BaseSchema schema) throws JSONException {
		final Object privileges = schema.getMetadata().get(MetadataService.RUNTIME_PRIVILEGES_KEY);
		if (privileges != null) {
			final boolean writePriv = PrivilegeType.WRITE.equals(privileges)
					&& !schema.getMode().alwaysReadPrivileges();
			serializer.put("priv_write", writePriv);
			boolean createPriv = writePriv;
			if (schema instanceof ITable) {
				createPriv &= !((ITable) schema).isSuperClass();
			}
			serializer.put("priv_create", createPriv);
		}
	}

	public static JSONArray buildJsonAvaiableMenuItems() throws JSONException {
		final JSONArray jsonAvaiableItems = new JSONArray();

		final JSONObject jsonClassesFolder = new JSONObject();
		final JSONObject jsonReportsFolder = new JSONObject();
		final JSONObject jsonProcessFolder = new JSONObject();
		final JSONObject jsonDashboardsFolder = new JSONObject();

		jsonClassesFolder.put("text", "class");
		jsonClassesFolder.put("id", AVAILABLE_CLASS);
		jsonClassesFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonClassesFolder.put("cmIndex", 1);

		jsonProcessFolder.put("text", "processclass");
		jsonProcessFolder.put("id", AVAILABLE_PROCESS_CLASS);
		jsonProcessFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonProcessFolder.put("cmIndex", 2);

		jsonReportsFolder.put("text", "report");
		jsonReportsFolder.put("id", AVAILABLE_REPORT);
		jsonReportsFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonReportsFolder.put("cmIndex", 3);

		jsonDashboardsFolder.put("text", "dashboard");
		jsonDashboardsFolder.put("id", AVAILABLE_DASHBOARDS);
		jsonDashboardsFolder.put("iconCls", "cmdbuild-tree-folder-icon");
		jsonDashboardsFolder.put("cmIndex", 4);

		jsonAvaiableItems.put(jsonReportsFolder);
		jsonAvaiableItems.put(jsonClassesFolder);
		jsonAvaiableItems.put(jsonProcessFolder);
		jsonAvaiableItems.put(jsonDashboardsFolder);

		return jsonAvaiableItems;
	}

	public static JSONObject serializeReportForMenu(final ReportCard report, final String type) throws JSONException {
		final JSONObject jsonReport = new JSONObject();
		jsonReport.put("text", report.getDescription());
		jsonReport.put("parent", AVAILABLE_REPORT);
		jsonReport.put("selectable", true);
		jsonReport.put("type", type);
		jsonReport.put("subtype", report.getType().toString().toLowerCase());
		jsonReport.put("objid", report.getId());
		jsonReport.put("id", report.getId() + type);
		jsonReport.put("leaf", true);
		return jsonReport;
	}

	public static JSONObject serializeExtentedProperties(final ITable table) throws JSONException {
		final JSONObject serializer = new JSONObject();
		final Map<String, Object> xp = table.getMetadata();
		for (final String key : xp.keySet()) {
			serializer.put(key, xp.get(key).toString());
		}
		return serializer;
	}

	public static JSONArray serialize(final Iterable<? extends CMAttribute> attributes, final boolean active)
			throws JSONException {
		final JSONArray attributeList = new JSONArray();
		for (final CMAttribute attribute : sortAttributes(attributes)) {
			if (active && !attribute.isActive()) {
				continue;
			}
			attributeList.put(serialize(attribute));
		}
		return attributeList;
	}

	/**
	 * we sort attributes on the class order and index number because Ext.JS
	 * DOES NOT ALLOW IT. Thanks Jack!
	 */
	private static Iterable<? extends CMAttribute> sortAttributes(final Iterable<? extends CMAttribute> attributes) {
		return new Ordering<CMAttribute>() {

			@Override
			public int compare(final CMAttribute left, final CMAttribute right) {
				if (left.getClassOrder() == right.getClassOrder()) {
					return (left.getIndex() - right.getIndex());
				} else {
					return (left.getClassOrder() - right.getClassOrder());
				}
			}

		}.immutableSortedCopy(attributes);
	}

	/**
	 * @deprecated use serialize(Iterable<CMAttribute>, boolean) instead.
	 */
	@Deprecated
	public static JSONArray serializeAttributeList(final BaseSchema table, final boolean active) throws JSONException {
		final List<IAttribute> sortedAttributes = sortAttributes(table.getAttributes().values());
		final JSONArray attributeList = new JSONArray();
		for (final IAttribute attribute : sortedAttributes) {
			if (attribute.getMode().equals(Mode.RESERVED))
				continue;
			if (active && !attribute.getStatus().isActive())
				continue;
			attributeList.put(Serializer.serializeAttribute(attribute));
		}
		return attributeList;
	}

	/*
	 * we sort attributes on the class order and index number because Ext.JS
	 * DOES NOT ALLOW IT. Thanks Jack!
	 */
	private static List<IAttribute> sortAttributes(final Collection<IAttribute> attributeCollection) {
		final List<IAttribute> sortedAttributes = new LinkedList<IAttribute>();
		sortedAttributes.addAll(attributeCollection);
		Collections.sort(sortedAttributes, new Comparator<IAttribute>() {
			@Override
			public int compare(final IAttribute a1, final IAttribute a2) {
				if (a1.getClassOrder() == a2.getClassOrder()) {
					return (a1.getIndex() - a2.getIndex());
				} else {
					return (a1.getClassOrder() - a2.getClassOrder());
				}
			}
		});
		return sortedAttributes;
	}

	// TODO: delete this method when old dao will be updated with new dao
	public static JSONObject serializeGroupCard(final GroupCard groupCard) throws JSONException {
		final JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", groupCard.getId());
		jsonGroup.put("name", groupCard.getName());
		jsonGroup.put("description", groupCard.getDescription());
		jsonGroup.put("email", groupCard.getEmail());
		jsonGroup.put("isAdministrator", groupCard.isAdmin());
		jsonGroup.put("startingClass", groupCard.getStartingClassId());
		jsonGroup.put("isActive", groupCard.getStatus().isActive());
		jsonGroup.put("text", groupCard.getDescription());
		jsonGroup.put("selectable", true);
		jsonGroup.put("type", "group");
		return jsonGroup;
	}

	public static JSONObject serialize(final CMGroup group) throws JSONException {
		final JSONObject jsonGroup = new JSONObject();
		jsonGroup.put("id", group.getId());
		jsonGroup.put("name", group.getName());
		jsonGroup.put("description", group.getDescription());
		jsonGroup.put("email", group.getEmail());
		jsonGroup.put("isAdministrator", group.isAdmin());
		jsonGroup.put("startingClass", group.getStartingClassId());
		jsonGroup.put("isActive", group.isActive());
		jsonGroup.put("text", group.getDescription());
		jsonGroup.put("selectable", true);
		jsonGroup.put("type", "group");
		return jsonGroup;
	}

	public static JSONArray serializeGroupsForUser(final CMUser user, final List<GroupInfo> groups)
			throws JSONException {
		final JSONArray jsonGroupList = new JSONArray();
		for (final GroupInfo group : groups) {
			final JSONObject row = new JSONObject();
			row.put("id", group.getId());
			row.put("description", group.getDescription());
			final String userDefaultGroupName = user.getDefaultGroupName();
			if (userDefaultGroupName != null && userDefaultGroupName.equalsIgnoreCase(group.getName())) {
				row.put("isdefault", true);
			} else {
				row.put("isdefault", false);
			}
			jsonGroupList.put(row);
		}
		return jsonGroupList;
	}

	public static JSONArray serializeGroupList(final boolean onlyActive, final String type) throws JSONException {
		final JSONArray jsonGroups = new JSONArray();
		Iterable<GroupCard> list = new LinkedList<GroupCard>();

		if (onlyActive) {
			list = GroupCard.allActive();
		} else {
			list = GroupCard.all();
		}

		for (final GroupCard group : list) {
			final JSONObject jsonGroup = new JSONObject();
			jsonGroup.put("id", group.getId());
			jsonGroup.put("text", group.getDescription());
			jsonGroup.put("leaf", true);
			jsonGroup.put("selectable", true);
			jsonGroup.put("type", type);

			jsonGroups.put(jsonGroup);
		}
		return jsonGroups;
	}

	public static JSONObject serializePrivilege(final PrivilegeInfo privilege) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put("groupId", privilege.getGroupId());
		if (privilege.mode.equals("w")) {
			row.put("privilege_mode", "write_privilege");
			row.put("write_privilege", true);
		} else if (privilege.mode.equals("r")) {
			row.put("privilege_mode", "read_privilege");
			row.put("read_privilege", true);
		} else {
			row.put("privilege_mode", "none_privilege");
			row.put("none_privilege", true);
		}
		row.put("classname", privilege.getPrivilegedObjectName());
		row.put("classid", privilege.getPrivilegeObjectId());
		return row;
	}

	public static JSONArray serializePrivilegeList(final List<PrivilegeInfo> privileges) throws JSONException {
		final JSONArray privilegeList = new JSONArray();
		for (final PrivilegeInfo privilege : privileges) {
			try {
				privilegeList.put(Serializer.serializePrivilege(privilege));
			} catch (final NotFoundException e) {
				Log.PERSISTENCE.warn("Class OID not found (" + privilege.getPrivilegeObjectId()
						+ ") while searching for grant for group " + privilege.getGroupId());
			}
		}
		return privilegeList;
	}

	public static JSONObject serialize(final CMUser user) throws JSONException {
		final JSONObject row = new JSONObject();
		row.put("userid", user.getId());
		row.put("username", user.getName());
		row.put("description", user.getDescription());
		row.put("email", user.getEmail());
		row.put("isactive", user.isActive());
		return row;
	}

	public static JSONArray serializeUsers(final List<CMUser> users) throws JSONException {
		final JSONArray userList = new JSONArray();
		for (final CMUser user : users) {
			userList.put(Serializer.serialize(user));
		}
		return userList;
	}

	public static JSONArray serializeMenuList(final Iterable<MenuCard> menuList, final UserContext userCtx,
			final Set<Integer> availableReports) throws JSONException {
		final JSONArray jsonMenuList = new JSONArray();

		for (final MenuCard menu : menuList) {
			boolean isFolder = true;
			final JSONObject jsonMenu = new JSONObject();

			if (menu.getCode() != null) {
				isFolder = menu.getCode().equals(MenuCodeType.FOLDER.getCodeType());
				if (menu.isReport()) {
					if (availableReports != null && !availableReports.contains(menu.getElementObjId())) {
						continue;
					}
				} else {
					try { // Ugly but I can't fix every design mistake right now
						final ITable menuEntryClass = UserOperations.from(UserContext.systemContext()).tables()
								.get(menu.getElementClassId());
						final PrivilegeType privileges = userCtx.privileges().getPrivilege(menuEntryClass);
						if (PrivilegeType.NONE.equals(privileges))
							continue; // Exits for the outer loop
						final MenuType menuType = menu.getTypeEnum();
						final boolean writePriv = PrivilegeType.WRITE.equals(privileges);
						jsonMenu.put("priv_write", writePriv);
						jsonMenu.put("priv_create", writePriv && !MenuType.SUPERCLASS.equals(menuType));
						jsonMenu.put("superclass", menuEntryClass.isSuperClass());
					} catch (final Exception e) {
						// Who cares if it fails
					}
				}
				jsonMenu.put("type", menu.getCode().toLowerCase());
				jsonMenu.put("subtype", menu.getType().toLowerCase());
				jsonMenu.put("text", menu.getDescription());
			} else {
				jsonMenu.put("type", MenuCodeType.CLASS.getCodeType());
				jsonMenu.put("subtype", MenuCodeType.CLASS.getCodeType());
				jsonMenu.put("text", menu.getDescription());
			}
			if (menu.isReport()) {
				jsonMenu.put("objid", menu.getElementObjId());
			}

			if (menu.getElementClassId() != 0) {
				if (menu.isReport()) {
					/**
					 * must be unique - and for report ElementClassId is always
					 * "Report" and there are two ElementObjId for each report
					 */
					jsonMenu.put("id", menu.getElementObjId() + menu.getCode());
				} else {
					jsonMenu.put("id", menu.getElementClassId());
				}
			}
			if (!jsonMenu.has("id")) { // this should be for folders
				jsonMenu.put("id", menu.getId());
			}

			if (menu.getParentId() > 0) {
				jsonMenu.put("parent", menu.getParentId());
			}

			jsonMenu.put("cmIndex", menu.getNumber());
			jsonMenu.put("leaf", !isFolder);
			jsonMenu.put("selectable", !isFolder);
			jsonMenuList.put(jsonMenu);
		}

		return jsonMenuList;
	}

	public static JSONObject serializeProcessAttributeHistory(final ICard card, final CardQuery cardQuery)
			throws JSONException {
		final JsonProcessAttributeHistoryFormatter formatter = new JsonProcessAttributeHistoryFormatter();
		formatter.addCard(card);
		for (final ICard historyCard : cardQuery) {
			final String processCode = historyCard.getCode();
			if (processCode != null && processCode.length() != 0) {
				formatter.addCard(historyCard);
			}
		}
		final JSONObject jsonResponse = new JSONObject();
		jsonResponse.put("rows", formatter.toJson());
		return jsonResponse;
	}

	public static void serializeCardAttributeHistory(final ICard card, final CardQuery cardQuery,
			final JSONObject jsonOutput) throws JSONException {
		final JsonCardAttributeHistoryFormatter formatter = new JsonCardAttributeHistoryFormatter();
		formatter.addCard(card);
		for (final ICard historyCard : cardQuery) {
			formatter.addCard(historyCard);
		}
		final JSONArray rows = jsonOutput.getJSONArray("rows");
		formatter.addJsonHistoryItems(rows);
	}

	private static class CardHistoryItem implements HistoryItem {
		protected ICard card;

		public CardHistoryItem(final ICard card) {
			this.card = card;
		}

		@Override
		public Long getId() {
			return Long.valueOf(card.getId());
		}

		@Override
		public long getInstant() {
			return card.getBeginDate().getTime();
		}

		@Override
		public Map<String, ValueAndDescription> getAttributes() {
			final Map<String, ValueAndDescription> map = new HashMap<String, ValueAndDescription>();
			for (final IAttribute attr : card.getSchema().getAttributes().values()) {
				if (attr.isDisplayable()) {
					final String name = attr.getName();
					final String description = attr.getDescription();
					final Object value = attr.valueToString(card.getValue(name));
					map.put(name, new ValueAndDescription(value, description));
				}
			}
			return map;
		}

		@Override
		public Map<String, Object> getExtraAttributes() {
			final Map<String, Object> map = new HashMap<String, Object>();
			map.put("_AttrHist", true);
			map.put("User", card.getUser());
			map.put("Code", card.getCode());
			map.put("BeginDate", card.getAttributeValue("BeginDate").toString());

			final Date endDateForSorting;
			if (card.getSchema().getAttributes().containsKey("EndDate")) {
				final AttributeValue endDateAttrVal = card.getAttributeValue("EndDate");
				map.put("EndDate", endDateAttrVal.toString());
				endDateForSorting = endDateAttrVal.getDate();
			} else {
				// Skip EndDate if not in history, but add a fake end date for
				// sorting
				endDateForSorting = new Date();
			}
			map.put("_EndDate", endDateForSorting.getTime());
			return map;
		}

		@Override
		public boolean isInOutput() {
			return true;
		}
	}

	private static class ProcessHistoryItem extends CardHistoryItem {
		private ICard previousCard = null;

		/**
		 * 
		 * @param card
		 *            the card that you want to extract the history
		 * @param previousCard
		 *            the previous card in the cycle, the more recent
		 */
		public ProcessHistoryItem(final ICard card, final ICard previousCard) {
			super(card);
			this.previousCard = previousCard;
		}

		@Override
		public Map<String, Object> getExtraAttributes() {
			final Map<String, Object> map = super.getExtraAttributes();

			// Add the performer
			if (previousCard != null) {
				final String[] currentActivities = getActivityInstanceIds(card);
				final String[] previousActivities = getActivityInstanceIds(previousCard);

				for (int i = 0; i < currentActivities.length; ++i) {
					final String id = currentActivities[i];
					if (ArrayUtils.contains(previousActivities, id)) {
						continue;
					} else {
						final String[] performers = getActivityInstancePerformers(card);
						map.put("Executor", performers[i]);
						break;
					}
				}
			}

			return map;
		}

		private String[] getActivityInstanceIds(final ICard card) {
			return card.getAttributeValue(ProcessAttributes.ActivityInstanceId.dbColumnName()).getStringArrayValue();
		}

		private String[] getActivityInstancePerformers(final ICard card) {
			return card.getAttributeValue(ProcessAttributes.CurrentActivityPerformers.dbColumnName())
					.getStringArrayValue();
		}
	}

	private static class JsonCardAttributeHistoryFormatter extends JsonHistory {
		public void addCard(final ICard card) {
			addHistoryItem(new CardHistoryItem(card));
		}
	}

	private static class JsonProcessAttributeHistoryFormatter extends JsonHistory {
		private ICard previousCard = null;

		public void addCard(final ICard card) {
			addHistoryItem(new ProcessHistoryItem(card, previousCard));
			previousCard = card;
		}
	}

	public static JSONObject serializeActivityIds(final ActivityIdentifier ai, final ICard processCard)
			throws JSONException {
		final JSONObject out = new JSONObject();
		out.put("Id", processCard.getId());
		out.put("IdClass", processCard.getIdClass());
		out.put("ProcessInstanceId", ai.getProcessInstanceId());
		out.put("WorkItemId", ai.getWorkItemId());
		return out;
	}

	public static void addAttachmentsData(final JSONObject jsonTable, final ITable table, final DmsLogic dmsLogic)
			throws JSONException {
		if (!DmsProperties.getInstance().isEnabled()) {
			return;
		}
		final Map<String, Map<String, String>> rulesByGroup = rulesByGroup(table, dmsLogic);

		final JSONObject jsonGroups = new JSONObject();
		for (final String groupName : rulesByGroup.keySet()) {
			jsonGroups.put(groupName, rulesByGroup.get(groupName));
		}

		final JSONObject jsonAutocompletion = new JSONObject();
		jsonAutocompletion.put("autocompletion", jsonGroups);

		final JSONObject jsonMeta = (JSONObject) jsonTable.get("meta");
		jsonMeta.put("attachments", jsonAutocompletion);
	}

	private static Map<String, Map<String, String>> rulesByGroup(final ITable table, final DmsLogic dmsLogic) {
		try {
			return dmsLogic.getAutoCompletionRulesByClass(table.getName());
		} catch (final DmsException e) {
			RequestListener.getCurrentRequest().pushWarning(e);
			return Collections.emptyMap();
		}
	}

}
