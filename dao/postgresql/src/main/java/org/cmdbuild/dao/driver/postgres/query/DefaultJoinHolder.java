package org.cmdbuild.dao.driver.postgres.query;

import java.util.Collection;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.collect.Sets;

class DefaultJoinHolder implements JoinHolder {

	public static class JoinElementImpl implements JoinElement {

		public final Alias from;
		public final Alias to;

		public JoinElementImpl(final Alias from, final Alias to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public Alias getFrom() {
			return from;
		}

		@Override
		public Alias getTo() {
			return to;
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(from) //
					.append(to) //
					.hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JoinElementImpl)) {
				return false;
			}
			final JoinElementImpl other = JoinElementImpl.class.cast(obj);
			return new EqualsBuilder() //
					.append(from, other.from) //
					.append(to, other.to) //
					.isEquals();
		}

	}

	private final Collection<JoinElement> elements = Sets.newHashSet();

	@Override
	public void add(final Alias from, final Alias to) {
		elements.add(new JoinElementImpl(from, to));
	}

	@Override
	public Iterable<JoinElement> getElements() {
		return elements;
	}
}
