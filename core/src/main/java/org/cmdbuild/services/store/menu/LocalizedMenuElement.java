package org.cmdbuild.services.store.menu;

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

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.translation.ReportTranslation;
import org.cmdbuild.logic.translation.TranslationFacade;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.ViewTranslation;
import org.cmdbuild.logic.translation.converter.ClassConverter;
import org.cmdbuild.services.localization.LocalizableStorableVisitor;

import com.google.common.base.Optional;

public class LocalizedMenuElement extends ForwardingMenuElement {

	private final MenuElement delegate;
	private final TranslationFacade facade;
	private final CMDataView dataView;

	public LocalizedMenuElement(final MenuElement delegate, final TranslationFacade facade, final CMDataView dataView) {
		this.delegate = delegate;
		this.facade = facade;
		this.dataView = dataView;
	}

	@Override
	public void accept(final LocalizableStorableVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected MenuElement delegate() {
		return delegate;
	}

	@Override
	public String getDescription() {
		return defaultIfBlank(searchTranslation(), super.getDescription());
	}

	private String searchTranslation() {
		final TranslationObject translationObject = org.cmdbuild.logic.translation.converter.MenuItemConverter //
				.of(org.cmdbuild.logic.translation.converter.MenuItemConverter.description()) //
				.create(getUuid());
		String translatedDescription = facade.read(translationObject);

		if (isBlank(translatedDescription)) {
			final MenuItemType type = getType();
			if (isClassOrProcess(type)) {
				final String className = getElementClassName();
				final ClassConverter converter = ClassConverter.of(ClassConverter.description());
				final TranslationObject classTranslation = converter.create(className);
				translatedDescription = facade.read(classTranslation);

			} else if (isReport(type)) {
				final Optional<String> _reportName = fetchReportName();
				if (_reportName.isPresent()) {
					final ReportTranslation reportTranslation = ReportTranslation.newInstance() //
							.withName(_reportName.get()) //
							.withField(DESCRIPTION_FOR_CLIENT) //
							.build();
					translatedDescription = facade.read(reportTranslation);
				}
			} else if (isView(type)) {
				final Optional<String> _viewName = fetchViewName();
				if (_viewName.isPresent()) {
					final ViewTranslation viewTranslation = ViewTranslation.newInstance() //
							.withName(_viewName.get()) //
							.withField(DESCRIPTION_FOR_CLIENT) //
							.build();
					translatedDescription = facade.read(viewTranslation);
				}
			} else if (isDashboard(type)) {
				// nothing to do
			}
		}
		return translatedDescription;
	}

	private Optional<String> fetchReportName() {
		final Number reportId = getElementId();
		final CMClass reportClass = dataView.findClass(REPORT_CLASS_NAME);
		return selectCodeFromIdAndClass(reportId, reportClass);
	}

	private Optional<String> fetchViewName() {
		final Number viewId = getElementId();
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

}
