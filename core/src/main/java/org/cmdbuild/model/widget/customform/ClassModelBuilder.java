package org.cmdbuild.model.widget.customform;

import static com.google.common.collect.FluentIterable.from;
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
import org.cmdbuild.services.meta.MetadataStoreFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

class ClassModelBuilder extends AttributesBasedModelBuilder {

	private final CMDataView dataView;
	private final MetadataStoreFactory metadataStoreFactory;
	private final String className;

	public ClassModelBuilder(final CMDataView dataView, final MetadataStoreFactory metadataStoreFactory,
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
									output.setFilter(new Attribute.Filter() {
										{
											setExpression(input.getFilter());
											setContext(toMap(metadataStoreFactory.storeForAttribute(input).readAll()));
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