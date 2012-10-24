package org.cmdbuild.dao.driver;

import java.util.Collection;

import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.logging.LoggingSupport;

public abstract class CachingDriver implements DBDriver, LoggingSupport {

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

		public T getByName(final String name) {
			for (final T e : entries) {
				if (e.getName().equals(name)) {
					return e;
				}
			}
			return null;
		}

		public T getById(final Long id) {
			for (final T e : entries) {
				if (e.getId().equals(id)) {
					return e;
				}
			}
			return null;
		}

	}

	private volatile TypeObjectStore<DBClass> allClassesStore;
	private volatile TypeObjectStore<DBDomain> allDomainsStore;
	private volatile TypeObjectStore<DBFunction> allFunctionsStore;

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
					this.allClassesStore = refForSafeClearCache = new TypeObjectStore<DBClass>(findAllClassesNoCache());
				}
			}
		}
		return refForSafeClearCache;
	}

	protected abstract Collection<DBClass> findAllClassesNoCache();

	@Override
	public final DBClass createClass(final String name, final DBClass parent) {
		final DBClass c = createClassNoCache(name, parent);
		if (allClassesStore != null) {
			allClassesStore.add(c);
		}
		return c;
	}

	@Override
	public final DBClass createSuperClass(final String name, final DBClass parent) {
		final DBClass c = createSuperClassNoCache(name, parent);
		if (allClassesStore != null) {
			allClassesStore.add(c);
		}
		return c;
	}

	protected abstract DBClass createClassNoCache(final String name, final DBClass parent);

	protected abstract DBClass createSuperClassNoCache(final String name, final DBClass parent);

	@Override
	public final void deleteClass(final DBClass dbClass) {
		deleteClassNoCache(dbClass);
		if (allClassesStore != null) {
			allClassesStore.remove(dbClass);
		}
	}

	protected abstract void deleteClassNoCache(final DBClass dbClass);

	@Override
	public final DBClass findClassById(final Long id) {
		DBClass dbClass = null;
		try {
			dbClass = getAllClassesStore().getById(id);
			logger.debug("class name for '{}' is '{}'", id, dbClass.getName());
		} catch (final Exception e) {
			logger.warn("no class found for id '{}'", id);
		}
		return dbClass;
	}

	@Override
	public final DBClass findClassByName(final String name) {
		return getAllClassesStore().getByName(name);
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
					this.allDomainsStore = refForSafeClearCache = new TypeObjectStore<DBDomain>(findAllDomainsNoCache());
				}
			}
		}
		return refForSafeClearCache;
	}

	protected abstract Collection<DBDomain> findAllDomainsNoCache();

	@Override
	public DBDomain createDomain(final DomainDefinition domainDefinition) {
		final DBDomain d = createDomainNoCache(domainDefinition);
		if (allDomainsStore != null) {
			allDomainsStore.add(d);
		}
		return d;
	}

	protected abstract DBDomain createDomainNoCache(final DomainDefinition domainDefinition);

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		deleteDomainNoCache(dbDomain);
		if (allDomainsStore != null) {
			allDomainsStore.remove(dbDomain);
		}
	}

	protected abstract void deleteDomainNoCache(final DBDomain dbDomain);

	@Override
	public DBDomain findDomainById(final Long id) {
		return getAllDomainsStore().getById(id);
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		return getAllDomainsStore().getByName(name);
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
					this.allFunctionsStore = refForSafeClearCache = new TypeObjectStore<DBFunction>(
							findAllFunctionsNoCache());
				}
			}
		}
		return refForSafeClearCache;
	}

	@Override
	public DBFunction findFunctionByName(final String name) {
		return getAllFunctionsStore().getByName(name);
	}

	protected abstract Collection<DBFunction> findAllFunctionsNoCache();

	/*
	 * Cache management
	 */

	public void clearCache() {
		synchronized (this) {
			clearClassesCache();
			clearDomainsCache();
			clearFunctionsCache();
		}
	}

	public void clearClassesCache() {
		synchronized (this) {
			allClassesStore = null;
		}
	}

	public void clearDomainsCache() {
		synchronized (this) {
			allDomainsStore = null;
		}
	}

	public void clearFunctionsCache() {
		synchronized (this) {
			allFunctionsStore = null;
		}
	}
}
