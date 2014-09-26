package org.cmdbuild.service.rest.cxf.serialization;

import static org.cmdbuild.service.rest.dto.Builders.newCard;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.dto.Card;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public abstract class ToCardFunction<T> implements Function<T, Card> {

	public static abstract class Builder<T> implements org.apache.commons.lang3.builder.Builder<ToCardFunction<T>> {

		private CMDataView dataView;
		private ErrorHandler errorHandler;

		protected Builder() {
			// use static method
		}

		@Override
		public ToCardFunction<T> build() {
			validate();
			return doBuild();
		}

		protected abstract ToCardFunction<T> doBuild();

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

	protected ToCardFunction(final Builder<T> builder) {
		this.dataView = builder.dataView;
		this.errorHandler = builder.errorHandler;
	}

	@Override
	public Card apply(final T input) {
		final String className = classNameOf(input);
		final CMClass target = dataView.findClass(className);
		if (target == null) {
			errorHandler.classNotFound(className);
		}
		final Map<String, Object> values = Maps.newHashMap();
		// TODO effective class
		for (final Entry<String, Object> entry : valuesOf(input)) {
			values.put(entry.getKey(), ValueConverter.newInstance() //
					.withType(target) //
					.build() //
					.convert(entry.getKey(), entry.getValue()));
		}
		return newCard() //
				.withType(className) //
				.withId(idOf(input)) //
				.withValues(values) //
				.build();
	}

	protected abstract String classNameOf(T input);

	protected abstract Long idOf(T input);

	protected abstract Iterable<Entry<String, Object>> valuesOf(T input);

}
