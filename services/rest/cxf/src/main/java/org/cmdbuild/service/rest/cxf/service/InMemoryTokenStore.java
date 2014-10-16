package org.cmdbuild.service.rest.cxf.service;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;

public class InMemoryTokenStore implements TokenStore {

	private static final Optional<Session> ABSENT = Optional.absent();

	private final Map<String, Session> map;

	public InMemoryTokenStore() {
		map = newHashMap();
	}

	@Override
	public void put(final String token, final Session credentials) {
		Validate.notNull(token, "invalid token");
		Validate.notNull(credentials, "invalid credentials");
		map.put(token, credentials);
	}

	@Override
	public Optional<Session> get(final String token) {
		Validate.notNull(token, "invalid token");
		final Session credentials = map.get(token);
		return (credentials == null) ? ABSENT : Optional.of(credentials);
	}

	@Override
	public void remove(final String token) {
		Validate.notNull(token, "invalid token");
		map.remove(token);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof InMemoryTokenStore)) {
			return false;
		}
		final InMemoryTokenStore other = InMemoryTokenStore.class.cast(obj);
		return new EqualsBuilder() //
				.append(this.map, other.map).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder() //
				.append(map) //
				.toHashCode();
	}

	@Override
	public final String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

}
