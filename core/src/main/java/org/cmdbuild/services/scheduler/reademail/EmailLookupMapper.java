package org.cmdbuild.services.scheduler.reademail;

import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.scheduler.reademail.StartWorkflow.Mapper;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class EmailLookupMapper implements Mapper {

	private static final Marker marker = MarkerFactory.getMarker(ResolverMapper.class.getName());

	private static enum Field {
		from {
			@Override
			public Object getFrom(final Email email) {
				return email.getFromAddress();
			}
		},
		subject {
			@Override
			public Object getFrom(final Email email) {
				return email.getSubject();
			}
		},
		content {
			@Override
			public Object getFrom(final Email email) {
				return email.getContent();
			}
		},
		date {
			@Override
			public Object getFrom(final Email email) {
				return email.getDate();
			}
		},
		unknown {
			@Override
			public Object getFrom(final Email email) {
				return NULL_VALUE;
			}
		};

		public abstract Object getFrom(Email email);

		public static Field from(final Object value) {
			for (final Field element : values()) {
				if (element.name().equals(value)) {
					return element;
				}
			}
			logger.warn(marker, "unknown field '{}'", value);
			return unknown;
		}

	}

	private final Email email;

	public EmailLookupMapper(final Email email) {
		this.email = email;
	}

	@Override
	public Object getValue(final String name) {
		logger.debug(marker, "getting attribute '{}' from email '{}'", name, email);
		return Field.from(name).getFrom(email);
	}

}
