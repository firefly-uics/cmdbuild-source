package org.cmdbuild.services;

import static org.cmdbuild.auth.user.AuthenticatedUserImpl.ANONYMOUS_USER;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.UserTypeStore;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.context.NullPrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.model.Report;
import org.cmdbuild.report.ReportFactory;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.servlets.json.management.dataimport.csv.CsvData;

/*
 * Should be merged with the RequestListener
 */

public class SessionVars implements UserStore, UserTypeStore {

	private static final String AUTH_KEY = "auth";
	private static final String AUTH_TYPE_KEY = "authType";
	private static final String LANGUAGE_KEY = "language";
	private static final String REPORTFACTORY_KEY = "ReportFactorySessionObj";
	private static final String NEWREPORT_KEY = "newReport";
	private static final String CSVDATA_KEY = "csvdata";

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

	@Override
	public UserType getType() {
		UserType type = (UserType) RequestListener.getCurrentSessionObject(AUTH_TYPE_KEY);
		if (type == null) {
			type = UserType.APPLICATION;
			setType(type);
		}
		return type;
	}

	@Override
	public void setType(final UserType type) {
		RequestListener.setCurrentSessionObject(AUTH_TYPE_KEY, type);
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

	public Report getNewReport() {
		return (Report) RequestListener.getCurrentSessionObject(NEWREPORT_KEY);
	}

	public void setNewReport(final Report newReport) {
		RequestListener.setCurrentSessionObject(NEWREPORT_KEY, newReport);
	}

	public void removeNewReport() {
		RequestListener.removeCurrentSessionObject(NEWREPORT_KEY);
	}

	public CsvData getCsvData() {
		return (CsvData) RequestListener.getCurrentSessionObject(CSVDATA_KEY);
	}

	public void setCsvData(final CsvData value) {
		RequestListener.setCurrentSessionObject(CSVDATA_KEY, value);
	}
}
