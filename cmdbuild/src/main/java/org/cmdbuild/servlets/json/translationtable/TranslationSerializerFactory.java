package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.json.JSONArray;

public class TranslationSerializerFactory {

	private static final String LOOKUP = "lookup";
	private static final String DOMAIN = "domain";
	private static final String PROCESS = "process";
	private static final String CLASS = "class";
	private final DataAccessLogic dataLogic;
	private final LookupStore lookupStore;
	private final TranslationLogic logic;
	private final String type;
	private final JSONArray sorters;
	private final boolean activeOnly;

	public static SerializerBuilder newInstance() {
		return new SerializerBuilder();
	}

	public TranslationSerializerFactory(final SerializerBuilder builder) {
		this.dataLogic = builder.dataLogic;
		this.lookupStore = builder.lookupStore;
		this.logic = builder.translationLogic;
		this.type = builder.type;
		this.sorters = builder.sorters;
		this.activeOnly = builder.activeOnly;
	}

	public TranslationSerializer createSerializer() {
		if (type.equalsIgnoreCase(CLASS)) {
			return new ClassTranslationSerializer(dataLogic, activeOnly, logic, sorters);
		} else if (type.equalsIgnoreCase(PROCESS)) {
			return new ProcessTranslationSerializer(dataLogic, activeOnly, logic, sorters);
		} else if (type.equalsIgnoreCase(DOMAIN)) {
			return new DomainTranslationSerializer(dataLogic, activeOnly, logic, sorters);
		} else if (type.equalsIgnoreCase(LOOKUP)) {
			return new LookupTranslationSerializer(lookupStore, activeOnly, logic, sorters);
		} else {
			throw new IllegalArgumentException("type '" + type + "' unsupported");
		}
	}

	public static final class SerializerBuilder implements Builder<TranslationSerializerFactory> {

		private DataAccessLogic dataLogic;
		private LookupStore lookupStore;
		private TranslationLogic translationLogic;
		private String type;
		private JSONArray sorters;
		private boolean activeOnly;

		public SerializerBuilder withDataAccessLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public SerializerBuilder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public SerializerBuilder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public SerializerBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public SerializerBuilder withSorters(final JSONArray sorters) {
			this.sorters = sorters;
			return this;
		}

		public SerializerBuilder withActiveOnly(final boolean activeOnly) {
			this.activeOnly = activeOnly;
			return this;
		}

		@Override
		public TranslationSerializerFactory build() {
			return new TranslationSerializerFactory(this);
		}

	}

}
