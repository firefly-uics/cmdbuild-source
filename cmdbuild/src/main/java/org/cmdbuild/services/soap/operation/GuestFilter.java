package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.json.JSONObject;
import org.slf4j.Logger;

class GuestFilter {

	private static final Logger logger = Log.CMDBUILD;

	private static final String METADATA_PORTLET_USER = "org.cmdbuild.portlet.user.id";
	private static final String CLASS_ATTRIBUTE_SEPARATOR = ".";

	private final OperationUser operationUser;

	public GuestFilter(final OperationUser operationUser) {
		logger.info("creating guest filter for user '{}'", //
				operationUser.getAuthenticatedUser().getUsername());
		this.operationUser = operationUser;
	}

	public QueryOptions apply(final QueryOptions original) {
		return QueryOptions.newQueryOption() //
				.clone(original) //
				.filter(withAdditions(original)) //
				.build();
	}

	private JSONObject withAdditions(final QueryOptions original) {
		// CardQuery filteredCardQuery = null;
		// if (userContext.isGuest()) {
		// for (final IAttribute attribute :
		// original.getTable().getAttributes().values()) {
		// logger.debug(format("trying filtering attribute '%s'",
		// attribute.getName()));
		// final MetadataMap metadata =
		// MetadataService.of(attribute).getMetadataMap();
		//
		// String targetAttributeName = null;
		//
		// if (metadata.get(METADATA_PORTLET_USER) != null) {
		// logger.debug(format("metadata '%s' found for attribute '%s'", //
		// METADATA_PORTLET_USER, attribute.getName()));
		// final String metadataValue =
		// metadata.get(METADATA_PORTLET_USER).toString();
		// logger.debug(format("metadata '%s' has value '%s'", //
		// METADATA_PORTLET_USER, metadataValue));
		// targetAttributeName = extractAttributeName(metadataValue);
		// }
		//
		// if (targetAttributeName != null) {
		// filteredCardQuery = (CardQuery) original.clone();
		// final ITable userTable = attribute.getReferenceTarget();
		// logger.debug(format("filtering results where attribute '%s.%s' equals '%s'",
		// //
		// userTable.getName(), targetAttributeName,
		// userContext.getRequestedUsername()));
		// final CardQuery userQuery = userTable //
		// .cards() //
		// .list() //
		// .filter(targetAttributeName, AttributeFilterType.EQUALS,
		// userContext.getRequestedUsername());
		// filteredCardQuery.cardInRelation(attribute.getReferenceDirectedDomain(),
		// userQuery);
		// break;
		// }
		// }
		// } else {
		// logger.warn("cannot apply filter, user is not guest");
		// }
		return original.getFilter();
	}

	private String extractAttributeName(final String metadataValue) {
		final String targetAttributeName;
		if (isNotBlank(metadataValue) && metadataValue.contains(CLASS_ATTRIBUTE_SEPARATOR)) {
			targetAttributeName = metadataValue.split(quote(CLASS_ATTRIBUTE_SEPARATOR))[1];
			logger.debug(format("extracted attribute name is '%s'", targetAttributeName));
		} else {
			logger.debug(format("cannot extract attribute name from '%s'", metadataValue));
			targetAttributeName = null;
		}
		return targetAttributeName;
	}

}
