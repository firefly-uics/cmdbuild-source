package org.cmdbuild.service.rest.v1.cxf.security;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.cxf.message.Message.PROTOCOL_HEADERS;
import static org.cmdbuild.service.rest.v1.cxf.security.Token.TOKEN_KEY;

import java.util.List;
import java.util.Map;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;

import com.google.common.base.Optional;

public class HeaderTokenExtractor implements TokenExtractor {

	private static final Map<String, List<String>> NO_HEADERS = emptyMap();
	private static final Optional<String> ABSENT = Optional.absent();

	@Override
	public Optional<String> extract(final Message message) {
		final Map<String, List<String>> headers = (Map<String, List<String>>) message.get(PROTOCOL_HEADERS);
		final List<String> tokens = defaultIfNull(headers, NO_HEADERS).get(TOKEN_KEY);
		return (tokens == null || tokens.isEmpty()) ? ABSENT : Optional.of(tokens.get(0));
	}

}
