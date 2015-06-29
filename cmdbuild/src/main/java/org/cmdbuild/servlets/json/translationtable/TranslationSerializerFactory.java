package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.json.JSONArray;

public class TranslationSerializerFactory {

	private static final String CLASS = "class";
	private static final String DOMAIN = "domain";
	private static final String FILTER = "filter";
	private static final String LOOKUP = "lookup";
	private static final String PROCESS = "process";
	private static final String VIEW = "view";
	private static final String REPORT = "report";

	private final boolean activeOnly;
	private final DataAccessLogic dataLogic;
	private final FilterStore filterStore;
	private final LookupStore lookupStore;
	private final TranslationLogic translationLogic;
	private final JSONArray sorters;
	private final String type;
	private final ViewLogic viewLogic;
	private final ReportStore reportStore;

	public static SerializerBuilder newInstance() {
		return new SerializerBuilder();
	}

	public TranslationSerializerFactory(final SerializerBuilder builder) {
		this.activeOnly = builder.activeOnly;
		this.dataLogic = builder.dataLogic;
		this.filterStore = builder.filterStore;
		this.lookupStore = builder.lookupStore;
		this.reportStore = builder.reportStore;
		this.sorters = builder.sorters;
		this.translationLogic = builder.translationLogic;
		this.type = builder.type;
		this.viewLogic = builder.viewLogic;
	}

	public TranslationSerializer createSerializer() {
		if (type.equalsIgnoreCase(CLASS)) {
			return new ClassTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters);
		} else if (type.equalsIgnoreCase(PROCESS)) {
			return new ProcessTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters);
		} else if (type.equalsIgnoreCase(DOMAIN)) {
			return new DomainTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters);
		} else if (type.equalsIgnoreCase(LOOKUP)) {
			return new LookupTranslationSerializer(lookupStore, activeOnly, translationLogic, sorters);
		} else if (type.equalsIgnoreCase(VIEW)) {
			return new ViewTranslationSerializer(viewLogic, translationLogic, sorters);
		} else if (type.equalsIgnoreCase(FILTER)) {
			return new FilterTranslationSerializer(filterStore, translationLogic, sorters);
		} else if (type.equalsIgnoreCase(REPORT)) {
			return new ReportTranslationSerializer(reportStore, translationLogic, sorters);
		} else {
			throw new IllegalArgumentException("type '" + type + "' unsupported");
		}
	}

	public static final class SerializerBuilder implements Builder<TranslationSerializerFactory> {

		private ViewLogic viewLogic;
		private DataAccessLogic dataLogic;
		private LookupStore lookupStore;
		private FilterStore filterStore;
		private ReportStore reportStore;
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

		public SerializerBuilder withViewLogic(final ViewLogic viewLogic) {
			this.viewLogic = viewLogic;
			return this;
		}

		public SerializerBuilder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public SerializerBuilder withReportStore(final ReportStore reportStore) {
			this.reportStore = reportStore;
			return this;
		}

		public SerializerBuilder withSorters(final JSONArray sorters) {
			this.sorters = sorters;
			return this;
		}

		public SerializerBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public SerializerBuilder withActiveOnly(final boolean activeOnly) {
			this.activeOnly = activeOnly;
			return this;
		}

		public SerializerBuilder withFilterStore(final FilterStore filterStore) {
			this.filterStore = filterStore;
			return this;
		}

		@Override
		public TranslationSerializerFactory build() {
			return new TranslationSerializerFactory(this);
		}

	}

}
