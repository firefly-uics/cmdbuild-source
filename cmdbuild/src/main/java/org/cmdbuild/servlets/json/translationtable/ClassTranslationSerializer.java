package org.cmdbuild.servlets.json.translationtable;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic.AttributesQuery;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.Converter;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.schema.TranslatableElement;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElement;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElementWithAttributes;
import org.cmdbuild.servlets.json.translationtable.objects.JsonField;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ClassTranslationSerializer extends EntryTypeTranslationSerializer {

	private static final String FILENAME = "classes.csv";

	private static final AttributesQuery NO_LIMIT_AND_OFFSET = new AttributesQuery() {

		@Override
		public Integer limit() {
			return null;
		}

		@Override
		public Integer offset() {
			return null;
		}

	};

	ClassTranslationSerializer(final DataAccessLogic dataLogic, final boolean activeOnly,
			final TranslationLogic translationLogic, final JSONArray sorters) {
		super(dataLogic, activeOnly, translationLogic);
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		if (sorters != null) {
			try {
				for (int i = 0; i < sorters.length(); i++) {
					final JSONObject object = JSONObject.class.cast(sorters.get(i));
					final String element = object.getString(ELEMENT);
					if (element.equalsIgnoreCase(CLASS) || element.equalsIgnoreCase(PROCESS)) {
						entryTypeOrdering = EntryTypeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					} else if (element.equalsIgnoreCase(ATTRIBUTE)) {
						attributeOrdering = AttributeSorter.of(object.getString(FIELD)) //
								.withDirection(object.getString(DIRECTION)) //
								.getOrientedOrdering();
					}
				}
			} catch (final JSONException e) {
				Log.JSONRPC.warn("ignoring malformed sorter");
			}
		}
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<? extends CMClass> sortedClasses = sortedClasses();
		return serialize(sortedClasses);
	}

	Iterable<? extends CMClass> sortedClasses() {
		final Iterable<? extends CMClass> classes = dataLogic.findClasses(activeOnly);
		final Iterable<? extends CMClass> sortedClasses = entryTypeOrdering.sortedCopy(classes);
		return sortedClasses;
	}

	JsonResponse serialize(final Iterable<? extends CMClass> sortedClasses) {
		final Collection<JsonElement> jsonClasses = Lists.newArrayList();
		for (final CMClass cmclass : sortedClasses) {
			final String className = cmclass.getName();
			final JsonElementWithAttributes jsonClass = new JsonElementWithAttributes();
			jsonClass.setName(className);
			final Collection<JsonField> classFields = readFields(cmclass);
			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(className, activeOnly,
					NO_LIMIT_AND_OFFSET);
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			final Collection<JsonElement> jsonAttributes = serializeAttributes(sortedAttributes);
			jsonClass.setAttributes(jsonAttributes);
			jsonClass.setFields(classFields);
			jsonClasses.add(jsonClass);
		}
		return JsonResponse.success(jsonClasses);
	}

	@Override
	public DataHandler serializeCsv() throws IOException {
		// FIXME
		final int separatorInt = ",".charAt(0);

		final CsvPreference exportCsvPrefs = new CsvPreference('"', separatorInt, "\n");
		final String dirName = System.getProperty("java.io.tmpdir");
		final File targetFile = new File(dirName, FILENAME);
		final Iterable<? extends CMClass> sortedClasses = sortedClasses();
		final ICsvMapWriter writer = new CsvMapWriter(new FileWriter(targetFile), exportCsvPrefs);
		writer.writeHeader(headers);
		for (final CMClass cmclass : sortedClasses) {
			final String className = cmclass.getName();
			String identifier = className;
			String owner = EMPTY;
			TranslatableElement element = TranslatableElement.CLASS;
			writeFields(writer, owner, identifier, element);

			final Iterable<? extends CMAttribute> allAttributes = dataLogic.getAttributes(className, activeOnly,
					NO_LIMIT_AND_OFFSET);
			final Iterable<? extends CMAttribute> sortedAttributes = sortAttributes(allAttributes);
			for (final CMAttribute attribute : sortedAttributes) {
				final String attributeName = attribute.getName();
				identifier = attributeName;
				owner = className;
				element = TranslatableElement.ATTRIBUTECLASS;
				writeFields(writer, owner, identifier, element);
			}
		}
		writer.close();
		final FileInputStream in = new FileInputStream(targetFile);
		final ByteArrayDataSource ds = new ByteArrayDataSource(in, "text/csv");
		ds.setName(targetFile.getName());
		return new DataHandler(ds);
	}

	void writeFields(final ICsvMapWriter writer, final String owner, final String identifier,
			final TranslatableElement element) throws IOException {
		final Iterable<String> allowedFields = element.allowedFields();
		for (final String field2 : allowedFields) {
			final Map<String, String> translations = readTranslations(owner, identifier, element, field2);
			for (final Entry<String, String> translation2 : translations.entrySet()) {
				writeRow(writer, owner, identifier, element, field2, translation2);
			}
		}
	}

	void writeRow(final ICsvMapWriter writer, final String owner, final String identifier,
			final TranslatableElement element, final String field, final Entry<String, String> tr) throws IOException {
		final Map<String, Object> newRow = Maps.newHashMap();
		newRow.put(headers[0], element.getType());
		newRow.put(headers[1], owner);
		newRow.put(headers[2], identifier);
		newRow.put(headers[3], field);
		newRow.put(headers[4], tr.getKey());
		newRow.put(headers[5], tr.getValue());
		writer.write(newRow, headers);
	}

	Map<String, String> readTranslations(final String owner, final String identifier,
			final TranslatableElement element, final String field) {
		final Converter converter = element.createConverter(field);
		final TranslationObject translationObject = converter.withIdentifier(identifier).create();
		final Map<String, String> translations = translationLogic.readAll(translationObject);
		return translations;
	}
}
