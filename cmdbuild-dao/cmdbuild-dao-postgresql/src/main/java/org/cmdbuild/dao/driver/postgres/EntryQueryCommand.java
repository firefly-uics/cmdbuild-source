package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Utils.STATUS_ACTIVE_VALUE;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.driver.postgres.Utils.SystemAttributes;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.DBQueryRow;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.AnyAttribute;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClauseVisitor;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class EntryQueryCommand {

	private final PostgresDriver driver;
	private final JdbcTemplate jdbcTemplate;
	private final QuerySpecs querySpecs;

	EntryQueryCommand(final PostgresDriver driver, final JdbcTemplate jdbcTemplate, final QuerySpecs querySpecs) {
		this.driver = driver;
		this.jdbcTemplate = jdbcTemplate;
		this.querySpecs = querySpecs;
	}

	public CMQueryResult run() {
		final QueryCreator qc = new QueryCreator(querySpecs);
		final ResultFiller rch = new ResultFiller(qc);
		jdbcTemplate.query(qc.getQuery(), qc.getParams().toArray(), rch);
		return rch.getResult();
	}

	private class ResultFiller implements RowCallbackHandler {

		final QueryCreator qc;

		final int start;
		final int end;

		final DBQueryResult result;

		private ResultFiller(final QueryCreator qc) {
			this.qc = qc;
			result = new DBQueryResult();
			start = (querySpecs.getOffset() != null) ? querySpecs.getOffset() : 0;
			end = (querySpecs.getLimit() != null) ? start + querySpecs.getLimit() : Integer.MAX_VALUE;
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			final int rowNum = result.getAndIncrementTotalSize();
			if (rowNum >= start && rowNum < end) {
				final DBQueryRow row = new DBQueryRow();

				for (Alias a : qc.getClassAliases()) {
					// Always extract a Long for the Id even if it's integer
					final Object id = rs.getLong(getAlias(a, SystemAttributes.Id));
					final Long classId = rs.getLong(getAlias(a, SystemAttributes.ClassId));
					final DBClass realClass = driver.findClassById(classId);
					final DBCard card = DBCard.create(driver, realClass, id);

					final DateTime beginDate = new DateTime(rs.getTimestamp(getAlias(a, SystemAttributes.BeginDate)).getTime());
					card.setBeginDate(beginDate); // TODO

					row.setCard(a, card);
				}

				for (Alias a : qc.getDomainAliases()) {
					final Object id = rs.getLong(getAlias(a, SystemAttributes.Id));
					final Long domainId = rs.getLong(getAlias(a, SystemAttributes.DomainId));
					final String querySource = rs.getString(getAlias(a, SystemAttributes.DomainQuerySource));
					final DBDomain realDomain = driver.findDomainById(domainId);
					final DBRelation relation = DBRelation.create(driver, realDomain, id);

					final DateTime beginDate = new DateTime(rs.getTimestamp(getAlias(a, SystemAttributes.BeginDate)).getTime());
					relation.setBeginDate(beginDate);
					// TODO Add card1 and card2 from the cards already extracted!

					final QueryRelation queryRelation = QueryRelation.create(relation, querySource);
					row.setRelation(a, queryRelation);
				}

				for (QueryAliasAttribute a : querySpecs.getAttributes()) {
					if (a instanceof AnyAttribute) {
						// FIXME
						continue;
					}
					final int column = qc.getIndexFor(a);
					row.setValue(a.getEntryTypeAlias(), a.getName(), rs.getObject(column));
				}

				result.add(row);
			}
		}

		CMQueryResult getResult() {
			return result;
		}
	}

	private class QueryCreator {
		private final StringBuilder sb;
		private final QuerySpecs query;
		private final List<Object> params;

		private Integer columnIndex;
		private final List<String> selectAttributes;
		private final Map<QueryAliasAttribute, Integer> columns;

		QueryCreator(final QuerySpecs query) {
			this.sb = new StringBuilder();
			this.query = query;
			this.params = new ArrayList<Object>();

			this.columnIndex = 0;
			this.selectAttributes = new ArrayList<String>();
			this.columns = new HashMap<QueryAliasAttribute, Integer>();

			buildQuery();
		}

		private void buildQuery() {
			appendSelect();
			appendFrom();
			appendJoin();
			appendWhere();
		}

		private void appendSelect() {
			sb.append("SELECT ").append(quoteAttributes(query.getAttributes()));
		}

		private String quoteAttributes(final Iterable<QueryAliasAttribute> attributes) {
			
			addSystemSelectAttributes(attributes);
			for (QueryAliasAttribute a : attributes) {
				addUserSelectAttribute(a);
			}
			return StringUtils.join(selectAttributes, ",");
		}

		private void addSystemSelectAttributes(final Iterable<QueryAliasAttribute> attributes) {
			// FIXME! Anyway tableoid can't be used because of the history table

			for (Alias a : query.getClassAliases()) {
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.ClassId));
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.Id));
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.BeginDate));
			}

			for (Alias a : query.getDomainAliases()) {
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.DomainId));
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.DomainQuerySource));
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.Id));
				addSystemSelectAttribute(getSelectString(a, SystemAttributes.BeginDate));
			}
		}

		private void addSystemSelectAttribute(final String selectAttributeString) {
			++columnIndex;
			selectAttributes.add(selectAttributeString);
		}

		private void addUserSelectAttribute(final QueryAliasAttribute a) {
			columns.put(a, ++columnIndex);
			selectAttributes.add(quoteAttribute(a));
		}

		private String getSelectString(final Alias entityTypeAlias, final SystemAttributes sa) {
			return String.format("%s%s AS %s", quoteAttribute(entityTypeAlias, sa.getDBName()),
					sa.getCastSuffix(), getAlias(entityTypeAlias, sa));
		}

		private void appendFrom() {
			final ClassAlias from = query.getDBFrom();
			sb.append(" FROM ").append(quoteType(from.getType())).append(" AS ").append(quoteAlias(from.getAlias()));
		}

		private void appendJoin() {
			final PartCreator joinCreator = new JoinCreator(query.getDBFrom().getAlias(), query.getJoins());
			appendPart(joinCreator);
		}

		private void appendWhere() {
			final PartCreator wherePartCreator = new WherePartCreator(query.getDBFrom().getAlias(),
					query.getWhereClause());
			appendPart(wherePartCreator);
		}

		private void appendPart(final PartCreator partCreator) {
			sb.append(" ").append(partCreator.getPart());
			params.addAll(partCreator.getParams());
		}

		public String getQuery() {
			return sb.toString();
		}

		public List<Object> getParams() {
			return params;
		}

		public int getIndexFor(final QueryAliasAttribute qa) {
			final Integer i = columns.get(qa);
			if (i == null) {
				throw new IllegalArgumentException("Invalid column");
			} else {
				return i;
			}
		}

		public Set<Alias> getClassAliases() {
			return query.getClassAliases();
		}

		public Set<Alias> getDomainAliases() {
			return query.getDomainAliases();
		}
	}

	private abstract class PartCreator {
		protected final StringBuilder sb;
		private final List<Object> params;

		protected PartCreator() {
			sb = new StringBuilder();
			params = new ArrayList<Object>();
		}

		public final String getPart() {
			return sb.toString();
		}

		protected final void addParam(final Object o) {
			params.add(o);
		}

		public final List<Object> getParams() {
			return params;
		}
	}

	private class JoinCreator extends PartCreator {
		private final Alias fromAlias;

		JoinCreator(Alias fromAlias, final List<JoinClause> joins) {
			this.fromAlias = fromAlias;
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
			appendDomainUnion(j.getQueryDomains());
			sb.append(" AS ").append(quoteAlias(j.getDomainAlias())).append(" ON ")
					.append(quoteAttribute(fromAlias, SystemAttributes.Id)).append(OPERATOR_EQ)
					.append(quoteAttribute(j.getDomainAlias(), SystemAttributes.DomainId1));
			// FIXME
			sb.append(" AND ").append(quoteAttribute(j.getDomainAlias(), SystemAttributes.Status)).append(OPERATOR_EQ).append("?");
			addParam(STATUS_ACTIVE_VALUE);
		}

		// TODO This is a copy/paste of appendClassUnion
		private void appendDomainUnion(final Set<QueryDomain> queryDomains) {
			sb.append("(");
			boolean first = true;
			for (QueryDomain queryDomain : queryDomains) {
				if (first) {
					first = false;
				} else {
					sb.append(" UNION ALL ");
				}
				sb.append("SELECT ");

				sb.append(quoteIdent(SystemAttributes.Id)).append(",");
				sb.append(quoteIdent(SystemAttributes.DomainId)).append(",");
				appendColumnAndAliasIfFirst("?", quoteIdent(SystemAttributes.DomainQuerySource), first).append(",");
				addParam(queryDomain.getQuerySource());
				if (queryDomain.getDirection()) {
					sb.append(quoteIdent(SystemAttributes.DomainId1)).append(",");
					sb.append(quoteIdent(SystemAttributes.DomainId2));
				} else {
					appendColumnAndAliasIfFirst(quoteIdent(SystemAttributes.DomainId2), quoteIdent(SystemAttributes.DomainId1), first).append(",");
					appendColumnAndAliasIfFirst(quoteIdent(SystemAttributes.DomainId1), quoteIdent(SystemAttributes.DomainId2), first);
				}
				// TODO Consider other attributes
				// FIXME
				sb.append(",").append(quoteIdent(SystemAttributes.BeginDate))
					.append(",").append(quoteIdent(SystemAttributes.Status));
				sb.append(" FROM ").append(quoteType(queryDomain.getDomain()));
			}
			sb.append(")");
		}

		private StringBuilder appendColumnAndAliasIfFirst(final Object attribute, final String alias, final boolean isFirst) {
			sb.append(attribute).append(" AS ").append(alias);
			return sb;
		}

		private void appendTargetJoin(final JoinClause j) {
			sb.append(" JOIN ");
			appendClassUnion(j.getTargets());
			sb.append(" AS ").append(quoteAlias(j.getTargetAlias()))
				.append(" ON ")
					.append(quoteAttribute(j.getDomainAlias(), SystemAttributes.DomainId2)).append(OPERATOR_EQ)
					.append(quoteAttribute(j.getTargetAlias(), SystemAttributes.Id));
			// FIXME
			sb.append(" AND ").append(quoteAttribute(j.getTargetAlias(), SystemAttributes.Status)).append(OPERATOR_EQ).append("?");
			addParam(STATUS_ACTIVE_VALUE);
		}

		private void appendClassUnion(final Set<CMClass> entryTypes) {
			if (entryTypes.size() > 1) {
				sb.append("(");
				boolean first = true;
				for (CMEntryType type : entryTypes) {
					if (first) {
						first = false;
					} else {
						sb.append(" UNION ALL ");
					}
					sb.append("SELECT ");
					sb.append(quoteIdent(SystemAttributes.Id)).append(",")
						.append(quoteIdent(SystemAttributes.ClassId)).append(",")
						// TODO!!!!!!!!!!!!!!
						.append(quoteIdent(SystemAttributes.Code)).append(",")
						.append(quoteIdent(SystemAttributes.Description)).append(",")
						.append(quoteIdent(SystemAttributes.BeginDate)).append(",")
						.append(quoteIdent(SystemAttributes.Status)); // FIXME Change with EndDate?! No, because of deleted cards
						// TODO Consider other attributes
					sb.append(" FROM ").append(quoteType(type));
				}
				sb.append(")");
			} else {
				sb.append(quoteType(entryTypes.iterator().next()));
			}
		}
	}

	private class WherePartCreator extends PartCreator implements WhereClauseVisitor {

		WherePartCreator(final Alias fromAlias, final WhereClause whereClause) {
			super();
			whereClause.accept(this);
			// FIXME: append the status IF NOT a history query
			and(attributeFilter(attribute(fromAlias, SystemAttributes.Status.getDBName()), OPERATOR_EQ, STATUS_ACTIVE_VALUE));
		}

		private WherePartCreator append(final String string) {
			if (sb.length() == 0) {
				sb.append("WHERE");
			}
			sb.append(" ").append(string);
			return this;
		}

		private void and(final String string) {
			if (sb.length() > 0) {
				append("AND");
			}
			append(string);
		}

		@Override
		public void visit(final SimpleWhereClause whereClause) {
			append(attributeFilter(whereClause.getAttribute(), OPERATOR_EQ, whereClause.getValue())); // FIXME OPERATOR
		}

		private String attributeFilter(final QueryAliasAttribute attribute, final String operator, final Object value) {
			final String lhs = quoteAttribute(attribute.getEntryTypeAlias(), attribute.getName());
			addParam(value); // TODO Handle CMDBuild and Geographic types conversion
			return String.format("%s%s?", lhs, operator);
		}

		@Override
		public void visit(final EmptyWhereClause whereClause) {
			if (sb.length() != 0) {
				throw new IllegalArgumentException("Cannot use an empty clause along with other where clauses");
			}
		}
	}

	private String getAlias(final Alias entityTypeAlias, final SystemAttributes sa) {
		return "_" + entityTypeAlias.getName() + sa.name();
	}
}
