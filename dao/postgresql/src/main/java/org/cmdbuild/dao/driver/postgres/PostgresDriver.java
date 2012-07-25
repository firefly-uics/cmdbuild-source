package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.cmdbuild.dao.driver.CachingDriver;
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
 * "If all you have is SQL, everything looks like a trigger" A. Maslow (readapted)
 */
public class PostgresDriver extends CachingDriver implements SelfVersioningDBDriver {

	private final JdbcTemplate jdbcTemplate;

	public PostgresDriver(final DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	/*
	 * CachingDriver abstract methods
	 */

	@Override
	protected List<DBClass> findAllClassesNoCache() {
		return new EntryTypeCommands(jdbcTemplate).findAllClasses();
	}

	@Override
	protected DBClass createClassNoCache(final String name, final DBClass superClass) {
		return new EntryTypeCommands(jdbcTemplate).createClass(name, superClass);
	}

	@Override
	protected void deleteClassNoCache(final DBClass dbClass) {
		new EntryTypeCommands(jdbcTemplate).deleteClass(dbClass);
	}

	@Override
	protected Collection<DBDomain> findAllDomainsNoCache() {
		return new EntryTypeCommands(jdbcTemplate).findAllDomains(this);
	}

	@Override
	protected DBDomain createDomainNoCache(final String name, final DBClass class1, final DBClass class2) {
		return new EntryTypeCommands(jdbcTemplate).createDomain(name, class1, class2);
	}

	@Override
	protected void deleteDomainNoCache(final DBDomain dbDomain) {
		new EntryTypeCommands(jdbcTemplate).deleteDomain(dbDomain);
	}

	@Override
	protected List<DBFunction> findAllFunctionsNoCache() {
		return new EntryTypeCommands(jdbcTemplate).findAllFunctions();
	}

	/*
	 * DBDriver
	 */

	@Override
	public Long create(final DBEntry entry) {
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

	/*
	 * SelfVersioningDBDriver
	 */

	@Override
	public void clearEntryType(DBEntryType type) {
		// truncate all subclasses as well
		jdbcTemplate.execute("TRUNCATE TABLE " + quoteType(type) + " CASCADE");
	}
}
