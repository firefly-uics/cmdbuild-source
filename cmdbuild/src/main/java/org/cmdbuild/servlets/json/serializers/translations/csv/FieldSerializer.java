package org.cmdbuild.servlets.json.serializers.translations.csv;

import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;

public interface FieldSerializer {

	CsvTranslationRecord serialize();

}
