package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ReportConverter;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.report.ReportFactory.ReportType;
import org.cmdbuild.services.store.report.Report;
import org.cmdbuild.services.store.report.ReportStore;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElement;
import org.cmdbuild.servlets.json.translationtable.objects.JsonField;
import org.json.JSONArray;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ReportTranslationSerializer implements TranslationSerializer {

	private final ReportStore reportStore;
	private final TranslationLogic translationLogic;
	Ordering<Report> ordering = ReportSorter.DEFAULT.getOrientedOrdering();

	public ReportTranslationSerializer(final ReportStore reportStore, final TranslationLogic translationLogic,
			final JSONArray sorters) {
		this.reportStore = reportStore;
		this.translationLogic = translationLogic;
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		// TODO
	}

	@Override
	public JsonResponse serialize() {
		final Collection<Report> allReports = Lists.newArrayList();
		for (final ReportType type : ReportType.values()) {
			final Iterable<Report> reportsOfType = reportStore.findReportsByType(type);
			Iterables.addAll(allReports, reportsOfType);
		}
		final Iterable<Report> sorterReports = ordering.sortedCopy(allReports);
		return serialize(sorterReports);
	}

	private JsonResponse serialize(final Iterable<Report> sortedReports) {
		final Collection<JsonElement> jsonReports = Lists.newArrayList();
		for (final Report report : sortedReports) {
			final String name = report.getCode();
			final JsonElement jsonReport = new JsonElement();
			jsonReport.setName(name);
			final Collection<JsonField> classFields = readFields(report);
			jsonReport.setFields(classFields);
			jsonReports.add(jsonReport);
		}
		return JsonResponse.success(jsonReports);
	}

	private Collection<JsonField> readFields(final Report report) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ReportConverter.DESCRIPTION //
				.withIdentifier(report.getCode()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(ViewConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(report.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}
	
	@Override
	public DataHandler serializeCsv() {
		throw new UnsupportedOperationException("to do");
	}

}
