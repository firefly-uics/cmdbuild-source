package org.cmdbuild.dao.driver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.logging.LoggingSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractDBDriver implements DBDriver, LoggingSupport {

	public static class DefaultTypeObjectCache implements TypeObjectCache {

		private final Map<Class<? extends CMTypeObject>, Map<Long, CMTypeObject>> idTypeObjectStore;
		private final Map<Class<? extends CMTypeObject>, Map<String, CMTypeObject>> nameTypeObjectStore;

		public DefaultTypeObjectCache() {
			idTypeObjectStore = Maps.newHashMap();
			idTypeObjectStore.put(DBClass.class, new HashMap<Long, CMTypeObject>());
			idTypeObjectStore.put(DBDomain.class, new HashMap<Long, CMTypeObject>());
			idTypeObjectStore.put(DBFunction.class, new HashMap<Long, CMTypeObject>());
			nameTypeObjectStore = Maps.newHashMap();
			nameTypeObjectStore.put(DBClass.class, new HashMap<String, CMTypeObject>());
			nameTypeObjectStore.put(DBDomain.class, new HashMap<String, CMTypeObject>());
			nameTypeObjectStore.put(DBFunction.class, new HashMap<String, CMTypeObject>());
		}

		@Override
		public void add(final CMTypeObject typeObject) {
			synchronized (this) {
				final Map<Long, CMTypeObject> idMap = idTypeObjectStore.get(typeObject.getClass());
				idMap.put(typeObject.getId(), typeObject);
				final Map<String, CMTypeObject> nameMap = nameTypeObjectStore.get(typeObject.getClass());
				nameMap.put(typeObject.getName(), typeObject);
			}
		}

		@Override
		public boolean hasNoClass() {
			synchronized (this) {
				return idTypeObjectStore.get(DBClass.class).isEmpty() || //
						nameTypeObjectStore.get(DBClass.class).isEmpty();
			}
		}

		@Override
		public void remove(final CMTypeObject typeObject) {
			synchronized (this) {
				idTypeObjectStore.get(typeObject.getClass()).remove(typeObject.getId());
				nameTypeObjectStore.get(typeObject.getClass()).remove(typeObject.getName());
			}
		}

		@Override
		public List<DBClass> fetchCachedClasses() {
			Map<Long, CMTypeObject> cachedClassesMap = idTypeObjectStore.get(DBClass.class);
			List<DBClass> cachedClasses = Lists.newArrayList();
			for (Long id : cachedClassesMap.keySet()) {
				cachedClasses.add((DBClass) cachedClassesMap.get(id));
			}
			return cachedClasses;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass, final Long id) {
			return (T) idTypeObjectStore.get(typeObjectClass).get(id);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass, final String name) {
			return (T) nameTypeObjectStore.get(typeObjectClass).get(name);
		}

		@Override
		public void clearCache() {
			synchronized (this) {
				logger.info("clearing all cache");
				clearClasses();
				clearDomains();
				clearFunctions();
			}
		}

		@Override
		public void clearClasses() {
			synchronized (this) {
				logger.info("clearing classes cache");
				idTypeObjectStore.get(DBClass.class).clear();
				nameTypeObjectStore.get(DBClass.class).clear();
			}
		}

		@Override
		public void clearDomains() {
			synchronized (this) {
				logger.info("clearing domains cache");
				idTypeObjectStore.get(DBDomain.class).clear();
				nameTypeObjectStore.get(DBDomain.class).clear();
			}
		}

		@Override
		public void clearFunctions() {
			synchronized (this) {
				logger.info("clearing functions cache");
				idTypeObjectStore.get(DBFunction.class).clear();
				nameTypeObjectStore.get(DBFunction.class).clear();
			}
		}
	}

	protected TypeObjectCache cache;

	protected AbstractDBDriver(final TypeObjectCache cache) {
		Validate.notNull(cache, "The driver cache cannot be null");
		this.cache = cache;
	}

	protected abstract Iterable<DBClass> findAllClassesNoCache();

	@Override
	public final DBClass findClassById(final Long id) {
		final DBClass cachedClass = cache.fetch(DBClass.class, id);
		if (cachedClass != null) {
			return cachedClass;
		}
		for (final DBClass dbClass : findAllClassesNoCache()) {
			if (dbClass.getId().equals(id)) {
				cache.add(dbClass);
				return dbClass;
			}
		}
		return null;
	}

	@Override
	public final DBClass findClassByName(final String name) {
		final DBClass cachedClass = cache.fetch(DBClass.class, name);
		if (cachedClass != null) {
			return cachedClass;
		}
		for (final DBClass dbClass : findAllClassesNoCache()) {
			if (dbClass.getName().equals(name)) {
				cache.add(dbClass);
				return dbClass;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomainById(final Long id) {
		final DBDomain cachedDomain = cache.fetch(DBDomain.class, id);
		if (cachedDomain != null) {
			return cachedDomain;
		}
		for (final DBDomain dbDomain : findAllDomains()) {
			if (dbDomain.getId().equals(id)) {
				cache.add(dbDomain);
				return dbDomain;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomainByName(final String name) {
		final DBDomain cachedDomain = cache.fetch(DBDomain.class, name);
		if (cachedDomain != null) {
			return cachedDomain;
		}
		for (final DBDomain dbDomain : findAllDomains()) {
			if (dbDomain.getName().equals(name)) {
				cache.add(dbDomain);
				return dbDomain;
			}
		}
		return null;
	}

	@Override
	public DBFunction findFunctionByName(final String name) {
		final DBFunction cachedFunction = cache.fetch(DBFunction.class, name);
		if (cachedFunction != null) {
			return cachedFunction;
		}
		for (final DBFunction dbFunction : findAllFunctions()) {
			if (dbFunction.getName().equals(name)) {
				cache.add(dbFunction);
				return dbFunction;
			}
		}
		return null;
	}

	public void clearCache() {
		cache.clearCache();
	}

	public void clearClassesCache() {
		cache.clearClasses();
	}

	public void clearDomainsCache() {
		cache.clearDomains();
	}

	public void clearFunctionsCache() {
		cache.clearFunctions();
	}

}
