package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.entrytype.CMAttribute.Mode.WRITE;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.Builder;
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
import org.cmdbuild.dao.function.CMFunction.CMFunctionParameter;
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

	private static interface FormBuilder extends Builder<String> {

	}

	private static class InvalidFormBuilder implements FormBuilder {

		private final String message;

		public InvalidFormBuilder(final String message) {
			this.message = message;
		}

		@Override
		public String build() {
			throw new RuntimeException(message);
		}

	}

	private static class FallbackOnExceptionFormBuilder implements FormBuilder {

		private final FormBuilder delegate;
		private final FormBuilder fallback;

		public FallbackOnExceptionFormBuilder(final FormBuilder delegate, final FormBuilder fallback) {
			this.delegate = delegate;
			this.fallback = fallback;
		}

		@Override
		public String build() {
			try {
				return delegate.build();
			} catch (final Exception e) {
				return fallback.build();
			}
		}

	}

	private static class IdentityFormBuilder implements FormBuilder {

		private final String value;

		public IdentityFormBuilder(final String value) {
			this.value = value;
		}

		@Override
		public String build() {
			return value;
		}

	}

	private static abstract class AttributesBasedFormBuilder implements FormBuilder {

		/**
		 * Usable by subclasses only.
		 */
		protected AttributesBasedFormBuilder() {
		}

		@Override
		public final String build() {
			return writeJsonString(newArrayList(attributes()));
		}

		protected abstract Iterable<Attribute> attributes();

	}

	private static class JsonStringFormBuilder extends AttributesBasedFormBuilder {

		private static final ObjectMapper mapper = new ObjectMapper();

		private static final TypeReference<Collection<? extends Attribute>> TYPE_REFERENCE = new TypeReference<Collection<? extends Attribute>>() {
		};

		private final String expression;

		public JsonStringFormBuilder(final String expression) {
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

	private static class ClassFormBuilder extends AttributesBasedFormBuilder {

		private final CMDataView dataView;
		private final MetadataStoreFactory metadataStoreFactory;
		private final String className;

		public ClassFormBuilder(final CMDataView dataView, final MetadataStoreFactory metadataStoreFactory,
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

	private static class FunctionFormBuilder extends AttributesBasedFormBuilder {

		private final CMDataView dataView;
		private final String functionName;

		public FunctionFormBuilder(final CMDataView dataView, final String functionName) {
			this.dataView = dataView;
			this.functionName = functionName;
		}

		@Override
		public Iterable<Attribute> attributes() {
			return from(dataView.findFunctionByName(functionName).getInputParameters()) //
					// TODO filter?
					.transform(new Function<CMFunctionParameter, Attribute>() {

						@Override
						public Attribute apply(final CMFunctionParameter input) {
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
							output.setDescription(input.getName());
							output.setUnique(false);
							output.setMandatory(false);
							output.setWritable(true);
							return output;
						}

					});
		}

	}

	private static final Marker MARKER = MarkerFactory.getMarker(CustomFormWidgetFactory.class.getName());

	private static final String WIDGET_NAME = "customForm";

	public static final String //
			REQUIRED = "Required", //
			READ_ONLY = "ReadOnly", //
			CONFIGURATION_TYPE = "ConfigurationType", //
			FORM = "Form", //
			CLASSNAME = "ClassName", //
			FUNCTIONNAME = "FunctionName", //
			LAYOUT = "Layout", //
			DISABLE_ADD_ROW = "AddRowDisabled", //
			DISABLE_IMPORT_FROM_CSV = "ImportCsvDisabled", //
			DISABLE_DELETE_ROW = "DeleteRowDisabled";

	private static final String[] KNOWN_PARAMETERS = { BUTTON_LABEL, REQUIRED, READ_ONLY, //
			CONFIGURATION_TYPE, //
			FORM, //
			CLASSNAME, //
			LAYOUT, //
			DISABLE_ADD_ROW, DISABLE_IMPORT_FROM_CSV, DISABLE_DELETE_ROW //
	};

	private static final String //
			TYPE_FORM = "form", //
			TYPE_CLASS = "class", //
			TYPE_FUNCTION = "function";

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
		widget.setForm(formBuilderOf(valueMap).build());
		widget.setLayout(String.class.cast(valueMap.get(LAYOUT)));
		widget.setAddRowDisabled(toBoolean(String.class.cast(valueMap.get(DISABLE_ADD_ROW))));
		widget.setDeleteRowDisabled(toBoolean(String.class.cast(valueMap.get(DISABLE_DELETE_ROW))));
		widget.setImportCsvDisabled(toBoolean(String.class.cast(valueMap.get(DISABLE_IMPORT_FROM_CSV))));
		widget.setVariables(extractUnmanagedParameters(valueMap, KNOWN_PARAMETERS));
		return widget;
	}

	private FormBuilder formBuilderOf(final Map<String, Object> valueMap) {
		final FormBuilder output;
		final String configurationType = String.class.cast(valueMap.get(CONFIGURATION_TYPE));
		if (TYPE_FORM.equalsIgnoreCase(configurationType)) {
			final String expression = defaultString(String.class.cast(valueMap.get(FORM)));
			Validate.isTrue(isNotBlank(expression), "invalid value for '%s'", FORM);
			output = new FallbackOnExceptionFormBuilder(new JsonStringFormBuilder(expression), new IdentityFormBuilder(
					expression));
		} else if (TYPE_CLASS.equalsIgnoreCase(configurationType)) {
			final String className = String.class.cast(valueMap.get(CLASSNAME));
			Validate.isTrue(isNotBlank(className), "invalid value for '%s'", CLASSNAME);
			output = new ClassFormBuilder(dataView, metadataStoreFactory, className);
		} else if (TYPE_FUNCTION.equalsIgnoreCase(configurationType)) {
			final String functionName = String.class.cast(valueMap.get(FUNCTIONNAME));
			Validate.isTrue(isNotBlank(functionName), "invalid value for '%s'", FUNCTIONNAME);
			output = new FunctionFormBuilder(dataView, functionName);
		} else {
			output = new InvalidFormBuilder(format("'%s' is not a valid value for '%s'", CONFIGURATION_TYPE));
		}
		return output;
	}

	private static String writeJsonString(final Collection<Attribute> attributes) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(attributes);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

}
