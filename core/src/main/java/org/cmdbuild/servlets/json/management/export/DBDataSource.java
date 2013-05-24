package org.cmdbuild.servlets.json.management.export;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.collect.Lists;

public class DBDataSource implements CMDataSource {

	private final CMDataView view;
	private final CMClass sourceClass;

	public DBDataSource(final CMDataView view, final CMClass sourceClass) {
		Validate.notNull(sourceClass);
		this.view = view;
		this.sourceClass = sourceClass;
	}

	@Override
	public Iterable<String> getHeaders() {
		final List<String> attributeNames = Lists.newArrayList();
		for (final CMAttribute attribute : sourceClass.getActiveAttributes()) {
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	@Override
	public Iterable<CMEntry> getEntries() {
		final List<CMEntry> entries = Lists.newArrayList();
		final CMQueryResult result = view.select(anyAttribute(sourceClass)).from(sourceClass).run();
		for (final CMQueryRow row : result) {
			entries.add(row.getCard(sourceClass));
		}
		return entries;
	}

}
