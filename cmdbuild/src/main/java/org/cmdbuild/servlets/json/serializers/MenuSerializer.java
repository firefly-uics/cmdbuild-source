package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.Constants.ID_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.data.converter.ViewConverter.VIEW_CLASS_NAME;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.model.Report.REPORT_CLASS_NAME;
import static org.cmdbuild.services.store.menu.MenuItemType.isClassOrProcess;
import static org.cmdbuild.services.store.menu.MenuItemType.isDashboard;
import static org.cmdbuild.services.store.menu.MenuItemType.isReport;
import static org.cmdbuild.services.store.menu.MenuItemType.isView;
import static org.cmdbuild.servlets.json.CommunicationConstants.DEFAULT_DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.translation.ClassTranslation;
import org.cmdbuild.logic.translation.MenuItemTranslation;
import org.cmdbuild.logic.translation.ReportTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.ViewTranslation;
import org.cmdbuild.services.store.menu.MenuItem;
import org.cmdbuild.services.store.menu.MenuItemDTO;
import org.cmdbuild.services.store.menu.MenuItemType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.base.Optional;

public class MenuSerializer {

	public static final String MENU = "menu", //
			CHILDREN = "children", //
			CLASS_NAME = "referencedClassName", //
			ELEMENT_ID = "referencedElementId", //
			INDEX = "index", //
			SPECIFIC_TYPE_VALUES = "specificTypeValues", //
			TYPE = "type", //
			UUID = "uuid";

	private final MenuItem rootItem;
	private final TranslationFacade translationFacade;
	private final CMDataView dataView;

	private MenuSerializer(final Builder builder) {
		this.rootItem = builder.rootItem;
		this.translationFacade = builder.translationFacade;
		this.dataView = builder.dataView;
	}

	public static Builder newInstance() {
		return new Builder();
	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<MenuSerializer> {

		MenuItem rootItem;
		TranslationFacade translationFacade;
		CMDataView dataView;

		@Override
		public MenuSerializer build() {
			return new MenuSerializer(this);
		}

		public Builder withRootItem(final MenuItem rootItem) {
			this.rootItem = rootItem;
			return this;
		}

		public Builder withTranslationFacade(final TranslationFacade translationFacade) {
			this.translationFacade = translationFacade;
			return this;
		}

		public Builder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}
	}

	/**
	 * Serialize the menu as tree sorting the items by index
	 * 
	 * @param menu
	 * @param withWrapper
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toClient( //
			final boolean withWrapper //
	) throws JSONException {
		final boolean sortByDescription = false;
		return toClient(rootItem, withWrapper, sortByDescription);
	}

	/**
	 * Serialize the menu as tree. If sortByDescription is false sort the items
	 * by index
	 * 
	 * @param menu
	 * @param withWrapper
	 * @param sortByDescription
	 * @return
	 * @throws JSONException
	 */
	public JSONObject toClient( //
			final MenuItem menu, //
			final boolean withWrapper, //
			final boolean sortByDescription //
	) throws JSONException {

		final JSONObject out = singleToClient(menu);
		if (menu.getChildren().size() > 0) {
			final JSONArray children = new JSONArray();

			if (sortByDescription) {
				menu.sortChildByDescription();
			} else {
				menu.sortChildByIndex();
			}

			for (final MenuItem child : menu.getChildren()) {
				children.put(toClient(child, false, sortByDescription));
			}
			out.put(CHILDREN, children);
		}

		if (withWrapper) {
			return new JSONObject() {
				{
					put(MENU, out);
				}
			};
		} else {
			return out;
		}
	}

	public JSONObject singleToClient(final MenuItem menuItem) throws JSONException {
		final JSONObject out = new JSONObject();
		final String elementType = menuItem.getType().getValue();
		final String translatedDescription = readTranslation(menuItem);
		out.put(TYPE, elementType);
		out.put(INDEX, menuItem.getIndex());
		out.put(ELEMENT_ID, menuItem.getReferencedElementId());
		out.put(UUID, menuItem.getUniqueIdentifier());
		out.put(DESCRIPTION, defaultIfNull(translatedDescription, menuItem.getDescription()));
		out.put(DEFAULT_DESCRIPTION, menuItem.getDescription());
		out.put(CLASS_NAME, menuItem.getReferedClassName());
		out.put(SPECIFIC_TYPE_VALUES, menuItem.getSpecificTypeValues());

		return out;
	}

