package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.ClassDescription;
import org.cmdbuild.logic.translation.ClassTranslationConverter;
import org.cmdbuild.logic.translation.TranslationObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableMap;

public class ClassDescriptionObjectCreationTest {

	private static final String classname = "Building";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedClassname = "Edificio";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedClassname);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final ClassTranslationConverter converter = ClassTranslationConverter.of(field);

		// when
		final TranslationObject translationObject = converter.create(classname, map);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassDescription.class.cast(translationObject).getName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedClassname));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final ClassTranslationConverter converter = ClassTranslationConverter.of(field);

		// when
		final TranslationObject translationObject = converter.create(classname, map);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassDescription.class.cast(translationObject).getName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedClassname));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given
		final String invalidfield = "invalidfield";

		// when
		final ClassTranslationConverter converter = ClassTranslationConverter.of(invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void invalidConverterThrowsException() {
		// given
		final String invalidfield = "invalidfield";
		final ClassTranslationConverter converter = ClassTranslationConverter.of(invalidfield);
		thrown.expect(IllegalArgumentException.class);

		// when
		converter.create(classname);

		// then
	}

	@Test
	public void createConverterForReading() {
		// given
		final ClassTranslationConverter converter = ClassTranslationConverter.of(field);

		// when
		final TranslationObject translationObject = converter.create(classname);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassDescription.class.cast(translationObject).getName().equals(classname));
	}

}
