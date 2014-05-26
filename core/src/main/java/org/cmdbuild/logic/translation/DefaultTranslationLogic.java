package org.cmdbuild.logic.translation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.lang.String.format;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreFactory;
import org.cmdbuild.data.store.translation.Element;
import org.cmdbuild.data.store.translation.Translation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class DefaultTranslationLogic implements TranslationLogic {

	public static final String DESCRIPTION_FOR_CLIENT = "Description";
	public static final String DESCRIPTION_FOR_PERSISTENCE = "description";
	public static final String DIRECT_DESCRIPTION_FOR_CLIENT = "directDescription";
	private static final String DIRECT_DESCRIPTION_FOR_PERSISTENCE = "directdescription";
	public static final String INVERSE_DESCRIPTION_FOR_CLIENT = "inverseDescription";
	private static final String INVERSE_DESCRIPTION_FOR_PERSISTENCE = "inversedescription";
	public static final String MASTER_DETAIL_LABEL_FOR_CLIENT = "masterDetailLabel";
	private static final String MASTER_DETAIL_LABEL_FOR_PERSISTENCE = "masterdetaillabel	";
	public static final String BUTTON_LABEL_FOR_CLIENT = "ButtonLabel";
	private static final String BUTTON_LABEL_FOR_PERSISTENCE = "buttonlabel	";
	public static final String INSTANCENAME_FOR_SERVER = "instancename";

	private static enum FieldMapper {

		DESCRIPTION(DESCRIPTION_FOR_CLIENT, DESCRIPTION_FOR_PERSISTENCE), //
		DIRECT_DESCRIPTION(DIRECT_DESCRIPTION_FOR_CLIENT, DIRECT_DESCRIPTION_FOR_PERSISTENCE), //
		INVERSE_DESCRIPTION(INVERSE_DESCRIPTION_FOR_CLIENT, INVERSE_DESCRIPTION_FOR_PERSISTENCE), //
		MASTER_DETAIL_LABEL(MASTER_DETAIL_LABEL_FOR_CLIENT, MASTER_DETAIL_LABEL_FOR_PERSISTENCE), //
		BUTTON_LABEL(BUTTON_LABEL_FOR_CLIENT, BUTTON_LABEL_FOR_PERSISTENCE), //
		;

		public static FieldMapper of(final String value) {
			for (final FieldMapper element : values()) {
				if (element.expected.equals(value)) {
					return element;
				}
			}
			throw new IllegalArgumentException("value not found");
		}

		private final String expected;
		private final String result;

		private FieldMapper(final String expected, final String result) {
			this.expected = expected;
			this.result = result;
		}

		public String getResult() {
			return result;
		}

	}

	private static class ElementCreator implements TranslationObjectVisitor {

		private static ElementCreator of(final TranslationObject translationObject) {
			return new ElementCreator(translationObject);
		}

		private final TranslationObject translationObject;
		private String value;

		private ElementCreator(final TranslationObject translationObject) {
			this.translationObject = translationObject;
		}

		public Element create() {
			translationObject.accept(this);
			Validate.notNull(value, "conversion error");
			return Element.of(value);
		}

		@Override
		public void visit(final ClassTranslation translationObject) {
			value = format("class.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final DomainTranslation translationObject) {
			value = format("domain.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final AttributeClassTranslation translationObject) {
			value = format("attributeclass.%s.%s.%s", //
					translationObject.getName(), //
					translationObject.getAttributeName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final AttributeDomainTranslation translationObject) {
			value = format("attributedomain.%s.%s.%s", //
					translationObject.getName(), //
					translationObject.getAttributeName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final ViewTranslation translationObject) {
			value = format("view.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final FilterTranslation translationObject) {
			value = format("filter.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final InstanceNameTranslation translationObject) {
			value = format(INSTANCENAME_FOR_SERVER);
		}

		@Override
		public void visit(final WidgetTranslation translationObject) {
			value = format("widget.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final DashboardTranslation translationObject) {
			value = format("dashboard.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final ChartTranslation translationObject) {
			value = format("chart.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final ReportTranslation translationObject) {
			value = format("report.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final LookupTranslation translationObject) {
			value = format("lookup.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

		@Override
		public void visit(final GisIconTranslation translationObject) {
			value = format("gisicon.%s.%s", //
					translationObject.getName(), //
					FieldMapper.of(translationObject.getField()).getResult());
		}

	}

	private static final class ContainedInTraslations implements Predicate<Translation> {

		public static final ContainedInTraslations containedIn(final Iterable<? extends Translation> translations) {
			return new ContainedInTraslations(translations);
		}

		private final Iterable<? extends Translation> translations;

		private ContainedInTraslations(final Iterable<? extends Translation> translations) {
			this.translations = translations;
		}

		@Override
		public boolean apply(final Translation input) {
			for (final Translation translation : translations) {
				if (translation.getLang().equals(input.getLang())) {
					return true;
				}
			}
			return false;
		}

	}

	private static Function<Translation, String> TRANSLATION_TO_LANG = new Function<Translation, String>() {

		@Override
		public String apply(final Translation input) {
			return input.getLang();
		}

	};

	private final StoreFactory<Translation> storeFactory;

	public DefaultTranslationLogic(final StoreFactory<Translation> storeFactory) {
		this.storeFactory = storeFactory;
	}

	@Override
	public void create(final TranslationObject translationObject) {
		// TODO check that element, lang and value must not be null
		final Element element = ElementCreator.of(translationObject).create();
		final Collection<Translation> translations = extractTranslations(translationObject, element);
		final Store<Translation> store = storeFactory.create(element);
		for (final Translation translation : translations) {
			store.create(translation);
		}
	}

	@Override
	public Map<String, String> read(final TranslationObject translationObject) {
		// TODO check that element, lang and value must not be null
		final Element element = ElementCreator.of(translationObject).create();
		final Store<Translation> store = storeFactory.create(element);
		final Map<String, String> map = newLinkedHashMap();
		for (final Translation translation : store.readAll()) {
			map.put(translation.getLang(), translation.getValue());
		}
		return map;
	}

	@Override
	public void update(final TranslationObject translationObject) {
		// TODO check that element, lang and value must not be null
		final Element element = ElementCreator.of(translationObject).create();
		final Collection<Translation> translations = extractTranslations(translationObject, element);
		final Map<String, Translation> translationsByLang = uniqueIndex(translations, TRANSLATION_TO_LANG);
		final Store<Translation> store = storeFactory.create(element);
		final Iterable<Translation> updateable = from(store.readAll()) //
				.filter(ContainedInTraslations.containedIn(translations));
		for (final Translation translationOnStore : updateable) {
			final String lang = translationOnStore.getLang();
			final Translation translationFromClient = translationsByLang.get(lang);
			translationOnStore.setValue(translationFromClient.getValue());
			store.update(translationOnStore);
		}
	}

	@Override
	public void delete(final TranslationObject translationObject) {
		final Element element = ElementCreator.of(translationObject).create();
		final Collection<Translation> translations = extractTranslations(translationObject, element);
		final Store<Translation> store = storeFactory.create(element);
		final Iterable<Translation> deleteable = from(store.readAll()) //
				.filter(ContainedInTraslations.containedIn(translations));
		for (final Translation translation : deleteable) {
			store.delete(translation);
		}
	}

	private Collection<Translation> extractTranslations(final TranslationObject translationObject, final Element element) {
		final Collection<Translation> translations = Lists.newArrayList();
		for (final Entry<String, String> entry : translationObject.getTranslations().entrySet()) {
			final Translation translation = new Translation();
			translation.setElement(String.class.cast(element.getGroupAttributeValue()));
			translation.setLang(entry.getKey());
			translation.setValue(entry.getValue());
			translations.add(translation);
		}
		return translations;
	}

}
