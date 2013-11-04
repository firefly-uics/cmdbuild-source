package org.cmdbuild.services.soap.operation;

import static java.lang.String.format;
import static java.util.regex.Pattern.quote;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.data.store.NullOnNotFoundReadStore;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.QueryOptions.QueryOptionsBuilder;
import org.cmdbuild.model.data.Metadata;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.servlets.json.util.JsonFilterHelper;
import org.cmdbuild.servlets.json.util.JsonFilterHelper.FilterElementGetter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

class GuestFilter {

	private static final Logger logger = Log.CMDBUILD;

	private static final String METADATA_PORTLET_USER = "org.cmdbuild.portlet.user.id";
	private static final String CLASS_ATTRIBUTE_SEPARATOR = ".";

	private static final Storable METADATA_PORTLET_USER_STORABLE = new Storable() {

		@Override
		public String getIdentifier() {
			return METADATA_PORTLET_USER;
		}

	};

	private final OperationUser operationUser;
	private final UserType userType;

	public GuestFilter(final OperationUser operationUser, final UserType userType) {
		logger.info("creating guest filter for user '{}' with type '{}'", //
				operationUser.getAuthenticatedUser().getUsername(), userType);
		this.operationUser = operationUser;
		this.userType = userType;
	}

	public QueryOptions apply(final CMClass target, final QueryOptions queryOptions) {
		final QueryOptionsBuilder queryOptionsBuilder = QueryOptions.newQueryOption().clone(queryOptions);
		if (userType != UserType.APPLICATION) {
			final MetadataStoreFactory metadataStoreFactory = applicationContext().getBean(MetadataStoreFactory.class);
			for (final CMAttribute attribute : target.getAttributes()) {
				logger.debug("trying filtering attribute '{}'", attribute.getName());

				final CMAttributeType<?> attributeType = attribute.getType();
				if (!(attributeType instanceof ReferenceAttributeType)) {
					logger.debug("not a reference type, skipping");
					continue;
				}

				String targetAttributeName = null;

				final Store<Metadata> _store = metadataStoreFactory.storeForAttribute(attribute);
				final NullOnNotFoundReadStore<Metadata> store = NullOnNotFoundReadStore.of(_store);
				final Metadata userMetadata = store.read(METADATA_PORTLET_USER_STORABLE);
				if (userMetadata != null) {
					logger.debug("metadata '{}' found for attribute '{}'", METADATA_PORTLET_USER, attribute.getName());
					logger.debug("metadata '{}' has value '{}'", METADATA_PORTLET_USER, userMetadata.value);
					targetAttributeName = extractAttributeName(userMetadata.value);
				}

				if (targetAttributeName != null) {
					/*
					 * absolutely ugly! QueryOptions needs to be refactored
					 * using Java objects instead of JSON
					 */
					final Long id = operationUser.getAuthenticatedUser().getId();
					final String attributeFilter = format(
							"{simple: {attribute: \"%s\", operator: \"equal\", value: [%d]}}", //
							targetAttributeName, id);
					final JSONObject original = queryOptions.getFilter();

					try {
						final JSONObject originalWithAddidion = new JsonFilterHelper(original)
								.merge(new FilterElementGetter() {

									@Override
									public boolean hasElement() {
										return true;
									}

									@Override
									public JSONObject getElement() throws JSONException {
										return new JSONObject(attributeFilter);
									}

								});
						queryOptionsBuilder.filter(originalWithAddidion);
					} catch (final Exception e) {
						// nothing to do
					}
					break;
				}
			}
		} else {
			logger.warn("cannot apply filter, user is not guest");
		}
		return queryOptionsBuilder.build();
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
