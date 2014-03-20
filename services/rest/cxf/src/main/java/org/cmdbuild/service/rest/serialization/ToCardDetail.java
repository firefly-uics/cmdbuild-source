package org.cmdbuild.service.rest.serialization;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.service.rest.dto.CardDetail;

import com.google.common.base.Function;

public class ToCardDetail implements Function<Card, CardDetail> {

	public static class Builder implements org.cmdbuild.common.Builder<ToCardDetail> {

		private CMDataView dataView;
		private ErrorHandler errorHandler;

		private Builder() {
			// use static method
		}

		@Override
		public ToCardDetail build() {
			validate();
			return new ToCardDetail(this);
		}

		private void validate() {
			Validate.notNull(dataView, "invalid data view");
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public Builder withErrorHandler(final ErrorHandler errorHandler) {
			this.errorHandler = errorHandler;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final CMDataView dataView;
	private final ErrorHandler errorHandler;

	private ToCardDetail(final Builder builder) {
		this.dataView = builder.dataView;
		this.errorHandler = builder.errorHandler;
	}

	@Override
	public CardDetail apply(final Card input) {
		final String className = input.getClassName();
		final CMClass target = dataView.findClass(className);
		if (target == null) {
			errorHandler.classNotFound(className);
		}
		final String descriptionAttribute = target.getDescriptionAttributeName();
		return CardDetail.newInstance() //
				.withId(input.getId()) //
				.withDescription(input.getAttribute(descriptionAttribute).toString()) //
				.build();
	}
}
