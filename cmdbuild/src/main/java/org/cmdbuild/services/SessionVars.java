package org.cmdbuild.services;

import java.util.Map;

import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.csv.CSVData;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.elements.report.ReportFactory;
import org.cmdbuild.elements.wrappers.ReportCard;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.services.auth.UserContext;

/*
 * Should be merged with the RequestListener
 */

public class SessionVars {

	private static final String AUTH_KEY = "auth";
	private static final String LANGUAGE_KEY = "language";	
	private static final String FILTER_MAP_KEY = "filters";
	private static final String REPORTFACTORY_KEY = "ReportFactorySessionObj";
	private static final String NEWREPORT_KEY = "newReport";
	private static final String CSVDATA_KEY = "csvdata";

	public UserContext getCurrentUserContext() {
		return (UserContext) RequestListener.getCurrentSessionObject(AUTH_KEY);
	}

	public void setCurrentUserContext(UserContext userCtx) {
		RequestListener.setCurrentSessionObject(AUTH_KEY, userCtx);
	}

	@SuppressWarnings("unchecked")
	public Map<String, CardQuery> getFilterMap() {
		return (Map<String, CardQuery>) RequestListener.getCurrentSessionObject(FILTER_MAP_KEY);
	}

	public void setFilterMap(Map<String, CardQuery> filterMap) {
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

	public void setLanguage(String language) {
		RequestListener.setCurrentSessionObject(LANGUAGE_KEY, language);
	}

	public ReportFactory getReportFactory() {
		return (ReportFactory) RequestListener.getCurrentSessionObject(REPORTFACTORY_KEY);
	}

	public void setReportFactory(ReportFactory value) {
		RequestListener.setCurrentSessionObject(REPORTFACTORY_KEY, value);
	}

	public void removeReportFactory() {
		RequestListener.removeCurrentSessionObject(REPORTFACTORY_KEY);
	}

	public ReportCard getNewReport() {
		return (ReportCard) RequestListener.getCurrentSessionObject(NEWREPORT_KEY);
	}

	public void setNewReport(ReportCard newReport) {
		RequestListener.setCurrentSessionObject(NEWREPORT_KEY, newReport);
	}

	public void removeNewReport() {
		RequestListener.removeCurrentSessionObject(NEWREPORT_KEY);
	}

	public CSVData getCsvData() {
		return (CSVData) RequestListener.getCurrentSessionObject(CSVDATA_KEY);
	}

	public void setCsvData(CSVData value) {
		RequestListener.setCurrentSessionObject(CSVDATA_KEY, value);
	}
}
