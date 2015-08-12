package org.cmdbuild.servlets.json.translationtable;

import java.util.Collection;
import java.util.Map;

import javax.activation.DataHandler;

import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.logic.translation.TranslationLogic;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.logic.translation.converter.ViewConverter;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.model.view.View;
import org.cmdbuild.servlets.json.translationtable.objects.EntryField;
import org.cmdbuild.servlets.json.translationtable.objects.GenericTableEntry;
import org.cmdbuild.servlets.json.translationtable.objects.TableEntry;
import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

public class ViewTranslationSerializer implements TranslationSerializer {

	private final ViewLogic viewLogic;
	final TranslationLogic translationLogic;
	Ordering<View> viewOrdering = ViewSorter.DEFAULT.getOrientedOrdering();

	ViewTranslationSerializer(final ViewLogic viewLogic, final TranslationLogic translationLogic,
			final JSONArray sorters, String separator, SetupFacade setupFacade) {
		this.viewLogic = viewLogic;
		this.translationLogic = translationLogic;
		setOrderings(sorters);
	}

	private void setOrderings(final JSONArray sorters) {
		// TODO
	}

	@Override
	public Iterable<GenericTableEntry> serialize() {
		final Iterable<View> views = viewLogic.fetchViewsOfAllTypes();
		final Iterable<View> sortedViews = viewOrdering.sortedCopy(views);
		return serialize(sortedViews);
	}

	Iterable<GenericTableEntry> serialize(final Iterable<View> sortedViews) {
		final Collection<GenericTableEntry> jsonViews = Lists.newArrayList();
		for (final View view : sortedViews) {
			final String name = view.getName();
			final TableEntry jsonView = new TableEntry();
			jsonView.setName(name);
			final Collection<EntryField> fields = readFields(view);
			jsonView.setFields(fields);
			jsonViews.add(jsonView);
		}
		return jsonViews;
	}

	private Collection<EntryField> readFields(final View view) {
		final Collection<EntryField> jsonFields = Lists.newArrayList();
		final TranslationObject translationObject = ViewConverter.DESCRIPTION //
				.withIdentifier(view.getName()) //
				.create();
		final Map<String, String> fieldTranslations = translationLogic.readAll(translationObject);
		final EntryField field = new EntryField();
		field.setName(ViewConverter.description());
		field.setTranslations(fieldTranslations);
		field.setValue(view.getDescription());
		jsonFields.add(field);
		return jsonFields;
	}

	@Override
	public DataHandler exportCsv() {
		throw new UnsupportedOperationException("to do");
	}

}