	private String readTranslation(final MenuItem menuItem) {
		final MenuItemTranslation itemTranslation = MenuItemTranslation.newInstance() //
				.withName(menuItem.getUniqueIdentifier()) //
				.withField(DESCRIPTION_FOR_CLIENT) //
				.build();

		String translatedDescription = translationFacade.read(itemTranslation);

		if (isBlank(translatedDescription)) {
			final MenuItemType type = menuItem.getType();
			if (isClassOrProcess(type)) {
				final String className = menuItem.getReferedClassName();
				final ClassTranslation classTranslation = ClassTranslation.newInstance() //
						.withName(className) //
						.withField(DESCRIPTION_FOR_CLIENT) //
						.build();
				translatedDescription = translationFacade.read(classTranslation);

			} else if (isReport(type)) {
				final Optional<String> _reportName = fetchReportName(menuItem);
				if (_reportName.isPresent()) {
					final ReportTranslation reportTranslation = ReportTranslation.newInstance() //
							.withName(_reportName.get()) //
							.withField(DESCRIPTION_FOR_CLIENT) //
							.build();
					translatedDescription = translationFacade.read(reportTranslation);
				}
			} else if (isView(type)) {
				final Optional<String> _viewName = fetchViewName(menuItem);
				if (_viewName.isPresent()) {
					final ViewTranslation viewTranslation = ViewTranslation.newInstance() //
							.withName(_viewName.get()) //
							.withField(DESCRIPTION_FOR_CLIENT) //
							.build();
					translatedDescription = translationFacade.read(viewTranslation);
				}
			} else if (isDashboard(type)) {
				// TODO
			}
		}
		return translatedDescription;
	}

	private Optional<String> fetchReportName(final MenuItem menuItem) {
		final Number reportId = menuItem.getReferencedElementId();
		final CMClass reportClass = dataView.findClass(REPORT_CLASS_NAME);
		return selectCodeFromIdAndClass(reportId, reportClass);
	}

	private Optional<String> fetchViewName(final MenuItem menuItem) {
		final Number viewId = menuItem.getReferencedElementId();
		final CMClass viewClass = dataView.findClass(VIEW_CLASS_NAME);
		return selectNameFromIdAndClass(viewId, viewClass);
	}

	private Optional<String> selectCodeFromIdAndClass(final Number id, final CMClass cmClass) {
		final Optional<CMCard> _reportCard = fetchCardFromIdAndClass(id, cmClass);
		Optional<String> _code;
		if (_reportCard.isPresent()) {
			final CMCard reportCard = _reportCard.get();
			final String code = String.class.cast(reportCard.getCode());
			_code = Optional.of(code);
		} else {
			_code = Optional.absent();
		}
		return _code;
	}

	private Optional<String> selectNameFromIdAndClass(final Number id, final CMClass cmClass) {
		final Optional<CMCard> _reportCard = fetchCardFromIdAndClass(id, cmClass);
		Optional<String> _name;
		if (_reportCard.isPresent()) {
			final CMCard reportCard = _reportCard.get();
			final String name = String.class.cast(reportCard.get("Name"));
			_name = Optional.of(name);
		} else {
			_name = Optional.absent();
		}
		return _name;
	}

	private Optional<CMCard> fetchCardFromIdAndClass(final Number id, final CMClass cmClass) {
		final CMQueryResult queryResult = dataView.select(anyAttribute(cmClass)) //
				.from(cmClass) //
				.where(condition(attribute(cmClass, ID_ATTRIBUTE), eq(id))) //
				.run();
		Optional<CMCard> _card;
		if (!queryResult.isEmpty()) {
			final CMCard card = queryResult.getOnlyRow().getCard(cmClass);
			_card = Optional.of(card);
		} else {
			_card = Optional.absent();
		}
		return _card;
	}

	public static MenuItem toServer(final JSONObject jsonMenu) throws JSONException {
		final MenuItem item = singleToServer(jsonMenu);
		if (jsonMenu.has(CHILDREN)) {
			final JSONArray children = jsonMenu.getJSONArray(CHILDREN);
			for (int i = 0; i < children.length(); ++i) {
				final JSONObject child = (JSONObject) children.get(i);
				item.addChild(toServer(child));
			}
		}

		return item;
	}

	private static MenuItem singleToServer(final JSONObject jsonMenu) throws JSONException {
		final MenuItem item = new MenuItemDTO();
		final MenuItemType type = MenuItemType.getType(jsonMenu.getString(TYPE));
		item.setType(type);

		if (!MenuItemType.ROOT.equals(type)) {
			item.setDescription(jsonMenu.getString(DESCRIPTION));
			item.setIndex(jsonMenu.getInt(INDEX));
			item.setReferedClassName(jsonMenu.getString(CLASS_NAME));
			item.setReferencedElementId(getElementId(jsonMenu));
			item.setUniqueIdentifier(defaultIfBlank(jsonMenu.getString(UUID), java.util.UUID.randomUUID().toString()));
		}

		return item;
	}

	private static Integer getElementId(final JSONObject jsonMenu) throws JSONException {
		Integer elementId = null;
		if (jsonMenu.has(ELEMENT_ID)) {
			final String stringElementId = (String) jsonMenu.get(ELEMENT_ID);
			if (notEmpty(stringElementId)) {
				elementId = Integer.valueOf(stringElementId);
			}
		}

		return elementId;
	}

	private static boolean notEmpty(final String s) {
		return !"".equals(s);
	}
}
