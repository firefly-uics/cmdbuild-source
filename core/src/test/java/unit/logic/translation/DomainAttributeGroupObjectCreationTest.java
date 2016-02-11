package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.NullTranslationObject;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class DomainAttributeGroupObjectCreationTest {

	private static final String entryType = "domain";
	private static final String domainname = "BuildingFloor";
	private static final String attributename = "Name";
	private static final String field = "Group";
	private static final String invalidField = "invalidfield";
	private static final String lang = "it";
	private static final String translatedGroupName = "Dati generali";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedGroupName);

	@Test
	public void forGroupFieldReturnsEmptyTranslations() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, field) //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(domainname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(translationObject instanceof NullTranslationObject);
		assertTrue(translationObject.getTranslations().isEmpty());
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, "gROup") //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(domainname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(translationObject instanceof NullTranslationObject);
		assertTrue(translationObject.getTranslations().isEmpty());
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
			converter.create(domainname, attributename);
		} catch (final Exception e) {
			thrown = e;
		}

		// then
		assertNotNull(thrown);
		assertTrue(thrown instanceof UnsupportedOperationException);
	}

}
