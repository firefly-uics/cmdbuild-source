package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.reference.CMReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import com.google.common.collect.Maps;

public class EntryInsertCommand {

	private final Map<String, Object> userAttributes;
	private final SimpleJdbcInsert insertActor;

	public EntryInsertCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		userAttributes = userAttributesFor(entry);
		insertActor = createInsertActor(jdbcTemplate, entry);
	}

	private Map<String, Object> userAttributesFor(final DBEntry entry) {
		final Map<String, Object> values = Maps.newHashMap();
		for (final Map.Entry<String, Object> v : entry.getValues()) {
			final String name = v.getKey();
			Object value = v.getValue();
			if (value instanceof CMReference) {
				value = CMReference.class.cast(value).getId();
			}
			values.put(quoteIdent(name), value);
		}
		// TODO ugly... a visitor is a better idea!
		if (entry instanceof DBRelation) {
			final DBRelation dbRelation = DBRelation.class.cast(entry);
			final CMCard card1 = dbRelation.getCard1();
			final CMCard card2 = dbRelation.getCard2();
			values.put(quoteIdent(SystemAttributes.DomainId1.getDBName()), card1.getId());
			values.put(quoteIdent(SystemAttributes.ClassId1.getDBName()), card1.getType().getId());
			values.put(quoteIdent(SystemAttributes.DomainId2.getDBName()), card2.getId());
			values.put(quoteIdent(SystemAttributes.ClassId2.getDBName()), card2.getType().getId());
		}
		return values;
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
