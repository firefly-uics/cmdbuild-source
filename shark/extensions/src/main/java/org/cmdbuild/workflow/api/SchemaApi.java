package org.cmdbuild.workflow.api;

import org.cmdbuild.workflow.type.LookupType;

/**
 * API to query the database structure.
 */
public interface SchemaApi {

	/**
	 * Temporary object till we find a decent solution
	 */
	class ClassInfo {
		private final String name;
		private final int id;

		public ClassInfo(final String name, final int id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		@Override
		public int hashCode() {
			return Integer.valueOf(id).hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ClassInfo other = (ClassInfo) obj;
			return (id == other.id);
		}
	}

	ClassInfo findClass(String className);

	ClassInfo findClass(int classId);

	LookupType selectLookupById(int id);

	LookupType selectLookupByCode(String type, String code);

	LookupType selectLookupByDescription(String type, String description);
}
