package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.Iterables.filter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMTableType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.data.converter.WidgetConverter;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction.Visitor;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Create;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Delete;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Update;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.Metadata;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.services.store.DataViewStore;
import org.cmdbuild.services.store.Store;
import org.cmdbuild.services.store.Store.Storable;
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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModClass extends JSONBase {

	@SuppressWarnings("unchecked")
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

		// TODO:
		// FIXME: add process classes (subclasses of activity) when that part is
		// completed
		// final Iterable<UserProcessClass> processClasses =
		// workflowLogic().findAllProcessClasses();

		final JSONArray serializedClasses = new JSONArray();
		for (final CMClass fetchedClass : fetchedClasses) {
			final JSONObject classObject = ClassSerializer.toClient(fetchedClass);
			Serializer.addAttachmentsData(classObject, fetchedClass, applicationContext.getBean(DmsLogic.class));
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
			attributesForClass = dataLogic.findClass(className).getAttributes();
		} else {
			attributesForClass = dataLogic.findClass(className).getAllAttributes();
		}

		out.put(SERIALIZATION_ATTRIBUTES, AttributeSerializer.toClient(attributesForClass, onlyActive));
		return out;
	}

	@JSONExported
	public void saveOrderCriteria(@Parameter(value = PARAMETER_ATTRIBUTES) final JSONObject orderCriteria, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className) throws Exception {

		final List<ClassOrder> classOrders = Lists.newArrayList();
		final Iterator<?> keysIterator = orderCriteria.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			classOrders.add(ClassOrder.from(key, orderCriteria.getInt(key)));
		}
		dataDefinitionLogic().changeClassOrders(className, classOrders);
	}

	/**
	 * 
	 * @param tableTypeStirng
	 *            can be CLASS or SIMPLECLASS
	 * @return a list of attribute types that a class or superclass can have.
	 * @throws JSONException
	 * @throws AuthException
	 */
	@JSONExported
	public JSONObject getAttributeTypes( //
			@Parameter(PARAMETER_TABLE_TYPE) final String tableTypeStirng) //
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);
		final List<CMAttributeType<?>> types = new LinkedList<CMAttributeType<?>>();
		for (final CMAttributeType<?> type : tableType.getAvaiableAttributeList()) {
			types.add(type);
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
			@Parameter(value = PARAMETER_FK_DESTINATION, required = false) final String fkDestinationName, //
			@Parameter(value = PARAMETER_GROUP, required = false) final String group, //
			@Parameter(value = PARAMETER_META_DATA, required = false) final JSONObject meta, //
			@Parameter(value = PARAMETER_EDITOR_TYPE, required = false) final String editorType, //
			@Parameter(value = PARAMETER_CLASS_NAME) final String className) throws Exception {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwner(className).withDescription(description) //
				.withGroup(group) //
				.withType(attributeTypeString) //
				.withLength(length) //
				.withPrecision(precision) //
				.withScale(scale) //
				.withLookupType(lookupType) //
				.withDomain(domainName) //
				.withDefaultValue(defaultValue) //
				.withMode(JsonModeMapper.modeFrom(fieldMode)) //
				.withEditorType(editorType) //
				.withForeignKeyDestinationClassName(fkDestinationName) //
				.thatIsDisplayableInList(isBaseDSP) //
				.thatIsMandatory(isNotNull) //
				.thatIsUnique(isUnique) //
				.thatIsActive(isActive) //
				.withMetadata(buildMetadataByAction(meta)) //
				// @Parameter(value = "fieldFilter", required = false) String
				// fieldFilter, //
				// @Parameter(value = "meta", required = false) JSONObject meta,
				.build();
		final CMAttribute cmAttribute = dataDefinitionLogic().createOrUpdate(attribute);
		final JSONObject result = AttributeSerializer.toClient(cmAttribute,
				buildMetadataForSerialization(attribute.getMetadata()));
		serializer.put(SERIALIZATION_ATTRIBUTE, result);
		return serializer;
	}

	private enum MetaStatus {

		DELETED(MetadataActions.DELETE), //
		MODIFIED(MetadataActions.UPDATE), //
		NEW(MetadataActions.CREATE), //
		UNDEFINED(null), //
		;

		private final MetadataAction action;

		private MetaStatus(final MetadataAction action) {
			this.action = action;
		}

		public boolean hasAction() {
			return (action != null);
		}

		public MetadataAction getAction() {
			return action;
		}

		public static MetaStatus forStatus(final String status) {
			for (final MetaStatus value : values()) {
				if (value.name().equals(status)) {
					return value;
				}
			}
			return UNDEFINED;
		}

	}

	private Map<MetadataAction, List<Metadata>> buildMetadataByAction(final JSONObject meta) throws Exception {
		final Map<MetadataAction, List<Metadata>> metadataMap = Maps.newHashMap();
		final Iterator<?> jsonMetadata = meta.keys();
		while (jsonMetadata.hasNext()) {
			final String name = (String) jsonMetadata.next();
			final JSONObject info = meta.getJSONObject(name);
			final String value = info.getString("value");
			final MetaStatus status = MetaStatus.forStatus(info.getString("status"));
			if (status.hasAction()) {
				final MetadataAction action = status.getAction();
				List<Metadata> list = metadataMap.get(action);
				if (list == null) {
					list = Lists.newArrayList();
					metadataMap.put(action, list);
				}
				list.add(new Metadata(name, value));

			}
		}
		return metadataMap;
	}

	private Iterable<Metadata> buildMetadataForSerialization(final Map<MetadataAction, List<Metadata>> metadataByAction) {
		final List<Metadata> metadata = Lists.newArrayList();
		for (final MetadataAction action : metadataByAction.keySet()) {
			final Iterable<Metadata> elements = metadataByAction.get(action);
			action.accept(new Visitor() {

				@Override
				public void visit(final Create action) {
					filter(elements, new Predicate<Metadata>() {
						@Override
						public boolean apply(final Metadata input) {
							metadata.add(input);
							return true;
						}
					});
				}

				@Override
				public void visit(final Update action) {
					filter(elements, new Predicate<Metadata>() {
						@Override
						public boolean apply(final Metadata input) {
							metadata.add(input);
							return true;
						}
					});
				}

				@Override
				public void visit(final Delete action) {
					filter(elements, new Predicate<Metadata>() {
						@Override
						public boolean apply(final Metadata input) {
							// nothing to do
							return true;
						}
					});
				}

			});
		}
		return metadata;
	}

	@JSONExported
	public void deleteAttribute( //
			@Parameter(PARAMETER_NAME) final String attributeName, //
			@Parameter(PARAMETER_CLASS_NAME) final String className) {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwner(className) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(attribute);
	}

	@JSONExported
	public void reorderAttribute( //
			@Parameter(PARAMETER_ATTRIBUTES) final String jsonAttributeList, //
			@Parameter(PARAMETER_CLASS_NAME) final String className //
	) throws Exception {
		final List<Attribute> attributes = Lists.newArrayList();
		final JSONArray jsonAttributes = new JSONArray(jsonAttributeList);
		for (int i = 0; i < jsonAttributes.length(); i++) {
			final JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			attributes.add(Attribute.newAttribute().withOwner(className)//
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

	@JSONExported
	public JSONObject getAllDomains(@Parameter(value = PARAMETER_ACTIVE, required = false) final boolean activeOnly)
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		Iterable<? extends CMDomain> domains;
		if (activeOnly) {
			domains = dataAccessLogic().findActiveDomains();
		} else {
			domains = dataAccessLogic().findAllDomains();
		}
		final JSONArray jsonDomains = new JSONArray();
		out.put(SERIALIZATION_DOMAINS, jsonDomains);
		for (final CMDomain domain : domains) {
			jsonDomains.put(DomainSerializer.toClient(domain, activeOnly));
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
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final List<CMDomain> domainsForSpecifiedClass = dataAccessLogic.findDomainsForClassWithName(className);
		for (final CMDomain domain : domainsForSpecifiedClass) {
			jsonDomains.put(DomainSerializer.toClient(domain, className));
		}

		out.put(SERIALIZATION_DOMAINS, jsonDomains);
		return out;
	}

	/**
	 * Given a class name, this method retrieves all the attributes for all the
	 * SIMPLE classes that have at least one attribute of type foreign key whose
	 * target class is the specified class
	 * 
	 * @param className
	 * @return
	 * @throws Exception
	 */
	@JSONExported
	public JSONArray getFKTargetingClass(@Parameter(PARAMETER_CLASS_NAME) final String className //
	) throws Exception {
		// TODO: improve performances by getting only simple classes (the
		// database should filter the simple classes)
		final JSONArray fk = new JSONArray();
		for (final CMClass activeClass : dataAccessLogic().findActiveClasses()) {
			final boolean isSimpleClass = !activeClass.holdsHistory();
			if (isSimpleClass) {
				for (final CMAttribute attribute : activeClass.getAttributes()) {
					final String referencedClassName = attribute.getForeignKeyDestinationClassName();
					final boolean isForeignKeyAttributeForSpecifiedClass = referencedClassName != null //
							&& referencedClassName.equalsIgnoreCase(className);
					if (isForeignKeyAttributeForSpecifiedClass) {
						fk.put(AttributeSerializer.toClient(attribute));
					}
				}
			}
		}
		return fk;
	}

	/**
	 * Retrieves all domains with cardinality 1:N or N:1 in which the class with
	 * the specified name is on the 'N' side
	 * 
	 * @param className
	 * @return
	 * @throws JSONException
	 */
	@Admin
	@JSONExported
	public JSONObject getReferenceableDomainList(@Parameter(PARAMETER_CLASS_NAME) final String className)
			throws JSONException {
		final DataAccessLogic systemDataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final Iterable<? extends CMDomain> referenceableDomains = systemDataAccessLogic
				.findReferenceableDomains(className);
		for (final CMDomain domain : referenceableDomains) {
			jsonDomains.put(DomainSerializer.toClient(domain, false));
		}
		out.put(SERIALIZATION_DOMAINS, jsonDomains);
		return out;
	}

	/*
	 * ========================================================= WIDGET
	 * ===========================================================
	 */

	@JSONExported
	public JsonResponse getAllWidgets() {
		final DataViewStore<Widget> widgetStore = getWidgetStore();
		final List<Widget> fetchedWidgets = widgetStore.list();
		final Map<String, List<Widget>> classNameToWidgetList = Maps.newHashMap();
		for (final Widget widget : fetchedWidgets) {
			List<Widget> widgetList;
			if (!classNameToWidgetList.containsKey(widget.getTargetClass())) {
				widgetList = Lists.newArrayList();
				classNameToWidgetList.put(widget.getTargetClass(), widgetList);
			} else {
				widgetList = classNameToWidgetList.get(widget.getTargetClass());
			}
			widgetList.add(widget);
		}
		return JsonResponse.success(classNameToWidgetList);
	}

	@Admin
	@JSONExported
	public JsonResponse saveWidgetDefinition(@Parameter(PARAMETER_CLASS_NAME) final String className, //
			@Parameter(value = PARAMETER_WIDGET, required = true) final String jsonWidget) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();
		final Widget widgetToSave = mapper.readValue(jsonWidget, Widget.class);
		widgetToSave.setTargetClass(className);
		final DataViewStore<Widget> widgetStore = getWidgetStore();
		if (widgetToSave.getIdentifier() == null) {
			widgetStore.create(widgetToSave);
		} else {
			widgetStore.update(widgetToSave);
		}
		return JsonResponse.success(widgetToSave);
	}

	@Admin
	@JSONExported
	public void removeWidgetDefinition(@Parameter(PARAMETER_CLASS_NAME) final String className, //
			@Parameter(PARAMETER_WIDGET_ID) final Long widgetId) throws Exception {
		final DataViewStore<Widget> widgetStore = getWidgetStore();
		final Storable storableToDelete = new Store.Storable() {
			@Override
			public String getIdentifier() {
				return Long.toString(widgetId);
			}
		};
		widgetStore.delete(storableToDelete);
	}

	private DataViewStore<Widget> getWidgetStore() {
		final WidgetConverter converter = new WidgetConverter();
		return new DataViewStore<Widget>(TemporaryObjectsBeforeSpringDI.getSystemView(), converter);
	}

	private DataDefinitionLogic dataDefinitionLogic() {
		return TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic();
	}

	private DataAccessLogic dataAccessLogic() {
		return TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
	}

}
