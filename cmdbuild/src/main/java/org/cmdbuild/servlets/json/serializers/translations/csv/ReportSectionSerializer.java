package org.cmdbuild.servlets.json.serializers.translations.csv;

import java.util.Collection;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.serializers.translations.commons.ReportSorter;
import org.cmdbuild.servlets.json.serializers.translations.commons.TranslationSectionSerializer;
import org.cmdbuild.servlets.json.serializers.translations.commons.ViewSorter;
import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;
import org.json.JSONArray;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ReportSectionSerializer implements TranslationSectionSerializer {
	
	private final Iterable<String> enabledLanguages;
	private final TranslationLogic translationLogic;
	private final ReportStore reportStore;
	Ordering<Report> ordering = ReportSorter.DEFAULT.getOrientedOrdering();
	
	private final Collection<TranslationSerialization> records = Lists.newArrayList();

	public ReportSectionSerializer(final TranslationLogic translationLogic, final JSONArray sorters,
			final SetupFacade setupFacade, final ReportStore reportStore) {
		this.reportStore = reportStore;
		this.translationLogic = translationLogic;
		this.enabledLanguages = setupFacade.getEnabledLanguages();
		// TODO: manage ordering configuration
	}

	@Override
	public Iterable<TranslationSerialization> serialize() {
		final Collection<Report> allReports = Lists.newArrayList();
		for (final ReportType type : ReportType.values()) {
			final Iterable<Report> reportsOfType = reportStore.findReportsByType(type);
			Iterables.addAll(allReports, reportsOfType);
		}
		final Iterable<Report> sorterReports = ordering.sortedCopy(allReports);

		for (final Report report : sorterReports) {
			records.addAll(ReportSerializer.newInstance() //
					.withEnabledLanguages(enabledLanguages) //
					.withTranslationLogic(translationLogic) //
					.withReportStore(reportStore) //
					.withReport(report) //
					.build() //
					.serialize());
		}
		return records;
	}

}
