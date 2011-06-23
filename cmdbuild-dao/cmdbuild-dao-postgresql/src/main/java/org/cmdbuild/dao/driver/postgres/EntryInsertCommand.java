package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.ID_ATTRIBUTE;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.DBEntry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class EntryInsertCommand {

	private final Map<String, Object> valueMap;
	private final SimpleJdbcInsert insertActor;

	public EntryInsertCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		valueMap = valueMapFor(entry);
		insertActor = createInsertActor(jdbcTemplate, entry);
	}

	private SimpleJdbcInsert createInsertActor(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		final String quotedTableName = quoteType(entry.getType());
		final String columns[] = valueMap.keySet().toArray(new String[valueMap.size()]);
		return new SimpleJdbcInsert(jdbcTemplate).withTableName(quotedTableName).usingColumns(columns)
				.usingGeneratedKeyColumns(ID_ATTRIBUTE);
	}

	private Map<String, Object> valueMapFor(final DBEntry entry) {
		final Map<String, Object> valueMap = new HashMap<String, Object>();
		for (Map.Entry<String, Object> v : entry.getValues()) {
			final String name = v.getKey();
			final Object value = v.getValue();
			valueMap.put(quoteIdent(name), value);
		}
		return valueMap;
	}

	public Object executeAndReturnKey() {
		// TODO LOG insertActor.getInsertString();
		return insertActor.executeAndReturnKey(valueMap).longValue();
	}
}
