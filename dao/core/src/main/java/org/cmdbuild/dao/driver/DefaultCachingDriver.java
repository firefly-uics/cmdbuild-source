package org.cmdbuild.dao.driver;

import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.logging.LoggingSupport;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView.DBAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView.DBClassDefinition;

public class DefaultCachingDriver extends AbstractDBDriver implements CachingDriver, LoggingSupport {

	private static class TypeObjectStore<T extends CMTypeObject> {

		private final Collection<T> entries;

		private TypeObjectStore(final Collection<T> entries) {
			this.entries = entries;
		}

		public Collection<T> getCollection() {
			return entries;
		}

		public void add(final T entry) {
			entries.add(entry);
		}

		public void remove(final T entry) {
			entries.remove(entry);
		}

	}

	private volatile TypeObjectStore<DBClass> allClassesStore;
	private volatile TypeObjectStore<DBDomain> allDomainsStore;
	private volatile TypeObjectStore<DBFunction> allFunctionsStore;

	private final DBDriver inner;

	public DefaultCachingDriver(final DBDriver driver) {
		Validate.notNull(driver, "null driver");
		inner = driver;
	}

	@Override
	public final Collection<DBClass> findAllClasses() {
		return getAllClassesStore().getCollection();
	}

	private TypeObjectStore<DBClass> getAllClassesStore() {
		TypeObjectStore<DBClass> refForSafeClearCache = allClassesStore;
		if (allClassesStore == null) {
			synchronized (this) {
				refForSafeClearCache = allClassesStore;
				if (allClassesStore == null) {
					logger.info("classes cache is empty, creating it");
					this.allClassesStore = refForSafeClearCache = new TypeObjectStore<DBClass>(inner.findAllClasses());
				}
			}
		}
		return refForSafeClearCache;
	}

	@Override
	public DBClass createClass(final DBClassDefinition definition) {
		final DBClass c = inner.createClass(definition);
		if (allClassesStore != null) {
			allClassesStore.add(c);
		}
		return c;
	}

	@Override
	public DBClass updateClass(final DBClassDefinition definition) {
		final DBClass c = inner.updateClass(definition);
		if (allClassesStore != null) {
			allClassesStore.add(c);
		}
		return c;
	}

	@Override
	public final void deleteClass(final DBClass dbClass) {
		inner.deleteClass(dbClass);
		if (allClassesStore != null) {
			allClassesStore.remove(dbClass);
		}
	}

	@Override
	public DBAttribute createAttribute(final DBAttributeDefinition definition) {
		return inner.createAttribute(definition);
	}

	@Override
	public DBAttribute updateAttribute(final DBAttributeDefinition definition) {
		return inner.updateAttribute(definition);
	}

	@Override
	public void deleteAttribute(final DBAttribute dbAttribute) {
		inner.deleteAttribute(dbAttribute);
	}

	@Override
	public final Collection<DBDomain> findAllDomains() {
		return getAllDomainsStore().getCollection();
	}

	private TypeObjectStore<DBDomain> getAllDomainsStore() {
		TypeObjectStore<DBDomain> refForSafeClearCache = allDomainsStore;
		if (allDomainsStore == null) {
			synchronized (this) {
				refForSafeClearCache = allDomainsStore;
				if (allDomainsStore == null) {
					logger.info("domains cache is empty, creating it");
					this.allDomainsStore = refForSafeClearCache = new TypeObjectStore<DBDomain>(inner.findAllDomains());
				}
			}
		}
		return refForSafeClearCache;
	}

	@Override
	public DBDomain createDomain(final DomainDefinition domainDefinition) {
		final DBDomain d = inner.createDomain(domainDefinition);
		if (allDomainsStore != null) {
			allDomainsStore.add(d);
		}
		return d;
	}

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		inner.deleteDomain(dbDomain);
		if (allDomainsStore != null) {
			allDomainsStore.remove(dbDomain);
		}
	}

	@Override
	public Collection<DBFunction> findAllFunctions() {
		return getAllFunctionsStore().getCollection();
	}

	private TypeObjectStore<DBFunction> getAllFunctionsStore() {
		TypeObjectStore<DBFunction> refForSafeClearCache = allFunctionsStore;
		if (allFunctionsStore == null) {
			synchronized (this) {
				refForSafeClearCache = allFunctionsStore;
				if (allFunctionsStore == null) {
					logger.info("functions cache is empty, creating it");
					this.allFunctionsStore = refForSafeClearCache = new TypeObjectStore<DBFunction>(
							inner.findAllFunctions());
				}
			}
		}
		return refForSafeClearCache;
	}

	@Override
	public Long create(final DBEntry entry) {
		return inner.create(entry);
	}

	@Override
	public void update(final DBEntry entry) {
		inner.update(entry);
	}

	@Override
	public void delete(final DBEntry entry) {
		inner.delete(entry);
	}

	@Override
	public CMQueryResult query(final QuerySpecs query) {
		return inner.query(query);
	}

	@Override
	public void clearCache() {
		synchronized (this) {
			logger.info("clearing all cache");
			clearClassesCache();
			clearDomainsCache();
			clearFunctionsCache();
		}
	}

	@Override
	public void clearClassesCache() {
		synchronized (this) {
			logger.info("clearing classes cache");
			allClassesStore = null;
		}
	}

	@Override
	public void clearDomainsCache() {
		synchronized (this) {
			logger.info("clearing domains cache");
			allDomainsStore = null;
		}
	}

	@Override
	public void clearFunctionsCache() {
		synchronized (this) {
			logger.info("clearing functions cache");
			allFunctionsStore = null;
		}
	}

}
