package org.cmdbuild.service.rest.cxf.serialization;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TextAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.metadata.Metadata;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.dto.AttributeDetail;
import org.cmdbuild.service.rest.dto.AttributeDetail.Filter;
import org.cmdbuild.services.meta.MetadataStoreFactory;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ToAttributeDetail implements Function<CMAttribute, AttributeDetail> {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ToAttributeDetail> {

		private AttributeTypeResolver attributeTypeResolver;
		private CMDataView dataView;
		private ErrorHandler errorHandler;
		private MetadataStoreFactory metadataStoreFactory;

		private Builder() {
			// use static method
		}

		@Override
		public ToAttributeDetail build() {
			validate();
			return new ToAttributeDetail(this);
		}

		private void validate() {
			Validate.notNull(attributeTypeResolver, "missing '%s'", AttributeTypeResolver.class);
			Validate.notNull(dataView, "missing '%s'", CMDataView.class);
			Validate.notNull(errorHandler, "missing '%s'", ErrorHandler.class);
			Validate.notNull(metadataStoreFactory, "missing '%s'", MetadataStoreFactory.class);
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

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final AttributeTypeResolver attributeTypeResolver;
	private final CMDataView dataView;
	private final ErrorHandler errorHandler;
	private final MetadataStoreFactory metadataStoreFactory;

	private ToAttributeDetail(final Builder builder) {
		this.attributeTypeResolver = builder.attributeTypeResolver;
		this.dataView = builder.dataView;
		this.errorHandler = builder.errorHandler;
		this.metadataStoreFactory = builder.metadataStoreFactory;
	}

	@Override
	public AttributeDetail apply(final CMAttribute input) {
		final AttributeDetail.Builder builder = AttributeDetail.newInstance() //
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
				.withGroup(input.getGroup());
		new NullAttributeTypeVisitor() {

			private CMAttribute attribute;
			private AttributeDetail.Builder builder;

			public void fill(final CMAttribute attribute, final AttributeDetail.Builder builder) {
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
				builder.withTargetClass(attributeType.getForeignKeyDestinationClassName());
			}

			@Override
			public void visit(final LookupAttributeType attributeType) {
				builder.withLookupType(attributeType.getLookupTypeName());
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

				builder.withTargetClass(target.getIdentifier().getLocalName()) //
						.withFilter(Filter.newInstance() //
								.withText(attribute.getFilter()) //
								.withParams(toMap(metadataStoreFactory.storeForAttribute(attribute).readAll())) //
								.build());
			};

			private Map<String, String> toMap(final Collection<Metadata> elements) {
				final Map<String, String> map = Maps.newHashMap();
				for (final Metadata element : elements) {
					map.put(element.name, element.value);
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
