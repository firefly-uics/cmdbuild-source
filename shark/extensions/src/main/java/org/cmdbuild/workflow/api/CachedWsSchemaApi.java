package org.cmdbuild.workflow.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.services.soap.Lookup;
import org.cmdbuild.services.soap.MenuSchema;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.type.LookupType;

public class CachedWsSchemaApi implements SchemaApi {

	private static final String ANY_DESCRIPTION = null;
	private static final boolean NO_PARENT_LIST = false;

	private final Private proxy;

	private final Map<String, ClassInfo> classesByName;
	private final Map<Integer, ClassInfo> classesById;

	private final Map<String, List<LookupType>> lookupsByType;
	private final Map<Integer, LookupType> lookupsById;

	public CachedWsSchemaApi(final Private proxy) {
		this.proxy = proxy;
		this.classesByName = new HashMap<String, ClassInfo>();
		this.classesById = new HashMap<Integer, ClassInfo>();
		this.lookupsByType = new HashMap<String, List<LookupType>>();
		this.lookupsById = new HashMap<Integer, LookupType>();
	}

	@Override
	public synchronized ClassInfo findClass(final String className) {
		if (!classesByName.containsKey(className)) {
			updateClassMap();
		}
		return classesByName.get(className);
	}

	@Override
	public synchronized ClassInfo findClass(final int classId) {
		if (!classesById.containsKey(classId)) {
			updateClassMap();
		}
		return classesById.get(classId);
	}

	private void updateClassMap() {
		addClassToMapRecursively(proxy.getCardMenuSchema());
		addClassToMapRecursively(proxy.getActivityMenuSchema());
	}

	private void addClassToMapRecursively(final MenuSchema classSchema) {
		final ClassInfo classInfo = new ClassInfo(classSchema.getClassname(), classSchema.getId());
		classesById.put(classInfo.getId(), classInfo);
		classesByName.put(classInfo.getName(), classInfo);
		for (final MenuSchema subclassSchema : classSchema.getChildren()) {
			addClassToMapRecursively(subclassSchema);
		}
	}

	@Override
	public synchronized LookupType selectLookupById(final int id) {
		if (!lookupsById.containsKey(id)) {
			final LookupType lookup = convertLookup(proxy.getLookupById(id));
			lookupsById.put(id, lookup);
		}
		return lookupsById.get(id);
	}

	@Override
	public synchronized LookupType selectLookupByCode(final String type, final String code) {
		List<LookupType> lookupList = getLookupsByType(type);
		LookupType lookup = getLookupByCode(lookupList, code);
		if (lookup == null) {
			lookupList = updateLookupType(type);
			lookup = getLookupByCode(lookupList, code);
		}
		return lookup;
	}

	@Override
	public synchronized LookupType selectLookupByDescription(final String type, final String description) {
		List<LookupType> lookupList = getLookupsByType(type);
		LookupType lookup = getLookupByDescription(lookupList, description);
		if (lookup == null) {
			lookupList = updateLookupType(type);
			lookup = getLookupByDescription(lookupList, description);
		}
		return lookup;
	}

	private List<LookupType> getLookupsByType(final String type) {
		List<LookupType> lookupList = lookupsByType.get(type);
		if (lookupList == null) {
			lookupList = updateLookupType(type);
		}
		return lookupList;
	}

	private List<LookupType> updateLookupType(final String type) {
		final List<Lookup> wsLookupList = proxy.getLookupList(type, ANY_DESCRIPTION, NO_PARENT_LIST);
		final List<LookupType> lookupList = convertLookupList(wsLookupList);
		lookupsByType.put(type, lookupList);
		for (final LookupType lookup : lookupList) {
			lookupsById.put(lookup.getId(), lookup);
		}
		return lookupList;
	}

	private LookupType getLookupByCode(final List<LookupType> lookupList, final String code) {
		for (final LookupType lookup : lookupList) {
			if (StringUtils.equals(code, lookup.getCode())) {
				return lookup;
			}
		}
		return null;
	}

	private LookupType getLookupByDescription(final List<LookupType> lookupList, final String description) {
		for (final LookupType lookup : lookupList) {
			if (StringUtils.equals(description, lookup.getDescription())) {
				return lookup;
			}
		}
		return null;
	}

	private List<LookupType> convertLookupList(final List<Lookup> wsLookupList) {
		final List<LookupType> lookupList = new ArrayList<LookupType>(wsLookupList.size());
		for (final Lookup wsLookup : wsLookupList) {
			lookupList.add(convertLookup(wsLookup));
		}
		return lookupList;
	}

	private LookupType convertLookup(final Lookup wsLookup) {
		if (wsLookup == null) {
			return null;
		}
		final LookupType lookup = new LookupType();
		lookup.setType(wsLookup.getType());
		lookup.setId(wsLookup.getId());
		lookup.setCode(wsLookup.getCode());
		lookup.setDescription(wsLookup.getDescription());
		return lookup;
	}

}
