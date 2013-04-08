package org.cmdbuild.servlets.json.schema;

import static com.google.common.collect.Iterables.filter;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DEFAULT_VALUE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAINS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_CARDINALITY;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_FIRST_CLASS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_IS_MASTER_DETAIL;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_MASTER_DETAIL_LABEL;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.DOMAIN_SECOND_CLASS_ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.EDITOR_TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FIELD_MODE;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.ComunicationConstants.FK_DESTINATION;
import static org.cmdbuild.servlets.json.ComunicationConstants.GROUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.INDEX;
import static org.cmdbuild.servlets.json.ComunicationConstants.INHERIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.IS_PROCESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.LENGTH;
import static org.cmdbuild.servlets.json.ComunicationConstants.LOOKUP;
import static org.cmdbuild.servlets.json.ComunicationConstants.META_DATA;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.NOT_NULL;
import static org.cmdbuild.servlets.json.ComunicationConstants.PRECISION;
import static org.cmdbuild.servlets.json.ComunicationConstants.SCALE;
import static org.cmdbuild.servlets.json.ComunicationConstants.SHOW_IN_GRID;
import static org.cmdbuild.servlets.json.ComunicationConstants.SUPERCLASS;
import static org.cmdbuild.servlets.json.ComunicationConstants.TABLE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TABLE_TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPE;
import static org.cmdbuild.servlets.json.ComunicationConstants.TYPES;
import static org.cmdbuild.servlets.json.ComunicationConstants.UNIQUE;
import static org.cmdbuild.servlets.json.ComunicationConstants.USER_STOPPABLE;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMTableType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataAction.Visitor;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Create;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Delete;
import org.cmdbuild.logic.data.DataDefinitionLogic.MetadataActions.Update;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.ClassOrder;
import org.cmdbuild.model.data.Domain;
import org.cmdbuild.model.data.EntryType;
import org.cmdbuild.model.data.Metadata;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer;
import org.cmdbuild.servlets.json.serializers.AttributeSerializer.JsonModeMapper;
import org.cmdbuild.servlets.json.serializers.ClassSerializer;
import org.cmdbuild.servlets.json.serializers.DomainSerializer;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.utils.StringUtils;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ModClass extends JSONBase {

	private DataDefinitionLogic dataDefinitionLogic() {
		return TemporaryObjectsBeforeSpringDI.getDataDefinitionLogic();
	}

	private DataAccessLogic dataAccessLogic() {
		return TemporaryObjectsBeforeSpringDI.getDataAccessLogic();
	}

	private WorkflowLogic workflowLogic() {
		return TemporaryObjectsBeforeSpringDI.getWorkflowLogic();
	}

	@JSONExported
	public JSONObject getAllClasses( //
			@Parameter(value = ACTIVE, required = false) final boolean active //
	) throws JSONException, AuthException, CMWorkflowException {
		final Iterable<? extends CMClass> fetchedClasses;
		final Iterable<? extends UserProcessClass> processClasses;
		if (active) {
			fetchedClasses = dataAccessLogic().findActiveClasses();
			processClasses = workflowLogic().findActiveProcessClasses();
		} else {
			fetchedClasses = dataAccessLogic().findAllClasses();
			processClasses = workflowLogic().findAllProcessClasses();
		}

		 

		final JSONArray serializedClasses = new JSONArray();
		for (final CMClass element : filter(fetchedClasses, nonProcessClasses())) {
			/*
			 * TODO create a java object that wraps the CMClass object and
			 * contains all metadata for a class
			 */
			final JSONObject classObject = ClassSerializer.newInstance().toClient(element);
			Serializer.addAttachmentsData(classObject, element, dmsLogic());
			serializedClasses.put(classObject);
		}
		for (final UserProcessClass element : processClasses) {
			/*
			 * TODO create a java object that wraps the CMClass object and
			 * contains all metadata for a class
			 */
			final JSONObject classObject = ClassSerializer.newInstance().toClient(element, active);
			Serializer.addAttachmentsData(classObject, element, dmsLogic());
			serializedClasses.put(classObject);
		}

		return new JSONObject() {
			{
				put("classes", serializedClasses);
			}
		};
	}

	private Predicate<CMClass> nonProcessClasses() {
		final CMClass processBaseClass = dataAccessLogic().getView().findClass(Constants.BASE_PROCESS_CLASS_NAME);
		final Predicate<CMClass> nonProcessClasses = new Predicate<CMClass>() {
			@Override
			public boolean apply(final CMClass input) {
				return !processBaseClass.isAncestorOf(input);
			}
		};
		return nonProcessClasses;
	}

	private DmsLogic dmsLogic() {
		return TemporaryObjectsBeforeSpringDI.getDmsLogic();
	}

	@JSONExported
	public JSONObject saveTable( //
			@Parameter(NAME) final String name, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = INHERIT, required = false) final int idParent, //
			@Parameter(value = SUPERCLASS, required = false) final boolean isSuperClass, //
			@Parameter(value = IS_PROCESS, required = false) final boolean isProcess, //
			@Parameter(value = TABLE_TYPE, required = false) String tableType, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter(USER_STOPPABLE) final boolean isProcessUserStoppable //
	) throws JSONException, CMDBException {

		if (tableType == "") {
			tableType = EntryType.TableType.standard.name();
		}

		final EntryType clazz = EntryType.newClass() //
				.withTableType(EntryType.TableType.valueOf(tableType)).withName(name) //
				.withDescription(description) //
				.withParent(Long.valueOf(idParent)) //
				.thatIsSuperClass(isSuperClass) //
				.thatIsProcess(isProcess) //
				.thatIsUserStoppable(isProcessUserStoppable) //
				.thatIsActive(isActive) //
				.build();

		final CMClass cmClass = dataDefinitionLogic().createOrUpdate(clazz);
		return ClassSerializer.newInstance().toClient(cmClass, TABLE);
	}

	@JSONExported
	public void deleteTable(@Parameter(value = CLASS_NAME) final String className) throws JSONException, CMDBException {

		final EntryType clazz = EntryType.newClass() //
				.withName(className) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(clazz);
	}

	/*
	 * ========================================================= ATTRIBUTES
	 * ===========================================================
	 */

	@JSONExported
	public JSONObject getAttributeList(@Parameter(value = ACTIVE, required = false) final boolean onlyActive, //
			@Parameter(value = CLASS_NAME) final String className) throws JSONException, AuthException {

		final JSONObject out = new JSONObject();

		Iterable<? extends CMAttribute> attributesForClass;
		final DataAccessLogic dataLogic = dataAccessLogic();
		if (onlyActive) {
			attributesForClass = dataLogic.findClass(className).getActiveAttributes();
		} else {
			attributesForClass = dataLogic.findClass(className).getAttributes();
		}

		out.put(ATTRIBUTES, AttributeSerializer.of(dataLogic.getView()).toClient(attributesForClass, onlyActive));
		return out;
	}

	@JSONExported
	public void saveOrderCriteria(@Parameter(value = ATTRIBUTES) final JSONObject orderCriteria, //
			@Parameter(value = CLASS_NAME) final String className) throws Exception {

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
			@Parameter(TABLE_TYPE) final String tableTypeStirng) //
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		final CMTableType tableType = CMTableType.valueOf(tableTypeStirng);
		final List<CMAttributeType<?>> types = new LinkedList<CMAttributeType<?>>();
		for (final CMAttributeType<?> type : tableType.getAvaiableAttributeList()) {
			types.add(type);
		}
		out.put(TYPES, AttributeSerializer.toClient(types));
		return out;
	}

	// TODO AUTHORIZATION ON ATTRIBUTES IS NEVER CHECKED!
	@JSONExported
	public JSONObject saveAttribute( //
			final JSONObject serializer, //
			@Parameter(value = NAME, required = false) final String name, //
			@Parameter(value = TYPE, required = false) final String attributeTypeString, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = DEFAULT_VALUE, required = false) final String defaultValue, //
			@Parameter(SHOW_IN_GRID) final boolean isBaseDSP, //
			@Parameter(NOT_NULL) final boolean isNotNull, //
			@Parameter(UNIQUE) final boolean isUnique, //
			@Parameter(ACTIVE) final boolean isActive, //
			@Parameter(FIELD_MODE) final String fieldMode, //
			@Parameter(value = LENGTH, required = false) final int length, //
			@Parameter(value = PRECISION, required = false) final int precision, //
			@Parameter(value = SCALE, required = false) final int scale, //
			@Parameter(value = LOOKUP, required = false) final String lookupType, //
			@Parameter(value = DOMAIN_NAME, required = false) final String domainName, //
			@Parameter(value = FILTER, required = false) final String fieldFilter, //
			@Parameter(value = FK_DESTINATION, required = false) final String fkDestinationName, //
			@Parameter(value = GROUP, required = false) final String group, //
			@Parameter(value = META_DATA, required = false) final JSONObject meta, //
			@Parameter(value = EDITOR_TYPE, required = false) final String editorType, //
			@Parameter(value = CLASS_NAME) final String className) throws Exception {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(name) //
				.withOwner(className) //
				.withDescription(description) //
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
				.build();
		final DataDefinitionLogic logic = dataDefinitionLogic();
		final CMAttribute cmAttribute = logic.createOrUpdate(attribute);
		final JSONObject result = AttributeSerializer.of(logic.getView()).toClient(cmAttribute,
				buildMetadataForSerialization(attribute.getMetadata()));
		serializer.put(ATTRIBUTE, result);
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
		if (meta != null) {
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
			@Parameter(NAME) final String attributeName, //
			@Parameter(CLASS_NAME) final String className) {
		final Attribute attribute = Attribute.newAttribute() //
				.withName(attributeName) //
				.withOwner(className) //
				.build();
		dataDefinitionLogic().deleteOrDeactivate(attribute);
	}

	@JSONExported
	public void reorderAttribute( //
			@Parameter(ATTRIBUTES) final String jsonAttributeList, //
			@Parameter(CLASS_NAME) final String className //
	) throws Exception {
		final List<Attribute> attributes = Lists.newArrayList();
		final JSONArray jsonAttributes = new JSONArray(jsonAttributeList);
		for (int i = 0; i < jsonAttributes.length(); i++) {
			final JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			attributes.add(Attribute.newAttribute().withOwner(className)//
					.withName(jsonAttribute.getString(NAME)) //
					.withIndex(jsonAttribute.getInt(INDEX)).build());
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
	public JSONObject getAllDomains(@Parameter(value = ACTIVE, required = false) final boolean activeOnly)
			throws JSONException, AuthException {

		final JSONObject out = new JSONObject();
		Iterable<? extends CMDomain> domains;
		if (activeOnly) {
			domains = dataAccessLogic().findActiveDomains();
		} else {
			domains = dataAccessLogic().findAllDomains();
		}
		final JSONArray jsonDomains = new JSONArray();
		out.put(DOMAINS, jsonDomains);
		for (final CMDomain domain : domains) {
			jsonDomains.put(DomainSerializer.toClient(domain, activeOnly));
		}
		return out;
	}

	@Admin
	@JSONExported
	public JSONObject saveDomain( //
			@Parameter(value = NAME, required = false) final String domainName, //
			@Parameter(value = DOMAIN_FIRST_CLASS_ID, required = false) final int classId1, //
			@Parameter(value = DOMAIN_SECOND_CLASS_ID, required = false) final int classId2, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(value = DOMAIN_CARDINALITY, required = false) final String cardinality, //
			@Parameter(DOMAIN_DESCRIPTION_STARTING_AT_THE_FIRST_CLASS) final String descriptionDirect, //
			@Parameter(DOMAIN_DESCRIPTION_STARTING_AT_THE_SECOND_CLASS) final String descriptionInverse, //
			@Parameter(DOMAIN_IS_MASTER_DETAIL) final boolean isMasterDetail, //
			@Parameter(value = DOMAIN_MASTER_DETAIL_LABEL, required = false) final String mdLabel, //
			@Parameter(ACTIVE) final boolean isActive //
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
		return DomainSerializer.toClient(createdOrUpdated, false, DOMAIN);
	}

	@JSONExported
	public void deleteDomain(@Parameter(value = DOMAIN_NAME, required = false) final String domainName //
	) throws JSONException {

		dataDefinitionLogic().deleteDomainByName(domainName);
	}

	@Admin
	@JSONExported
	public JSONObject getDomainList(@Parameter(CLASS_NAME) final String className //
	) throws JSONException {

		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		// TODO system really needed
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final List<CMDomain> domainsForSpecifiedClass = dataAccessLogic.findDomainsForClassWithName(className);
		for (final CMDomain domain : domainsForSpecifiedClass) {
			jsonDomains.put(DomainSerializer.toClient(domain, className));
		}
		out.put(DOMAINS, jsonDomains);
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
	public JSONArray getFKTargetingClass(@Parameter(CLASS_NAME) final String className //
	) throws Exception {
		// TODO: improve performances by getting only simple classes (the
		// database should filter the simple classes)
		final DataAccessLogic logic = dataAccessLogic();
		final JSONArray fk = new JSONArray();
		for (final CMClass activeClass : logic.findActiveClasses()) {
			final boolean isSimpleClass = !activeClass.holdsHistory();
			if (isSimpleClass) {
				for (final CMAttribute attribute : activeClass.getActiveAttributes()) {
					final String referencedClassName = attribute.getForeignKeyDestinationClassName();
					final boolean isForeignKeyAttributeForSpecifiedClass = referencedClassName != null //
							&& referencedClassName.equalsIgnoreCase(className);
					if (isForeignKeyAttributeForSpecifiedClass) {
						fk.put(AttributeSerializer.of(logic.getView()).toClient(attribute));
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
	public JSONObject getReferenceableDomainList(@Parameter(CLASS_NAME) final String className) throws JSONException {
		final DataAccessLogic systemDataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final JSONObject out = new JSONObject();
		final JSONArray jsonDomains = new JSONArray();
		final Iterable<? extends CMDomain> referenceableDomains = systemDataAccessLogic
				.findReferenceableDomains(className);
		for (final CMDomain domain : referenceableDomains) {
			jsonDomains.put(DomainSerializer.toClient(domain, false));
		}
		out.put(DOMAINS, jsonDomains);
		return out;
	}

}
