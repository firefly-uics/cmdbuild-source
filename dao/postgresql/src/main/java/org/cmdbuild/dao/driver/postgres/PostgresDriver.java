package org.cmdbuild.dao.driver.postgres;

import static org.cmdbuild.dao.driver.postgres.Utils.quoteType;

import java.util.Collection;

import javax.sql.DataSource;

import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.driver.SelfVersioningDBDriver;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;
import org.cmdbuild.dao.view.DBDataView.DBDomainDefinition;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 20th century driver working with triggers, thus needing a lot more Java hacks
 * 
 * "If all you have is SQL, everything looks like a trigger" A. Maslow
 * (readapted)
 */
public class PostgresDriver extends AbstractDBDriver implements SelfVersioningDBDriver {

	private final JdbcTemplate jdbcTemplate;

	public PostgresDriver(final DataSource datasource, final TypeObjectCache typeObjectCache) {
		super(typeObjectCache);
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	@Override
	public Collection<DBClass> findAllClasses() {
		final Collection<DBClass> fetchedClasses = doToTypes().findAllClasses();
		for (final DBClass dbClass : fetchedClasses) {
			cache.add(dbClass);
		}
		return fetchedClasses;
	}

	@Override
	public DBClass createClass(final DBClassDefinition definition) {
		final DBClass createdClass = doToTypes().createClass(definition);
		cache.add(createdClass);
		return createdClass;
	}

	@Override
	public DBClass updateClass(final DBClassDefinition definition) {
		final DBClass updatedClass = doToTypes().updateClass(definition);
		cache.add(updatedClass);
		return updatedClass;
	}

	@Override
	public void deleteClass(final DBClass dbClass) {
		doToTypes().deleteClass(dbClass);
		cache.remove(dbClass);
	}

	@Override
	public DBAttribute createAttribute(final DBAttributeDefinition definition) {
		return doToTypes().createAttribute(definition);
	}

	@Override
	public DBAttribute updateAttribute(final DBAttributeDefinition definition) {
		return doToTypes().updateAttribute(definition);
	}

	@Override
	public void deleteAttribute(final DBAttribute attribute) {
		doToTypes().deleteAttribute(attribute);
	}

	@Override
	public Collection<DBDomain> findAllDomains() {
		final Collection<DBDomain> fetchedDomains = doToTypes().findAllDomains();
		for (final DBDomain dbDomain : fetchedDomains) {
			cache.add(dbDomain);
		}
		return fetchedDomains;
	}

	@Override
	public DBDomain createDomain(final DBDomainDefinition definition) {
		final DBDomain createdDomain = doToTypes().createDomain(definition);
		cache.add(createdDomain);
		return createdDomain;
	}

	@Override
	public DBDomain updateDomain(final DBDomainDefinition definition) {
		final DBDomain updatedDomain = doToTypes().updateDomain(definition);
		cache.add(updatedDomain);
		return updatedDomain;
	}

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		doToTypes().deleteDomain(dbDomain);
		cache.remove(dbDomain);
	}

	private EntryTypeCommands doToTypes() {
		return new EntryTypeCommands(this, jdbcTemplate);
	}

	@Override
	public Collection<DBFunction> findAllFunctions() {
		// FIXME: improve performances
		final Collection<DBFunction> fetchedFunctions = doToTypes().findAllFunctions();
		for (final DBFunction dbFunction : fetchedFunctions) {
			cache.add(dbFunction);
		}
		return fetchedFunctions;
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
		clearEntryType(entry.getType()); // FIXME
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
