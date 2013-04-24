package org.cmdbuild.report.query;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.model.data.Card;

import com.google.common.collect.Iterables;

/**
 * Build a query to return all the card attributes, selecting the Description for
 * Reference and Lookup attributes
 */
public class CardReportQuery {
	final static String ATTRIBUTE_TEMPLATE = "\"%s\".\"%s\" AS \"%s_%s\" ";
	final static String WHERE_TEMPLATE = "WHERE \"%s\".\"Status\" = 'A' AND \"%s\".\"Id\" = %s";
	final static String JOIN_TEMPLATE = "LEFT JOIN \"%s\" AS \"%s\" ON \"%s\".\"%s\" = \"%s\".\"Id\"";

	final String query;

	public CardReportQuery(final Card card, final CMDataView dataView) {
		final CMClass table = card.getType();
		final StringBuilder selectStringBuilder = new StringBuilder();
		final StringBuilder fromStringBuilder = new StringBuilder();
		final String tableName = table.getIdentifier().getLocalName();

		selectStringBuilder.append("SELECT ");
		fromStringBuilder.append("FROM \"" + tableName + "\" ");

		int index = 1;
		int attributesSize = Iterables.size(table.getAttributes());
		for (final CMAttribute attribute: table.getAttributes()) {
			final CMAttributeType<?> attributeType = attribute.getType();
			final String attributeName = attribute.getName();

			if (attributeType instanceof LookupAttributeType) {
				final String lookupAlias = lookupTableAliasForAttributeName(attributeName);
				selectStringBuilder.append(String.format(ATTRIBUTE_TEMPLATE, lookupAlias, "Description", tableName, attributeName));
				fromStringBuilder.append(String.format(JOIN_TEMPLATE, "LookUp", lookupAlias, tableName, attributeName, lookupAlias));

			} else if (attributeType instanceof ReferenceAttributeType) {
				final String domainName = ((ReferenceAttributeType) attribute.getType()).getDomainName();
				final CMDomain domain = dataView.findDomain(domainName);
				final CMEntryType owner = attribute.getOwner();
				final CMClass target;

				if (domain.getClass1().getIdentifier().getLocalName().equals(owner.getIdentifier().getLocalName())) {
					target = domain.getClass2();
				} else {
					target = domain.getClass1();
				}

				final String referencedTableName = target.getIdentifier().getLocalName();
				final String referencedTableAlias = String.format("%s_%s", referencedTableName, attributeName);

				fromStringBuilder.append(String.format(JOIN_TEMPLATE, referencedTableName, referencedTableAlias, tableName, attributeName, referencedTableAlias));
				selectStringBuilder.append(String.format(ATTRIBUTE_TEMPLATE, referencedTableAlias, "Description", tableName, attributeName));
			} else {
				selectStringBuilder.append(String.format(ATTRIBUTE_TEMPLATE, tableName, attributeName, tableName, attributeName));
			}

			if (index++ < attributesSize) {
				selectStringBuilder.append(", ");
			}
		}

		final String wherePart = String.format(WHERE_TEMPLATE, tableName, tableName, card.getId());
		this.query = String.format("%s %s %s", selectStringBuilder.toString(), fromStringBuilder.toString(), wherePart);
	}

	private static String lookupTableAliasForAttributeName(String attributeName) {
		return String.format("LookUp_%s", attributeName);
	}

	@Override
	public String toString() {
		return query;
	}
}
