package unit.serializers.translationtable.importcsv;

import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.object.ClassDescription;
import org.cmdbuild.servlets.json.serializers.translations.csv.read.DefaultRecordDeserializer;
import org.cmdbuild.servlets.json.translationtable.objects.csv.CsvTranslationRecord;
import org.junit.Test;

import com.google.common.collect.Maps;

public class RecordDeserializerTest {

	@Test
	public void validRecordForClassReturnsTranslationObject() throws Exception {
		// given
		Map<String, Object> map = Maps.newHashMap();
		map.put("identifier", "class.Application.description");
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		CsvTranslationRecord record = new CsvTranslationRecord(map);

		// when
		TranslationObject object = a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record)) //
				.deserialize();

		// then
		assertTrue(object instanceof ClassDescription);
		assertTrue(ClassDescription.class.cast(object).getName().equals("Application"));
		assertTrue(ClassDescription.class.cast(object).getTranslations().size() == 2);
	}

	@Test
	public void extraColumnsAreInterpretedAsLanguages() throws Exception {
		// given
		Map<String, Object> map = Maps.newHashMap();
		map.put("identifier", "class.Application.description");
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("foo", "bar");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		CsvTranslationRecord record = new CsvTranslationRecord(map);

		// when
		TranslationObject object = a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record)) //
				.deserialize();

		// then
		assertTrue(object instanceof ClassDescription);
		assertTrue(ClassDescription.class.cast(object).getName().equals("Application"));
		assertTrue(ClassDescription.class.cast(object).getTranslations().size() == 3);
		assertTrue(ClassDescription.class.cast(object).getTranslations().get("foo").equals("bar"));
	}

	@Test(expected = NullPointerException.class)
	public void missingColumns() throws Exception {
		// given
		Map<String, Object> map = Maps.newHashMap();
		map.put("description", "description of the class 'Application'");
		map.put("default", "Application");
		map.put("it", "Applicazione");
		map.put("ja", "アプリケーション");
		CsvTranslationRecord record = new CsvTranslationRecord(map);

		// when
		a(DefaultRecordDeserializer.newInstance() //
				.withRecord(record)) //
				.deserialize();
		// then

	}

}
