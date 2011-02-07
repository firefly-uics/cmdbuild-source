package org.cmdbuild.portlet.operation;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.cmdbuild.portlet.Log;
import org.cmdbuild.portlet.auth.AuthMethod;
import org.cmdbuild.portlet.configuration.PortletConfiguration;
import org.cmdbuild.portlet.metadata.User;
import org.cmdbuild.portlet.ws.SOAPClient;
import org.cmdbuild.services.soap.Attribute;
import org.cmdbuild.services.soap.AttributeSchema;
import org.cmdbuild.services.soap.CardList;
import org.cmdbuild.services.soap.Filter;
import org.cmdbuild.services.soap.MenuSchema;
import org.cmdbuild.services.soap.Metadata;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.Query;

public class WSOperation {

	private final Private service;

	public WSOperation(final SOAPClient client) {
		Validate.notNull(client);
		this.service = client.getService();
	}

	public Private getService() {
		return service;
	}

	public User getUser(final String login) {
		final String usertable = PortletConfiguration.getInstance().getCMDBuildUserClass();
		if (StringUtils.isNotEmpty(usertable)) {
			return getUserInformations(usertable, login);
		} else {
			Log.PORTLET.debug("No user class defined in properties");
			return null;
		}
	}

	private User getUserInformations(final String usertable, final String login) {
		Log.PORTLET.debug("Getting information for user " + login);
		final PortletConfiguration portletConfiguration = PortletConfiguration.getInstance();
		final String[] mail = new String[1];
		mail[0] = login;
		final Query query = new Query();
		final Filter filter = new Filter();
		final AuthMethod method = portletConfiguration.getAuthMethod();
		final String table;
		switch (method) {
		case USERNAME:
			table = portletConfiguration.getCMDBuildUserUsername();
			break;
		case EMAIL:
			table = portletConfiguration.getCMDBuildUserEmail();
			break;
		default:
			Log.PORTLET.error("Illegal authentication method " + method);
			table = StringUtils.EMPTY;
			break;
		}
		filter.setName(table);
		filter.setOperator("EQUALS");
		filter.getValue().add(login);
		query.setFilter(filter);
		User user = null;
		final CardList card = service.getCardList(usertable, null, query, null, 0, 0, null, null);
		if (card != null && card.getCards().size() == 1) {
			user = new User();
			final int id = card.getCards().get(0).getId();
			user.setId(id);
			user.setLogin(login);
			final List<Attribute> attributes = card.getCards().get(0).getAttributeList();
			user.setName(getDescription(attributes));
		} else {
			if (card == null || card.getCards() == null) {
				Log.PORTLET.error("Error querying for user in table " + usertable);
			} else if (card.getCards().isEmpty()) {
				Log.PORTLET.error("No user in table " + usertable + " with email " + login);
			} else {
				Log.PORTLET.error("Too many users in table " + usertable + " with email " + login);
			}
		}
		return user;
	}

	private String getDescription(final List<Attribute> attributes) {
		String description = StringUtils.EMPTY;
		for (final Attribute attribute : attributes) {
			if ("Description".equals(attribute.getName())) {
				description = attribute.getValue();
			}
		}
		return description;
	}

	public MenuSchema getProcessMenu() {
		return service.getActivityMenuSchema();
	}

	public MenuSchema getClassMenu() {
		return service.getCardMenuSchema();
	}

	public MenuSchema getMenu() {
		return service.getMenuSchema();
	}

	public String getGroup(final MenuSchema schema) {
		if (schema.getId() > 0) {
			for (final Metadata m : schema.getMetadata()) {
				if (m != null && "runtime.groupname".equals(m.getKey())) {
					return m.getValue();
				}
			}
			return StringUtils.EMPTY;
		} else {
			return getGroup(schema.getChildren().get(0));
		}
	}

	public List<AttributeSchema> getAttributeList(final String classname) {
		Log.PORTLET.debug("Getting attribute schema for class " + classname);
		final List<AttributeSchema> list = service.getAttributeList(classname);
		return sortAttributes(list);
	}

	private static List<AttributeSchema> sortAttributes(final List<AttributeSchema> attributeCollection) {
		final List<AttributeSchema> sortedAttributes = new LinkedList<AttributeSchema>();
		sortedAttributes.addAll(attributeCollection);
		Collections.sort(sortedAttributes, new Comparator<AttributeSchema>() {

			public int compare(final AttributeSchema a1, final AttributeSchema a2) {
				return (a1.getIndex() - a2.getIndex());
			}
		});

		return sortedAttributes;
	}

}
