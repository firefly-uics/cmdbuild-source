package org.cmdbuild.dao.query.clause.join;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;

public class DirectJoinClause {

	public static class Builder implements org.cmdbuild.common.Builder<DirectJoinClause> {

		private boolean left = false;
		private CMClass targetClass;
		private Alias targetClassAlias;
		private QueryAliasAttribute sourceAttribute;
		private QueryAliasAttribute targetAttribute;

		@Override
		public DirectJoinClause build() {
			Validate.notNull(targetClass);
			Validate.notNull(sourceAttribute);
			Validate.notNull(targetAttribute);
			if (targetClassAlias == null) {
				targetClassAlias = EntryTypeAlias.canonicalAlias(targetClass);
			}
			return new DirectJoinClause(this);
		}

		public Builder join(final CMClass targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public Builder leftJoin(final CMClass targetClass) {
			this.left = true;
			this.targetClass = targetClass;
			return this;
		}

		public Builder as(final Alias targetClassAlias) {
			this.targetClassAlias = targetClassAlias;
			return this;
		}

		public Builder equalsTo(final QueryAliasAttribute sourceAttribute) {
			this.sourceAttribute = sourceAttribute;
			return this;
		}

		public Builder on(final QueryAliasAttribute targetAttribute) {
			this.targetAttribute = targetAttribute;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final boolean left;
	private final CMClass targetClass;
	private final Alias targetClassAlias;
	private final QueryAliasAttribute sourceAttribute;
	private final QueryAliasAttribute targetAttribute;

	private DirectJoinClause(final Builder builder) {
		this.left = builder.left;
		this.targetClass = builder.targetClass;
		this.targetClassAlias = builder.targetClassAlias;
		this.sourceAttribute = builder.sourceAttribute;
		this.targetAttribute = builder.targetAttribute;
	}

	public CMClass getTargetClass() {
		return targetClass;
	}

	public Alias getTargetClassAlias() {
		return targetClassAlias;
	}

	public boolean isLeft() {
		return left;
	}

	public QueryAliasAttribute getSourceAttribute() {
		return sourceAttribute;
	}

	public QueryAliasAttribute getTargetAttribute() {
		return targetAttribute;
	}

}
