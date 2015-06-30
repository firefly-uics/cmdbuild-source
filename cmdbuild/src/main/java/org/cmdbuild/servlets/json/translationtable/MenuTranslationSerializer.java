package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegePair;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.MenuItemConverter;
import org.cmdbuild.services.store.menu.MenuConstants;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.servlets.json.management.JsonResponse;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElement;
import org.cmdbuild.servlets.json.translationtable.objects.JsonElementWithChildren;
import org.cmdbuild.servlets.json.translationtable.objects.JsonField;
import org.json.JSONArray;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class MenuTranslationSerializer implements TranslationSerializer {

	private static final String DEFAULT_MENU_GROUP_DESCRIPTION = "*Default*";
	private final AuthenticationLogic authLogic;
	private final MenuLogic menuLogic;
	private final TranslationLogic translationLogic;
	private final Ordering<MenuItem> menuNodesOrdering = MenuItemSorter.DEFAULT.getOrientedOrdering();
	private final Ordering<CMGroup> menusOrdering = MenuSorter.DEFAULT.getOrientedOrdering();

	private final Predicate<CMGroup> HAS_MENU = new Predicate<CMGroup>() {

		@Override
		public boolean apply(final CMGroup input) {
			final String groupName = input.getName();
			final MenuItem menuForGroup = menuLogic.read(groupName);
			return menuForGroup != null && menuForGroup.getChildren().size() > 0;
		}
	};

	public MenuTranslationSerializer(final AuthenticationLogic authLogic, final MenuLogic menuLogic,
			final TranslationLogic translationLogic, final JSONArray sorters) {
		this.authLogic = authLogic;
		this.menuLogic = menuLogic;
		this.translationLogic = translationLogic;
	}

	@Override
	public JsonResponse serialize() {
		final Iterable<CMGroup> groups = authLogic.getAllGroups();
		final Iterable<CMGroup> groupsWithMenu = Iterables.filter(groups, HAS_MENU);
		final Collection<CMGroup> allGroupsPlusDefault = Lists.newArrayList(groupsWithMenu);
		allGroupsPlusDefault.add(fakeGroupForDefaultMenu);
		final Collection<CMGroup> sortedGroups = menusOrdering.sortedCopy(allGroupsPlusDefault);
		final Collection<JsonElement> jsonMenus = Lists.newArrayList();
		for (final CMGroup group : sortedGroups) {
			final MenuItem rootNode = menuLogic.read(group.getName());
			final JsonElementWithChildren rootElement = new JsonElementWithChildren();
			rootElement.setName(group.getDescription());
			final Collection<JsonElement> childrenElement = Lists.newArrayList();
			final Iterable<MenuItem> _children = rootNode.getChildren();
			final Iterable<MenuItem> sortedChildren = menuNodesOrdering.sortedCopy(_children);
			for (final MenuItem child : sortedChildren) {
				final JsonElementWithChildren childElement = new JsonElementWithChildren();
				childElement.setName(child.getUniqueIdentifier());
				final Collection<JsonField> childFields = readFields(child);
				childElement.setFields(childFields);
				final List<MenuItem> nephews = child.getChildren();
				serialize(childElement, nephews);
				childrenElement.add(childElement);
			}
			rootElement.setChildren(childrenElement);
			jsonMenus.add(rootElement);
		}
		return JsonResponse.success(jsonMenus);
	}

	private void serialize(final JsonElementWithChildren rootElement, final List<MenuItem> children) {
		final Collection<JsonElement> jsonChildren = Lists.newArrayList();
		for (final MenuItem child : children) {
			final JsonElementWithChildren childElement = new JsonElementWithChildren();
			childElement.setName(child.getUniqueIdentifier());
			final Collection<JsonField> fields = readFields(child);
			childElement.setFields(fields);
			jsonChildren.add(childElement);
			serialize(childElement, child.getChildren());
		}
		rootElement.setChildren(jsonChildren);

	}

	private Collection<JsonField> readFields(final MenuItem child) {
		final Collection<JsonField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = MenuItemConverter.DESCRIPTION //
				.withIdentifier(child.getUniqueIdentifier()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final JsonField field = new JsonField();
		field.setName(MenuItemConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(child.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	private static final CMGroup fakeGroupForDefaultMenu = new CMGroup() {

		@Override
		public boolean isRestrictedAdmin() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isAdmin() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isActive() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long getStartingClassId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			return MenuConstants.DEFAULT_MENU_GROUP_NAME;
		}

		@Override
		public Long getId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getEmail() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<String> getDisabledModules() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getDescription() {
			return DEFAULT_MENU_GROUP_DESCRIPTION;
		}

		@Override
		public List<PrivilegePair> getAllPrivileges() {
			throw new UnsupportedOperationException();
		}
	};

}
