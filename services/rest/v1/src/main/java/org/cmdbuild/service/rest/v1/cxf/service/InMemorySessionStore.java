package org.cmdbuild.service.rest.v1.cxf.service;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.service.rest.v1.model.Session;

import com.google.common.base.Optional;

public class InMemorySessionStore implements SessionStore {

	private final Map<String, Session> map;

	public InMemorySessionStore() {
		map = newHashMap();
	}

	@Override
	public boolean has(final String id) {
		return map.containsKey(id);
	}

	@Override
	public Optional<Session> get(final String id) {
		Validate.notNull(id, "invalid id");
		final Session value = map.get(id);
		return fromNullable(value);
	}

	@Override
	public void put(final Session value) {
		Validate.notNull(value, "invalid value");
		Validate.notBlank(value.getId(), "invalid id");
		map.put(value.getId(), value);
	}

	@Override
	public void remove(final String id) {
		Validate.notNull(id, "invalid id");
		map.remove(id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof InMemorySessionStore)) {
			return false;
		}
		final InMemorySessionStore other = InMemorySessionStore.class.cast(obj);
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
		return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
	}

}
