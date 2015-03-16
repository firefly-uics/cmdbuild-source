package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.AttributeConverter;
import org.cmdbuild.logic.translation.object.DomainAttributeDescription;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class DomainAttributeDescriptionObjectCreationTest {

	private static final String entryType = "domain";
	private static final String floorname = "BuildingFloor";
	private static final String attributename = "Name";
	private static final String field = "Description";
	private static final String lang = "it";
	private static final String translatedAttributename = "Descrizione";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedAttributename);

	@Test
	public void forDescriptionFieldReturnsValidObject() {
		// given
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, field)//
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(floorname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(DomainAttributeDescription.class.cast(translationObject).getDomainName().equals(floorname));
		assertTrue(DomainAttributeDescription.class.cast(translationObject).getName().equals(attributename));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedAttributename));
	}

	@Test
	public void converterIsCaseInsensitiveForTheField() {
		// given
		final AttributeConverter converter = AttributeConverter//
				.of(entryType, "dEscRiptION") //
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(floorname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(DomainAttributeDescription.class.cast(translationObject).getDomainName().equals(floorname));
		assertTrue(DomainAttributeDescription.class.cast(translationObject).getName().equals(attributename));
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedAttributename));
	}

	@Test
	public void unsupportedFieldGeneratesNotValidConverter() {
		// given
		final String invalidfield = "invalidfield";

		// when
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, invalidfield);

		// then
		assertTrue(!converter.isValid());
	}

	@Test
	public void invalidConverterThrowsException() {
		// given
		final String invalidfield = "invalidfield";
		final AttributeConverter converter = AttributeConverter //
				.of(entryType, invalidfield);
		Exception thrown = null;

		// when
		try {
			converter.create(floorname, attributename);
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
		final TranslationObject translationObject = converter.create(floorname, attributename);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(DomainAttributeDescription.class.cast(translationObject).getDomainName().equals(floorname));
	}

}
