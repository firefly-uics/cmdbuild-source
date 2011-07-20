package org.cmdbuild.dao.driver;

import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;

public abstract class CachingDriver implements DBDriver {

	private class EntryTypeStore<T extends CMEntryType> {
		final Collection<T> collection;

		EntryTypeStore(Collection<T> collection) {
			this.collection = collection;
		}

		Collection<T> getCollection() {
			return collection;
		}

		void add(T newEntryType) {
			collection.add(newEntryType);
		}

		void remove(T entry) {
			collection.remove(entry);
		}

		T getByName(final String name) {
			for (T e : collection) {
				if (e.getName().equals(name)) {
					return e;
				}
			}
			return null;
		}

		T getById(final Object id) {
			for (T e : collection) {
				if (e.getId().equals(id)) {
					return e;
				}
			}
			return null;
		}
	}

	private EntryTypeStore<DBClass> allClassesStore;
	private EntryTypeStore<DBDomain> allDomainsStore;

	@Override
	public final Collection<DBClass> findAllClasses() {
		return getAllClassesStore().getCollection();
	}

	private EntryTypeStore<DBClass> getAllClassesStore() {
		if (allClassesStore == null) {
			this.allClassesStore = new EntryTypeStore<DBClass>(findAllClassesNoCache());
		}
		return allClassesStore;
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

	protected abstract DBClass createClassNoCache(final String name, final DBClass parent);

	@Override
	public final void deleteClass(final DBClass dbClass) {
		deleteClassNoCache(dbClass);
		if (allClassesStore != null) {
			allClassesStore.remove(dbClass);
		}
	}

	protected abstract void deleteClassNoCache(final DBClass dbClass);

	@Override
	public final DBClass findClassById(Object id) {
		try {
			return getAllClassesStore().getById(normalizeId(id));
		} catch (Exception e) {
			return null;
		}
	}

	// TODO It should be implemented by every driver
	//public abstract Object normalizeId(Object id);
	public Object normalizeId(final Object id) {
		if (id instanceof Long) {
			return id;
		} else if (id instanceof Number) {
			return ((Number) id).longValue();
		} else if (id instanceof String) {
			return Long.valueOf((String) id);
		} else {
			throw new IllegalArgumentException("Invalid Id");
		}
	}

	@Override
	public final DBClass findClassByName(final String name) {
		return getAllClassesStore().getByName(name);
	}

	@Override
	public final Collection<DBDomain> findAllDomains() {
		return getAllDomainsStore().getCollection();
	}

	private EntryTypeStore<DBDomain> getAllDomainsStore() {
		if (allDomainsStore == null) {
			this.allDomainsStore = new EntryTypeStore<DBDomain>(findAllDomainsNoCache());
		}
		return allDomainsStore;
	}

	protected abstract Collection<DBDomain> findAllDomainsNoCache();

	@Override
	public DBDomain createDomain(final String name, final DBClass class1, final DBClass class2) {
		final DBDomain d = createDomainNoCache(name, class1, class2);
		if (allDomainsStore != null) {
			allDomainsStore.add(d);
		}
		return d;
	}

	protected abstract DBDomain createDomainNoCache(final String name, final DBClass class1, final DBClass class2);

	@Override
	public void deleteDomain(final DBDomain dbDomain) {
		deleteDomainNoCache(dbDomain);
		if (allDomainsStore != null) {
			allDomainsStore.remove(dbDomain);
		}
	}

	protected abstract void deleteDomainNoCache(final DBDomain dbDomain);

	@Override
	public DBDomain findDomainById(final Object id) {
		return getAllDomainsStore().getById(id);
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		return getAllDomainsStore().getByName(name);
	}
}
