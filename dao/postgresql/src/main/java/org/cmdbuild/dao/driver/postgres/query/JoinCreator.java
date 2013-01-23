package org.cmdbuild.dao.driver.postgres.query;

import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.BeginDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.ClassId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId1;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainId2;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.DomainQuerySource;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.EndDate;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Status;
import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.User;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;

import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;

public class JoinCreator extends PartCreator {

	private enum DataQueryType {

		HISTORIC {

			@Override
			String quoteTypeName(final CMEntryType type) {
				return Utils.quoteTypeHistory(type);
			}

			@Override
			String quotedEndDateAttribute() {
				return quoteIdent(EndDate);
			}

		},
		CURRENT {

			@Override
			String quoteTypeName(final CMEntryType type) {
				return Utils.quoteType(type);
			}

			@Override
			String quotedEndDateAttribute() {
				return "NULL";
			}

		};

		abstract String quoteTypeName(final CMEntryType type);

		abstract String quotedEndDateAttribute();

	}

	private abstract class UnionCreator<T> {

		private final Iterable<T> typeSet;
		protected final Alias typeAlias;
		private final boolean includeHistoryTable;

		UnionCreator(final Iterable<T> typeSet, final Alias typeAlias, final boolean includeHistoryTable) {
			this.typeSet = typeSet;
			this.typeAlias = typeAlias;
			this.includeHistoryTable = includeHistoryTable;
		}

		public void append() {
			sb.append("(");
			boolean first = true;
			for (final T type : typeSet) {
				if (includeHistoryTable) {
					appendTableSelect(type, DataQueryType.HISTORIC, first);
					first = false;
				}
				appendTableSelect(type, DataQueryType.CURRENT, first);
				first = false;
			}
			sb.append(")");
		}

		private void appendTableSelect(final T type, final DataQueryType dataQueryType, final boolean first) {
			final String quotedTableName = dataQueryType.quoteTypeName(getEntryType(type));
			if (!first) {
				sb.append(" UNION ALL ");
			}
			sb.append("SELECT ");
			appendSystemAttributes(type, dataQueryType, first);
			appendUserAttributes(type, first);
			sb.append(" FROM ")
			/*
			 * TODO check if this is really needed
			 * 
			 * .append("ONLY ")
			 */
			.append(quotedTableName);
			appendStatusWhere(dataQueryType);
		}

		protected void appendStatusWhere(final DataQueryType dataQueryType) {
			if (dataQueryType == DataQueryType.CURRENT) {
				sb.append(" WHERE ").append(quoteIdent(Status)).append(OPERATOR_EQ)
						.append(param(CardStatus.ACTIVE.value()));
			}
		}

		abstract void appendSystemAttributes(T type, final DataQueryType dataQueryType, boolean first);

		void appendUserAttributes(final T type, final boolean first) {
			final CMEntryType entryType = getEntryType(type);
			for (final EntryTypeAttribute eta : columnMapper.getAttributes(typeAlias, entryType)) {
				final boolean nullValue = (eta.name == null);
				sb.append(",");
				if (nullValue) {
					sb.append(Const.NULL);
				} else {
					sb.append(quoteIdent(eta.name));
				}
				if (first) {
					if (nullValue) {
						// Null values need an explicit cast
						sb.append("::").append(eta.sqlTypeString);
					}
					if (eta.alias != null) {
						sb.append(" AS ").append(quoteIdent(eta.alias.getName()));
					}
				}
			}
		}

		abstract protected CMEntryType getEntryType(T type);

		protected final StringBuilder appendColumnAndAliasIfFirst(final Object attribute, final String alias,
				final boolean isFirst) {
			sb.append(attribute).append(" AS ").append(alias);
			return sb;
		}
	}

	private final Alias fromAlias;
	private final ColumnMapper columnMapper;

