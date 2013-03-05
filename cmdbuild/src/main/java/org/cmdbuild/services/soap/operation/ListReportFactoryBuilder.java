package org.cmdbuild.services.soap.operation;

import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.CardQueryImpl;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

import com.google.common.collect.Lists;

/*
 * TODO
 * Looks like WebService only stuff
 * Correct it when porting to the new DAO the web services
 */

public class ListReportFactoryBuilder implements ReportFactoryBuilder<ReportFactory> {

	private static final String CLASSNAME_PROPERTY = "classname";
	private static final String ATTRIBUTES_PROPERTY = "attributes";
	private static final String ATTRIBUTES_SEPARATOR = ",";

	private final UserContext userContext;

	private String extension;
	private Map<String, String> properties;

	public ListReportFactoryBuilder(final UserContext userContext) {
		this.userContext = userContext;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withExtension(final String extension) {
		this.extension = extension;
		return this;
	}

	@Override
	public ReportFactoryBuilder<ReportFactory> withProperties(final Map<String, String> properties) {
		this.properties = properties;
		return this;
	}

	@Override
	public ReportFactory build() {
		throw new UnsupportedOperationException("Whaiting new DAO integration");
//		try {
//			final CardQuery cardQuery = cardQuery();
//			return new ReportFactoryTemplateList( //
//					ReportExtension.valueOf(extension.toUpperCase()), //
//					cardQuery, //
//					attributes(), //
//					cardQuery.getTable());
//		} catch (final Throwable e) {
//			throw new Error(e);
//		}
	}

	private CardQuery cardQuery() {
		final GuestFilter guestFilter = new GuestFilter(userContext);
		final ITable table = table();
		final CardQuery unfilteredCardQuery = unfilteredCardQuery(table);
		final CardQuery filteredCardQuery;
		if (table.isActivity()) {
			filteredCardQuery = guestFilter.apply(unfilteredCardQuery);
			if (filteredCardQuery == null) {
				unfilteredCardQuery.setPrevExecutorsFilter(userContext);
			}

		} else {
			filteredCardQuery = unfilteredCardQuery;
		}
		return (filteredCardQuery == null) ? unfilteredCardQuery : filteredCardQuery;
	}

	private static CardQuery unfilteredCardQuery(final ITable table) {
		return new CardQueryImpl(table);
	}

	private List<String> attributes() {
		final String attributes = properties.get(ATTRIBUTES_PROPERTY);
		return Lists.newArrayList(attributes.split(ATTRIBUTES_SEPARATOR));
	}

	private ITable table() {
		final String className = properties.get(CLASSNAME_PROPERTY);
		final ITable table = UserOperations.from(userContext).tables().get(className);
		return table;
	}

}
