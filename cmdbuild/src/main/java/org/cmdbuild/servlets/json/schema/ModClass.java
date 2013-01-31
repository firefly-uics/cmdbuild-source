package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.services.meta.MetadataService;
import org.cmdbuild.services.store.DBClassWidgetStore;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer.JsonModeMapper;
import org.cmdbuild.servlets.json.serializers.ClassSerializer;
import org.cmdbuild.servlets.json.serializers.DomainSerializer;
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

import com.google.common.collect.Lists;

public class ModClass extends JSONBase {

	private static final Long SIMPLE_TABLE_HAVE_NO_PARENT = null;

	/* =========================================================
	 * CLASSES
	 =========================================================== */

	/**
	 * Return a JSONObject with the a field "classes"
	 * that is a JSONArray with the serialization of all
	 * the classes that the user could read
	 * (standard classes, simple tables and processes)
	 * 
	 * @param active
	 * @param userCtx
	 * @return {@link JSONObject}
	 * @throws JSONException
	 * @throws AuthException
	 * @throws CMWorkflowException
	 */
	@OldDao
	@Legacy("")
	@JSONExported
	public JSONObject getAllClasses(
			@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean active,
			final UserContext userCtx) throws JSONException, AuthException,
			CMWorkflowException {

		final JSONObject out = new JSONObject();
		final Iterable<ITable> allTables = UserOperations.from(userCtx).tables().list();
		final Iterable<UserProcessClass> processClasses = workflowLogic(userCtx).findAllProcessClasses();
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

			final JSONObject jsonTable = ClassSerializer.toClient(table);
			// TODO ugly pass the logic to the serializer...
			Serializer.addAttachmentsData(jsonTable, table, applicationContext.getBean(DmsLogic.class));
			out.append("classes", jsonTable);
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
					final JSONObject jsonTable = ClassSerializer.toClient(table, pc);
					// TODO ugly pass the logic to the serializer...
					Serializer.addAttachmentsData(jsonTable, table, applicationContext.getBean(DmsLogic.class));
					out.append("classes", jsonTable);
				}
			}
		}

		return out;
	}

	@JSONExported
	public JSONObject saveTable( //
			@Parameter(PARAMETER_NAME) final String name, //
			@Parameter("description") final String description, //
			@Parameter(value = "inherits", required = false) final int idParent, //
			@Parameter(value = "superclass", required = false) final boolean isSuperClass, //
			@Parameter(value = "isprocess", required = false) final boolean isProcess, //
			@Parameter(value = "tableType", required = false) final String tableType, //
			@Parameter(PARAMETER_ACTIVE) final boolean isActive, //
			@Parameter("userstoppable") final boolean isProcessUserStoppable //
	) throws JSONException, CMDBException {
		// TODO define "simpletable" elsewhere
		final boolean isSimpleTable = "simpletable".equals(tableType);
		final Class clazz = Class.newClass() //
				.withName(name) //
				.withDescription(description) //
				.withParent(isSimpleTable ? SIMPLE_TABLE_HAVE_NO_PARENT : Long.valueOf(idParent)) //
				.thatIsSuperClass(isSimpleTable ? false : isSuperClass) //
				.thatIsProcess(isSimpleTable ? false : isProcess) //
				.thatIsUserStoppable(isSimpleTable ? false : isProcessUserStoppable) //
				.thatIsHoldingHistory(!isSimpleTable) //
				.thatIsActive(isActive) //
				.build();
		final CMClass cmClass = dataDefinitionLogic().createOrUpdate(clazz);
		return ClassSerializer.toClient(cmClass, "table");
	}

	@OldDao
	@JSONExported
	public JSONObject deleteTable( //
			final JSONObject serializer, //
			final ITable table) throws JSONException, CMDBException {
		final Class clazz = Class.newClass() //
				.withName(table.getName()) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(clazz);
		return serializer;
	}

	/* =========================================================
	 * ATTRIBUTES
	 =========================================================== */

	@JSONExported
	public JSONObject getAttributeList(
			@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean onlyActive, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className)
			throws JSONException, AuthException {

		JSONObject out = new JSONObject();

		Iterable<? extends CMAttribute> attributesForClass;
		final DataAccessLogic dataLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		if (onlyActive) {
			attributesForClass = dataLogic.findClassByName(className).getAttributes();
		} else {
			attributesForClass = dataLogic.findClassByName(className).getAllAttributes();
		}

		out.put(SERIALIZATION_ATTRIBUTES, AttributeSerializer.toClient(attributesForClass, onlyActive));
		return out;
	}

	@OldDao
	@JSONExported
	public JSONObject saveOrderCriteria( //
			@Parameter(value = "records") final JSONObject orderCriteria, //
			final JSONObject serializer, //
			final ITable table //
	) throws JSONException, AuthException {
		final List<ClassOrder> classOrders = Lists.newArrayList();
		@SuppressWarnings("rawtypes")
		final Iterator keysIterator = orderCriteria.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			classOrders.add(ClassOrder.from(key, orderCriteria.getInt(key)));
		}
		dataDefinitionLogic().changeClassOrders(table.getDBName(), classOrders);
		return serializer;
	}

	@OldDao
	@JSONExported
	public JSONObject getAttributeTypes( //
			@Parameter(PARAMETER_TABLE_TYPE) final String tableTypeStirng) //
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);
		final List<AttributeType> types = new LinkedList<AttributeType>();

		for (final AttributeType type : tableType.getAvaiableAttributeList()) {
			if (!type.isReserved()) {
				types.add(type);
			}
		}

		out.put(SERIALIZATION_ATTRIBUTE_TYPES, AttributeSerializer.toClient(types));
		return out;
	}

	@JSONExported
	public JSONObject getFieldModes(final JSONObject serializer) throws JSONException, AuthException {
		for (final JsonModeMapper fieldModeMapper : JsonModeMapper.values()) {
			final JSONObject jo = new JSONObject();
			jo.put("fieldmode_value", getTraslation("administration.modClass.attributeProperties.field_"
					+ fieldModeMapper.getText()));
			jo.put("fieldmode", fieldModeMapper.getText());
			serializer.append("modes", jo);
		}
		return serializer;
	}

	// TODO AUTHORIZATION ON ATTRIBUTES IS NEVER CHECKED!
	@JSONExported
	public JSONObject saveAttribute( //
			final JSONObject serializer, //
			@Parameter(value = PARAMETER_NAME, required = false) final String name, //
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
			@Parameter(value = "idDomain", required = false) final String domainName, //
			@Parameter(value = "fieldFilter", required = false) final String fieldFilter, //
			@Parameter(value = "fkDestination", required = false) final int fkDestinationId, //
			@Parameter(value = "group", required = false) final String group, //
			@Parameter(value = "meta", required = false) final JSONObject meta, //
			@Parameter(value = "editorType", required = false) final String editorType, //
			@Parameter(value = "tableId") final Long tableId) throws JSONException, CMDBException {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwner(tableId) //
				.withDescription(description) //
				.withGroup(group) //
				.withType(attributeTypeString) //
				.withLength(length) //
				.withPrecision(precision) //
				.withScale(scale) //
				.withLookupType(lookupType) //
				.withDomain(domainName) //
				// ...
				.withDefaultValue(defaultValue) //
				.withMode(JsonModeMapper.modeFrom(fieldMode)) //
				.withEditorType(editorType) //
				.thatIsDisplayableInList(isBaseDSP) //
				.thatIsMandatory(isNotNull) //
				.thatIsUnique(isUnique) //
				.thatIsActive(isActive) //
				// @Parameter(value = "fieldFilter", required = false) String
				// fieldFilter, //
				// @Parameter(value = "fkDestination", required = false) int
				// fkDestinationId, //
				// @Parameter(value = "meta", required = false) JSONObject meta,
				.build();
		final CMAttribute cmAttribute = dataDefinitionLogic().createOrUpdate(attribute);
		final JSONObject result = AttributeSerializer.toClient(cmAttribute);
		serializer.put("attribute", result);
		return serializer;
	}


	@OldDao
	@JSONExported
	public void deleteAttribute( //
			@Parameter(PARAMETER_NAME) final String attributeName, //
			final BaseSchema table //
	) throws JSONException {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwner(Long.valueOf(table.getId())) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(attribute);
	}

	@OldDao
	@JSONExported
	public JSONObject reorderAttribute( //
			final JSONObject serializer, //
			@Parameter("attributes") final String jsonAttributeList, //
			final BaseSchema baseSchema //
	) throws JSONException, CMDBException {
		final List<Attribute> attributes = Lists.newArrayList();
		final JSONArray jsonAttributes = new JSONArray(jsonAttributeList);
		for (int i = 0; i < jsonAttributes.length(); i++) {
			final JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			attributes.add(Attribute.newAttribute().withOwner(Long.valueOf(baseSchema.getId())) //
					.withName(jsonAttribute.getString(PARAMETER_NAME)) //
					.withIndex(jsonAttribute.getInt("idx")).build());
		}
		for (final Attribute attribute : attributes) {
			dataDefinitionLogic().reorder(attribute);
		}
		return serializer;
	}

	/* =========================================================
	 * DOMAIN
	 =========================================================== */

	@OldDao
	@JSONExported
	public JSONObject getAllDomains(
			@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean activeOnly,
			final UserContext userCtx) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final Iterable<IDomain> allDomains = UserOperations.from(userCtx).domains().list();
		for (final IDomain domain : allDomains) {
			if (domain.getMode().isCustom()
					&& (!activeOnly || isActiveWithActiveClasses(domain, workflowLogic(userCtx)))) {
				out.append("domains", DomainSerializer.toClient(domain, activeOnly));
			}
		}

		return out;
	}

	@Admin
	@JSONExported
	public JSONObject saveDomain( //
			@Parameter(value = PARAMETER_NAME, required = false) final String domainName, //
			@Parameter(value = "idClass1", required = false) final int classId1, //
			@Parameter(value = "idClass2", required = false) final int classId2, //
			@Parameter("description") final String description, //
			@Parameter(value = "cardinality", required = false) final String cardinality, //
			@Parameter("descr_1") final String descriptionDirect, //
			@Parameter("descr_2") final String descriptionInverse, //
			@Parameter("isMasterDetail") final boolean isMasterDetail, //
			@Parameter(value = "md_label", required = false) final String mdLabel, //
			@Parameter("active") final boolean isActive //
	) throws JSONException, AuthException, NotFoundException {
		final Domain domain = Domain.newDomain() //
				.withName(domainName) //
				.withIdClass1(classId1) //
				.withIdClass2(classId2) //
				.withDescription(description) //
				.withCardinality(cardinality) //
				.withDirectDescription(descriptionDirect) //
				.withInverseDescription(descriptionInverse) //
				.thatIsMasterDetail(isMasterDetail) //
				.withMasterDetailDescription(mdLabel) //
				.thatIsActive(isActive) //
				.build();
		final CMDomain createdOrUpdated = dataDefinitionLogic().createOrUpdate(domain);
		return DomainSerializer.toClient(createdOrUpdated, false, "domain");
	}

	@OldDao
	@JSONExported
	public void deleteDomain( //
			final IDomain domain //
	) throws JSONException {
		dataDefinitionLogic().deleteDomainByName(domain.getName());
	}

	@Admin
	@JSONExported
	public JSONObject getDomainList(@Parameter(value = "idClass") final Long classId, //
			final JSONObject serializer) throws JSONException {
		final JSONArray rows = new JSONArray();
		final DataAccessLogic dataAccesslogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final List<CMDomain> domainsForSpecifiedClass = dataAccesslogic.findDomainsForClassWithId(classId);
		for (final CMDomain domain : domainsForSpecifiedClass) {
			rows.put(DomainSerializer.toClient(domain, classId));
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@OldDao
	@JSONExported
	public JSONArray getFKTargetingClass(final ITable table) throws JSONException,
			CMDBException {
		final JSONArray fk = new JSONArray();
		for (final IAttribute attribute : table.fkDetails()) {
			if (attribute.getMode().isDisplayable()) {
				fk.put(AttributeSerializer.toClient(attribute));
			}
		}
		return fk;
	}

	@OldDao
	@Admin
	@JSONExported
	public JSONObject getReferenceableDomainList(final ITable table,
			final DomainFactory df, final ITableFactory tf) throws JSONException {
		JSONObject out = new JSONObject();
		for (final IDomain domain : df.list(table).inherited()) {
			final String cardinality = domain.getCardinality();
			final String class1 = domain.getTables()[0].getName();
			final String class2 = domain.getTables()[1].getName();
			final Collection<String> classWithAncestor = tf.fullTree().path(table.getName());
			if ((cardinality.equals(IDomain.CARDINALITY_1N) && classWithAncestor.contains(class2))
					|| (cardinality.equals(IDomain.CARDINALITY_N1) && classWithAncestor.contains(class1))) {
				out.append("rows", (DomainSerializer.toClient(domain, false)));
			}
		}

		return out;
	}

	/* =========================================================
	 * WIDGET
	 =========================================================== */

	// FIXME: why success false? fix it
		@OldDao
		@JSONExported
		public JsonResponse getAllWidgets(@Parameter(value = "active", required = false) final boolean active,
				final UserContext userCtx) {
			final Iterable<ITable> allTables = UserOperations.from(userCtx).tables().list();
			final Map<String, List<Widget>> allWidgets = new HashMap<String, List<Widget>>();
			for (final ITable table : allTables) {
				if (!table.getMode().isDisplayable()) {
					continue;
				}
				if (active && !isActive(table, workflowLogic(userCtx))) {
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

		@OldDao
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

		@OldDao
		@Admin
		@JSONExported
		public void removeWidgetDefinition(final ITable table, // className
				@Parameter("id") final String widgetId, final UserContext userCtx) {
			final DBClassWidgetStore classWidgets = new DBClassWidgetStore(table);
			classWidgets.removeWidget(widgetId);
		}

	/* =========================================================
	 * PRIVATE
	 =========================================================== */

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

	// FIXME I think that the meta data was handled when save
	// a reference attribute
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

	private DataDefinitionLogic dataDefinitionLogic() {
		return TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic();
	}

	private WorkflowLogic workflowLogic(final UserContext userContext) {
		return TemporaryObjectsBeforeSpringDI.getWorkflowLogic(userContext);
	}

}
