package org.cmdbuild.service.rest.v1.cxf.security;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.cxf.message.Message.QUERY_STRING;
import static org.cmdbuild.service.rest.v1.cxf.security.Token.TOKEN_KEY;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;
import org.cmdbuild.service.rest.v1.logging.LoggingSupport;

import com.google.common.base.Optional;

public class QueryStringTokenExtractor implements TokenExtractor, LoggingSupport {

	private static final String NAME_VALUES_SEPARATOR = "&";
	private static final String NAME_VALUE_SEPARATOR = "=";

	private static final Optional<String> ABSENT = Optional.absent();

	@Override
	public Optional<String> extract(final Message message) {
		final String queryString = (String) message.get(QUERY_STRING);
		final List<String> parts = asList(split(defaultString(queryString), NAME_VALUES_SEPARATOR));
		for (final String part : parts) {
			if (part.contains(NAME_VALUE_SEPARATOR)) {
				final String[] keyValue = split(part, NAME_VALUE_SEPARATOR);
				if (keyValue.length >= 2) {
					final String name = keyValue[0];
					if (TOKEN_KEY.equals(name)) {
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
