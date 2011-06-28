package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.BEGIN_DATE_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.CODE_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.DESCRIPTION_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.DOMAIN_ID1_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.DOMAIN_ID2_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.DOMAIN_ID_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.ID_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.OPERATOR_EQ;
import static org.cmdbuild.dao.driver.postgres.Utils.STATUS_ACTIVE_VALUE;
import static org.cmdbuild.dao.driver.postgres.Utils.STATUS_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.entry.DBCard;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public class EntryQueryCommand {

	private static final String DOMAIN_DIRECTION_ATTRIBUTE = "_Dir";

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
					final Object id = rs.getLong(getIdAliasFor(a));
					final Long classId = rs.getLong(getClassIdAliasFor(a));
					final DBClass realClass = driver.findClassById(classId);
					final DBCard card = DBCard.create(driver, realClass, id);
					row.setCard(a, card);
				}

				for (Alias a : qc.getDomainAliases()) {
					//final Object id = rs.getLong(getIdAliasFor(a)); // TODO
					// TODO Add card1 and card2 from the cards already extracted!
					final Long domainId = rs.getLong(getDomainIdAliasFor(a));
					final boolean direction = rs.getBoolean(getDomainDirectionAliasFor(a));
					final DBDomain realDomain = driver.findDomainById(domainId);
					final QueryRelation queryRelation = QueryRelation.create(driver, realDomain, direction);
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
		private final Map<QueryAliasAttribute, Integer> columns;

		QueryCreator(final QuerySpecs query) {
			this.sb = new StringBuilder();
			this.query = query;
			this.params = new ArrayList<Object>();

			this.columnIndex = 0;
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
			final List<String> quoted = getSystemSelectAttributes(attributes);
			for (QueryAliasAttribute a : attributes) {
				columns.put(a, ++columnIndex);
				quoted.add(quoteAttribute(a));
			}
			return StringUtils.join(quoted, ",");
		}

		private List<String> getSystemSelectAttributes(final Iterable<QueryAliasAttribute> attributes) {
			// FIXME! Anyway tableoid can't be used because of the history table
			final List<String> idSelectAttributes = new ArrayList<String>();

			for (Alias a : query.getClassAliases()) {
				columnIndex += 2;
				idSelectAttributes.add(getClassIdSelectAttribute(a));
				idSelectAttributes.add(getIdSelectAttribute(a));
			}

			for (Alias a : query.getDomainAliases()) {
				columnIndex += 3;
				idSelectAttributes.add(getDomainIdSelectAttribute(a));
				idSelectAttributes.add(getDomainDirectionSelectAttribute(a));
				idSelectAttributes.add(getIdSelectAttribute(a));
			}

			return idSelectAttributes;
		}

		private String getClassIdSelectAttribute(final Alias entityTypeAlias) {
			return String.format("%s AS %s", quoteAttribute(entityTypeAlias, CLASS_ID_ATTRIBUTE, "oid"),
					getClassIdAliasFor(entityTypeAlias));
		}

		private String getIdSelectAttribute(final Alias entityTypeAlias) {
			return String.format("%s AS %s", quoteAttribute(entityTypeAlias, ID_ATTRIBUTE),
					getIdAliasFor(entityTypeAlias));
		}

		private String getDomainIdSelectAttribute(final Alias entityTypeAlias) {
			return String.format("%s AS %s", quoteAttribute(entityTypeAlias, DOMAIN_ID_ATTRIBUTE, "oid"),
					getDomainIdAliasFor(entityTypeAlias));
		}

		private String getDomainDirectionSelectAttribute(final Alias entityTypeAlias) {
			return String.format("%s AS %s", quoteAttribute(entityTypeAlias, DOMAIN_DIRECTION_ATTRIBUTE),
					getDomainDirectionAliasFor(entityTypeAlias));
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
					.append(quoteAttribute(fromAlias, ID_ATTRIBUTE)).append(OPERATOR_EQ)
					.append(quoteAttribute(j.getDomainAlias(), DOMAIN_ID1_ATTRIBUTE));
			// FIXME
			sb.append(" AND ").append(quoteAttribute(j.getDomainAlias(), STATUS_ATTRIBUTE)).append(OPERATOR_EQ).append("?");
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

				sb.append(quoteIdent(ID_ATTRIBUTE)).append(",");
				sb.append(quoteIdent(DOMAIN_ID_ATTRIBUTE)).append(",");
				appendColumnAndAliasIfFirst(queryDomain.getDirection(), quoteIdent(DOMAIN_DIRECTION_ATTRIBUTE), first).append(",");
				if (queryDomain.getDirection()) {
					sb.append(quoteIdent(DOMAIN_ID1_ATTRIBUTE)).append(",");
					sb.append(quoteIdent(DOMAIN_ID2_ATTRIBUTE));
				} else {
					appendColumnAndAliasIfFirst(quoteIdent(DOMAIN_ID2_ATTRIBUTE), quoteIdent(DOMAIN_ID1_ATTRIBUTE), first).append(",");
					appendColumnAndAliasIfFirst(quoteIdent(DOMAIN_ID1_ATTRIBUTE), quoteIdent(DOMAIN_ID2_ATTRIBUTE), first);
				}
				// TODO Consider other attributes
				// FIXME
				sb.append(",").append(quoteIdent(STATUS_ATTRIBUTE))
					.append(",").append(quoteIdent(BEGIN_DATE_ATTRIBUTE));
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
					.append(quoteAttribute(j.getDomainAlias(), DOMAIN_ID2_ATTRIBUTE)).append(OPERATOR_EQ)
					.append(quoteAttribute(j.getTargetAlias(), ID_ATTRIBUTE));
			// FIXME
			sb.append(" AND ").append(quoteAttribute(j.getTargetAlias(), STATUS_ATTRIBUTE)).append(OPERATOR_EQ).append("?");
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
					sb.append(quoteIdent(ID_ATTRIBUTE)).append(",")
						.append(quoteIdent(CLASS_ID_ATTRIBUTE)).append(",")
						// TODO!!!!!!!!!!!!!!
						.append(quoteIdent(CODE_ATTRIBUTE)).append(",")
						.append(quoteIdent(DESCRIPTION_ATTRIBUTE)).append(",")
						.append(quoteIdent(STATUS_ATTRIBUTE)); // FIXME Change with EndDate?! No, because of deleted cards
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
			and(attributeFilter(attribute(fromAlias, STATUS_ATTRIBUTE), OPERATOR_EQ, STATUS_ACTIVE_VALUE));
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

	private String getClassIdAliasFor(final Alias entityTypeAlias) {
		return "_" + entityTypeAlias.getName() + "_ClassId";
	}

	private String getDomainIdAliasFor(final Alias entityTypeAlias) {
		return "_" + entityTypeAlias.getName() + "_DomainId";
	}

	private String getIdAliasFor(final Alias entityTypeAlias) {
		return "_" + entityTypeAlias.getName() + "_Id";
	}

	private String getDomainDirectionAliasFor(final Alias domainAlias) {
		return "_" + domainAlias.getName() + DOMAIN_DIRECTION_ATTRIBUTE;
	}
}
