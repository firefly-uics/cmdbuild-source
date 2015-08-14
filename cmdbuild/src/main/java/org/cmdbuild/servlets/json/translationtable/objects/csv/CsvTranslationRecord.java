package org.cmdbuild.servlets.json.translationtable.objects.csv;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cmdbuild.servlets.json.translationtable.objects.TranslationSerialization;

public class CsvTranslationRecord implements TranslationSerialization {

	private final Map<String, Object> record;

	public CsvTranslationRecord(final Map<String, Object> record) {
		this.record = record;
	}

	public Map<String, Object> getValues() {
		return record;
	}
	
	public String get(String key) {
		return String.class.cast(record.get(key));
	}
	
	public Set<Entry<String, Object>> getEntrySet(){
		return record.entrySet();
	}
	
	public Set<String> getKeySet(){
		return record.keySet();
	}

}
