package org.cmdbuild.dao.driver;

import java.util.Collection;

import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;

public class CachingDriver implements DBDriver {

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

	private DBDriver innerDriver;
	private EntryTypeStore<DBClass> allClassesStore;

	public CachingDriver(final DBDriver innerDriver) {
		super();
		this.innerDriver = innerDriver;
	}

	@Override
	public final Collection<DBClass> findAllClasses() {
		return getAllClassesStore().getCollection();
	}

	// FIXME: Need to be sure that allClassesStore is never referenced outside
	private EntryTypeStore<DBClass> getAllClassesStore() {
		if (allClassesStore == null) {
			this.allClassesStore = new EntryTypeStore<DBClass>(innerDriver.findAllClasses());
		}
		return allClassesStore;
	}

	@Override
	public final DBClass createClass(final String name, final DBClass parent) {
		final DBClass c = innerDriver.createClass(name, parent);
		if (allClassesStore != null) {
			allClassesStore.add(c);
		}
		return c;
	}

	@Override
	public final void deleteClass(final DBClass dbClass) {
		innerDriver.deleteClass(dbClass);
		if (allClassesStore != null) {
			allClassesStore.remove(dbClass);
		}
	}

	@Override
	public void update(DBEntry entry) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DBClass findClassById(Object id) {
		return getAllClassesStore().getById(id);
	}

	@Override
	public DBClass findClassByName(final String name) {
		return getAllClassesStore().getByName(name);
	}

	@Override
	public Object create(DBEntry entry) {
		return innerDriver.create(entry);
	}

	@Override
	public void delete(DBEntry entry) {
		innerDriver.delete(entry);
	}

	@Override
	public CMQueryResult query(QuerySpecs query) {
		return innerDriver.query(query);
	}
}
