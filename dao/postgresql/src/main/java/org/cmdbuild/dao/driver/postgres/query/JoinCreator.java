package org.cmdbuild.dao.driver.postgres.query;

import static org.cmdbuild.dao.driver.postgres.Const.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Const.STATUS_ACTIVE_VALUE;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;

import java.util.List;
import java.util.Set;

import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;

public class JoinCreator extends PartCreator {

	private abstract class UnionCreator<T> { // TODO: use CMEntryType instead of <T>

		private final Iterable<T> typeSet;
		protected final Alias typeAlias;
		private boolean includeHistoryTable;

		UnionCreator(final Set<T> typeSet, final Alias typeAlias, final boolean includeHistoryTable) {
			this.typeSet = typeSet;
			this.typeAlias = typeAlias;
			this.includeHistoryTable = includeHistoryTable;
		}

		public void append() {
			sb.append("(");
			boolean first = true;
			for (T type : typeSet) {
				if (includeHistoryTable) {
					appendTableSelect(type, true, first);
					first = false;
				}
				appendTableSelect(type, false, first);
				first = false;
			}
			sb.append(")");
		}

		private void appendTableSelect(T type, boolean isHistoryAppend, boolean first) {
			final CMEntryType entryType = getEntryType(type);
			final String quotedTableName = isHistoryAppend ? Utils.quoteTypeHistory(entryType) : Utils.quoteType(entryType);
			if (!first) {
				sb.append(" UNION ALL ");
			}
			sb.append("SELECT ");
			appendSystemAttributes(type, isHistoryAppend, first);
			appendUserAttributes(type, first);
			sb.append(" FROM ONLY ").append(quotedTableName);
			appendStatusWhere(isHistoryAppend);
		}

		protected void appendStatusWhere(boolean isHistoryAppend) {
			if (isHistoryAppend) {
				return;
			}
			sb.append(" WHERE ")
					.append(quoteIdent(SystemAttributes.Status))
					.append(OPERATOR_EQ)
					.append(param(STATUS_ACTIVE_VALUE));
		}

		abstract void appendSystemAttributes(T type, boolean isHistoryAppend, boolean first);

		void appendUserAttributes(T type, final boolean first) {
			final CMEntryType entryType = getEntryType(type);
			for (EntryTypeAttribute eta : columnMapper.getEntryTypeAttributes(typeAlias, entryType)) {
				final boolean nullValue = (eta.name == null);
				sb.append(",");
				if (nullValue) {
					sb.append(Const.NULL);
				} else {
					sb.append(quoteIdent(eta.name));
				}
				if (first) {
					if (nullValue) {
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

	public JoinCreator(final Alias fromAlias, final List<JoinClause> joins, final ColumnMapper columnMapper) {
		this.fromAlias = fromAlias;
		this.columnMapper = columnMapper;
		for (JoinClause j : joins) {
			appendJoinWithDomainAndTarget(j);
		}
	}

	private void appendJoinWithDomainAndTarget(final JoinClause j) {
		if (!j.getQueryDomains().isEmpty() && !j.getTargets().isEmpty()) {
			appendDomainJoin(j);
			appendTargetJoin(j);
		}
	}

	private void appendDomainJoin(final JoinClause j) {
		sb.append("JOIN ");
		appendDomainUnion(j);
		sb.append(" AS ").append(quoteAlias(j.getDomainAlias())).append(" ON ")
				.append(quoteAttribute(fromAlias, SystemAttributes.Id)).append(OPERATOR_EQ)
				.append(quoteAttribute(j.getDomainAlias(), SystemAttributes.DomainId1));
	}

	private void appendDomainUnion(final JoinClause j) {
		final boolean includeHistoryTable = j.isDomainHistory();
		new UnionCreator<QueryDomain>(j.getQueryDomains(), j.getDomainAlias(), includeHistoryTable) {
			@Override
			void appendSystemAttributes(final QueryDomain queryDomain, final boolean isHistoryAppend, final boolean first) {
				final String endDateField = isHistoryAppend ? quoteIdent(SystemAttributes.EndDate) : "NULL";
				sb.append(quoteIdent(SystemAttributes.Id)).append(",")
					.append(quoteIdent(SystemAttributes.DomainId)).append(",");
				appendColumnAndAliasIfFirst(param(queryDomain.getQuerySource()), quoteIdent(SystemAttributes.DomainQuerySource), first).append(",");
				if (queryDomain.getDirection()) {
					sb.append(quoteIdent(SystemAttributes.DomainId1)).append(",")
						.append(quoteIdent(SystemAttributes.DomainId2));
				} else {
					appendColumnAndAliasIfFirst(quoteIdent(SystemAttributes.DomainId2),
							quoteIdent(SystemAttributes.DomainId1), first).append(",");
					appendColumnAndAliasIfFirst(quoteIdent(SystemAttributes.DomainId1),
							quoteIdent(SystemAttributes.DomainId2), first);
				}
				sb.append(",")
					.append(quoteIdent(SystemAttributes.User)).append(",")
					.append(quoteIdent(SystemAttributes.BeginDate)).append(",");
				appendColumnAndAliasIfFirst(endDateField, quoteIdent(SystemAttributes.EndDate), first);
			}

			@Override
			protected CMEntryType getEntryType(final QueryDomain queryDomain) {
				return queryDomain.getDomain();
			}
		}.append();
	}

	private void appendTargetJoin(final JoinClause j) {
		sb.append(" JOIN ");
		appendClassUnion(j);
		sb.append(" AS ").append(quoteAlias(j.getTargetAlias())).append(" ON ")
				.append(quoteAttribute(j.getDomainAlias(), SystemAttributes.DomainId2)).append(OPERATOR_EQ)
				.append(quoteAttribute(j.getTargetAlias(), SystemAttributes.Id));
	}

	private void appendClassUnion(final JoinClause j) {
		final boolean includeStatusCheck = !j.isDomainHistory();
		final boolean includeHistoryTable = false;
		new UnionCreator<CMClass>(j.getTargets(), j.getTargetAlias(), includeHistoryTable) {
			@Override
			void appendSystemAttributes(final CMClass type, final boolean isHistoryAppend, final boolean first) {
				sb.append(quoteIdent(SystemAttributes.Id)).append(",").append(quoteIdent(SystemAttributes.ClassId))
						.append(",").append(quoteIdent(SystemAttributes.User))
						.append(",").append(quoteIdent(SystemAttributes.BeginDate))
						.append(", NULL AS ").append(quoteIdent(SystemAttributes.EndDate));
			}

			@Override
			protected CMEntryType getEntryType(final CMClass type) {
				return type;
			}

			@Override
			protected void appendStatusWhere(boolean isHistoryAppend) {
				if (includeStatusCheck) {
					super.appendStatusWhere(isHistoryAppend);
				}
			}
		}.append();
	}
}