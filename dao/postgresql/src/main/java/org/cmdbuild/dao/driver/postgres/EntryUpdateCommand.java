package org.cmdbuild.dao.driver.postgres;

import static java.lang.String.format;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteIdent;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.*;
import org.cmdbuild.dao.entry.DBEntry;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.common.collect.Lists;

public class EntryUpdateCommand extends EntryCommand {

	private final Map<String, Object> userAttributes;

	public EntryUpdateCommand(final JdbcTemplate jdbcTemplate, final DBEntry entry) {
		super(jdbcTemplate, entry);
		this.userAttributes = userAttributesFor(entry);
	}

	public void execute() {
		final String sql = format("UPDATE %s SET %s WHERE %s = ?", //
				quoteType(entry().getType()), //
				columns(), //
				quoteIdent(entry().getType().getKeyAttributeName()));
		jdbcTemplate().update(sql, arguments());
	}

	private String columns() {
		final List<String> columns = Lists.newArrayList();
		for (final String key : userAttributes.keySet()) {
			columns.add(format("%s = ?", key));
		}
		return join(columns, ", ");
	}

	private Object[] arguments() {
		final List<Object> arguments = Lists.newArrayList();
		arguments.addAll(userAttributes.values());
		arguments.add(entry().getId());
		return arguments.toArray();
	}

}
