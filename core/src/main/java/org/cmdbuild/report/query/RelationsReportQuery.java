package org.cmdbuild.report.query;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.model.data.Card;

import com.google.common.collect.Iterables;

/*
 * Query example
 * 

 SELECT	_cm_read_comment(
 _cm_comment_for_class('Room'),
 'DESCR'
 ) AS "TargetDescription",
 _cm_read_comment(
 _cm_comment_for_class('Map_RoomAsset'),
 'DESCRINV'
 ) AS "DomainDescription",

 "Target"."BeginDate",
 "Target"."Code",
 "Target"."Description"

 FROM "Asset" AS "Target"
 JOIN "Map_RoomAsset" ON "Target"."Id" = "Map_RoomAsset"."IdObj2" AND "Map_RoomAsset"."IdObj1" = 272
 WHERE "Target"."Status" = 'A'

 UNION

 ...

 *
 *
 */

public class RelationsReportQuery {

	private static final String DOMAIN_DESCRIPTION_TEMPLATE = "_cm_read_comment(_cm_comment_for_class('Map_%s'), '%s') AS \"DomainDescription\"";
	private static final String TARGET_CLASS_DESCRIPTION_TEMPLATE = "_cm_read_comment(_cm_comment_for_class('%s'), 'DESCR') AS \"TargetDescription\"";
	private static final String SELECT_TEMPLATE = "SELECT %s, %s, 	\"Target\".\"BeginDate\", \"Target\".\"Code\", \"Target\".\"Description\" ";
	private static final String FROM_TEMPLATE = "FROM \"%s\" AS \"Target\" ";
	private static final String JOIN_TEMPLATE = "JOIN \"Map_%s\" ON \"Target\".\"Id\" = \"Map_%s\".\"%s\" AND \"Map_%s\".\"%s\" = %s AND \"Map_%s\".\"Status\" = 'A'";
	private static final String WHERE_PART = "WHERE \"Target\".\"Status\" = 'A' ";
	private static final String ORDER_BY_PART = "ORDER BY \"DomainDescription\", \"Description\" ";

	private static final String DIRECT_DESCRIPTION = "DESCRDIR";
	private static final String INVERSE_DESCRIPTION = "DESCRINV";
	private static final String FIRST_OBJECT_ID = "IdObj1";
	private static final String SECOND_OBJECT_ID = "IdObj2";

	public static final String DOMAIN_DESCRIPTION = "DomainDescription";
	public static final String CLASS_DESCRIPTION = "TargetDescription";
	public static final String BEGIN_DATE = "BeginDate";
	public static final String DESCRIPTION = "Description";
	public static final String CODE = "Code";

	private final String query;

	public RelationsReportQuery(final Card card, final Iterable<? extends CMDomain> domains) {
		final CMClass source = card.getType();
		final StringBuilder queryStringBuilder = new StringBuilder();
		boolean first = true;

		if (Iterables.size(domains) > 0) {
			for (final CMDomain domain : domains) {
				if (first) {
					first = false;
				} else {
					queryStringBuilder.append(" UNION ");
				}

				final CMClass class1 = domain.getClass1();
				if (class1.isAncestorOf(source)) {
					queryStringBuilder.append(buildDirectSubSelect(card, domain));
				} else {
					queryStringBuilder.append(buildInverseSubSelect(card, domain));
				}
			}

			queryStringBuilder.append(ORDER_BY_PART);
			query = queryStringBuilder.toString();
		} else {
			query = "";
		}
	}

	private String buildInverseSubSelect(final Card card, final CMDomain domain) {
		final StringBuilder query = new StringBuilder();
		final String targetClassName = domain.getClass1().getIdentifier().getLocalName();
		final String domainName = domain.getIdentifier().getLocalName();
		query.append(//
		String.format(SELECT_TEMPLATE, //
				String.format(TARGET_CLASS_DESCRIPTION_TEMPLATE, targetClassName), //
				String.format(DOMAIN_DESCRIPTION_TEMPLATE, domainName, INVERSE_DESCRIPTION)));

		query.append(//
		String.format(FROM_TEMPLATE, targetClassName));

		query.append(//
		String.format(JOIN_TEMPLATE, //
				domainName, //
				domainName, //
				FIRST_OBJECT_ID, //
				domainName, //
				SECOND_OBJECT_ID, //
				card.getId(), //
				domainName //
		) //
		);

		query.append(WHERE_PART);

		return query.toString();
	}

	private String buildDirectSubSelect(final Card card, final CMDomain domain) {
		final StringBuilder query = new StringBuilder();
		final String domainName = domain.getIdentifier().getLocalName();
		final String targetClassName = domain.getClass2().getIdentifier().getLocalName();

		query.append(//
		String.format(SELECT_TEMPLATE, //
				String.format(TARGET_CLASS_DESCRIPTION_TEMPLATE, targetClassName), //
				String.format(DOMAIN_DESCRIPTION_TEMPLATE, domainName, DIRECT_DESCRIPTION) //
		));

		query.append(//
		String.format(FROM_TEMPLATE, targetClassName));

		query.append(//
		String.format(JOIN_TEMPLATE, //
				domainName, //
				domainName, //
				SECOND_OBJECT_ID, //
				domainName, //
				FIRST_OBJECT_ID, //
				card.getId(), //
				domainName //
		) //
		);

		query.append(WHERE_PART);

		return query.toString();
	}

	@Override
	public String toString() {
		return query;
	}
}
