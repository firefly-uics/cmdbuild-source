package org.cmdbuild.dao.query.clause;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.cmdbuild.dao.query.clause.alias.Alias;

import com.google.common.base.Predicate;

public class Predicates {

	private static class WithAlias implements Predicate<QueryAttribute> {

		private final Alias alias;

		public WithAlias(final Alias alias) {
			this.alias = alias;
		}

		@Override
		public boolean apply(final QueryAttribute input) {
			return input.getAlias().equals(alias);
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof WithAlias)) {
				return false;
			}
			final WithAlias other = WithAlias.class.cast(obj);
			return (this.alias.equals(other.alias));
		}

		@Override
		public int hashCode() {
			return alias.hashCode();
		}

		@Override
		public String toString() {
			return reflectionToString(this, SHORT_PREFIX_STYLE);
		}

	}

	public static Predicate<QueryAttribute> withAlias(final Alias alias) {
		return new WithAlias(alias);
	}

	private Predicates() {
		// prevents instantiation
	}

}
