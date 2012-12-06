package org.cmdbuild.services.auth;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.meta.MetadataMap;
import org.cmdbuild.services.meta.MetadataService;

public class DomainUserUtils {

	public static class MetadataUtils {

		private static final String ORG_CMDBUILD_PORTLET_GROUP_DOMAIN = "org.cmdbuild.portlet.group.domain";
		private static final String ORG_CMDBUILD_PORTLET_USER_EMAIL = "org.cmdbuild.portlet.user.email";
		private static final String ORG_CMDBUILD_PORTLET_USER_USERNAME = "org.cmdbuild.portlet.user.username";
		private static final String ORG_CMDBUILD_PORTLET_USER_TABLE = "org.cmdbuild.portlet.user.table";

		private static MetadataMap metadataMap;

		private MetadataUtils() {
			// prevents instantiation
		}

		private static MetadataMap getMetadataMap() {
			if (metadataMap == null) {
				metadataMap = MetadataService.getMetadata();
			}
			return metadataMap;
		}

		public static String getUserTable() {
			return (String) getMetadataMap().get(ORG_CMDBUILD_PORTLET_USER_TABLE);
		}

		public static String getUserName() {
			return (String) getMetadataMap().get(ORG_CMDBUILD_PORTLET_USER_USERNAME);
		}

		public static String getUserEmail() {
			return (String) getMetadataMap().get(ORG_CMDBUILD_PORTLET_USER_EMAIL);
		}

		public static String getGroupTable() {
			return (String) getMetadataMap().get(ORG_CMDBUILD_PORTLET_GROUP_DOMAIN);
		}

	}

	public static interface DomainUserQuery {

		boolean isFound();

		ICard getCard();

	}

	private DomainUserUtils() {
		// prevents instantiation
	}

	public static DomainUserQuery queryDomainUser(final String login) {
		try {
			final String table = DomainUserUtils.MetadataUtils.getUserTable();
			final String username = DomainUserUtils.MetadataUtils.getUserName();
			final String email = DomainUserUtils.MetadataUtils.getUserEmail();

			final String loginAttribute = login.contains("@") ? email : username;

			final ITable userTable = UserOperations.from(UserContext.systemContext()).tables().get(table);
			final CardQuery cardQuery = userTable.cards().list() //
					.filter(loginAttribute, AttributeFilterType.EQUALS, login);

			final ICard card = cardQuery.get(false);

			return new DomainUserQuery() {

				@Override
				public ICard getCard() {
					return card;
				}

				@Override
				public boolean isFound() {
					return true;
				}

			};
		} catch (final Exception e) {
			return new DomainUserQuery() {

				@Override
				public boolean isFound() {
					return false;
				}

				@Override
				public ICard getCard() {
					return null;
				}

			};
		}
	}
}
