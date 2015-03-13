package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.object.ClassAttributeGroup;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ClassAttributeGroupObjectCreationTest {
	
	private static final String entryType = "class";
	private static final String classname = "Building";
	private static final String attributename = "Name";
	private static final String field = "Group";
	private static final String invalidField = "invalidfield";
	private static final String lang = "it";
	private static final String translatedGroupName = "Dati generali";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedGroupName);

	@Test
	public void forGroupFieldReturnsValidObject() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType,field) //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeGroup.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedGroupName));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, "gROup") //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeGroup.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedGroupName));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given

		// when
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, invalidField);

		// then
		assertTrue(!converter.isValid());
	}

	@Test
	public void invalidConverterThrowsException() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, invalidField);
		Exception thrown = null;

		// when
		try {
			converter.create(classname, attributename);
		} catch (final Exception e) {
			thrown = e;
		}

		// then
		assertNotNull(thrown);
		assertTrue(thrown instanceof UnsupportedOperationException);
	}

	@Test
	public void createConverterForReading() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, field);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeGroup.class.cast(translationObject).getClassName().equals(classname));
	}

}
