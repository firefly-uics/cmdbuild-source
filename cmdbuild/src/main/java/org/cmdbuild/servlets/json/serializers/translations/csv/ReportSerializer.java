package org.cmdbuild.servlets.json.serializers.translations.csv;

import static org.cmdbuild.servlets.json.serializers.translations.commons.Constants.NO_OWNER;

import java.util.Collection;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.serializers.translations.csv.ViewSerializer.Builder;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public class ReportSerializer extends DefaultElementSerializer {
	
	private final Report theReport;

	public static Builder newInstance() {
		return new Builder();
	}

	@Override
	public Collection<? extends CsvTranslationRecord> serialize() {
		final String code = theReport.getCode();
		final TranslatableElement element = TranslatableElement.REPORT;
		return serializeFields(NO_OWNER, code, element);
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ReportSerializer> {

		private Iterable<String> enabledLanguages;
		private TranslationLogic translationLogic;
		public Report theReport;
		public ReportStore reportStore;

		@Override
		public ReportSerializer build() {
			return new ReportSerializer(this);
		}

		public Builder withReport(final Report theReport) {
			this.theReport = theReport;
			return this;
		}

		public Builder withEnabledLanguages(final Iterable<String> enabledLanguages) {
			this.enabledLanguages = enabledLanguages;
			return this;
		}

		public Builder withTranslationLogic(final TranslationLogic translationLogic) {
			this.translationLogic = translationLogic;
			return this;
		}

		public Builder withReportStore(final ReportStore reportStore) {
			this.reportStore = reportStore;
			return this;
		}

	}

	private ReportSerializer(final Builder builder) {
		super.reportStore = builder.reportStore;
		super.enabledLanguages = builder.enabledLanguages;
		super.translationLogic = builder.translationLogic;
		this.theReport = builder.theReport;
	}

}
