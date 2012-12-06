package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMAttribute.Mode;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.SchemaStatus;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.store.DBClassWidgetStore;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModClass extends JSONBase {

	private static final Long SIMPLE_TABLE_HAVE_NO_PARENT = null;

	// NdPaolo: Is this still needed?
	@JSONExported
	public JSONArray tree(@Parameter(value = "active", required = false) final boolean active, final ITableFactory tf)
			throws JSONException, AuthException {
		final TableTree tree = tf.fullTree().displayable();
		if (active) {
			tree.active();
		}
		final JSONObject treeRoot = Serializer.serializeTableTree(tree.exclude(ProcessType.BaseTable).getRootElement());
		final JSONArray JSONTree = new JSONArray();
		JSONTree.put(treeRoot);
		return JSONTree;
	}

	@JSONExported
	public JSONArray getSimpleTablesTree(@Parameter(value = "active", required = false) final boolean active,
			final ITableFactory tf) throws JSONException, AuthException {
		final JSONArray tableList = new JSONArray();
		for (final ITable t : tf.list()) {
			if (t.getTableType() == CMTableType.SIMPLECLASS && t.getMode().isDisplayable()
					&& (!active || t.getStatus().isActive())) {
				final JSONObject jt = Serializer.serializeTable(t);
				if (jt != null) {
					tableList.put(jt);
				}
			}
		}
		return tableList;
	}

	@JSONExported
	public JSONObject getSuperClasses(final JSONObject serializer, final ITableFactory tf) throws JSONException,
			AuthException {
		for (final ITable table : tf.fullTree().superclasses().active().exclude(ProcessType.BaseTable)) {
			final JSONObject element = new JSONObject();
			element.put("value", table.getId());
			element.put("description", table.getDescription());
			element.put("classname", table.getDBName());
			serializer.append("superclasses", element);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getProcessSuperClasses(final JSONObject serializer, final ITableFactory tf) throws JSONException,
			AuthException {
		for (final ITable table : tf.fullTree().superclasses().active().branch(ProcessType.BaseTable)) {
			final JSONObject element = new JSONObject();
			element.put("value", table.getId());
			element.put("description", table.getDescription());
			element.put("classname", table.getDBName());
			serializer.append("superclasses", element);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getAllClasses(final JSONObject serializer,
			@Parameter(value = "active", required = false) final boolean active, final UserContext userCtx)
			throws JSONException, AuthException, CMWorkflowException {

		final WorkflowLogic workflowLogic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final Iterable<ITable> allTables = UserOperations.from(userCtx).tables().list();
		final Iterable<UserProcessClass> processClasses = workflowLogic.findAllProcessClasses();
		final HashMap<String, ITable> processTables = new HashMap<String, ITable>();

		for (final ITable table : allTables) {
			// Skip the table not displayable and
			// the ones not active if only active is required
			if (!table.getMode().isDisplayable() || (active && !table.getStatus().isActive())) {

				continue;
			}

			// Skip here the processes. Serialize them after,
			// using the workflow logic to retrieve them
			if (table.isActivity()) {
				processTables.put(table.getName(), table);
				continue;
			}

			final JSONObject jsonTable = Serializer.serializeTable(table);
			Serializer.addAttachmentsData(jsonTable, table, applicationContext.getBean(DmsLogic.class));
			serializer.append("classes", jsonTable);
		}

		// add the processes
		for (final UserProcessClass pc : processClasses) {
			if (active && !pc.isUsable() && !pc.isSuperclass()) { // serialize
																	// always
																	// the
																	// superclasses

				continue;
			} else {
				final ITable table = processTables.get(pc.getName());
				if (table != null) {
					final JSONObject jsonTable = Serializer.serializeTable(table, pc);
					Serializer.addAttachmentsData(jsonTable, table, applicationContext.getBean(DmsLogic.class));
					serializer.append("classes", jsonTable);
				}
			}
		}

		return serializer;
	}

	@JSONExported
	public JSONObject getAllDomains(final JSONObject serializer,
			@Parameter(value = "active", required = false) final boolean activeOnly, final UserContext userCtx)
			throws JSONException, AuthException {
		final WorkflowLogic workflowLogic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final Iterable<IDomain> allDomains = UserOperations.from(userCtx).domains().list();
		final JSONArray jsonDomains = new JSONArray();
		for (final IDomain domain : allDomains) {
			if (domain.getMode().isCustom() && (!activeOnly || isActiveWithActiveClasses(domain, workflowLogic))) {
				jsonDomains.put(Serializer.serializeDomain(domain, activeOnly));
			}
		}
		serializer.put("domains", jsonDomains);
		return serializer;
	}

	private boolean isActiveWithActiveClasses(final IDomain domain, final WorkflowLogic workflowLogic) {
		return domain.getStatus().isActive() && isActive(domain.getClass1(), workflowLogic)
				&& isActive(domain.getClass2(), workflowLogic);
	}

	private boolean isActive(final ITable table, final WorkflowLogic workflowLogic) {
		if (!table.getStatus().isActive()) {
			return false;
		}
		// consider a process active if is
		// in the wfcache --> have an XPDL
		if (table.isActivity() && !table.isSuperClass()) {
			return workflowLogic.isProcessUsable(table.getName());
		}
		return true;
	}

	@JSONExported
	public JSONObject getAttributeList(@Parameter(value = "active", required = false) final boolean active,
			final JSONObject serializer, final ITable table) throws JSONException, AuthException {
		serializer.put("rows", Serializer.serializeAttributeList(table, active));
		return serializer;
	}

	@JSONExported
	public JSONObject saveOrderCriteria(@Parameter(value = "records") final JSONObject orderCriteria,
			final JSONObject serializer, final ITable table) throws JSONException, AuthException {
		final Map<String, IAttribute> attributes = table.getAttributes();
		for (final String keyAttr : attributes.keySet()) {
			final IAttribute attribute = attributes.get(keyAttr);
			final String attrName = attribute.getName();
			Log.PERSISTENCE.debug(attrName);
			if (attribute.isReserved())
				continue;
			if (orderCriteria.has(attrName)) {
				attribute.setClassOrder(orderCriteria.getInt(attrName));
				attribute.save();
			} else {
				attribute.setClassOrder(0);
				attribute.save();
			}
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getAttributeTypes(final ITable table, @Parameter("tableType") final String tableTypeStirng,
			final JSONObject serializer) throws JSONException, AuthException {

		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);

		for (final AttributeType type : tableType.getAvaiableAttributeList()) {
			if (type.isReserved()) {
				continue;
			}
			final JSONObject jatv = new JSONObject();
			jatv.put("name", type.toString());
			jatv.put("value", type.toString());
			serializer.append("types", jatv);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getFieldModes(final JSONObject serializer) throws JSONException, AuthException {
		for (final JsonModeMapper fieldModeMapper : JsonModeMapper.values()) {
			final JSONObject jo = new JSONObject();
			jo.put("fieldmode_value", getTraslation("administration.modClass.attributeProperties.field_"
					+ fieldModeMapper.text));
			jo.put("fieldmode", fieldModeMapper.text);
			serializer.append("modes", jo);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject saveTable( //
			final UserContext userContext, //
			final JSONObject serializer, //
			@Parameter(value = "name", required = false) final String name, //
			@Parameter("description") final String description, //
			@Parameter(value = "inherits", required = false) final int idParent, //
			@Parameter(value = "superclass", required = false) final boolean isSuperClass, //
			@Parameter(value = "isprocess", required = false) final boolean isProcess, //
			@Parameter(value = "tableType", required = false) final String tableType, //
			@Parameter("active") final boolean isActive, //
			@Parameter("userstoppable") final boolean isProcessUserStoppable //
	) throws JSONException, CMDBException {
		// TODO define "simpletable" elsewhere
		final boolean isSimpleTable = "simpletable".equals(tableType);
		final CMDataView dataView = TemporaryObjectsBeforeSpringDI.getSystemView();
		final Class clazz = Class.newClass() //
				.withName(name) //
				.withDescription(description) //
				.withParent(isSimpleTable ? SIMPLE_TABLE_HAVE_NO_PARENT : idParent) //
				.thatIsSuperClass(isSuperClass) //
				.thatIsProcess(isProcess) //
				.thatIsUserStoppable(isProcessUserStoppable) //
				.thatIsHoldingHistory(isSimpleTable) //
				.thatIsActive(isActive) //
				.build();
		final DataDefinitionLogic ddl = TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic(userContext);
		final CMClass cmClass = ddl.createOrUpdate(clazz);
		final JSONObject result = Serializer.serialize(cmClass);
		serializer.put("table", result);
		return serializer;
	}

	@JSONExported
	public JSONObject deleteTable( //
			final UserContext userContext, //
			final JSONObject serializer, //
			final ITable table) throws JSONException, CMDBException {
		final Class clazz = Class.newClass() //
				.withName(table.getName()) //
				.build();
		final DataDefinitionLogic ddl = TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic(userContext);
		ddl.deleteOrDeactivate(clazz);
		return serializer;
	}

	// TODO move away
	public enum JsonModeMapper {

		WRITE("write", Mode.WRITE), //
		READ("read", Mode.READ), //
		HIDDEN("hidden", Mode.HIDDEN), //
		;

		private final String text;
		private final Mode mode;

		private JsonModeMapper(final String text, final Mode mode) {
			this.text = text;
			this.mode = mode;
		}

		public static Mode modeFrom(final String text) {
			for (final JsonModeMapper mapper : values()) {
				if (mapper.text.equals(text)) {
					return mapper.mode;
				}
			}
			return Mode.WRITE;
		}

		public static String textFrom(final Mode mode) {
			for (final JsonModeMapper mapper : values()) {
				if (mapper.mode.equals(mode)) {
					return mapper.text;
				}
			}
			return WRITE.text;
		}

	}

	// TODO AUTHORIZATION ON ATTRIBUTES IS NEVER CHECKED!
	@JSONExported
	public JSONObject saveAttribute( //
			final UserContext userContext, //
			final JSONObject serializer, //
			@Parameter(value = "name", required = false) final String name, //
			@Parameter(value = "type", required = false) final String attributeTypeString, //
			@Parameter("description") final String description, //
			@Parameter(value = "defaultvalue", required = false) final String defaultValue, //
			@Parameter("isbasedsp") final boolean isBaseDSP, //
			@Parameter("isnotnull") final boolean isNotNull, //
			@Parameter("isunique") final boolean isUnique, //
			@Parameter("isactive") final boolean isActive, //
			@Parameter("fieldmode") final String fieldMode, //
			@Parameter(value = "len", required = false) final int length, //
			@Parameter(value = "precision", required = false) final int precision, //
			@Parameter(value = "scale", required = false) final int scale, //
			@Parameter(value = "lookup", required = false) final String lookupType, //
			@Parameter(value = "idDomain", required = false) final int domainId, //
			@Parameter(value = "fieldFilter", required = false) final String fieldFilter, //
			@Parameter(value = "fkDestination", required = false) final int fkDestinationId, //
			@Parameter(value = "group", required = false) final String group, //
			@Parameter(value = "meta", required = false) final JSONObject meta, //
			@Parameter(value = "editorType", required = false) final String editorType, //
			final BaseSchema table //
	) throws JSONException, CMDBException {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwner(Long.valueOf(table.getId())) //
				.withDescription(description) //
				.withGroup(group) //
				.withType(attributeTypeString) //
				.withLength(length) //
				.withPrecision(precision) //
				.withScale(scale) //
				.withLookupType(lookupType) //
				// ...
				.withDefaultValue(defaultValue) //
				.withMode(JsonModeMapper.modeFrom(fieldMode)) //
				.thatIsDisplayableInList(isBaseDSP) //
				.thatIsMandatory(isNotNull) //
				.thatIsUnique(isUnique) //
				.thatIsActive(isActive) //
				// @Parameter(value = "idDomain", required = false) int
				// domainId, //
				// @Parameter(value = "fieldFilter", required = false) String
				// fieldFilter, //
				// @Parameter(value = "fkDestination", required = false) int
				// fkDestinationId, //
				// @Parameter(value = "meta", required = false) JSONObject meta,
				// //
				// @Parameter(value = "editorType", required = false) String
				// editorType, //
				.build();
		final DataDefinitionLogic ddl = TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic(userContext);
		final CMAttribute cmAttribute = ddl.createOrUpdate(attribute);
		final JSONObject result = Serializer.serialize(cmAttribute);
		serializer.put("attribute", result);
		return serializer;
	}

	enum MetaStatus {
		DELETED, MODIFIED, NEW, NOT_MODIFIED // notmodified?
	}

	private void manageMetaData(final JSONObject metaInRequest, final IAttribute attribute, final BaseSchema table)
			throws JSONException {
		final Iterator<?> keyRequest = metaInRequest.keys();
		while (keyRequest.hasNext()) {
			final String metaName = (String) keyRequest.next();
			final JSONObject metaInfo = metaInRequest.getJSONObject(metaName);
			final MetaStatus metaStatus = MetaStatus.valueOf(metaInfo.getString("status"));
			final String metaValue = metaInfo.getString("value");
			switch (metaStatus) {
			case DELETED:
				MetadataService.deleteMetadata(attribute, metaName);
				break;
			case MODIFIED:
			case NEW:
				MetadataService.updateMetadata(attribute, metaName, metaValue);
				break;
			case NOT_MODIFIED:
			}
		}
	}

	@JSONExported
	public JSONObject deleteAttribute( //
			final UserContext userContext, //
			final JSONObject serializer, //
			@Parameter("name") final String attributeName, //
			final BaseSchema table //
	) throws JSONException {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwner(Long.valueOf(table.getId())) //
				.build();
		final DataDefinitionLogic ddl = TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic(userContext);
		ddl.deleteOrDeactivate(attribute);
		return serializer;
	}

	@JSONExported
	public JSONObject reorderAttribute(final JSONObject serializer,
			@Parameter("attributes") final String jsonAttributeList, final BaseSchema baseSchema) throws JSONException,
			CMDBException {
		final JSONArray attributeList = new JSONArray(jsonAttributeList);
		final Map<String, Integer> attributePositions = new HashMap<String, Integer>();
		for (int i = 0; i < attributeList.length(); ++i) {
			final JSONObject jattr = attributeList.getJSONObject(i);
			final String attrName = jattr.getString("name");
			final int attrIdx = jattr.getInt("idx");
			attributePositions.put(attrName, attrIdx);
		}
		for (final String name : attributePositions.keySet()) {
			final int index = attributePositions.get(name);
			final IAttribute attribute = baseSchema.getAttribute(name);
			attribute.setIndex(index);
			attribute.save();
		}
		return serializer;
	}

	@Admin
	@JSONExported
	public JSONObject saveDomain(final IDomain domain, final JSONObject serializer,
			@Parameter(value = "name", required = false) final String domainName,
			@Parameter(value = "idClass1", required = false) final int classId1,
			@Parameter(value = "idClass2", required = false) final int classId2,
			@Parameter("description") final String description,
			@Parameter(value = "cardinality", required = false) final String cardinality,
			@Parameter("descr_1") final String descriptionDirect,
			@Parameter("descr_2") final String descriptionInverse,
			@Parameter("isMasterDetail") final boolean isMasterDetail,
			@Parameter(value = "md_label", required = false) final String mdLabel,
			@Parameter("active") final boolean isActive) throws JSONException, AuthException, NotFoundException {
		if (domain.isNew()) {
			domain.setClass1(UserOperations.from(UserContext.systemContext()).tables().get(classId1));
			domain.setClass2(UserOperations.from(UserContext.systemContext()).tables().get(classId2));
			domain.setName(domainName);
			if (cardinality != null && !cardinality.equals("")) {
				domain.setCardinality(cardinality);
			}
		}
		domain.setDescription(description);
		domain.setDescriptionDirect(descriptionDirect);
		domain.setDescriptionInverse(descriptionInverse);
		domain.setMasterDetail(isMasterDetail);
		domain.setMDLabel(mdLabel);
		domain.setStatus(SchemaStatus.fromBoolean(isActive));
		domain.save();

		serializer.put("domain", Serializer.serializeDomain(domain, false));
		return serializer;
	}

	@JSONExported
	public void deleteDomain(final IDomain domain) throws JSONException {

		boolean hasReference = false;
		final String cardinality = domain.getCardinality();
		if (cardinality.equals(IDomain.CARDINALITY_11) || cardinality.equals(IDomain.CARDINALITY_1N)) {
			final ITable table = domain.getClass2();
			hasReference = searchReference(table, domain);
		}
		if (!hasReference && (cardinality.equals(IDomain.CARDINALITY_11) || cardinality.equals(IDomain.CARDINALITY_N1))) {
			final ITable table = domain.getClass1();
			hasReference = searchReference(table, domain);
		}
		if (hasReference) {
			throw ORMExceptionType.ORM_DOMAIN_HAS_REFERENCE.createException();
		} else {
			domain.delete();
		}
	}

	private static boolean searchReference(final ITable table, final IDomain domain) {
		final Map<String, IAttribute> attributes = table.getAttributes();
		for (final String attrName : attributes.keySet()) {
			final IAttribute attribute = attributes.get(attrName);
			final IDomain attributeDom = attribute.getReferenceDomain();
			if (attributeDom != null && (attributeDom.getName()).equals(domain.getName())) {
				return true;
			}
		}
		return false;
	}

	@Admin
	@JSONExported
	public JSONObject getDomainList(@Parameter("WithSuperclasses") final boolean withSuperclasses,
			final JSONObject serializer, final ITable table, final DomainFactory df, final ITableFactory tf)
			throws JSONException {
		final JSONArray rows = new JSONArray();
		for (final IDomain domain : df.list(table).inherited()) {
			if (domain.getMode().isDisplayable()) {
				rows.put(Serializer.serializeDomain(domain, table));
			}
		}
		serializer.put("rows", rows);
		if (withSuperclasses) {
			serializer.put("superclasses", tf.fullTree().idPath(table.getName()));
		}
		return serializer;
	}

	@JSONExported
	public JSONArray getFKTargetingClass(final ITableFactory tf, final ITable table) throws JSONException,
			CMDBException {
		final JSONArray fk = new JSONArray();
		for (final IAttribute attribute : table.fkDetails()) {
			if (attribute.getMode().isDisplayable()) {
				fk.put(Serializer.serializeAttribute(attribute));
			}
		}
		return fk;
	}

	@Admin
	@JSONExported
	public JSONObject getReferenceableDomainList(final JSONObject serializer, final ITable table,
			final DomainFactory df, final ITableFactory tf) throws JSONException {
		final JSONArray rows = new JSONArray();
		for (final IDomain domain : df.list(table).inherited()) {
			final String cardinality = domain.getCardinality();
			final String class1 = domain.getTables()[0].getName();
			final String class2 = domain.getTables()[1].getName();
			final Collection<String> classWithAncestor = tf.fullTree().path(table.getName());
			if ((cardinality.equals(IDomain.CARDINALITY_1N) && classWithAncestor.contains(class2))
					|| (cardinality.equals(IDomain.CARDINALITY_N1) && classWithAncestor.contains(class1))) {
				rows.put(Serializer.serializeDomain(domain, false));
			}
		}
		serializer.put("rows", rows);
		return serializer;
	}

	/*
	 * Widget Definition
	 */

	@JSONExported
	public JsonResponse getAllWidgets(@Parameter(value = "active", required = false) final boolean active,
			final UserContext userCtx) {
		final WorkflowLogic workflowLogic = TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userCtx);
		final Iterable<ITable> allTables = UserOperations.from(userCtx).tables().list();
		final Map<String, List<Widget>> allWidgets = new HashMap<String, List<Widget>>();
		for (final ITable table : allTables) {
			if (!table.getMode().isDisplayable()) {
				continue;
			}
			if (active && !isActive(table, workflowLogic)) {
				continue;
			}
			final List<Widget> widgetList = new DBClassWidgetStore(table).getWidgets();
			if (widgetList.isEmpty()) {
				continue;
			}
			allWidgets.put(table.getName(), widgetList);
		}
		return JsonResponse.success(allWidgets);
	}

	@Admin
	@JSONExported
	public JsonResponse saveWidgetDefinition(final ITable table, // className
			@Parameter(value = "widget", required = true) final String jsonWidget, final UserContext userCtx)
			throws JsonParseException, JsonMappingException, IOException {

		final ObjectMapper mapper = new ObjectMapper();
		final Widget w = mapper.readValue(jsonWidget, Widget.class);

		final DBClassWidgetStore classWidgets = new DBClassWidgetStore(table);
		classWidgets.saveWidget(w);

		return JsonResponse.success(w);
	}

	@Admin
	@JSONExported
	public void removeWidgetDefinition(final ITable table, // className
			@Parameter("id") final String widgetId, final UserContext userCtx) {
		final DBClassWidgetStore classWidgets = new DBClassWidgetStore(table);
		classWidgets.removeWidget(widgetId);
	}
}
