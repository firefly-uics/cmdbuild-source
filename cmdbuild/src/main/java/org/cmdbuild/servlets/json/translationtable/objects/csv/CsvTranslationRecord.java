package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.util.Map;

import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

public class CsvTranslationRecord implements TranslationSerialization {

	private final Map<String, Object> record;

	public CsvTranslationRecord(final Map<String, Object> record) {
		this.record = record;
	}

	public Map<String, Object> getRecord() {
		return record;
	}

}
