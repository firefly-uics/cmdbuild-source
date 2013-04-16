package org.cmdbuild.cql.sqlbuilder;

import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;

import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mapping.FilterMapper;

import com.google.common.collect.Lists;

class CqlFilterMapper implements FilterMapper {

	public static class CqlFilterMapperBuilder implements Builder<FilterMapper> {

		private CMEntryType entryType;
		private final List<WhereClause> whereClauses = Lists.newArrayList();
		private final List<JoinElement> joinElements = Lists.newArrayList();

		@Override
		public FilterMapper build() {
			return new CqlFilterMapper(this);
		}

		public CqlFilterMapperBuilder withEntryType(final CMClass value) {
			this.entryType = value;
			return this;
		}

		public CqlFilterMapperBuilder add(final WhereClause value) {
			whereClauses.add(value);
			return this;
		}

		public CqlFilterMapperBuilder add(final JoinElement value) {
			joinElements.add(value);
			return this;
		}

	}

	public static CqlFilterMapperBuilder newInstance() {
		return new CqlFilterMapperBuilder();
	}

	private final CMEntryType entryType;
	private final WhereClause whereClause;
	private final Iterable<JoinElement> joinElements;

	private CqlFilterMapper(final CqlFilterMapper.CqlFilterMapperBuilder builder) {
		this.entryType = builder.entryType;

		if (builder.whereClauses.isEmpty()) {
			this.whereClause = trueWhereClause();
		} else if (builder.whereClauses.size() == 1) {
			this.whereClause = builder.whereClauses.get(0);
		} else if (builder.whereClauses.size() == 2) {
			this.whereClause = and(builder.whereClauses.get(0), builder.whereClauses.get(1));
		} else {
			this.whereClause = and( //
					builder.whereClauses.get(0), //
					builder.whereClauses.get(1), //
					builder.whereClauses.subList(2, builder.whereClauses.size()) //
							.toArray(new WhereClause[builder.whereClauses.size() - 2]));
		}

		this.joinElements = builder.joinElements;
	}

	@Override
	public CMEntryType entryType() {
		return entryType;
	}

	@Override
	public WhereClause whereClause() {
		return whereClause;
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		return joinElements;
	}

}
