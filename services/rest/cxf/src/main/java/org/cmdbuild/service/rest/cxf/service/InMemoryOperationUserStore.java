package org.cmdbuild.service.rest.cxf.service;

import static com.google.common.collect.Maps.newHashMap;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.service.rest.model.Session;

import com.google.common.base.Optional;

public class InMemoryOperationUserStore implements OperationUserStore {

	private static final Optional<OperationUser> ABSENT = Optional.absent();

	private final Map<Session, OperationUser> map;

	public InMemoryOperationUserStore() {
		map = newHashMap();
	}

	@Override
	public void put(final Session key, final OperationUser value) {
		Validate.notNull(key, "invalid key");
		Validate.notNull(value, "invalid value");
		map.put(key, value);

	}

	@Override
	public Optional<OperationUser> get(final Session key) {
		Validate.notNull(key, "invalid key");
		final OperationUser value = map.get(key);
		return (value == null) ? ABSENT : Optional.of(value);
	}

	@Override
	public void remove(final Session key) {
		Validate.notNull(key, "invalid key");
		map.remove(key);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof InMemoryOperationUserStore)) {
			return false;
		}
		final InMemoryOperationUserStore other = InMemoryOperationUserStore.class.cast(obj);
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
