package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;

import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.reference.CMReference;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Maps;

abstract class EntryCommand {

	private final JdbcTemplate jdbcTemplate;
	private final DBEntry entry;

	protected EntryCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		this.jdbcTemplate = jdbcTemplate;
		this.entry = entry;
	}

	protected JdbcTemplate jdbcTemplate() {
		return jdbcTemplate;
	}

	protected DBEntry entry() {
		return entry;
	}

	protected Map<String, Object> userAttributesFor(final DBEntry entry) {
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

}
