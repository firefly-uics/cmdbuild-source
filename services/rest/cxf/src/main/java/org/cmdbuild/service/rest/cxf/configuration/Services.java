package org.cmdbuild.service.rest.cxf.configuration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.common.reflect.AnnouncingInvocationHandler;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler.Announceable;
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
import org.cmdbuild.service.rest.cxf.service.InMemoryTokenStore;
import org.cmdbuild.service.rest.cxf.service.RandomTokenGenerator;
import org.cmdbuild.service.rest.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.cxf.service.TokenStore;
import org.cmdbuild.service.rest.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.reflect.Reflection;

@Configuration
public class Services implements LoggingSupport {

	@Autowired
	private Core core;

	@Bean
	protected ErrorHandler errorHandler() {
		return new WebApplicationExceptionErrorHandler();
	}

	@Bean
	public Cards cxfCards() {
		final CxfCards service = new CxfCards(errorHandler(), core.userDataAccessLogic(), core.systemDataView(),
				core.userDataView());
		return proxy(Cards.class, service);
	}

	@Bean
	public ClassAttributes cxfClassAttributes() {
		final CxfClassAttributes service = new CxfClassAttributes(errorHandler(), core.userDataAccessLogic(),
				core.systemDataView(), core.metadataStoreFactory(), core.lookupLogic());
		return proxy(ClassAttributes.class, service);
	}

	@Bean
	public Classes cxfClasses() {
		final CxfClasses service = new CxfClasses(errorHandler(), core.userDataAccessLogic());
		return proxy(Classes.class, service);
	}

	@Bean
	public DomainAttributes cxfDomainAttributes() {
		final CxfDomainAttributes service = new CxfDomainAttributes(errorHandler(), core.userDataAccessLogic(),
				core.systemDataView(), core.metadataStoreFactory(), core.lookupLogic());
		return proxy(DomainAttributes.class, service);
	}

	@Bean
	public Relations cxfRelations() {
		final CxfRelations service = new CxfRelations(errorHandler(), core.userDataAccessLogic());
		return proxy(Relations.class, service);
	}

	@Bean
	public Domains cxfDomains() {
		final CxfDomains service = new CxfDomains(errorHandler(), core.userDataAccessLogic());
		return proxy(Domains.class, service);
	}

	@Bean
	public LookupTypes cxfLookupTypes() {
		final CxfLookupTypes service = new CxfLookupTypes(errorHandler(), core.lookupLogic());
		return proxy(LookupTypes.class, service);
	}

	@Bean
	public LookupTypeValues cxfLookupTypeValues() {
		final CxfLookupTypeValues service = new CxfLookupTypeValues(errorHandler(), core.lookupLogic());
		return proxy(LookupTypeValues.class, service);
	}

	@Bean
	public Menu cxfMenu() {
		final CxfMenu service = new CxfMenu(core.currentGroupNameSupplier(), core.menuLogic(), core.systemDataView());
		return proxy(Menu.class, service);
	}

	@Bean
	public ProcessAttributes cxfProcessAttributes() {
		final CxfProcessAttributes service = new CxfProcessAttributes(errorHandler(), core.userDataAccessLogic(),
				core.systemDataView(), core.metadataStoreFactory(), core.lookupLogic());
		return proxy(ProcessAttributes.class, service);
	}

	@Bean
	public Processes cxfProcesses() {
		final CxfProcesses service = new CxfProcesses(errorHandler(), core.userWorkflowLogic());
		return proxy(Processes.class, service);
	}

	@Bean
	public ProcessInstanceActivities cxfProcessInstanceActivities() {
		final CxfProcessInstanceActivities service = new CxfProcessInstanceActivities(errorHandler(),
				core.userWorkflowLogic());
		return proxy(ProcessInstanceActivities.class, service);
	}

	@Bean
	public ProcessInstances cxfProcessInstances() {
		final CxfProcessInstances service = new CxfProcessInstances(errorHandler(), core.userWorkflowLogic());
		return proxy(ProcessInstances.class, service);
	}

	@Bean
	public ProcessStartActivities cxfProcessStartActivities() {
		final CxfProcessStartActivities service = new CxfProcessStartActivities(errorHandler(),
				core.userWorkflowLogic());
		return proxy(ProcessStartActivities.class, service);
	}

	@Bean
	public Sessions cxfSessions() {
		final CxfSessions service = new CxfSessions(errorHandler(), tokenGenerator(), tokenStore());
		return proxy(Sessions.class, service);
	}

	@Bean
	protected TokenGenerator tokenGenerator() {
		return new RandomTokenGenerator();
	}

	@Bean
	public TokenStore tokenStore() {
		return new InMemoryTokenStore();
	}

	private <T> T proxy(final Class<T> type, final T service) {
		final InvocationHandler serviceWithAnnounces = AnnouncingInvocationHandler.of(service, announceable());
		return Reflection.newProxy(type, serviceWithAnnounces);
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

}
