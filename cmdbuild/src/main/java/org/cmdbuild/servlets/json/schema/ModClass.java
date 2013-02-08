package org.cmdbuild.servlets.json.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.annotations.CheckIntegration;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class ModClass extends JSONBase {

	
	@SuppressWarnings("unchecked")
	@CheckIntegration
	@JSONExported
	public JSONObject getAllClasses(@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean active)
			throws JSONException, AuthException, CMWorkflowException {
		final JSONObject out = new JSONObject();
		final Iterable<CMClass> fetchedClasses;
		if (active) {
			fetchedClasses = (Iterable<CMClass>) dataAccessLogic().findActiveClasses();
		} else {
			fetchedClasses = (Iterable<CMClass>) dataAccessLogic().findAllClasses();
		}
		// final Iterable<UserProcessClass> processClasses =
		// workflowLogic().findAllProcessClasses();
		final JSONArray serializedClasses = new JSONArray();
		for (final CMClass fetchedClass : fetchedClasses) {
			final JSONObject classObject = ClassSerializer.toClient(fetchedClass);
//			Serializer.addAttachmentsData(classObject, fetchedClass, applicationContext.getBean(DmsLogic.class));
			serializedClasses.put(classObject);
		}
		return out.put("classes", serializedClasses);
	}

	@JSONExported
	public JSONObject saveTable( //
			@Parameter(PARAMETER_NAME) final String name, //
			@Parameter(PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_INHERIT, required = false) final int idParent, //
			@Parameter(value = PARAMETER_SUPERCLASS, required = false) final boolean isSuperClass, //
			@Parameter(value = PARAMETER_IS_PROCESS, required = false) final boolean isProcess, //
			@Parameter(value = PARAMETER_TABLE_TYPE, required = false) final String tableType, //
			@Parameter(PARAMETER_ACTIVE) final boolean isActive, //
			@Parameter(PARAMETER_USER_STOPPABLE) final boolean isProcessUserStoppable //
	) throws JSONException, CMDBException {
		final Class clazz = Class.newClass() //
				.withTableType(Class.TableType.valueOf(tableType)).withName(name) //
				.withDescription(description) //
				.withParent(Long.valueOf(idParent)) //
				.thatIsSuperClass(isSuperClass) //
				.thatIsProcess(isProcess) //
				.thatIsUserStoppable(isProcessUserStoppable) //
				.thatIsActive(isActive) //
				.build();

		final CMClass cmClass = dataDefinitionLogic().createOrUpdate(clazz);
		return ClassSerializer.toClient(cmClass, SERIALIZATION_TABLE);
	}

	@JSONExported
	public void deleteTable(@Parameter(value = PARAMETER_CLASS_NAME) final String className) throws JSONException,
			CMDBException {

		final Class clazz = Class.newClass() //
				.withName(className) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(clazz);
	}

	/*
	 * ========================================================= ATTRIBUTES
	 * ===========================================================
	 */

	@JSONExported
	public JSONObject getAttributeList(@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean onlyActive, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();

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
	public void saveOrderCriteria( //
			@Parameter(value = PARAMETER_ATTRIBUTES) final JSONObject orderCriteria, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className) throws Exception {

		final ITable table = buildTable(className); // FIXME: Old Dao
		final List<ClassOrder> classOrders = Lists.newArrayList();
		final Iterator<?> keysIterator = orderCriteria.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			classOrders.add(ClassOrder.from(key, orderCriteria.getInt(key)));
		}

		dataDefinitionLogic().changeClassOrders(table.getDBName(), classOrders);
	}

	@OldDao
	@JSONExported
	public JSONObject getAttributeTypes( //
			@Parameter(PARAMETER_TABLE_TYPE) final String tableTypeStirng) //
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);
		final List<AttributeType> types = new LinkedList<AttributeType>(); // FIXME:
		// Old
		// Dao

		for (final AttributeType type : tableType.getAvaiableAttributeList()) {
			if (!type.isReserved()) {
				types.add(type);
			}
		}

		out.put(SERIALIZATION_ATTRIBUTE_TYPES, AttributeSerializer.toClient(types));
		return out;
	}

	// TODO AUTHORIZATION ON ATTRIBUTES IS NEVER CHECKED!
	@JSONExported
	public JSONObject saveAttribute( //
			final JSONObject serializer, //
			@Parameter(value = PARAMETER_NAME, required = false) final String name, //
			@Parameter(value = PARAMETER_TYPE, required = false) final String attributeTypeString, //
			@Parameter(PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_DEFAULT_VALUE, required = false) final String defaultValue, //
			@Parameter(PARAMETER_SHOW_IN_GRID) final boolean isBaseDSP, //
			@Parameter(PARAMETER_NOT_NULL) final boolean isNotNull, //
			@Parameter(PARAMETER_UNIQUE) final boolean isUnique, //
			@Parameter(PARAMETER_ACTIVE) final boolean isActive, //
			@Parameter(PARAMETER_FIELD_MODE) final String fieldMode, //
			@Parameter(value = PARAMETER_LENGTH, required = false) final int length, //
			@Parameter(value = PARAMETER_PRECISION, required = false) final int precision, //
			@Parameter(value = PARAMETER_SCALE, required = false) final int scale, //
			@Parameter(value = PARAMETER_LOOKUP, required = false) final String lookupType, //
			@Parameter(value = PARAMETER_DOMAIN_NAME, required = false) final String domainName, //
			@Parameter(value = PARAMETER_FILTER, required = false) final String fieldFilter, //
			@Parameter(value = PARAMETER_FK_DESTINATION, required = false) final int fkDestinationId, //
			@Parameter(value = PARAMETER_GROUP, required = false) final String group, //
			@Parameter(value = PARAMETER_META_DATA, required = false) final JSONObject meta, //
			@Parameter(value = PARAMETER_EDITOR_TYPE, required = false) final String editorType, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className) throws Exception {
		final ITable table = buildTable(className);
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwner(new Long(table.getId())) // FIXME if owner is managed
				// as className
				// there are no reasons to retrieve the full table
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
		serializer.put(SERIALIZATION_ATTRIBUTE, result);
		return serializer;
	}

	@OldDao
	@JSONExported
	public void deleteAttribute( //
			@Parameter(PARAMETER_NAME) final String attributeName, //
			@Parameter(PARAMETER_CLASS_NAME) final String className //
	) throws Exception {
		final ITable table = buildTable(className); // FIXME: Old Dao
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwner(Long.valueOf(table.getId())) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(attribute);
	}

	@OldDao
	@JSONExported
	public void reorderAttribute( //
			@Parameter(PARAMETER_ATTRIBUTES) final String jsonAttributeList, //
			@Parameter(PARAMETER_CLASS_NAME) final String className //
	) throws Exception {
		final List<Attribute> attributes = Lists.newArrayList();
		final JSONArray jsonAttributes = new JSONArray(jsonAttributeList);
		final ITable table = buildTable(className); // FIXME: Old Dao
		for (int i = 0; i < jsonAttributes.length(); i++) {
			final JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			attributes.add(Attribute.newAttribute().withOwner(Long.valueOf(table.getId()))//
					.withName(jsonAttribute.getString(PARAMETER_NAME)) //
					.withIndex(jsonAttribute.getInt(PARAMETER_INDEX)).build());
		}

		for (final Attribute attribute : attributes) {
			dataDefinitionLogic().reorder(attribute);
		}
	}

	/*
	 * ========================================================= DOMAIN
	 * ===========================================================
	 */

	@OldDao
	@JSONExported
	public JSONObject getAllDomains(@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean activeOnly,
			final UserContext userCtx) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final Iterable<IDomain> allDomains = UserOperations.from(userCtx).domains().list();
		for (final IDomain domain : allDomains) { // FIXME: Old Dao
			if (domain.getMode().isCustom() && (!activeOnly || isActiveWithActiveClasses(domain, workflowLogic()))) {
				out.append(SERIALIZATION_DOMAINS, DomainSerializer.toClient(domain, activeOnly));
			}
		}

		return out;
	}

	@Admin
	@JSONExported
	public JSONObject saveDomain( //
			@Parameter(value = PARAMETER_NAME, required = false) final String domainName, //
			@Parameter(value = PARAMETER_DOMAIN_FIRST_CLASS_ID, required = false) final int classId1, //
			@Parameter(value = PARAMETER_DOMAIN_SECOND_CLASS_ID, required = false) final int classId2, //
			@Parameter(PARAMETER_DESCRIPTION) final String description, //
			@Parameter(value = PARAMETER_DOMAIN_CARDINALITY, required = false) final String cardinality, //
			@Parameter(PARAMETER_DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS) final String descriptionDirect, //
			@Parameter(PARAMETER_DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS) final String descriptionInverse, //
			@Parameter(PARAMETER_DOMAIN_IS_MASTER_DETAIL) final boolean isMasterDetail, //
			@Parameter(value = PARAMETER_DOMAIN_MASTER_DETAIL_LABEL, required = false) final String mdLabel, //
			@Parameter(PARAMETER_ACTIVE) final boolean isActive //
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
		return DomainSerializer.toClient(createdOrUpdated, false, SERIALIZATION_DOMAIN);
	}

	@JSONExported
	public void deleteDomain(@Parameter(value = PARAMETER_DOMAIN_NAME, required = false) final String domainName //
	) throws JSONException {

		dataDefinitionLogic().deleteDomainByName(domainName);
	}

	@Admin
	@JSONExported
	public JSONObject getDomainList(@Parameter(PARAMETER_CLASS_NAME) final String className //
	) throws JSONException {

		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final DataAccessLogic dataAccesslogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();

		final List<CMDomain> domainsForSpecifiedClass = dataAccesslogic.findDomainsForClassWithName(className);
		for (final CMDomain domain : domainsForSpecifiedClass) {
			jsonDomains.put(DomainSerializer.toClient(domain, className));
		}

		out.put(SERIALIZATION_DOMAINS, jsonDomains);
		return out;
	}

	@OldDao
	@JSONExported
	public JSONArray getFKTargetingClass(@Parameter(PARAMETER_CLASS_NAME) final String className //
	) throws Exception {

		final JSONArray fk = new JSONArray();
		final ITable table = buildTable(className); // FIXME Old Dao
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
	public JSONObject getReferenceableDomainList(@Parameter(PARAMETER_CLASS_NAME) final String className, //
			final DomainFactory df, // FIXME Old Dao
			final ITableFactory tf // FIXME Old Dao
	) throws Exception {

		final JSONObject out = new JSONObject();
		final ITable table = buildTable(className);

		for (final IDomain domain : df.list(table).inherited()) {
			final String cardinality = domain.getCardinality();
			final String class1 = domain.getTables()[0].getName();
			final String class2 = domain.getTables()[1].getName();
			final Collection<String> classWithAncestor = tf.fullTree().path(table.getName());
			if ((cardinality.equals(IDomain.CARDINALITY_1N) && classWithAncestor.contains(class2))
					|| (cardinality.equals(IDomain.CARDINALITY_N1) && classWithAncestor.contains(class1))) {
				out.append(SERIALIZATION_DOMAINS, (DomainSerializer.toClient(domain, false)));
			}
		}

		return out;
	}

	/*
	 * ========================================================= WIDGET
	 * ===========================================================
	 */

	@OldDao
	@JSONExported
	public JsonResponse getAllWidgets(final UserContext userCtx) { // FIXME: Old
		// Dao

		final Iterable<ITable> allTables = UserOperations.from(userCtx).tables().list();
		final Map<String, List<Widget>> allWidgets = new HashMap<String, List<Widget>>();
		for (final ITable table : allTables) {
			if (!table.getMode().isDisplayable()) {
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
	public JsonResponse saveWidgetDefinition(@Parameter(PARAMETER_CLASS_NAME) final String className, //
			@Parameter(value = PARAMETER_WIDGET, required = true) final String jsonWidget, //
			final UserContext userCtx // FIXME: Old Dao
	) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final Widget w = mapper.readValue(jsonWidget, Widget.class);

		final ITable table = buildTable(className); // FIXME Old Dao
		final DBClassWidgetStore classWidgets = new DBClassWidgetStore(table);
		classWidgets.saveWidget(w);

		return JsonResponse.success(w);
	}

	@OldDao
	@Admin
	@JSONExported
	public void removeWidgetDefinition(@Parameter(PARAMETER_CLASS_NAME) final String className, //
			@Parameter(PARAMETER_WIDGET_ID) final String widgetId, final UserContext userCtx) throws Exception {

		final ITable table = buildTable(className); // FIXME Old Dao
		final DBClassWidgetStore classWidgets = new DBClassWidgetStore(table);
		classWidgets.removeWidget(widgetId);
	}

	/*
	 * ========================================================= PRIVATE
	 * ===========================================================
	 */

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

	private DataAccessLogic dataAccessLogic() {
		return TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
	}

	private WorkflowLogic workflowLogic() {
		return TemporaryObjectsBeforeSpringDI.getWorkflowLogic();
	}

}
