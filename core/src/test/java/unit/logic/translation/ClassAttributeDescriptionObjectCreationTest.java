package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ClassAttributeConverter;
import org.cmdbuild.logic.translation.object.ClassAttributeDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ClassAttributeDescriptionObjectCreationTest {

	private static final String classname = "Building";
	private static final String attributename = "Name";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedAttributename = "Descrizione";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedAttributename);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final ClassAttributeConverter converter = ClassAttributeConverter //
				.of(field)//
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getName().equals(attributename));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedAttributename));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final ClassAttributeConverter converter = ClassAttributeConverter//
				.of("dEscRiptION") //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getName().equals(attributename));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedAttributename));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given
		final String invalidfield = "invalidfield";

		// when
		final ClassAttributeConverter converter = ClassAttributeConverter //
				.of(invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Test
	public void invalidConverterThrowsException() {
		// given
		final String invalidfield = "invalidfield";
		final ClassAttributeConverter converter = ClassAttributeConverter //
				.of(invalidfield);
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
		final ClassAttributeConverter converter = ClassAttributeConverter //
				.of(field);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeDescription.class.cast(translationObject).getClassName().equals(classname));
	}

}
