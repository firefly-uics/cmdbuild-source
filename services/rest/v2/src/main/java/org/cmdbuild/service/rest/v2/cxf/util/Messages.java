package org.cmdbuild.service.rest.v2.cxf.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.apache.cxf.message.Message.QUERY_STRING;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class Messages {

	public static interface StringFromMessage extends Function<Message, Optional<String>> {

		Optional<String> ABSENT = Optional.absent();

		@Override
		Optional<String> apply(Message message);

	}

	public static class FirstPresentOrAbsent implements StringFromMessage {

		public static FirstPresentOrAbsent of(final Iterable<? extends StringFromMessage> elements) {
			return new FirstPresentOrAbsent(elements);
		}

		private final Iterable<? extends StringFromMessage> elements;

		private FirstPresentOrAbsent(final Iterable<? extends StringFromMessage> elements) {
			this.elements = elements;
		}

		@Override
		public Optional<String> apply(final Message message) {
			for (final StringFromMessage element : elements) {
				final Optional<String> optional = element.apply(message);
				if (optional.isPresent()) {
					return optional;
				}
			}
			return ABSENT;
		}

	}

	public static class HeaderValue implements StringFromMessage {

		public static HeaderValue of(final String name) {
			return new HeaderValue(name);
		}

		private static final Map<String, List<String>> NO_HEADERS = emptyMap();

		private final String name;

		private HeaderValue(final String name) {
			this.name = name;
		}

		@Override
		public Optional<String> apply(final Message message) {
			final Map<String, List<String>> headers = (Map<String, List<String>>) message.get(PROTOCOL_HEADERS);
			final List<String> tokens = defaultIfNull(headers, NO_HEADERS).get(name);
			return (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
		}

	}

	public static class ParameterValue implements StringFromMessage, LoggingSupport {

		public static ParameterValue of(final String name) {
			return new ParameterValue(name);
		}

		private static final String NAME_VALUES_SEPARATOR = "&";
		private static final String NAME_VALUE_SEPARATOR = "=";

		private final String name;

		private ParameterValue(final String name) {
			this.name = name;
		}

		@Override
		public Optional<String> apply(final Message message) {
			final String queryString = (String) message.get(QUERY_STRING);
			final List<String> parts = asList(split(defaultString(queryString), NAME_VALUES_SEPARATOR));
			for (final String part : parts) {
				if (part.contains(NAME_VALUE_SEPARATOR)) {
					final String[] keyValue = split(part, NAME_VALUE_SEPARATOR);
					if (keyValue.length >= 2) {
						final String name = keyValue[0];
						if (this.name.equals(name)) {
							return Optional.of(uriDecode(keyValue[1]));
						}
					}
				}
			}
			return ABSENT;
		}

		private String uriDecode(final String value) {
			try {
				return URLDecoder.decode(value, "UTF-8");
			} catch (final UnsupportedEncodingException e) {
				logger.warn(value + " can not be decoded: " + e.getMessage());
			}
			return value;
		}

	}

	private Messages() {
		// prevents instantiation
	}

}
