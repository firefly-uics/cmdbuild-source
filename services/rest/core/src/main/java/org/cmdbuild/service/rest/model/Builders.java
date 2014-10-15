package org.cmdbuild.service.rest.model;

import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Maps.immutableEntry;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.lang.Boolean.FALSE;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.utils.guava.Functions.toKey;
import static org.cmdbuild.common.utils.guava.Functions.toValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.service.rest.model.Attribute.Filter;
import org.cmdbuild.service.rest.model.ProcessActivityWithFullDetails.AttributeStatus;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class Builders {

	private static abstract class ModelBuilder<T> implements org.apache.commons.lang3.builder.Builder<T> {

		@Override
		public final T build() {
			doValidate();
			return doBuild();
		}

		protected void doValidate() {
			// no validation required
		}

		protected abstract T doBuild();

		@Override
		public final String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
		}

	}

	public static class AttributeBuilder extends ModelBuilder<Attribute> {

		private String id;
		private String type;
		private String name;
		private String description;
		private Boolean displayableInList;
		private Boolean unique;
		private Boolean mandatory;
		private Boolean inherited;
		private Boolean active;
		private Long index;
		private String defaultValue;
		private String group;
		private Long precision;
		private Long scale;
		private String targetClass;
		private Long length;
		private String editorType;
		private String lookupType;
		private Attribute.Filter filter;

		private AttributeBuilder() {
			// use factory method
		}

		@Override
		protected Attribute doBuild() {
			final Attribute output = new Attribute();
			output.setId(id);
			output.setType(type);
			output.setName(name);
			output.setDescription(description);
			output.setDisplayableInList(displayableInList);
			output.setUnique(unique);
			output.setMandatory(mandatory);
			output.setInherited(inherited);
			output.setActive(active);
			output.setIndex(index);
			output.setDefaultValue(defaultValue);
			output.setGroup(group);
			output.setPrecision(precision);
			output.setScale(scale);
			output.setTargetClass(targetClass);
			output.setLength(length);
			output.setEditorType(editorType);
			output.setLookupType(lookupType);
			output.setFilter(filter);
			return output;
		}

		public AttributeBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public AttributeBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public AttributeBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public AttributeBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public AttributeBuilder thatIsDisplayableInList(final Boolean displayableInList) {
			this.displayableInList = displayableInList;
			return this;
		}

		public AttributeBuilder thatIsUnique(final Boolean unique) {
			this.unique = unique;
			return this;
		}

		public AttributeBuilder thatIsMandatory(final Boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

		public AttributeBuilder thatIsInherited(final Boolean inherited) {
			this.inherited = inherited;
			return this;
		}

		public AttributeBuilder thatIsActive(final Boolean active) {
			this.active = active;
			return this;
		}

		public AttributeBuilder withIndex(final Long index) {
			this.index = index;
			return this;
		}

		public AttributeBuilder withDefaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}

		public AttributeBuilder withGroup(final String group) {
			this.group = group;
			return this;
		}

		public AttributeBuilder withPrecision(final Long precision) {
			this.precision = precision;
			return this;
		}

		public AttributeBuilder withScale(final Long scale) {
			this.scale = scale;
			return this;
		}

		public AttributeBuilder withTargetClass(final String targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public AttributeBuilder withLength(final Long length) {
			this.length = length;
			return this;
		}

		public AttributeBuilder withEditorType(final String editorType) {
			this.editorType = editorType;
			return this;
		}

		public AttributeBuilder withLookupType(final String lookupType) {
			this.lookupType = lookupType;
			return this;
		}

		public AttributeBuilder withFilter(final Attribute.Filter filter) {
			this.filter = filter;
			return this;
		}

	}

	public static class AttributeStatusBuilder extends ModelBuilder<AttributeStatus> {

		private String id;
		private Boolean writable;
		private Boolean mandatory;

		private AttributeStatusBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			writable = defaultIfNull(writable, FALSE);
			mandatory = defaultIfNull(mandatory, FALSE);
		}

		@Override
		protected AttributeStatus doBuild() {
			final AttributeStatus output = new AttributeStatus();
			output.setId(id);
			output.setWritable(writable);
			output.setMandatory(mandatory);
			return output;
		}

		public AttributeStatusBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public AttributeStatusBuilder withWritable(final Boolean writable) {
			this.writable = writable;
			return this;
		}

		public AttributeStatusBuilder withMandatory(final Boolean mandatory) {
			this.mandatory = mandatory;
			return this;
		}

	}

	public static class CardBuilder extends ModelBuilder<Card> {

		private String type;
		private Long id;
		final Map<String, Object> values = newHashMap();

		private CardBuilder() {
			// use factory method
		}

		@Override
		protected Card doBuild() {
			final Card output = new Card();
			output.setType(type);
			output.setId(id);
			output.setValues(values);
			return output;
		}

		public CardBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public CardBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public CardBuilder withValue(final String name, final Object value) {
			return withValue(immutableEntry(name, value));
		}

		public CardBuilder withValue(final Entry<String, ? extends Object> value) {
			return withValues(asList(value));
		}

		public CardBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			final Function<Entry<? extends String, ? extends Object>, String> key = toKey();
			final Function<Entry<? extends String, ? extends Object>, Object> value = toValue();
			final Map<String, Object> allValues = transformValues(uniqueIndex(values, key), value);
			return withValues(allValues);
		}

		public CardBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static class ClassWithBasicDetailsBuilder extends ModelBuilder<ClassWithBasicDetails> {

		private String id;
		private String name;
		private String description;
		private String parent;
		private Boolean prototype;

		private ClassWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ClassWithBasicDetails doBuild() {
			final ClassWithBasicDetails output = new ClassWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setParent(parent);
			output.setPrototype(prototype);
			return output;
		}

		public ClassWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ClassWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ClassWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassWithBasicDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

		public ClassWithBasicDetailsBuilder thatIsPrototype(final Boolean prototype) {
			this.prototype = prototype;
			return this;
		}

	}

	public static class ClassWithFullDetailsBuilder extends ModelBuilder<ClassWithFullDetails> {

		private String id;
		private String name;
		private String description;
		private Boolean prototype;
		private String descriptionAttributeName;
		private String parent;

		private ClassWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ClassWithFullDetails doBuild() {
			final ClassWithFullDetails output = new ClassWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setPrototype(prototype);
			output.setDescriptionAttributeName(descriptionAttributeName);
			output.setParent(parent);
			return output;

		}

		public ClassWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ClassWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ClassWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ClassWithFullDetailsBuilder thatIsPrototype(final Boolean superclass) {
			this.prototype = superclass;
			return this;
		}

		public ClassWithFullDetailsBuilder withDescriptionAttributeName(final String descriptionAttributeName) {
			this.descriptionAttributeName = descriptionAttributeName;
			return this;
		}

		public ClassWithFullDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class CredentialsBuilder extends ModelBuilder<Credentials> {

		private String token;
		private String username;
		private String password;
		private String group;

		private CredentialsBuilder() {
			// use factory method
		}

		private CredentialsBuilder(final Credentials existing) {
			// use factory method
			this.token = existing.getToken();
			this.username = existing.getUsername();
			this.password = existing.getPassword();
			this.group = existing.getGroup();
		}

		@Override
		protected Credentials doBuild() {
			final Credentials output = new Credentials();
			output.setToken(token);
			output.setUsername(username);
			output.setPassword(password);
			output.setGroup(group);
			return output;
		}

		public CredentialsBuilder withToken(final String token) {
			this.token = token;
			return this;
		}

		public CredentialsBuilder withUsername(final String username) {
			this.username = username;
			return this;
		}

		public CredentialsBuilder withPassword(final String password) {
			this.password = password;
			return this;
		}

		public CredentialsBuilder withGroup(final String group) {
			this.group = group;
			return this;
		}

	}

	public static class DomainWithBasicDetailsBuilder extends ModelBuilder<DomainWithBasicDetails> {

		private String id;
		private String name;
		private String description;

		private DomainWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected DomainWithBasicDetails doBuild() {
			final DomainWithBasicDetails output = new DomainWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			return output;
		}

		public DomainWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DomainWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public DomainWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

	}

	public static class DomainWithFullDetailsBuilder extends ModelBuilder<DomainWithFullDetails> {

		private String id;
		private String name;
		private String description;
		private Long classSource;
		private Long classDestination;
		private String cardinality;
		private String descriptionDirect;
		private String descriptionInverse;
		private String descriptionMasterDetail;

		private DomainWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected DomainWithFullDetails doBuild() {
			final DomainWithFullDetails output = new DomainWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setClassSource(classSource);
			output.setClassDestination(classDestination);
			output.setCardinality(cardinality);
			output.setDescriptionDirect(descriptionDirect);
			output.setDescriptionInverse(descriptionInverse);
			output.setDescriptionMasterDetail(descriptionMasterDetail);
			return output;
		}

		public DomainWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DomainWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public DomainWithFullDetailsBuilder withClassSource(final Long classSource) {
			this.classSource = classSource;
			return this;
		}

		public DomainWithFullDetailsBuilder withClassDestination(final Long classDestination) {
			this.classDestination = classDestination;
			return this;
		}

		public DomainWithFullDetailsBuilder withCardinality(final String cardinality) {
			this.cardinality = cardinality;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescriptionDirect(final String descriptionDirect) {
			this.descriptionDirect = descriptionDirect;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescriptionInverse(final String descriptionInverse) {
			this.descriptionInverse = descriptionInverse;
			return this;
		}

		public DomainWithFullDetailsBuilder withDescriptionMasterDetail(final String descriptionMasterDetail) {
			this.descriptionMasterDetail = descriptionMasterDetail;
			return this;
		}

	}

	public static class FilterBuilder extends ModelBuilder<Filter> {

		private String text;
		private Map<String, String> params;

		private FilterBuilder() {
			// use factory method
		}

		@Override
		protected Filter doBuild() {
			final Filter output = new Filter();
			output.setText(text);
			output.setParams(params);
			return output;
		}

		public FilterBuilder withText(final String text) {
			this.text = text;
			return this;
		}

		public FilterBuilder withParams(final Map<String, String> params) {
			this.params = params;
			return this;
		}

	}

	public static class LookupDetailBuilder extends ModelBuilder<LookupDetail> {

		private Long id;
		private String code;
		private String description;
		private String type;
		private Long number;
		private Boolean active;
		private Boolean isDefault;
		private Long parentId;
		private String parentType;

		private LookupDetailBuilder() {
			// use factory method
		}

		@Override
		protected LookupDetail doBuild() {
			final LookupDetail output = new LookupDetail();
			output.setId(id);
			output.setCode(code);
			output.setDescription(description);
			output.setType(type);
			output.setNumber(number);
			output.setActive(active);
			output.setDefault(isDefault);
			output.setParentId(parentId);
			output.setParentType(parentType);
			return output;
		}

		public LookupDetailBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public LookupDetailBuilder withCode(final String code) {
			this.code = code;
			return this;
		}

		public LookupDetailBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public LookupDetailBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public LookupDetailBuilder withNumber(final Long number) {
			this.number = number;
			return this;
		}

		public LookupDetailBuilder thatIsActive(final Boolean active) {
			this.active = active;
			return this;
		}

		public LookupDetailBuilder thatIsDefault(final Boolean isDefault) {
			this.isDefault = isDefault;
			return this;
		}

		public LookupDetailBuilder withParentId(final Long parentId) {
			this.parentId = parentId;
			return this;
		}

		public LookupDetailBuilder withParentType(final String parentType) {
			this.parentType = parentType;
			return this;
		}

	}

	public static class LookupTypeDetailBuilder extends ModelBuilder<LookupTypeDetail> {

		private String id;
		private String name;
		private String parent;

		private LookupTypeDetailBuilder() {
			// use factory method
		}

		@Override
		protected LookupTypeDetail doBuild() {
			final LookupTypeDetail output = new LookupTypeDetail();
			output.setId(id);
			output.setName(name);
			output.setParent(parent);
			return output;
		}

		public LookupTypeDetailBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public LookupTypeDetailBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public LookupTypeDetailBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class MenuBuilder extends ModelBuilder<MenuDetail> {

		private static final Iterable<MenuDetail> NO_CHILDREN = Collections.emptyList();

		private String menuType;
		private Long index;
		private Long objectType;
		private Long objectId;
		private String objectDescription;
		private Iterable<MenuDetail> children;

		private MenuBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			index = defaultIfNull(index, 0L);
			children = defaultIfNull(children, NO_CHILDREN);
		}

		@Override
		protected MenuDetail doBuild() {
			final MenuDetail output = new MenuDetail();
			output.setMenuType(menuType);
			output.setIndex(index);
			output.setObjectType(objectType);
			output.setObjectId(objectId);
			output.setObjectDescription(objectDescription);
			output.setChildren(Lists.newArrayList(children));
			return output;
		}

		public MenuBuilder withMenuType(final String menuType) {
			this.menuType = menuType;
			return this;
		}

		public MenuBuilder withIndex(final Long index) {
			this.index = index;
			return this;
		}

		public MenuBuilder withObjectType(final Long objectType) {
			this.objectType = objectType;
			return this;
		}

		public MenuBuilder withObjectId(final Long objectId) {
			this.objectId = objectId;
			return this;
		}

		public MenuBuilder withObjectDescription(final String objectDescription) {
			this.objectDescription = objectDescription;
			return this;
		}

		public MenuBuilder withChildren(final Iterable<MenuDetail> children) {
			this.children = children;
			return this;
		}

	}

	public static class MetadataBuilder extends ModelBuilder<DetailResponseMetadata> {

		private Long total;

		private MetadataBuilder() {
			// use factory method
		}

		@Override
		protected DetailResponseMetadata doBuild() {
			final DetailResponseMetadata output = new DetailResponseMetadata();
			output.setTotal(total);
			return output;
		}

		public MetadataBuilder withTotal(final Long total) {
			this.total = total;
			return this;
		}

	}

	public static class ProcessActivityWithBasicDetailsBuilder extends ModelBuilder<ProcessActivityWithBasicDetails> {

		private String id;
		private Boolean writable;

		private ProcessActivityWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			writable = defaultIfNull(writable, FALSE);
		}

		@Override
		protected ProcessActivityWithBasicDetails doBuild() {
			final ProcessActivityWithBasicDetails output = new ProcessActivityWithBasicDetails();
			output.setId(id);
			output.setWritable(writable);
			return output;
		}

		public ProcessActivityWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessActivityWithBasicDetailsBuilder withWritableStatus(final boolean writable) {
			this.writable = writable;
			return this;
		}

	}

	public static class ProcessActivityWithFullDetailsBuilder extends ModelBuilder<ProcessActivityWithFullDetails> {

		private static final Collection<? extends AttributeStatus> NO_ATTRIBUTES = Collections.emptyList();

		private String id;
		private String description;
		private String instructions;
		private final Collection<AttributeStatus> attributes = Lists.newArrayList();

		private ProcessActivityWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected ProcessActivityWithFullDetails doBuild() {
			final ProcessActivityWithFullDetails output = new ProcessActivityWithFullDetails();
			output.setId(id);
			output.setDescription(description);
			output.setInstructions(instructions);
			output.setAttributes(attributes);
			return output;
		}

		public ProcessActivityWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withInstructions(final String instructions) {
			this.instructions = instructions;
			return this;
		}

		public ProcessActivityWithFullDetailsBuilder withAttribute(
				final ProcessActivityWithFullDetails.AttributeStatus attribute) {
			return withAttributes(asList(attribute));
		}

		public ProcessActivityWithFullDetailsBuilder withAttributes(
				final Collection<? extends ProcessActivityWithFullDetails.AttributeStatus> attributes) {
			this.attributes.addAll(defaultIfNull(attributes, NO_ATTRIBUTES));
			return this;
		}

	}

	public static class ProcessInstanceBuilder extends ModelBuilder<ProcessInstance> {

		private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
		private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

		private String type;
		private Long id;
		private String name;
		final Map<String, Object> values = newHashMap();

		private ProcessInstanceBuilder() {
			// use factory method
		}

		@Override
		protected ProcessInstance doBuild() {
			final ProcessInstance output = new ProcessInstance();
			output.setType(type);
			output.setId(id);
			output.setName(name);
			output.setValues(values);
			return output;
		}

		public ProcessInstanceBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public ProcessInstanceBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public ProcessInstanceBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessInstanceBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			return withValues(transformValues(uniqueIndex(values, KEY), VALUE));
		}

		public ProcessInstanceBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static class ProcessInstanceAdvanceBuilder extends ModelBuilder<ProcessInstanceAdvanceable> {

		private static final Function<Entry<? extends String, ? extends Object>, String> KEY = toKey();
		private static final Function<Entry<? extends String, ? extends Object>, Object> VALUE = toValue();

		private String type;
		private Long id;
		private String name;
		final Map<String, Object> values = newHashMap();
		private String activityId;
		private Boolean advance;

		private ProcessInstanceAdvanceBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			advance = defaultIfNull(advance, FALSE);
		}

		@Override
		protected ProcessInstanceAdvanceable doBuild() {
			final ProcessInstanceAdvanceable output = new ProcessInstanceAdvanceable();
			output.setType(type);
			output.setId(id);
			output.setName(name);
			output.setValues(values);
			output.setActivity(activityId);
			output.setAdvance(advance);
			return output;
		}

		public ProcessInstanceAdvanceBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			return withValues(transformValues(uniqueIndex(values, KEY), VALUE));
		}

		public ProcessInstanceAdvanceBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

		public ProcessInstanceAdvanceBuilder withActivity(final String activityId) {
			this.activityId = activityId;
			return this;
		}

		public ProcessInstanceAdvanceBuilder withAdvance(final boolean advance) {
			this.advance = advance;
			return this;
		}

	}

	public static class ProcessWithBasicDetailsBuilder extends ModelBuilder<ProcessWithBasicDetails> {

		private String id;
		private String name;
		private String description;
		private String parent;
		private Boolean prototype;

		private ProcessWithBasicDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ProcessWithBasicDetails doBuild() {
			final ProcessWithBasicDetails output = new ProcessWithBasicDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setParent(parent);
			output.setPrototype(prototype);
			return output;
		}

		public ProcessWithBasicDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessWithBasicDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessWithBasicDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ProcessWithBasicDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

		public ProcessWithBasicDetailsBuilder thatIsPrototype(final Boolean prototype) {
			this.prototype = prototype;
			return this;
		}

	}

	public static class ProcessWithFullDetailsBuilder extends ModelBuilder<ProcessWithFullDetails> {

		private String id;
		private String name;
		private String description;
		private Boolean prototype;
		private String descriptionAttributeName;
		private String parent;

		private ProcessWithFullDetailsBuilder() {
			// use factory method
		}

		@Override
		protected void doValidate() {
			prototype = defaultIfNull(prototype, FALSE);
		}

		@Override
		protected ProcessWithFullDetails doBuild() {
			final ProcessWithFullDetails output = new ProcessWithFullDetails();
			output.setId(id);
			output.setName(name);
			output.setDescription(description);
			output.setPrototype(prototype);
			output.setDescriptionAttributeName(descriptionAttributeName);
			output.setParent(parent);
			return output;
		}

		public ProcessWithFullDetailsBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public ProcessWithFullDetailsBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public ProcessWithFullDetailsBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ProcessWithFullDetailsBuilder thatIsPrototype(final Boolean superclass) {
			this.prototype = superclass;
			return this;
		}

		public ProcessWithFullDetailsBuilder withDescriptionAttributeName(final String descriptionAttributeName) {
			this.descriptionAttributeName = descriptionAttributeName;
			return this;
		}

		public ProcessWithFullDetailsBuilder withParent(final String parent) {
			this.parent = parent;
			return this;
		}

	}

	public static class RelationBuilder extends ModelBuilder<Relation> {

		private String type;
		private Long id;
		private Card source;
		private Card destination;
		private final Map<String, Object> values = newHashMap();

		private RelationBuilder() {
			// use factory method
		}

		@Override
		protected Relation doBuild() {
			final Relation output = new Relation();
			output.setType(type);
			output.setId(id);
			output.setSource(source);
			output.setDestination(destination);
			output.setValues(values);
			return output;
		}

		public RelationBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public RelationBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public RelationBuilder withSource(final Card source) {
			this.source = source;
			return this;
		}

		public RelationBuilder withDestination(final Card destination) {
			this.destination = destination;
			return this;
		}

		public RelationBuilder withValues(final Iterable<? extends Entry<String, ? extends Object>> values) {
			final Function<Entry<? extends String, ? extends Object>, String> key = toKey();
			final Function<Entry<? extends String, ? extends Object>, Object> value = toValue();
			final Map<String, Object> allValues = transformValues(uniqueIndex(values, key), value);
			return withValues(allValues);
		}

		public RelationBuilder withValues(final Map<String, ? extends Object> values) {
			this.values.putAll(values);
			return this;
		}

	}

	public static class ResponseSingleBuilder<T> extends ModelBuilder<ResponseSingle<T>> {

		private T element;

		private ResponseSingleBuilder() {
			// use factory method
		}

		@Override
		protected ResponseSingle<T> doBuild() {
			final ResponseSingle<T> output = new ResponseSingle<T>();
			output.setElement(element);
			return output;
		}

		public ResponseSingleBuilder<T> withElement(final T element) {
			this.element = element;
			return this;
		}

	}

	public static class ResponseMultipleBuilder<T> extends ModelBuilder<ResponseMultiple<T>> {

		private final Iterable<T> NO_ELEMENTS = Collections.emptyList();

		private final Collection<T> elements = Lists.newArrayList();
		private DetailResponseMetadata metadata;

		private ResponseMultipleBuilder() {
			// use factory method
		}

		@Override
		protected ResponseMultiple<T> doBuild() {
			final ResponseMultiple<T> output = new ResponseMultiple<T>();
			output.setElements(elements);
			output.setMetadata(metadata);
			return output;
		}

		@SuppressWarnings("unchecked")
		public ResponseMultipleBuilder<T> withElement(final T element) {
			addAll(this.elements, (element == null) ? NO_ELEMENTS : asList(element));
			return this;
		}

		public ResponseMultipleBuilder<T> withElements(final Iterable<T> elements) {
			addAll(this.elements, defaultIfNull(elements, NO_ELEMENTS));
			return this;
		}

		public ResponseMultipleBuilder<T> withMetadata(final DetailResponseMetadata metadata) {
			this.metadata = metadata;
			return this;
		}

	}

	public static AttributeBuilder newAttribute() {
		return new AttributeBuilder();
	}

	public static AttributeStatusBuilder newAttributeStatus() {
		return new AttributeStatusBuilder();
	}

	public static CardBuilder newCard() {
		return new CardBuilder();
	}

	public static ClassWithBasicDetailsBuilder newClassWithBasicDetails() {
		return new ClassWithBasicDetailsBuilder();
	}

	public static ClassWithFullDetailsBuilder newClassWithFullDetails() {
		return new ClassWithFullDetailsBuilder();
	}

	public static CredentialsBuilder newCredentials() {
		return new CredentialsBuilder();
	}

	public static CredentialsBuilder newCredentials(final Credentials existing) {
		return new CredentialsBuilder(existing);
	}

	public static DomainWithBasicDetailsBuilder newDomainWithBasicDetails() {
		return new DomainWithBasicDetailsBuilder();
	}

	public static DomainWithFullDetailsBuilder newDomainWithFullDetails() {
		return new DomainWithFullDetailsBuilder();
	}

	public static FilterBuilder newFilter() {
		return new FilterBuilder();
	}

	public static LookupDetailBuilder newLookupDetail() {
		return new LookupDetailBuilder();
	}

	public static LookupTypeDetailBuilder newLookupTypeDetail() {
		return new LookupTypeDetailBuilder();
	}

	public static MenuBuilder newMenu() {
		return new MenuBuilder();
	}

	public static MetadataBuilder newMetadata() {
		return new MetadataBuilder();
	}

	public static ProcessActivityWithBasicDetailsBuilder newProcessActivityWithBasicDetails() {
		return new ProcessActivityWithBasicDetailsBuilder();
	}

	public static ProcessActivityWithFullDetailsBuilder newProcessActivityWithFullDetails() {
		return new ProcessActivityWithFullDetailsBuilder();
	}

	public static ProcessInstanceBuilder newProcessInstance() {
		return new ProcessInstanceBuilder();
	}

	public static ProcessInstanceAdvanceBuilder newProcessInstanceAdvance() {
		return new ProcessInstanceAdvanceBuilder();
	}

	public static ProcessWithBasicDetailsBuilder newProcessWithBasicDetails() {
		return new ProcessWithBasicDetailsBuilder();
	}

	public static ProcessWithFullDetailsBuilder newProcessWithFullDetails() {
		return new ProcessWithFullDetailsBuilder();
	}

	public static RelationBuilder newRelation() {
		return new RelationBuilder();
	}

	@Deprecated
	public static <T> ResponseSingleBuilder<T> newResponseSingle() {
		return new ResponseSingleBuilder<T>();
	}

	public static <T> ResponseSingleBuilder<T> newResponseSingle(final Class<T> type) {
		return newResponseSingle();
	}

	@Deprecated
	public static <T> ResponseMultipleBuilder<T> newResponseMultiple() {
		return new ResponseMultipleBuilder<T>();
	}

	public static <T> ResponseMultipleBuilder<T> newResponseMultiple(final Class<T> type) {
		return newResponseMultiple();
	}

	private Builders() {
		// prevents instantation
	}

}
