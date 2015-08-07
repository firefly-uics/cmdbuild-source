package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.entrytype.CMAttribute.Mode.WRITE;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.CharAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DoubleAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IpAddressAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringArrayAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.metadata.Metadata;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.model.widget.customform.CustomForm.Attribute;
import org.cmdbuild.model.widget.customform.CustomForm.Attribute.Filter;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.widget.ValuePairWidgetFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class CustomFormWidgetFactory extends ValuePairWidgetFactory {

	private static final String //
			TYPE_BOOLEAN = "boolean", //
			TYPE_CHAR = "char", //
			TYPE_DATE = "date", //
			TYPE_DATE_TIME = "dateTime", //
			TYPE_DECIMAL = "decimal", //
			TYPE_DOUBLE = "double", //
			TYPE_ENTRY_TYPE = "entryType", //
			TYPE_INTEGER = "integer", //
			TYPE_IP_ADDRESS = "ipAddress", //
			TYPE_LOOKUP = "lookup", //
			TYPE_REFERENCE = "reference", //
			TYPE_STRING_ARRAY = "stringArray", //
			TYPE_STRING = "string", //
			TYPE_TEXT = "text", //
			TYPE_TIME = "time";

	private static interface AttributesFetcher {

		Iterable<Attribute> attributes();

	}

	private static class NullAttributeFetcher implements AttributesFetcher {

		private static Iterable<Attribute> empty = emptyList();

		@Override
		public Iterable<Attribute> attributes() {
			return empty;
		}

	}

	private static class RawAttributeFetcher implements AttributesFetcher {

		private static final ObjectMapper mapper = new ObjectMapper();

		private static final TypeReference<Collection<? extends Attribute>> TYPE_REFERENCE = new TypeReference<Collection<? extends Attribute>>() {
		};

		private final String expression;

		public RawAttributeFetcher(final String expression) {
			this.expression = expression;
		}

		@Override
		public Iterable<Attribute> attributes() {
			try {
				logger.debug(MARKER, "parsing expression '{}'", expression);
				return mapper.readValue(expression, TYPE_REFERENCE);
			} catch (final Exception e) {
				logger.error(MARKER, "error parsing expression", e);
				throw new RuntimeException(e);
			}
		}

	}

	private static class ClassAttributeFetcher implements AttributesFetcher {

		private final CMDataView dataView;
		private final MetadataStoreFactory metadataStoreFactory;
		private final String className;

		public ClassAttributeFetcher(final CMDataView dataView, final MetadataStoreFactory metadataStoreFactory,
				final String className) {
			this.dataView = dataView;
			this.metadataStoreFactory = metadataStoreFactory;
			this.className = className;
		}

		@Override
		public Iterable<Attribute> attributes() {
			return from(dataView.findClass(className).getAttributes()) //
					// TODO filter?
					.transform(new Function<CMAttribute, Attribute>() {

						@Override
						public Attribute apply(final CMAttribute input) {
							final Attribute output = new Attribute();
							input.getType().accept(new CMAttributeTypeVisitor() {

								@Override
								public void visit(final BooleanAttributeType attributeType) {
									output.setType(TYPE_BOOLEAN);
								}

								@Override
								public void visit(final CharAttributeType attributeType) {
									output.setType(TYPE_CHAR);
								}

								@Override
								public void visit(final DateAttributeType attributeType) {
									output.setType(TYPE_DATE);
								}

								@Override
								public void visit(final DateTimeAttributeType attributeType) {
									output.setType(TYPE_DATE_TIME);
								}

								@Override
								public void visit(final DoubleAttributeType attributeType) {
									output.setType(TYPE_DOUBLE);
								}

								@Override
								public void visit(final DecimalAttributeType attributeType) {
									output.setType(TYPE_DECIMAL);
								}

								@Override
								public void visit(final EntryTypeAttributeType attributeType) {
									output.setType(TYPE_ENTRY_TYPE);
								}

								@Override
								public void visit(final ForeignKeyAttributeType attributeType) {
									output.setType(TYPE_REFERENCE);
								}

								@Override
								public void visit(final IntegerAttributeType attributeType) {
									output.setType(TYPE_INTEGER);
								}

								@Override
								public void visit(final IpAddressAttributeType attributeType) {
									output.setType(TYPE_IP_ADDRESS);
								}

								@Override
								public void visit(final LookupAttributeType attributeType) {
									output.setType(TYPE_LOOKUP);
								}

								@Override
								public void visit(final ReferenceAttributeType attributeType) {
									output.setType(TYPE_REFERENCE);
								}

								@Override
								public void visit(final StringAttributeType attributeType) {
									output.setType(TYPE_STRING);
								}

								@Override
								public void visit(final StringArrayAttributeType attributeType) {
									output.setType(TYPE_STRING_ARRAY);
								}

								@Override
								public void visit(final TextAttributeType attributeType) {
									output.setType(TYPE_TEXT);
								}

								@Override
								public void visit(final TimeAttributeType attributeType) {
									output.setType(TYPE_TIME);
								}

							});
							output.setName(input.getName());
							output.setDescription(input.getDescription());
							output.setUnique(input.isUnique());
							output.setMandatory(input.isMandatory());
							output.setWritable(WRITE.equals(input.getMode()));
							input.getType().accept(new ForwardingAttributeTypeVisitor() {

								private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

								@Override
								protected CMAttributeTypeVisitor delegate() {
									return DELEGATE;
								}

								@Override
								public void visit(final DecimalAttributeType attributeType) {
									output.setPrecision(Long.valueOf(attributeType.precision));
									output.setScale(Long.valueOf(attributeType.scale));
								}

								@Override
								public void visit(final ForeignKeyAttributeType attributeType) {
									output.setTargetClass(attributeType.getForeignKeyDestinationClassName());
								}

								@Override
								public void visit(final LookupAttributeType attributeType) {
									output.setLookupType(attributeType.getLookupTypeName());
								};

								@Override
								public void visit(final ReferenceAttributeType attributeType) {
									final String domainName = attributeType.getDomainName();
									final CMDomain domain = dataView.findDomain(domainName);
									Validate.notNull(domain, "domain '%s' not found", domain);
									final String domainCardinality = domain.getCardinality();
									CMClass target = null;
									if ("N:1".equals(domainCardinality)) {
										target = domain.getClass2();
									} else if ("1:N".equals(domainCardinality)) {
										target = domain.getClass1();
									}
									output.setTargetClass(target.getName());
									if (isNotBlank(input.getFilter())) {
										output.setFilter(new Filter() {
											{
												setExpression(input.getFilter());
												setContext(toMap(metadataStoreFactory.storeForAttribute(input)
														.readAll()));
											}
										});
									}
								};

								private Map<String, String> toMap(final Collection<Metadata> elements) {
									final Map<String, String> map = Maps.newHashMap();
									for (final Metadata element : elements) {
										map.put(element.name(), element.value());
									}
									return map;
								}

								@Override
								public void visit(final StringAttributeType attributeType) {
									output.setLength(Long.valueOf(attributeType.length));
								}

								@Override
								public void visit(final TextAttributeType attributeType) {
									output.setEditorType(input.getEditorType());
								}

							});
							return output;
						}

					});
		}
	}

	private static final Marker MARKER = MarkerFactory.getMarker(CustomFormWidgetFactory.class.getName());

	private static final String WIDGET_NAME = "customForm";

	public static final String REQUIRED = "Required";
	public static final String READ_ONLY = "ReadOnly";
	public static final String CONFIGURATION_TYPE = "ConfigurationType";
	public static final String RAW_ATTRIBUTES = "RawAttributes";
	public static final String CLASSNAME = "ClassName";
	public static final String LAYOUT = "Layout";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, REQUIRED, READ_ONLY, CONFIGURATION_TYPE,
			RAW_ATTRIBUTES, CLASSNAME, LAYOUT };

	private static final String RAW = "raw";
	private static final String CLASS = "class";

	private final CMDataView dataView;
	private final MetadataStoreFactory metadataStoreFactory;

	public CustomFormWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier,
			final CMDataView dataView, final MetadataStoreFactory metadataStoreFactory) {
		super(templateRespository, notifier);
		this.dataView = dataView;
		this.metadataStoreFactory = metadataStoreFactory;
	}

	@Override
	public String getWidgetName() {
		return WIDGET_NAME;
	}

	@Override
	protected Widget createWidget(final Map<String, Object> valueMap) {
		final CustomForm widget = new CustomForm();
		widget.setRequired(readBooleanFalseIfMissing(valueMap.get(REQUIRED)));
		widget.setReadOnly(readBooleanFalseIfMissing(valueMap.get(READ_ONLY)));
		widget.setAttributes(newArrayList(attributesFetcherOf(valueMap).attributes()));
		widget.setLayout(String.class.cast(valueMap.get(LAYOUT)));
		widget.setVariables(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		return widget;
	}

	private AttributesFetcher attributesFetcherOf(final Map<String, Object> valueMap) {
		final AttributesFetcher attributeFetcher;
		final String configurationType = String.class.cast(valueMap.get(CONFIGURATION_TYPE));
		if (RAW.equalsIgnoreCase(configurationType)) {
			final String expression = defaultString(String.class.cast(valueMap.get(RAW_ATTRIBUTES)));
			Validate.isTrue(isNotBlank(expression), "invalid value for '%s'", RAW_ATTRIBUTES);
			attributeFetcher = new RawAttributeFetcher(expression);
		} else if (CLASS.equalsIgnoreCase(configurationType)) {
			final String className = String.class.cast(valueMap.get(CLASSNAME));
			Validate.isTrue(isNotBlank(className), "invalid value for '%s'", CLASSNAME);
			attributeFetcher = new ClassAttributeFetcher(dataView, metadataStoreFactory, className);
		} else {
			Validate.isTrue(false, "invalid '%s'", CONFIGURATION_TYPE);
			attributeFetcher = new NullAttributeFetcher();
		}
		return attributeFetcher;
	}
}
