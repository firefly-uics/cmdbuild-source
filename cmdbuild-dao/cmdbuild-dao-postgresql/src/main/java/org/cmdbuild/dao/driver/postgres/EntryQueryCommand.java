package org.cmdbuild.dao.driver.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper;
import org.cmdbuild.dao.driver.postgres.query.ColumnMapper.EntryTypeAttribute;
import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
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
			if (rowNum >= start && rowNum < end) {
				final DBQueryRow row = new DBQueryRow();
				createBasicCards(rs, row);
				createBasicRelations(rs, row);
				result.add(row);
			}
		}

		private void createBasicCards(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (Alias a : columnMapper.getClassAliases()) {
				// Always extract a Long for the Id even if it's integer
				final Object id = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.Id));
				final Long classId = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.ClassId));
				final DBClass realClass = driver.findClassById(classId);
				final DBCard card = DBCard.create(driver, realClass, id);

				card.setBeginDate(getDateTime(rs, Utils.getSystemAttributeAlias(a, SystemAttributes.BeginDate)));
				// TODO It's not supported yet because the FROM class has no such column
				//card.setEndDate(getDateTime(rs, Utils.getAttributeAlias(a, SystemAttributes.EndDate)));

				addUserAttributes(a, card, rs);

				row.setCard(a, card);
			}
		}

		private void createBasicRelations(final ResultSet rs, final DBQueryRow row) throws SQLException {
			for (Alias a : columnMapper.getDomainAliases()) {
				final Object id = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.Id));
				final Long domainId = rs.getLong(Utils.getSystemAttributeAlias(a, SystemAttributes.DomainId));
				final String querySource = rs.getString(Utils.getSystemAttributeAlias(a, SystemAttributes.DomainQuerySource));
				final DBDomain realDomain = driver.findDomainById(domainId);
				final DBRelation relation = DBRelation.create(driver, realDomain, id);

				relation.setBeginDate(getDateTime(rs, Utils.getSystemAttributeAlias(a, SystemAttributes.BeginDate)));
				relation.setEndDate(getDateTime(rs, Utils.getSystemAttributeAlias(a, SystemAttributes.EndDate)));
				// TODO Add card1 and card2 from the cards already extracted!

				addUserAttributes(a, relation, rs);

				final QueryRelation queryRelation = QueryRelation.create(relation, querySource);
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

		private void addUserAttributes(final Alias typeAlias, final DBEntry entry, final ResultSet rs) throws SQLException {
			for (EntryTypeAttribute a : columnMapper.getEntryTypeAttributes(typeAlias, entry.getType())) {
				if (a.name != null) { // Not part of this entry type
					entry.setOnly(a.name, rs.getObject(a.index));
				}
			}
		}

		CMQueryResult getResult() {
			return result;
		}
	}
}
