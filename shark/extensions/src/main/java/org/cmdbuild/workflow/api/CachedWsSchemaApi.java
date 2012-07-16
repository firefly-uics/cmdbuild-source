package org.cmdbuild.workflow.api;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.services.soap.MenuSchema;
import org.cmdbuild.services.soap.Private;

public class CachedWsSchemaApi implements SchemaApi {

	private final Private proxy;

	private Map<String, ClassInfo> classesByName;
	private Map<Integer, ClassInfo> classesById;

	public CachedWsSchemaApi(final Private proxy) {
		this.proxy = proxy;
		this.classesByName = new HashMap<String, ClassInfo>();
		this.classesById = new HashMap<Integer, ClassInfo>();
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
		for (MenuSchema subclassSchema : classSchema.getChildren()) {
			addClassToMapRecursively(subclassSchema);
		}
	}
}