	public JoinCreator(final Alias fromAlias, final Iterable<JoinClause> joinClauses, final ColumnMapper columnMapper) {
		this.fromAlias = fromAlias;
		this.columnMapper = columnMapper;
		for (final JoinClause joinClause : joinClauses) {
			appendJoinWithDomainAndTarget(joinClause);
		}
	}

	private void appendJoinWithDomainAndTarget(final JoinClause joinClause) {
		if (joinClause.hasQueryDomains() && joinClause.hasTargets()) {
			appendDomainJoin(joinClause);
			appendTargetJoin(joinClause);
		}
	}

	private void appendDomainJoin(final JoinClause joinClause) {
		if (joinClause.isLeft()) {
			sb.append("LEFT ");
		}
		sb.append("JOIN ");
		appendDomainUnion(joinClause);
		sb.append(" AS ").append(quoteAlias(joinClause.getDomainAlias())).append(" ON ")
				.append(quoteAttribute(fromAlias, Id)).append(OPERATOR_EQ)
				.append(quoteAttribute(joinClause.getDomainAlias(), DomainId1));
	}

	private void appendDomainUnion(final JoinClause joinClause) {
		final boolean includeHistoryTable = joinClause.isDomainHistory();
		new UnionCreator<QueryDomain>(joinClause.getQueryDomains(), joinClause.getDomainAlias(), includeHistoryTable) {

			@Override
			void appendSystemAttributes(final QueryDomain queryDomain, final DataQueryType dataQueryType,
					final boolean first) {
				final String endDateField = dataQueryType.quotedEndDateAttribute();
				sb.append(quoteIdent(Id)).append(",").append(quoteIdent(DomainId)).append(",");
				appendColumnAndAliasIfFirst(param(queryDomain.getQuerySource()), quoteIdent(DomainQuerySource), first)
						.append(",");
				if (queryDomain.getDirection()) {
					sb.append(quoteIdent(DomainId1)).append(",").append(quoteIdent(DomainId2));
				} else {
					appendColumnAndAliasIfFirst(quoteIdent(DomainId2), quoteIdent(DomainId1), first).append(",");
					appendColumnAndAliasIfFirst(quoteIdent(DomainId1), quoteIdent(DomainId2), first);
				}
				sb.append(",").append(quoteIdent(User)).append(",").append(quoteIdent(BeginDate)).append(",");
				appendColumnAndAliasIfFirst(endDateField, quoteIdent(EndDate), first);
			}

			@Override
			protected CMEntryType getEntryType(final QueryDomain queryDomain) {
				return queryDomain.getDomain();
			}

		}.append();
	}

	private void appendTargetJoin(final JoinClause joinClause) {
		if (joinClause.isLeft()) {
			sb.append(" LEFT ");
		}
		sb.append(" JOIN ");
		appendClassUnion(joinClause);
		sb.append(" AS ").append(quoteAlias(joinClause.getTargetAlias())).append(" ON ")
				.append(quoteAttribute(joinClause.getDomainAlias(), DomainId2)).append(OPERATOR_EQ)
				.append(quoteAttribute(joinClause.getTargetAlias(), Id));
	}

	private void appendClassUnion(final JoinClause joinClause) {
		final boolean includeStatusCheck = !joinClause.isDomainHistory();
		final boolean includeHistoryTable = false;
		new UnionCreator<CMClass>(joinClause.getTargets(), joinClause.getTargetAlias(), includeHistoryTable) {

			@Override
			void appendSystemAttributes(final CMClass type, final DataQueryType dataQueryType, final boolean first) {
				sb.append(quoteIdent(Id)).append(",").append(quoteIdent(ClassId)).append(",").append(quoteIdent(User))
						.append(",").append(quoteIdent(BeginDate)).append(", NULL AS ").append(quoteIdent(EndDate));
			}

			@Override
			protected CMEntryType getEntryType(final CMClass type) {
				return type;
			}

			@Override
			protected void appendStatusWhere(final DataQueryType dataQueryType) {
				if (includeStatusCheck) {
					super.appendStatusWhere(dataQueryType);
				}
			}

		}.append();
	}
}
