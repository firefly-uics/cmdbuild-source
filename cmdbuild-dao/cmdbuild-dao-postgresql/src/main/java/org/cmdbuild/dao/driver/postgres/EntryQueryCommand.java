package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.DBQueryRow;
import org.cmdbuild.dao.query.QuerySpecs;
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
		final ResultFiller rch = new ResultFiller();
		jdbcTemplate.query(createSelectQuery(querySpecs), rch);
		return rch.getResult();
	}

	private class ResultFiller implements RowCallbackHandler {

		final DBClass from;
		final Map<Object, DBClass> temporaryFromMap;
		final int start;
		final int end;

		final DBQueryResult result;

		private ResultFiller() {
			from = querySpecs.getDBFrom();
			temporaryFromMap = createTemporaryFromMap(from);
			result = new DBQueryResult();
			start = (querySpecs.getOffset() != null) ? querySpecs.getOffset() : 0;
			end = (querySpecs.getLimit() != null) ? start + querySpecs.getLimit() : Integer.MAX_VALUE;
		}

		private Map<Object, DBClass> createTemporaryFromMap(final DBClass from) {
			Map<Object, DBClass> fromMap = new HashMap<Object, DBClass>();
			fillFromMap(fromMap, from);
			return fromMap;
		}

		private void fillFromMap(final Map<Object, DBClass> fromMap, final DBClass c) {
			fromMap.put(c.getId(), c);
			for (DBClass child : c.getChildren()) {
				fillFromMap(fromMap, child);
			}
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			final int rowNum = result.getAndIncrementTotalSize();
			if (rowNum >= start && rowNum < end) {
				final DBQueryRow row = new DBQueryRow();
	        	final Object id = rs.getObject(ID_ATTRIBUTE); // FIXME!!!!
	        	final Long classId = rs.getLong(CLASS_ID_ATTRIBUTE);
	        	final DBClass realFrom = temporaryFromMap.get(classId);
	        	final DBCard card = DBCard.create(driver, realFrom, id);
	        	for (CMAttribute a : querySpecs.getAttributes()) {
	        		final String name = a.getName();
	        		final String alias = a.getName(); // TODO
	        		card.set(name, rs.getObject(alias));
	        	}
	        	row.setCard(from /* should be an alias */, card);
	        	result.add(row);
			}
		}

		CMQueryResult getResult() {
			return result;
		}
	}

	private String createSelectQuery(final QuerySpecs query) {
		final String quotedFrom = quoteType(query.getDBFrom());
		final String quotedAttributes = quoteAndJoin(query.getAttributes());
		return String.format("SELECT %s FROM %s", quotedAttributes, quotedFrom); // TODO WHERE "Status"='A'
	}
}
