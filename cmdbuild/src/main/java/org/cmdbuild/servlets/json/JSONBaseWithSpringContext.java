package org.cmdbuild.servlets.json;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.sql.DataSource;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.listeners.RequestListener;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TranslationService;
import org.cmdbuild.services.localization.Localization;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.menu.MenuStore;

public class JSONBaseWithSpringContext extends JSONBase {

	protected OperationUser operationUser() {
		return applicationContext().getBean(OperationUser.class);
	}

	/*
	 * Properties
	 */

	protected CmdbuildProperties cmdbuildConfiguration() {
		return applicationContext().getBean(CmdbuildProperties.class);
	}

	protected GraphProperties graphProperties() {
		return applicationContext().getBean(GraphProperties.class);
	}

	/*
	 * Database
	 */

	protected DataSource dataSource() {
		return applicationContext().getBean(DataSource.class);
	}

	protected PatchManager patchManager() {
		return applicationContext().getBean(PatchManager.class);
	}

	protected CMDataView systemDataView() {
		return applicationContext().getBean(DBDataView.class);
	}

	protected CMDataView userDataView() {
		return applicationContext().getBean(UserDataView.class);
	}

	/*
	 * Stores
	 */

	protected FilterStore filterStore() {
		return applicationContext().getBean(FilterStore.class);
	}

	protected LookupStore lookupStore() {
		return applicationContext().getBean(LookupStore.class);
	}

	protected MenuStore menuStore() {
		return applicationContext().getBean(MenuStore.class);
	}

	protected UserStore userStore() {
		return applicationContext().getBean(UserStore.class);
	}

	protected LanguageStore languageStore() {
		return applicationContext().getBean(LanguageStore.class);
	}

	/*
	 * Logics
	 */

	protected AuthenticationLogic authLogic() {
		return applicationContext().getBean("authLogic", AuthenticationLogic.class);
	}

	protected CachingLogic cachingLogic() {
		return applicationContext().getBean(CachingLogic.class);
	}

	protected DataAccessLogic systemDataAccessLogic() {
		return applicationContext().getBean("systemDataAccessLogic", DataAccessLogic.class);
	}

	protected DataAccessLogic userDataAccessLogic() {
		return applicationContext().getBean("userDataAccessLogic", DataAccessLogic.class);
	}

	protected DataDefinitionLogic dataDefinitionLogic() {
		return applicationContext().getBean(DataDefinitionLogic.class);
	}

	protected DmsLogic dmsLogic() {
		return applicationContext().getBean(DmsLogic.class);
	}

	protected EmailLogic emailLogic() {
		return applicationContext().getBean(EmailLogic.class);
	}

	protected GISLogic gisLogic() {
		return applicationContext().getBean(GISLogic.class);
	}

	protected LookupLogic lookupLogic() {
		return applicationContext().getBean(LookupLogic.class);
	}

	protected SchedulerLogic schedulerLogic() {
		return applicationContext().getBean(SchedulerLogic.class);
	}

	protected SecurityLogic securityLogic() {
		return applicationContext().getBean(SecurityLogic.class);
	}

	protected ViewLogic viewLogic() {
		return applicationContext().getBean(ViewLogic.class);
	}

	protected WorkflowLogic workflowLogic() {
		return applicationContext().getBean(WorkflowLogic.class);
	}

	/*
	 * Localization
	 */

	protected Localization localization() {
		return new Localization() {

			@Override
			public String get(final String key) {
				return TranslationService.getInstance() //
						.getTranslation(languageStore().getLanguage(), key);
			}
		};
	}

	/*
	 * Web
	 */

	protected RequestListener requestListener() {
		return applicationContext().getBean(RequestListener.class);
	}

	@Deprecated
	protected SessionVars sessionVars() {
		return applicationContext().getBean(SessionVars.class);
	}

}
