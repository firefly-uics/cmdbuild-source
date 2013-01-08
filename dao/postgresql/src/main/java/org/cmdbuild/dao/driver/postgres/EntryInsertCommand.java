package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.DBEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class EntryInsertCommand extends EntryCommand {

	private final Map<String, Object> userAttributes;
	private final SimpleJdbcInsert insertActor;

	public EntryInsertCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		super(jdbcTemplate, entry);
		userAttributes = userAttributesFor(entry);
		insertActor = createInsertActor(jdbcTemplate, entry);
	}

	private SimpleJdbcInsert createInsertActor(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		final String quotedTableName = quoteType(entry.getType());
		return new SimpleJdbcInsert(jdbcTemplate) //
				.withTableName(quotedTableName) //
				.usingColumns(userAttributeNames()) //
				.usingGeneratedKeyColumns(SystemAttributes.Id.getDBName());
	}

	private String[] userAttributeNames() {
		return userAttributes.keySet().toArray(new String[userAttributes.size()]);
	}

	public Long executeAndReturnKey() {
		// TODO LOG insertActor.getInsertString();
		return insertActor.executeAndReturnKey(userAttributes).longValue();
	}

}
