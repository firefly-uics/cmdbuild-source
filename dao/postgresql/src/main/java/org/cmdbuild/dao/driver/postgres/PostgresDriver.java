package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.Collection;

import javax.sql.DataSource;

import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.driver.SelfVersioningDBDriver;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 20th century driver working with triggers, thus needing a lot more Java hacks
 * 
 * "If all you have is SQL, everything looks like a trigger" A. Maslow
 * (readapted)
 */
public class PostgresDriver extends AbstractDBDriver implements SelfVersioningDBDriver {

	private final JdbcTemplate jdbcTemplate;

	public PostgresDriver(final DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	@Override
	public Collection<DBClass> findAllClasses() {
		return new EntryTypeCommands(jdbcTemplate).findAllClasses();
	}

	@Override
	public DBClass createClass(final String name, final DBClass parent) {
		return new EntryTypeCommands(jdbcTemplate).createClass(name, parent);
	}

	@Override
	public DBClass createSuperClass(final String name, final DBClass parent) {
		return new EntryTypeCommands(jdbcTemplate).createSuperClass(name, parent);
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		new EntryTypeCommands(jdbcTemplate).deleteClass(dbClass);
	}

	@Override
	public Collection<DBDomain> findAllDomains() {
		return new EntryTypeCommands(jdbcTemplate).findAllDomains(this);
	}

	@Override
	public DBDomain createDomain(final DomainDefinition domainDefinition) {
		return new EntryTypeCommands(jdbcTemplate).createDomain(domainDefinition);
	}

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		new EntryTypeCommands(jdbcTemplate).deleteDomain(dbDomain);
	}

	@Override
	public Collection<DBFunction> findAllFunctions() {
		return new EntryTypeCommands(jdbcTemplate).findAllFunctions();
	}

	@Override
	public Long create(final DBEntry entry) {
		return new EntryInsertCommand(jdbcTemplate, entry).executeAndReturnKey();
	}

	@Override
	public void update(final DBEntry entry) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void delete(final DBEntry entry) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		return new EntryQueryCommand(this, jdbcTemplate, query).run();
	}

	@Override
	public void clearEntryType(final DBEntryType type) {
		// truncate all subclasses as well
		jdbcTemplate.execute("TRUNCATE TABLE " + quoteType(type) + " CASCADE");
	}

}
