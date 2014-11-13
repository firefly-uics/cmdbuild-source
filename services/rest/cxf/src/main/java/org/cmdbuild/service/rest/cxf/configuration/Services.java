package org.cmdbuild.service.rest.cxf.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.common.reflect.AnnouncingInvocationHandler;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler.Announceable;
import org.cmdbuild.service.rest.AttachmentsConfiguration;
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
import org.cmdbuild.service.rest.cxf.CxfAttachmentsConfiguration;
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
import org.cmdbuild.service.rest.cxf.CxfSessions.AuthenticationLogicAdapter;
import org.cmdbuild.service.rest.cxf.CxfSessions.LoginHandler;
import org.cmdbuild.service.rest.cxf.ErrorHandler;
import org.cmdbuild.service.rest.cxf.WebApplicationExceptionErrorHandler;
import org.cmdbuild.service.rest.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.cxf.service.RandomTokenGenerator;
import org.cmdbuild.service.rest.cxf.service.SessionStore;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.google.common.base.Supplier;

@Configuration
public class Services implements LoggingSupport {

	@Autowired
	private ApplicationContextHelper helper;

	@Bean
	public AttachmentsConfiguration cxfAttachmentsConfiguration() {
		final CxfAttachmentsConfiguration service = new CxfAttachmentsConfiguration(helper.dmsLogic());
		return proxy(AttachmentsConfiguration.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Cards cxfCards() {
		final CxfCards service = new CxfCards(errorHandler(), helper.userDataAccessLogic(), helper.systemDataView(),
				helper.userDataView());
		return proxy(Cards.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassAttributes cxfClassAttributes() {
		final CxfClassAttributes service = new CxfClassAttributes(errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ClassAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Classes cxfClasses() {
		final CxfClasses service = new CxfClasses(errorHandler(), helper.userDataAccessLogic());
		return proxy(Classes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public DomainAttributes cxfDomainAttributes() {
		final CxfDomainAttributes service = new CxfDomainAttributes(errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(DomainAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Relations cxfRelations() {
		final CxfRelations service = new CxfRelations(errorHandler(), helper.userDataAccessLogic());
		return proxy(Relations.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Domains cxfDomains() {
		final CxfDomains service = new CxfDomains(errorHandler(), helper.userDataAccessLogic());
		return proxy(Domains.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypes cxfLookupTypes() {
		final CxfLookupTypes service = new CxfLookupTypes(errorHandler(), helper.lookupLogic());
		return proxy(LookupTypes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypeValues cxfLookupTypeValues() {
		final CxfLookupTypeValues service = new CxfLookupTypeValues(errorHandler(), helper.lookupLogic());
		return proxy(LookupTypeValues.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Menu cxfMenu() {
		final CxfMenu service = new CxfMenu(currentGroupNameSupplier(), helper.menuLogic(), helper.systemDataView());
		return proxy(Menu.class, service);
	}

	private Supplier<String> currentGroupNameSupplier() {
		return new Supplier<String>() {

			@Override
			public String get() {
				return helper.userStore().getUser().getPreferredGroup().getName();
			}

		};
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessAttributes cxfProcessAttributes() {
		final CxfProcessAttributes service = new CxfProcessAttributes(errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ProcessAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Processes cxfProcesses() {
		final CxfProcesses service = new CxfProcesses(errorHandler(), helper.userWorkflowLogic());
		return proxy(Processes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceActivities cxfProcessInstanceActivities() {
		final CxfProcessInstanceActivities service = new CxfProcessInstanceActivities(errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessInstanceActivities.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstances cxfProcessInstances() {
		final CxfProcessInstances service = new CxfProcessInstances(errorHandler(), helper.userWorkflowLogic());
		return proxy(ProcessInstances.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessStartActivities cxfProcessStartActivities() {
		final CxfProcessStartActivities service = new CxfProcessStartActivities(errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessStartActivities.class, service);
	}

	@Bean
	public Sessions cxfSessions() {
		final CxfSessions service = new CxfSessions(errorHandler(), tokenGenerator(), sessionStore(), loginHandler(),
				operationUserStore());
		return proxy(Sessions.class, service);
	}

	@Bean
	protected LoginHandler loginHandler() {
		return new AuthenticationLogicAdapter(helper.authenticationLogic());
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

}
