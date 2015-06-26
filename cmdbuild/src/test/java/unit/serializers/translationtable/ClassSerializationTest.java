package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.servlets.json.translationtable.ClassTranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.JsonElement;
import org.cmdbuild.servlets.json.translationtable.JsonField;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializerFactory;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ClassSerializationTest {

	CMClass class1 = mock(CMClass.class);
	CMClass class2 = mock(CMClass.class);
	Collection<CMClass> classes = Lists.newArrayList(class1, class2);

	@Before
	public void setup() {
		doReturn("a").when(class1).getName();
		doReturn("b").when(class2).getName();
		doReturn("B").when(class1).getDescription();
		doReturn("A").when(class2).getDescription();
	}

	@Test
	public void typeClassCreatesClassSerializer() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();

		// when
		final TranslationSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof ClassTranslationSerializer);
	}

	@Test
	public void nullSortersSetSortersToDefault() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final ClassTranslationSerializer serializer = ClassTranslationSerializer.class.cast(factory.createSerializer());

		// when
		final Object response = serializer.serialize().getResponse();

		// then
		final List<JsonElement> elements = Lists.newArrayList((Collection<JsonElement>) response);
		assertTrue(elements.size() == 2);
		assertTrue(elements.get(0).getName().equals("b"));
		assertTrue(elements.get(1).getName().equals("a"));
	}

	@Test
	public void classesSerializationHaveOnlyDescriptionField() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final ClassTranslationSerializer serializer = ClassTranslationSerializer.class.cast(factory.createSerializer());

		// when
		final Object response = serializer.serialize().getResponse();

		// then
		final List<JsonElement> elements = Lists.newArrayList((Collection<JsonElement>) response);
		final JsonElement firstClass = elements.get(0);
		final List<JsonField> fields = Lists.newArrayList(firstClass.getFields());
		assertTrue(fields.size() == 1);
		assertTrue(fields.get(0).getName().equalsIgnoreCase("description"));
	}

	@Test
	public void ifTheClassHasNoAttributesThenSerializationHasEmptyAttributesNode() throws Exception {

		// given
		final DataAccessLogic dataLogic = mock(DataAccessLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(classes).when(dataLogic).findClasses(true);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withDataAccessLogic(dataLogic) //
				.withLookupStore(null) //
				.withTranslationLogic(translationLogic).withType("class") //
				.withActiveOnly(true) //
				.withSorters(null) //
				.build();
		final ClassTranslationSerializer serializer = ClassTranslationSerializer.class.cast(factory.createSerializer());

		// when
		final Object response = serializer.serialize().getResponse();

		// then
		final List<JsonElement> elements = Lists.newArrayList((Collection<JsonElement>) response);
		final JsonElement firstClass = elements.get(0);
		assertTrue(Iterables.isEmpty(firstClass.getAttributes()));
	}
}
