package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.ClassAttributeGroup;
import org.cmdbuild.logic.translation.ClassAttributeTranslationConverter;
import org.cmdbuild.logic.translation.TranslationObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableMap;

public class ClassAttributeGroupObjectCreationTest {

	private static final String classname = "Building";
	private static final String attributename = "Name";
	private static final String field = "Group";
	private static final String lang = "it";
	private static final String translatedGroupName = "Dati generali";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedGroupName);

	@Test
	public void forGroupFieldReturnsValidObject() {
		// given
		final ClassAttributeTranslationConverter converter = ClassAttributeTranslationConverter.of(field);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename, map);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeGroup.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedGroupName));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final ClassAttributeTranslationConverter converter = ClassAttributeTranslationConverter.of("gROup");

		// when
		final TranslationObject translationObject = converter.create(classname, attributename, map);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeGroup.class.cast(translationObject).getClassName().equals(classname));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedGroupName));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given
		final String invalidfield = "invalidfield";

		// when
		final ClassAttributeTranslationConverter converter = ClassAttributeTranslationConverter.of(invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void invalidConverterThrowsException() {
		// given
		final String invalidfield = "invalidfield";
		final ClassAttributeTranslationConverter converter = ClassAttributeTranslationConverter.of(invalidfield);
		thrown.expect(IllegalArgumentException.class);

		// when
		converter.create(classname, attributename);

		// then
	}

	@Test
	public void createConverterForReading() {
		// given
		final ClassAttributeTranslationConverter converter = ClassAttributeTranslationConverter.of(field);

		// when
		final TranslationObject translationObject = converter.create(classname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(ClassAttributeGroup.class.cast(translationObject).getClassName().equals(classname));
	}

}
