package org.cmdbuild.dao.driver.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBFunctionCallOutput;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.DBQueryRow;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;
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
		final ResultFiller rch = new ResultFiller(qc.getColumnMapper());
		jdbcTemplate.query(qc.getQuery(), qc.getParams(), rch);
		return rch.getResult();
	}

	private class ResultFiller implements RowCallbackHandler {

		final ColumnMapper columnMapper;

		final long start;
		final long end;

		final DBQueryResult result;

		private ResultFiller(final ColumnMapper columnMapper) {
			this.columnMapper = columnMapper;
			result = new DBQueryResult();
			start = (querySpecs.getOffset() != null) ? querySpecs.getOffset() : 0;
			end = (querySpecs.getLimit() != null) ? start + querySpecs.getLimit() : Integer.MAX_VALUE;
		}

		@Override
		public void processRow(final ResultSet rs) throws SQLException {
			final int rowNum = result.getAndIncrementTotalSize();
			if (start <= rowNum && rowNum < end) {
				final DBQueryRow row = new DBQueryRow();
				createBasicCards(rs, row);
				createBasicRelations(rs, row);
				createFunctionCallOutput(rs, row);
				result.add(row);
			}
		}

		private void createFunctionCallOutput(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (Alias a : columnMapper.getFunctionCallAliases()) {
				final DBFunctionCallOutput out = new DBFunctionCallOutput();
				final CMEntryType hackSinceAFunctionCanAppearOnlyInTheFromClause = querySpecs.getFromType();
				for (EntryTypeAttribute eta : columnMapper.getEntryTypeAttributes(a,
						hackSinceAFunctionCanAppearOnlyInTheFromClause)) {
					final Object sqlValue = rs.getObject(eta.index);
					out.set(eta.name, eta.sqlType.sqlToJavaValue(sqlValue));
				}
				row.setFunctionCallOutput(a, out);
			}
		}

		private void createBasicCards(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (Alias a : columnMapper.getClassAliases()) {
				// Always extract a Long for the Id even if it's integer
				final Long id = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.Id));
				final Long classId = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.ClassId));
				final DBClass realClass = driver.findClassById(classId);
				final DBCard card = DBCard.newInstance(driver, realClass, id);

				card.setUser(rs.getString(Utils.getSystemAttributeAlias(a, SystemAttributes.User)));
				card.setBeginDate(getDateTime(rs, Utils.getSystemAttributeAlias(a, SystemAttributes.BeginDate)));
				/*
				 * TODO not supported yet
				 * 
				 * the FROM class has no such column
				 * 
				 * card.setEndDate(getDateTime(rs, Utils.getAttributeAlias(a,
				 * SystemAttributes.EndDate)));
				 */

				addUserAttributes(a, card, rs);

				row.setCard(a, card);
			}
		}

		private void createBasicRelations(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (Alias a : columnMapper.getDomainAliases()) {
				final Long id = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.Id));
				final Long domainId = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.DomainId));
				final String querySource = rs.getString(Utils.getSystemAttributeAlias(a,
						SystemAttributes.DomainQuerySource));
				final DBDomain realDomain = driver.findDomainById(domainId);
				final DBRelation relation = DBRelation.newInstance(driver, realDomain, id);

				relation.setUser(rs.getString(Utils.getSystemAttributeAlias(a, SystemAttributes.User)));
				relation.setBeginDate(getDateTime(rs, Utils.getSystemAttributeAlias(a, SystemAttributes.BeginDate)));
				relation.setEndDate(getDateTime(rs, Utils.getSystemAttributeAlias(a, SystemAttributes.EndDate)));
				// TODO Add card1 and card2 from the cards already extracted!

				addUserAttributes(a, relation, rs);

				final QueryRelation queryRelation = QueryRelation.newInstance(relation, querySource);
				row.setRelation(a, queryRelation);
			}
		}

		private DateTime getDateTime(final ResultSet rs, final String attributeAlias) throws SQLException {
			final java.sql.Timestamp ts = rs.getTimestamp(attributeAlias);
			if (ts != null) {
				return new DateTime(ts.getTime());
			} else {
				return null;
			}
		}

		private void addUserAttributes(final Alias typeAlias, final DBEntry entry, final ResultSet rs)
				throws SQLException {
			for (EntryTypeAttribute a : columnMapper.getEntryTypeAttributes(typeAlias, entry.getType())) {
				if (a.name != null) {
					final Object sqlValue = rs.getObject(a.index);
					entry.setOnly(a.name, a.sqlType.sqlToJavaValue(sqlValue));
				} else {
					// skipping, not belonging to this entry type
				}
			}
		}

		CMQueryResult getResult() {
			return result;
		}
	}
}
