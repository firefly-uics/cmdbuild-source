package org.cmdbuild.service.rest.serialization;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.ForeignKeyAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.service.rest.constants.Serialization;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public abstract class FromSomeKindOfCardToMap<T> implements Function<T, Map<String, Object>> {

	public static abstract class Builder<T> implements
			org.apache.commons.lang3.builder.Builder<FromSomeKindOfCardToMap<T>> {

		private CMDataView dataView;
		private ErrorHandler errorHandler;

		protected Builder() {
			// use static method
		}

		@Override
		public FromSomeKindOfCardToMap<T> build() {
			validate();
			return doBuild();
		}

		protected abstract FromSomeKindOfCardToMap<T> doBuild();

		private void validate() {
			Validate.notNull(dataView, "invalid data view");
		}

		public Builder<T> withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder<T> withErrorHandler(final ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
			return this;
		}

	}

	private final CMDataView dataView;
	private final ErrorHandler errorHandler;

	protected FromSomeKindOfCardToMap(final Builder<T> builder) {
		this.dataView = builder.dataView;
		this.errorHandler = builder.errorHandler;
	}

	@Override
	public Map<String, Object> apply(final T input) {
		final String className = classNameOf(input);
		final CMClass target = dataView.findClass(className);
		if (target == null) {
			errorHandler.classNotFound(className);
		}
		final Map<String, Object> values = Maps.newHashMap();
		if (idAndClassnameRequired()) {
			values.put(Serialization.ID_WITH_UNSERSCORE, idOf(input));
			values.put(Serialization.CLASSNAME_WITH_UNSERSCORE, className);
		}
		// TODO effective class
		for (final Entry<String, Object> entry : valuesOf(input)) {
			values.put(entry.getKey(), new NullAttributeTypeVisitor() {

				private Object value;

				public Object value() {
					value = entry.getValue();
					target.getAttribute(entry.getKey()).getType() //
							.accept(this);
					return value;
				}

				@Override
				public void visit(final ForeignKeyAttributeType attributeType) {
					value = idFromIdAndDescription(value);
				}

				@Override
				public void visit(final LookupAttributeType attributeType) {
					value = idFromIdAndDescription(value);
				};

				@Override
				public void visit(final ReferenceAttributeType attributeType) {
					value = idFromIdAndDescription(value);
				}

				private Object idFromIdAndDescription(final Object value) {
					return IdAndDescription.class.cast(value).getId();
				}

			}.value());
		}
		return values;
	}

	protected abstract String classNameOf(T input);

	protected abstract Long idOf(T input);

	protected abstract boolean idAndClassnameRequired();

	protected abstract Iterable<Entry<String, Object>> valuesOf(T input);

}
