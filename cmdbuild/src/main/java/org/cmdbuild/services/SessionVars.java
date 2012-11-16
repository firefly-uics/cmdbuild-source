package org.cmdbuild.services;

import java.util.Map;

import org.cmdbuild.auth.AuthenticatedUser;
import org.cmdbuild.auth.AuthenticatedUserImpl;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.csv.CSVData;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.services.auth.AuthenticatedUserWrapper;
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

	@Override
	public AuthenticatedUser getUser() {
		AuthenticatedUser authUser = (AuthenticatedUser) RequestListener.getCurrentSessionObject(AUTH_KEY);
		if (authUser == null) {
			authUser = AuthenticatedUserImpl.newInstance(null);
			setUser(authUser);
		}
		return authUser;
	}

	@Override
	public void setUser(final AuthenticatedUser user) {
		RequestListener.setCurrentSessionObject(AUTH_KEY, user);
	}

	@Deprecated
	public UserContext getCurrentUserContext() {
		final AuthenticatedUser authUser = getUser();
		if (authUser.getId() == null) { // Anonymous User
			return null;
		} else {
			return new AuthenticatedUserWrapper(authUser);
		}
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
