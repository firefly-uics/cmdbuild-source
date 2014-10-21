package org.cmdbuild.service.rest.cxf.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler.Announceable;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.workflow.UserWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.Cards;
import org.cmdbuild.service.rest.ClassAttributes;
import org.cmdbuild.service.rest.Classes;
import org.cmdbuild.service.rest.DomainAttributes;
import org.cmdbuild.service.rest.Domains;
import org.cmdbuild.service.rest.LookupTypeValues;
import org.cmdbuild.service.rest.LookupTypes;
import org.cmdbuild.service.rest.Menu;
import org.cmdbuild.service.rest.ProcessAttributes;
import org.cmdbuild.service.rest.ProcessInstanceActivities;
import org.cmdbuild.service.rest.ProcessInstances;
import org.cmdbuild.service.rest.ProcessStartActivities;
import org.cmdbuild.service.rest.Processes;
import org.cmdbuild.service.rest.Relations;
import org.cmdbuild.service.rest.Sessions;
import org.cmdbuild.service.rest.cxf.CxfCards;
import org.cmdbuild.service.rest.cxf.CxfClassAttributes;
import org.cmdbuild.service.rest.cxf.CxfClasses;
import org.cmdbuild.service.rest.cxf.CxfDomainAttributes;
import org.cmdbuild.service.rest.cxf.CxfDomains;
import org.cmdbuild.service.rest.cxf.CxfLookupTypeValues;
import org.cmdbuild.service.rest.cxf.CxfLookupTypes;
import org.cmdbuild.service.rest.cxf.CxfMenu;
import org.cmdbuild.service.rest.cxf.CxfProcessAttributes;
import org.cmdbuild.service.rest.cxf.CxfProcessInstanceActivities;
import org.cmdbuild.service.rest.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.cxf.CxfProcessStartActivities;
import org.cmdbuild.service.rest.cxf.CxfProcesses;
import org.cmdbuild.service.rest.cxf.CxfRelations;
import org.cmdbuild.service.rest.cxf.CxfSessions;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.cxf.WebApplicationExceptionErrorHandler;
import org.cmdbuild.service.rest.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.cxf.service.RandomTokenGenerator;
import org.cmdbuild.service.rest.cxf.service.SessionStore;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Supplier;

@Configuration
public class Services implements LoggingSupport {

	@Autowired
	private ApplicationContext applicationContext;

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Cards cxfCards() {
		final CxfCards service = new CxfCards(errorHandler(), userDataAccessLogic(), systemDataView(), userDataView());
		return proxy(Cards.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassAttributes cxfClassAttributes() {
		final CxfClassAttributes service = new CxfClassAttributes(errorHandler(), userDataAccessLogic(),
				systemDataView(), metadataStoreFactory(), lookupLogic());
		return proxy(ClassAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Classes cxfClasses() {
		final CxfClasses service = new CxfClasses(errorHandler(), userDataAccessLogic());
		return proxy(Classes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public DomainAttributes cxfDomainAttributes() {
		final CxfDomainAttributes service = new CxfDomainAttributes(errorHandler(), userDataAccessLogic(),
				systemDataView(), metadataStoreFactory(), lookupLogic());
		return proxy(DomainAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Relations cxfRelations() {
		final CxfRelations service = new CxfRelations(errorHandler(), userDataAccessLogic());
		return proxy(Relations.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Domains cxfDomains() {
		final CxfDomains service = new CxfDomains(errorHandler(), userDataAccessLogic());
		return proxy(Domains.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypes cxfLookupTypes() {
		final CxfLookupTypes service = new CxfLookupTypes(errorHandler(), lookupLogic());
		return proxy(LookupTypes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypeValues cxfLookupTypeValues() {
		final CxfLookupTypeValues service = new CxfLookupTypeValues(errorHandler(), lookupLogic());
		return proxy(LookupTypeValues.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Menu cxfMenu() {
		final CxfMenu service = new CxfMenu(currentGroupNameSupplier(), menuLogic(), systemDataView());
		return proxy(Menu.class, service);
	}

	private Supplier<String> currentGroupNameSupplier() {
		return new Supplier<String>() {

			@Override
			public String get() {
				return applicationContext.getBean(UserStore.class).getUser().getPreferredGroup().getName();
			}

		};
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessAttributes cxfProcessAttributes() {
		final CxfProcessAttributes service = new CxfProcessAttributes(errorHandler(), userDataAccessLogic(),
				systemDataView(), metadataStoreFactory(), lookupLogic());
		return proxy(ProcessAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Processes cxfProcesses() {
		final CxfProcesses service = new CxfProcesses(errorHandler(), userWorkflowLogic());
		return proxy(Processes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceActivities cxfProcessInstanceActivities() {
		final CxfProcessInstanceActivities service = new CxfProcessInstanceActivities(errorHandler(),
				userWorkflowLogic());
		return proxy(ProcessInstanceActivities.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstances cxfProcessInstances() {
		final CxfProcessInstances service = new CxfProcessInstances(errorHandler(), userWorkflowLogic());
		return proxy(ProcessInstances.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessStartActivities cxfProcessStartActivities() {
		final CxfProcessStartActivities service = new CxfProcessStartActivities(errorHandler(), userWorkflowLogic());
		return proxy(ProcessStartActivities.class, service);
	}

	@Bean
	public Sessions cxfSessions() {
		final CxfSessions service = new CxfSessions(errorHandler(), tokenGenerator(), sessionStore(),
				authenticationLogic(), operationUserStore());
		return proxy(Sessions.class, service);
	}

	@Bean
	protected TokenGenerator tokenGenerator() {
		return new RandomTokenGenerator();
	}

	@Bean
	public SessionStore sessionStore() {
		return new InMemorySessionStore();
	}

	@Bean
	public OperationUserStore operationUserStore() {
		return new InMemoryOperationUserStore();
	}

	private <T> T proxy(final Class<T> type, final T service) {
		final InvocationHandler serviceWithAnnounces = AnnouncingInvocationHandler.of(service, announceable());
		return newProxy(type, serviceWithAnnounces);
	}

	@Bean
	protected Announceable announceable() {
		return new LoggingAnnounceable();
	}

	private static final class LoggingAnnounceable implements Announceable {

		@Override
		public void announce(final Method method, final Object[] args) {
			logger.info("invoking method '{}' with arguments '{}'", method, args);
		}

	}

	@Bean
	protected ErrorHandler errorHandler() {
		return new WebApplicationExceptionErrorHandler();
	}

	private AuthenticationLogic authenticationLogic() {
		return applicationContext.getBean(SoapAuthenticationLogicBuilder.class).build();
	}

	private LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	private MenuLogic menuLogic() {
		return applicationContext.getBean(MenuLogic.class);
	}

	private MetadataStoreFactory metadataStoreFactory() {
		return applicationContext.getBean(MetadataStoreFactory.class);
	}

	private CMDataView systemDataView() {
		return applicationContext.getBean(DBDataView.class);
	}

	private DataAccessLogic userDataAccessLogic() {
		return applicationContext.getBean(UserDataAccessLogicBuilder.class).build();
	}

	private CMDataView userDataView() {
		return applicationContext.getBean("UserDataView", CMDataView.class);
	}

	private WorkflowLogic userWorkflowLogic() {
		return applicationContext.getBean(UserWorkflowLogicBuilder.class).build();
	}

}
