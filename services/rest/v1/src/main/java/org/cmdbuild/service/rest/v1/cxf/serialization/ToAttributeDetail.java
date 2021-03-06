package org.cmdbuild.service.rest.v1.cxf.serialization;

import static org.cmdbuild.dao.entrytype.CMAttribute.Mode.HIDDEN;
import static org.cmdbuild.dao.entrytype.CMAttribute.Mode.WRITE;
import static org.cmdbuild.service.rest.v1.model.Models.newAttribute;
import static org.cmdbuild.service.rest.v1.model.Models.newFilter;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForwardingAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.data.store.metadata.Metadata;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.service.rest.v1.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v1.model.Attribute;
import org.cmdbuild.service.rest.v1.model.Models.AttributeBuilder;
import org.cmdbuild.services.meta.MetadataStoreFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ToAttributeDetail implements Function<CMAttribute, Attribute> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToAttributeDetail> {

		private ErrorHandler errorHandler;
		private AttributeTypeResolver attributeTypeResolver;
		private CMDataView dataView;
		private MetadataStoreFactory metadataStoreFactory;
		private LookupLogic lookupLogic;

		private Builder() {
			// use static method
		}

		@Override
		public ToAttributeDetail build() {
			validate();
			return new ToAttributeDetail(this);
		}

		private void validate() {
			Validate.notNull(errorHandler, "missing '%s'", ErrorHandler.class);
			Validate.notNull(attributeTypeResolver, "missing '%s'", AttributeTypeResolver.class);
			Validate.notNull(dataView, "missing '%s'", CMDataView.class);
			Validate.notNull(metadataStoreFactory, "missing '%s'", MetadataStoreFactory.class);
			Validate.notNull(lookupLogic, "missing '%s'", LookupLogic.class);
		}

		public Builder withAttributeTypeResolver(final AttributeTypeResolver attributeTypeResolver) {
			this.attributeTypeResolver = attributeTypeResolver;
			return this;
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder withErrorHandler(final ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
			return this;
		}

		public Builder withMetadataStoreFactory(final MetadataStoreFactory metadataStoreFactory) {
			this.metadataStoreFactory = metadataStoreFactory;
			return this;
		}

		public Builder withLookupLogic(final LookupLogic lookupLogic) {
			this.lookupLogic = lookupLogic;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final ErrorHandler errorHandler;
	private final AttributeTypeResolver attributeTypeResolver;
	private final CMDataView dataView;
	private final MetadataStoreFactory metadataStoreFactory;
	private final LookupLogic lookupLogic;

	private ToAttributeDetail(final Builder builder) {
		this.attributeTypeResolver = builder.attributeTypeResolver;
		this.dataView = builder.dataView;
		this.errorHandler = builder.errorHandler;
		this.metadataStoreFactory = builder.metadataStoreFactory;
		this.lookupLogic = builder.lookupLogic;
	}

	@Override
	public Attribute apply(final CMAttribute input) {
		final AttributeBuilder builder = newAttribute() //
				.withId(input.getName()) //
				.withType(attributeTypeResolver.resolve(input).asString()) //
				.withName(input.getName()) //
				.withDescription(input.getDescription()) //
				.thatIsDisplayableInList(input.isDisplayableInList()) //
				.thatIsUnique(input.isUnique()) //
				.thatIsMandatory(input.isMandatory()) //
				.thatIsInherited(input.isInherited()) //
				.thatIsActive(input.isActive()) //
				.withIndex(Long.valueOf(input.getIndex())) //
				.withDefaultValue(input.getDefaultValue()) //
				.withGroup(input.getGroup()) //
				.thatIsWritable(WRITE.equals(input.getMode())) //
				.thatIsHidden(HIDDEN.equals(input.getMode()));
		new ForwardingAttributeTypeVisitor() {

			private final CMAttributeTypeVisitor DELEGATE = NullAttributeTypeVisitor.getInstance();

			private CMAttribute attribute;
			private AttributeBuilder builder;

			@Override
			protected CMAttributeTypeVisitor delegate() {
				return DELEGATE;
			}

			public void fill(final CMAttribute attribute, final AttributeBuilder builder) {
				this.attribute = attribute;
				this.builder = builder;
				attribute.getType().accept(this);
			}

			@Override
			public void visit(final DecimalAttributeType attributeType) {
				builder.withPrecision(Long.valueOf(attributeType.precision)) //
						.withScale(Long.valueOf(attributeType.scale));
			}

			@Override
			public void visit(final ForeignKeyAttributeType attributeType) {
				final String className = attributeType.getForeignKeyDestinationClassName();
				final CMClass found = dataView.findClass(className);
				if (found == null) {
					errorHandler.classNotFound(className);
				}

				builder.withTargetClass(found.getName());
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				final String name = attributeType.getLookupTypeName();
				final LookupType found = lookupLogic.typeFor(name);
				if (found == null) {
					errorHandler.lookupTypeNotFound(name);
				}
				builder.withLookupType(name);
			};

			@Override
			public void visit(final ReferenceAttributeType attributeType) {
				final String domainName = attributeType.getDomainName();
				final CMDomain domain = dataView.findDomain(domainName);
				if (domain == null) {
					errorHandler.domainNotFound(domainName);
				}

				final String domainCardinality = domain.getCardinality();
				CMClass target = null;
				if ("N:1".equals(domainCardinality)) {
					target = domain.getClass2();
				} else if ("1:N".equals(domainCardinality)) {
					target = domain.getClass1();
				}

				builder.withTargetClass(target.getName()) //
						.withDomainName(domain.getName()) //
						.withFilter(newFilter() //
								.withText(attribute.getFilter()) //
								.withParams(toMap(metadataStoreFactory.storeForAttribute(attribute).readAll())) //
								.build());
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
				builder.withLength(Long.valueOf(attributeType.length));
			}

			@Override
			public void visit(final TextAttributeType attributeType) {
				builder.withEditorType(attribute.getEditorType());
			}

		}.fill(input, builder);
		return builder.build();
	}

}
