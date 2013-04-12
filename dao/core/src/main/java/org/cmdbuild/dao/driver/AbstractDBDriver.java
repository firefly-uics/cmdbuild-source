package org.cmdbuild.dao.driver;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.TypeObjectCache;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.logging.LoggingSupport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class AbstractDBDriver implements DBDriver, LoggingSupport {

	private static class Identifier {

		public static Identifier from(final CMTypeObject typeObject) {
			return from(typeObject.getIdentifier());
		}

		public static Identifier from(final CMIdentifier identifier) {
			return new Identifier(identifier.getLocalName(), identifier.getNameSpace());
		}

		public final String localname;
		public final String namespace;

		private final transient int hashCode;
		private final transient String toString;

		public Identifier(final String localname, final String namespace) {
			this.localname = localname;
			this.namespace = namespace;

			this.hashCode = new HashCodeBuilder() //
					.append(localname) //
					.append(namespace) //
					.hashCode();
			this.toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.append("localname", localname) //
					.append("namespace", namespace) //
					.toString();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Identifier)) {
				return false;
			}
			final Identifier other = Identifier.class.cast(obj);
			return new EqualsBuilder() //
					.append(localname, other.localname) //
					.append(namespace, other.namespace) //
					.isEquals();
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	public static class DefaultTypeObjectCache implements TypeObjectCache {

		private final Map<Class<? extends CMTypeObject>, Map<Long, CMTypeObject>> storeById;
		private final Map<Class<? extends CMTypeObject>, Map<Identifier, CMTypeObject>> storeByIdentifier;

		public DefaultTypeObjectCache() {
			storeById = Maps.newHashMap();
			storeById.put(DBClass.class, newMapById());
			storeById.put(DBDomain.class, newMapById());
			storeById.put(DBFunction.class, newMapById());

			storeByIdentifier = Maps.newHashMap();
			storeByIdentifier.put(DBClass.class, newMapByIdentifier());
			storeByIdentifier.put(DBDomain.class, newMapByIdentifier());
			storeByIdentifier.put(DBFunction.class, newMapByIdentifier());
		}

		private static Map<Long, CMTypeObject> newMapById() {
			return Maps.newHashMap();
		}

		private static Map<Identifier, CMTypeObject> newMapByIdentifier() {
			return Maps.newHashMap();
		}

		@Override
		public void add(final CMTypeObject typeObject) {
			synchronized (this) {
				storeById.get(classOf(typeObject)).put(idOf(typeObject), typeObject);
				storeByIdentifier.get(classOf(typeObject)).put(identifierOf(typeObject), typeObject);
			}
		}

		@Override
		public boolean hasNoClass() {
			synchronized (this) {
				return storeById.get(DBClass.class).isEmpty() || //
						storeByIdentifier.get(DBClass.class).isEmpty();
			}
		}

		@Override
		public void remove(final CMTypeObject typeObject) {
			synchronized (this) {
				storeById.get(classOf(typeObject)).remove(idOf(typeObject));
				storeByIdentifier.get(classOf(typeObject)).remove(identifierOf(typeObject));
			}
		}

		@Override
		public List<DBClass> fetchCachedClasses() {
			final Map<Long, CMTypeObject> cachedClassesMap = storeById.get(DBClass.class);
			final List<DBClass> cachedClasses = Lists.newArrayList();
			for (final Long id : cachedClassesMap.keySet()) {
				cachedClasses.add((DBClass) cachedClassesMap.get(id));
			}
			return cachedClasses;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass, final Long id) {
			return (T) storeById.get(typeObjectClass).get(id);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends CMTypeObject> T fetch(final Class<? extends CMTypeObject> typeObjectClass,
				final CMIdentifier identifier) {
			return (T) storeByIdentifier.get(typeObjectClass).get(Identifier.from(identifier));
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
				storeById.get(DBClass.class).clear();
				storeByIdentifier.get(DBClass.class).clear();
			}
		}

		@Override
		public void clearDomains() {
			synchronized (this) {
				logger.info("clearing domains cache");
				storeById.get(DBDomain.class).clear();
				storeByIdentifier.get(DBDomain.class).clear();
			}
		}

		@Override
		public void clearFunctions() {
			synchronized (this) {
				logger.info("clearing functions cache");
				storeById.get(DBFunction.class).clear();
				storeByIdentifier.get(DBFunction.class).clear();
			}
		}

		private static Class<? extends CMTypeObject> classOf(final CMTypeObject typeObject) {
			return typeObject.getClass();
		}

		private static Long idOf(final CMTypeObject typeObject) {
			return typeObject.getId();
		}

		private static Identifier identifierOf(final CMTypeObject typeObject) {
			return Identifier.from(typeObject);
		}

	}

	protected TypeObjectCache cache;

	protected AbstractDBDriver(final TypeObjectCache cache) {
		Validate.notNull(cache, "The driver cache cannot be null");
		this.cache = cache;
	}

	protected abstract Iterable<DBClass> findAllClassesNoCache();

	@Override
	public final DBClass findClass(final Long id) {
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
	public final DBClass findClass(final String name) {
		return findClass(name, CMIdentifier.DEFAULT_NAMESPACE);
	}

	@Override
	public DBClass findClass(final String localname, final String namespace) {
		final DBClass cachedClass = cache.fetch(DBClass.class, identifierFrom(localname, namespace));
		if (cachedClass != null) {
			return cachedClass;
		}
		for (final DBClass dbClass : findAllClassesNoCache()) {
			final CMIdentifier identifier = dbClass.getIdentifier();
			if (new EqualsBuilder() //
					.append(identifier.getLocalName(), localname) //
					.append(identifier.getNameSpace(), namespace) //
					.isEquals()) {
				cache.add(dbClass);
				return dbClass;
			}
		}
		return null;
	}

	@Override
	public DBDomain findDomain(final Long id) {
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
	public DBDomain findDomain(final String localname) {
		return findDomain(localname, CMIdentifier.DEFAULT_NAMESPACE);
	}

	@Override
	public DBDomain findDomain(final String localname, final String namespace) {
		final DBDomain cachedDomain = cache.fetch(DBDomain.class, identifierFrom(localname, namespace));
		if (cachedDomain != null) {
			return cachedDomain;
		}
		for (final DBDomain dbDomain : findAllDomains()) {
			final CMIdentifier identifier = dbDomain.getIdentifier();
			if (new EqualsBuilder() //
					.append(identifier.getLocalName(), localname) //
					.append(identifier.getNameSpace(), namespace) //
					.isEquals()) {
				cache.add(dbDomain);
				return dbDomain;
			}
		}
		return null;
	}

	@Override
	public DBFunction findFunction(final String localname) {
		final DBFunction cachedFunction = cache.fetch(DBFunction.class, identifierFrom(localname));
		if (cachedFunction != null) {
			return cachedFunction;
		}
		for (final DBFunction dbFunction : findAllFunctions()) {
			if (dbFunction.getIdentifier().getLocalName().equals(localname)) {
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

	private static CMIdentifier identifierFrom(final String localname) {
		return identifierFrom(localname, CMIdentifier.DEFAULT_NAMESPACE);
	}

	private static CMIdentifier identifierFrom(final String localname, final String namespace) {
		return new CMIdentifier() {
			@Override
			public String getLocalName() {
				return localname;
			}

			@Override
			public String getNameSpace() {
				return namespace;
			}

			@Override
			public String toString() {
				return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
						.append(localname) //
						.append(namespace).toString();
			}
		};
	}

}
