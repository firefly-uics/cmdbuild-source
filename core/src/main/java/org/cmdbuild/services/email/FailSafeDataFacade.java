package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.logger.Log;
import org.cmdbuild.services.email.EmailTemplateResolver.DataFacade;
import org.slf4j.Logger;

// TODO use a proxy
public class FailSafeDataFacade extends ForwardingDataFacade {

	private static final Logger logger = Log.EMAIL;

	private static final List<String> EMPTY_LIST = Collections.emptyList();

	public FailSafeDataFacade(final DataFacade dataFacade) {
		super(dataFacade);
	}

	@Override
	public String getEmailForUser(final String user) {
		try {
			return super.getEmailForUser(user);
		} catch (final Exception e) {
			logger.error("error getting emails for user", e);
		}
		return EMPTY;
	}

	@Override
	public String getEmailForGroup(final String group) {
		try {
			return super.getEmailForGroup(group);
		} catch (final Exception e) {
			logger.error("error getting emails for group", e);
		}
		return EMPTY;
	}

	@Override
	public Iterable<String> getEmailsForGroupUsers(final String group) {
		try {
			return super.getEmailsForGroupUsers(group);
		} catch (final Exception e) {
			logger.error("error getting emails for group users", e);
		}
		return EMPTY_LIST;
	}

	@Override
	public String getAttributeValue(final String attribute) {
		try {
			return super.getAttributeValue(attribute);
		} catch (final Exception e) {
			logger.error("error getting attribute", e);
		}
		return EMPTY;
	}

	@Override
	public String getReferenceAttributeValue(final String attribute, final String subAttribute) {
		try {
			return super.getReferenceAttributeValue(attribute, subAttribute);
		} catch (final Exception e) {
			logger.error("error getting attribute", e);
		}
		return EMPTY;
	}

}