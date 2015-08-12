package org.cmdbuild.servlets.json.translationtable;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializerFactory.SerializerBuilder;
import org.json.JSONArray;

public class TranslationSerializerFactory {

	private static final String CLASS = "class";
	private static final String DOMAIN = "domain";
	private static final String FILTER = "filter";
	private static final String LOOKUP = "lookup";
	private static final String PROCESS = "process";
	private static final String VIEW = "view";
	private static final String REPORT = "report";
	private static final String MENU = "menu";

	private final boolean activeOnly;
	private final DataAccessLogic dataLogic;
	private final FilterStore filterStore;
	private final LookupStore lookupStore;
	private final TranslationLogic translationLogic;
	private final JSONArray sorters;
	private final String type;
	private final ViewLogic viewLogic;
	private final ReportStore reportStore;
	private final AuthenticationLogic authLogic;
	private final MenuLogic menuLogic;
	private final String separator;
	private SetupFacade setupFacade;

	public static SerializerBuilder newInstance() {
		return new SerializerBuilder();
	}

	public TranslationSerializerFactory(final SerializerBuilder builder) {
		this.activeOnly = builder.activeOnly;
		this.authLogic = builder.authLogic;
		this.dataLogic = builder.dataLogic;
		this.filterStore = builder.filterStore;
		this.lookupStore = builder.lookupStore;
		this.menuLogic = builder.menuLogic;
		this.reportStore = builder.reportStore;
		this.sorters = builder.sorters;
		this.translationLogic = builder.translationLogic;
		this.type = builder.type;
		this.viewLogic = builder.viewLogic;
		this.separator = builder.separator;
		this.setupFacade = builder.setupFacade;
	}

	public TranslationSerializer createSerializer() {
		if (type.equalsIgnoreCase(CLASS)) {
			return new ClassTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(DOMAIN)) {
			return new DomainTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(FILTER)) {
			return new FilterTranslationSerializer(filterStore, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(LOOKUP)) {
			return new LookupTranslationSerializer(lookupStore, activeOnly, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(MENU)) {
			return new MenuTranslationSerializer(authLogic, menuLogic, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(PROCESS)) {
			return new ProcessTranslationSerializer(dataLogic, activeOnly, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(REPORT)) {
			return new ReportTranslationSerializer(reportStore, translationLogic, sorters, separator, setupFacade);
		} else if (type.equalsIgnoreCase(VIEW)) {
			return new ViewTranslationSerializer(viewLogic, translationLogic, sorters, separator, setupFacade);
		} else {
			throw new IllegalArgumentException("type '" + type + "' unsupported");
		}
	}

	public static final class SerializerBuilder implements Builder<TranslationSerializerFactory> {

		public String separator;
		private boolean activeOnly;
		private AuthenticationLogic authLogic;
		private DataAccessLogic dataLogic;
		private FilterStore filterStore;
		private LookupStore lookupStore;
		private MenuLogic menuLogic;
		private JSONArray sorters;
		private ReportStore reportStore;
		private TranslationLogic translationLogic;
		private String type;
		private ViewLogic viewLogic;
		private SetupFacade setupFacade;

		public SerializerBuilder withActiveOnly(final boolean activeOnly) {
			this.activeOnly = activeOnly;
			return this;
		}

		public SerializerBuilder withAuthLogic(final AuthenticationLogic authLogic) {
			this.authLogic = authLogic;
			return this;
		}

		public SerializerBuilder withDataAccessLogic(final DataAccessLogic dataLogic) {
			this.dataLogic = dataLogic;
			return this;
		}

		public SerializerBuilder withFilterStore(final FilterStore filterStore) {
			this.filterStore = filterStore;
			return this;
		}

		public SerializerBuilder withLookupStore(final LookupStore lookupStore) {
			this.lookupStore = lookupStore;
			return this;
		}

		public SerializerBuilder withMenuLogic(final MenuLogic menuLogic) {
			this.menuLogic = menuLogic;
			return this;
		}

		public SerializerBuilder withReportStore(final ReportStore reportStore) {
			this.reportStore = reportStore;
			return this;
		}

		public SerializerBuilder withSeparator(final String separator) {
			this.separator = separator;
			return this;
		}
		
		public SerializerBuilder withSetupFacade(SetupFacade setupFacade) {
			this.setupFacade = setupFacade;
			return this;
		}

		public SerializerBuilder withSorters(final JSONArray sorters) {
			this.sorters = sorters;
			return this;
		}

		public SerializerBuilder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public SerializerBuilder withType(final String type) {
			this.type = type;
			return this;
		}

		public SerializerBuilder withViewLogic(final ViewLogic viewLogic) {
			this.viewLogic = viewLogic;
			return this;
		}

		@Override
		public TranslationSerializerFactory build() {
			return new TranslationSerializerFactory(this);
		}

	}

}
