package org.cmdbuild.dao.driver.postgres;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entry.DBRelation;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.reference.CMReference;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

abstract class EntryCommand {

	private final JdbcTemplate jdbcTemplate;
	private final DBEntry entry;
	protected List<SystemAttributes> systemDomainAttributes;

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

	protected List<AttributeValueType> userAttributesFor(final DBEntry entry) {
		final CMEntryType entryType = entry.getType();
		final List<AttributeValueType> values = Lists.newArrayList();
		for (final Map.Entry<String, Object> v : entry.getValues()) {
			final String attributeName = v.getKey();
			Object value = v.getValue();
			if (value instanceof CMReference) {
				value = CMReference.class.cast(value).getId();
			}
			final CMAttributeType<?> attributeType = entryType.getAttribute(attributeName).getType();
			final AttributeValueType attrValueType = new AttributeValueType(attributeName, //
					SqlType.getSqlType(attributeType).javaToSqlValue(value), attributeType);
			values.add(attrValueType);
		}
		// TODO ugly... a visitor is a better idea!
		if (entry instanceof DBRelation) {
			final DBRelation dbRelation = DBRelation.class.cast(entry);
			final CMCard card1 = dbRelation.getCard1();
			final CMCard card2 = dbRelation.getCard2();
			values.add(new AttributeValueType(SystemAttributes.DomainId1.getDBName(), card1.getId(),
					new IntegerAttributeType()));
			values.add(new AttributeValueType(SystemAttributes.ClassId1.getDBName(), card1.getType().getId(),
					new EntryTypeAttributeType()));
			values.add(new AttributeValueType(SystemAttributes.DomainId2.getDBName(), card2.getId(),
					new IntegerAttributeType()));
			values.add(new AttributeValueType(SystemAttributes.ClassId2.getDBName(), card2.getType().getId(),
					new EntryTypeAttributeType()));
		}
		return values;
	}

}
