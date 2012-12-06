package org.cmdbuild.services;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import java.util.Map;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.csv.CSVData;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.services.auth.OperationUserWrapper;
import org.cmdbuild.services.auth.UserContext;

/*
 * Should be merged with the RequestListener
 */

public class SessionVars implements UserStore {

	private static final String AUTH_KEY = "auth";
	private static final String LANGUAGE_KEY = "language";
	private static final String FILTER_MAP_KEY = "filters";
	private static final String REPORTFACTORY_KEY = "ReportFactorySessionObj";
	private static final String NEWREPORT_KEY = "newReport";
	private static final String CSVDATA_KEY = "csvdata";

	@Deprecated
	public UserContext getCurrentUserContext() {
		final OperationUser operationUser = getUser();
		if (operationUser.getAuthenticatedUser().isAnonymous()) {
			return null;
		} else {
			return new OperationUserWrapper(operationUser);
		}
	}

	@Override
	public OperationUser getUser() {
		OperationUser operationUser = (OperationUser) RequestListener.getCurrentSessionObject(AUTH_KEY);
		if (operationUser == null) {
			operationUser = new OperationUser(ANONYMOUS_USER, new NullPrivilegeContext(), new NullGroup());
			setUser(operationUser);
		}
		return operationUser;
	}

	@Override
	public void setUser(final OperationUser user) {
		RequestListener.setCurrentSessionObject(AUTH_KEY, user);
	}

	@SuppressWarnings("unchecked")
	public Map<String, CardQuery> getFilterMap() {
		return (Map<String, CardQuery>) RequestListener.getCurrentSessionObject(FILTER_MAP_KEY);
	}

	public void setFilterMap(final Map<String, CardQuery> filterMap) {
		RequestListener.setCurrentSessionObject(FILTER_MAP_KEY, filterMap);
	}

	public String getLanguage() {
		String language = (String) RequestListener.getCurrentSessionObject(LANGUAGE_KEY);
		if (language == null) {
			language = CmdbuildProperties.getInstance().getLanguage();
			setLanguage(language);
		}
		return language;
	}

	public void setLanguage(final String language) {
		RequestListener.setCurrentSessionObject(LANGUAGE_KEY, language);
	}

	public ReportFactory getReportFactory() {
		return (ReportFactory) RequestListener.getCurrentSessionObject(REPORTFACTORY_KEY);
	}

	public void setReportFactory(final ReportFactory value) {
		RequestListener.setCurrentSessionObject(REPORTFACTORY_KEY, value);
	}

	public void removeReportFactory() {
		RequestListener.removeCurrentSessionObject(REPORTFACTORY_KEY);
	}

	public ReportCard getNewReport() {
		return (ReportCard) RequestListener.getCurrentSessionObject(NEWREPORT_KEY);
	}

	public void setNewReport(final ReportCard newReport) {
		RequestListener.setCurrentSessionObject(NEWREPORT_KEY, newReport);
	}

	public void removeNewReport() {
		RequestListener.removeCurrentSessionObject(NEWREPORT_KEY);
	}

	public CSVData getCsvData() {
		return (CSVData) RequestListener.getCurrentSessionObject(CSVDATA_KEY);
	}

	public void setCsvData(final CSVData value) {
		RequestListener.setCurrentSessionObject(CSVDATA_KEY, value);
	}
}
