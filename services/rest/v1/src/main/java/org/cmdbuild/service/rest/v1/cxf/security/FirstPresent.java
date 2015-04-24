package org.cmdbuild.service.rest.v1.cxf.security;

import org.apache.cxf.message.Message;
import org.cmdbuild.service.rest.v1.cxf.security.TokenHandler.TokenExtractor;

import com.google.common.base.Optional;

public class FirstPresent implements TokenExtractor {

	public static FirstPresent firstPresent(final Iterable<? extends TokenExtractor> elements) {
		return new FirstPresent(elements);
	}

	private static Optional<String> ABSENT = Optional.absent();

	private final Iterable<? extends TokenExtractor> elements;

	private FirstPresent(final Iterable<? extends TokenExtractor> elements) {
		this.elements = elements;
	}

	@Override
	public Optional<String> extract(final Message message) {
		for (final TokenExtractor element : elements) {
			final Optional<String> optional = element.extract(message);
			if (optional.isPresent()) {
				return optional;
			}
		}
		return ABSENT;
	}

}
