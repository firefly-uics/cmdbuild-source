package org.cmdbuild.servlets.json.serializers.translations.csv.read;

import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.servlets.json.schema.Translation;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SafeRecordDeserializer extends ForwardingRecordDeserializer {
	
	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(Translation.class.getName());

	private final RecordDeserializer delegate;
	private ErrorListener listener;

	public static SafeRecordDeserializer of(final RecordDeserializer delegate) {
		return new SafeRecordDeserializer(delegate);
	}

	public RecordDeserializer withErrorListener(final ErrorListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	protected RecordDeserializer delegate() {
		return delegate;
	}

	private SafeRecordDeserializer(final RecordDeserializer delegate) {
		this.delegate = delegate;
	}

	@Override
	public TranslationObject deserialize() {
		final TranslationSerialization record = getInput();
		logger.info("deserializing record '{}'", record);
		TranslationObject output = TranslationObject.INVALID;
		try {
			output = delegate().deserialize();
		} catch (final Throwable throwable) {
			listener.handleError(record, throwable);
			logger.warn(marker, "record '{}' skipped", record);
		}
		return output;
	}

}
