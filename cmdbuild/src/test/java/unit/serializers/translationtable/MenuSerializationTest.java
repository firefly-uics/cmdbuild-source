package unit.serializers.translationtable;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.List;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.services.store.menu.MenuConstants;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.services.store.menu.MenuItemType;
import org.cmdbuild.servlets.json.translationtable.MenuTranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializer;
import org.cmdbuild.servlets.json.translationtable.TranslationSerializerFactory;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElement;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

public class MenuSerializationTest {

	CMGroup group1 = mock(CMGroup.class);
	CMGroup group2 = mock(CMGroup.class);
	Collection<CMGroup> groups = Lists.newArrayList(group1, group2);

	MenuItem item1 = mock(MenuItem.class);
	MenuItem item2 = mock(MenuItem.class);
	MenuItem defaultItem = mock(MenuItem.class);

	MenuItem child1 = mock(MenuItem.class);
	MenuItem child2 = mock(MenuItem.class);

	@Before
	public void setup() {
		doReturn("a").when(group1).getName();
		doReturn("b").when(group2).getName();
		doReturn("B").when(group1).getDescription();
		doReturn("A").when(group2).getDescription();
		doReturn("uuid1").when(child1).getUniqueIdentifier();
		doReturn("uuid2").when(child2).getUniqueIdentifier();
	}

	@Test
	public void typeMenuCreatesMenuSerializer() throws Exception {

		// given
		final AuthenticationLogic authLogic = mock(AuthenticationLogic.class);
		final MenuLogic menuLogic = mock(MenuLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withAuthLogic(authLogic) //
				.withMenuLogic(menuLogic) //
				.withTranslationLogic(translationLogic) //
				.withType("menu") //
				.build();

		// when
		final TranslationSerializer serializer = factory.createSerializer();

		// then
		assertTrue(serializer instanceof MenuTranslationSerializer);
	}

	@Test
	public void considerOnlyGroupsWithMenu() throws Exception {

		// given
		final AuthenticationLogic authLogic = mock(AuthenticationLogic.class);
		final MenuLogic menuLogic = mock(MenuLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(groups).when(authLogic).getAllGroups();
		doReturn(item1).when(menuLogic).read("a");
		doReturn(Lists.newArrayList()).when(item1).getChildren();
		doReturn(defaultItem).when(menuLogic).read("*");
		doReturn(MenuItemType.CLASS).when(item1).getType();
		doReturn(MenuItemType.ROOT).when(defaultItem).getType();

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withAuthLogic(authLogic) //
				.withMenuLogic(menuLogic) //
				.withTranslationLogic(translationLogic) //
				.withType("menu") //
				.build();

		final TranslationSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize().getResponse();

		// then
		final List<JsonElement> elements = Lists.newArrayList((Collection<JsonElement>) response);
		assertTrue(elements.size() == 1);
		assertTrue(elements.get(0).getName().equals("*Default*"));
	}

	@Test
	public void rootElementsHaveOnlyTheDescriptionOfTheGroup() throws Exception {

		// given
		final AuthenticationLogic authLogic = mock(AuthenticationLogic.class);
		final MenuLogic menuLogic = mock(MenuLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(groups).when(authLogic).getAllGroups();
		doReturn(item1).when(menuLogic).read("a");
		doReturn(Lists.newArrayList()).when(item1).getChildren();
		doReturn(defaultItem).when(menuLogic).read("*");
		doReturn(MenuItemType.CLASS).when(item1).getType();
		doReturn(MenuItemType.ROOT).when(defaultItem).getType();

		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withAuthLogic(authLogic) //
				.withMenuLogic(menuLogic) //
				.withTranslationLogic(translationLogic) //
				.withType("menu") //
				.build();

		final TranslationSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize().getResponse();

		// then
		// then
		final List<JsonElement> elements = Lists.newArrayList((Collection<JsonElement>) response);
		assertTrue(elements.size() == 1);
		assertTrue(elements.get(0).getName().equals("*Default*"));
		assertTrue(elements.get(0).getFields() == null);
	}

	@Test
	public void menusAreOrderedAccordingToGroupDescriptionsButTheDefaultOneIsAlwaysTheFirst() throws Exception {

		// given
		final AuthenticationLogic authLogic = mock(AuthenticationLogic.class);
		final MenuLogic menuLogic = mock(MenuLogic.class);
		final TranslationLogic translationLogic = mock(TranslationLogic.class);
		doReturn(groups).when(authLogic).getAllGroups();
		doReturn(item1).when(menuLogic).read("a");
		doReturn(item2).when(menuLogic).read("b");
		doReturn(Lists.newArrayList(child1)).when(item1).getChildren();
		doReturn(Lists.newArrayList(child2)).when(item2).getChildren();
		doReturn(defaultItem).when(menuLogic).read(MenuConstants.DEFAULT_MENU_GROUP_NAME);
		doReturn(MenuItemType.FOLDER).when(item1).getType();
		doReturn(MenuItemType.FOLDER).when(item2).getType();
		doReturn(MenuItemType.CLASS).when(child1).getType();
		doReturn(MenuItemType.FOLDER).when(child2).getType();
		doReturn(MenuItemType.ROOT).when(defaultItem).getType();
		final TranslationSerializerFactory factory = TranslationSerializerFactory //
				.newInstance() //
				.withAuthLogic(authLogic) //
				.withMenuLogic(menuLogic) //
				.withTranslationLogic(translationLogic) //
				.withType("menu") //
				.build();

		final TranslationSerializer serializer = factory.createSerializer();

		// when
		final Object response = serializer.serialize().getResponse();

		// then
		final List<JsonElement> elements = Lists.newArrayList((Collection<JsonElement>) response);
		assertTrue(elements.get(0).getName().equals("*Default*"));
		assertTrue(elements.get(1).getName().equals("A"));
		assertTrue(elements.get(2).getName().equals("B"));

	}

}
