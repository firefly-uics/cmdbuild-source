package org.cmdbuild.service.rest.v2.cxf.localization;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v2.cxf.util.Messages.StringFromMessage;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

import com.google.common.base.Optional;

public class LocalizationHandler implements RequestHandler, LoggingSupport {

	public static interface TokenExtractor {

		Optional<String> extract(Message message);

	}

	private final StringFromMessage localizedFromMessage;
	private final StringFromMessage localizationFromMessage;
	private final org.cmdbuild.services.localization.RequestHandler requestHandler;

	public LocalizationHandler(final StringFromMessage localized, final StringFromMessage localization,
			final org.cmdbuild.services.localization.RequestHandler requestHandler) {
		this.localizedFromMessage = localized;
		this.localizationFromMessage = localization;
		this.requestHandler = requestHandler;
	}

	@Override
	public Response handleRequest(final Message message, final ClassResourceInfo resourceClass) {
		final Optional<String> localized = localizedFromMessage.apply(message);
		final Optional<String> localization = localizationFromMessage.apply(message);
		requestHandler.setLocalized(localized.isPresent() ? toBoolean(localized.get()) : false);
		requestHandler.setLocalization(localization.isPresent() ? localization.get() : null);
		return null;
	}

}
