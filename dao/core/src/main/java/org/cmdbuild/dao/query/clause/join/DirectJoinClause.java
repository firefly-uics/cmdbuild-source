package org.cmdbuild.dao.query.clause.join;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;

public class DirectJoinClause {

	public static class DirectJoinClauseBuilder implements Builder<DirectJoinClause> {

		private boolean left = false;
		private CMClass targetClass;
		private Alias targetClassAlias;
		private QueryAliasAttribute sourceAttribute;
		private QueryAliasAttribute targetAttribute;

		public DirectJoinClauseBuilder join(CMClass targetClass) {
			this.targetClass = targetClass;
			return this;
		}

		public DirectJoinClauseBuilder leftJoin(CMClass targetClass) {
			this.left = true;
			this.targetClass = targetClass;
			return this;
		}

		public DirectJoinClauseBuilder as(Alias targetClassAlias) {
			this.targetClassAlias = targetClassAlias;
			return this;
		}

		public DirectJoinClauseBuilder equalsTo(QueryAliasAttribute sourceAttribute) {
			this.sourceAttribute = sourceAttribute;
			return this;
		}

		public DirectJoinClauseBuilder on(QueryAliasAttribute targetAttribute) {
			this.targetAttribute = targetAttribute;
			return this;
		}

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

	}

	private final boolean left;
	private final CMClass targetClass;
	private final Alias targetClassAlias;
	private final QueryAliasAttribute sourceAttribute;
	private final QueryAliasAttribute targetAttribute;

	public static DirectJoinClauseBuilder newDirectJoinClause() {
		return new DirectJoinClauseBuilder();
	}

	private DirectJoinClause(final DirectJoinClauseBuilder builder) {
		this.left = builder.left;
		this.targetClass = builder.targetClass;
		this.targetClassAlias = builder.targetClassAlias;
		this.sourceAttribute = builder.sourceAttribute;
		this.targetAttribute = builder.targetAttribute;
	}

	public CMClass targetClass() {
		return targetClass;
	}

	public Alias targetClassAlias() {
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
