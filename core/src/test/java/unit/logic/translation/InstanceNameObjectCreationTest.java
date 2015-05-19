package unit.logic.translation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.InstanceConverter;
import org.cmdbuild.logic.translation.object.InstanceName;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class InstanceNameObjectCreationTest {

	private static final String lang = "it";
	private static final String translatedName = "demo-it";
	private static final Map<String, String> map = ImmutableMap.of(lang, translatedName);

	@Test
	public void forInstanceNameReturnsValidObject() {
		// given
		final InstanceConverter converter = InstanceConverter //
				.of(InstanceConverter.nameField())//
				.withTranslations(map);

		// when
		final TranslationObject translationObject = converter.create(StringUtils.EMPTY);

		// then
		assertTrue(converter.isValid());
		assertNotNull(translationObject);
		assertTrue(translationObject instanceof InstanceName);
		assertTrue(translationObject.getTranslations().get(lang).equals(translatedName));
	}

}
