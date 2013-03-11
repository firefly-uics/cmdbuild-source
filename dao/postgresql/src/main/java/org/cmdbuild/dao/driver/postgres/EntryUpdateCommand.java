package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.join;

import java.util.List;

import org.cmdbuild.dao.driver.postgres.quote.EntryTypeQuoter;
import org.cmdbuild.dao.driver.postgres.quote.IdentQuoter;
import org.cmdbuild.dao.entry.DBEntry;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

public class EntryUpdateCommand extends EntryCommand {

	private final List<AttributeValueType> attributesToBeUpdated;

	public EntryUpdateCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		super(jdbcTemplate, entry);
		this.attributesToBeUpdated = userAttributesFor(entry);
	}

	public void execute() {
		final String sql = format("UPDATE %s SET %s WHERE %s = ?", //
				EntryTypeQuoter.quote(entry().getType()), //
				columns(), //
				IdentQuoter.quote(entry().getType().getKeyAttributeName()));
		final Object[] arguments = arguments();
		jdbcTemplate().update(sql, arguments);
	}

	private String columns() {
		final List<String> columns = Lists.newArrayList();
		for (final AttributeValueType attributeValueType : attributesToBeUpdated) {
			String sqlCast;
			sqlCast = SqlType.getSqlType(attributeValueType.getType()).sqlCast();
			if (sqlCast == null) {
				columns.add(format("%s = ?%s", IdentQuoter.quote(attributeValueType.getName()), ""));
			} else {
				columns.add(format("%s = ?%s", IdentQuoter.quote(attributeValueType.getName()), "::" + sqlCast));
			}
		}
		return join(columns, ", ");
	}

	private Object[] arguments() {
		final List<Object> arguments = Lists.newArrayList();
		for (final AttributeValueType avt : attributesToBeUpdated) {
			if (!(avt.getValue() instanceof String[])) {
				arguments.add(avt.getValue());
			} else {
				try {
					arguments.add(jdbcTemplate().getDataSource().getConnection()
							.createArrayOf("text", (String[]) avt.getValue()));
				} catch (final Exception ex) {
				}

			}
		}
		arguments.add(entry().getId());
		return arguments.toArray();
	}

}
