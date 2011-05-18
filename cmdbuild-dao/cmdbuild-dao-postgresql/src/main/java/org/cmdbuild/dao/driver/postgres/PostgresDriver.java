package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteTypeAndHistory;

import java.util.List;

import javax.sql.DataSource;

import org.cmdbuild.dao.driver.SelfVersioningDBDriver;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 20th century driver working with triggers, thus needing a lot more Java hacks
 * 
 * "If all you have is SQL, everything looks like a trigger" A. Maslow (readapted)
 */
public class PostgresDriver implements SelfVersioningDBDriver {

	private final JdbcTemplate jdbcTemplate;

	public PostgresDriver(final DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	@Override
	public List<DBClass> findAllClasses() {
		return new EntryTypeCommands(jdbcTemplate).findAllClasses();
	}

	@Override
	public DBClass createClass(final String name, final DBClass superClass) {
		return new EntryTypeCommands(jdbcTemplate).createClass(name, superClass);
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		new EntryTypeCommands(jdbcTemplate).deleteClass(dbClass);
	}

	@Override
	public DBClass findClassById(final Object id) {
		for (DBClass c : findAllClasses()) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		throw new IllegalArgumentException("Class " + id + " not found");
	}

	@Override
	public DBClass findClassByName(final String name) {
		for (DBClass c : findAllClasses()) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		throw new IllegalArgumentException("Class \"" + name + "\" not found");
	}

	@Override
	public Object create(final DBEntry entry) {
		return new EntryInsertCommand(jdbcTemplate, entry).executeAndReturnKey();
	}

	@Override
	public void update(DBEntry entry) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void delete(DBEntry entry) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		return new EntryQueryCommand(this, jdbcTemplate, query).run();
	}

	@Override
	public void clearEntryType(DBEntryType type) {
		// truncate all subclasses as well
		jdbcTemplate.execute("TRUNCATE TABLE " + quoteTypeAndHistory(type));
	}
}
