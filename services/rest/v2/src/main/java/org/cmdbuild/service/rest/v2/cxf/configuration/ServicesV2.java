package org.cmdbuild.service.rest.v2.cxf.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static org.springframework.context.annotation.ScopedProxyMode.TARGET_CLASS;
import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler;
import org.cmdbuild.common.reflect.AnnouncingInvocationHandler.Announceable;
import org.cmdbuild.service.rest.v2.AttachmentsConfiguration;
import org.cmdbuild.service.rest.v2.Cards;
import org.cmdbuild.service.rest.v2.ClassAttributes;
import org.cmdbuild.service.rest.v2.ClassPrivileges;
import org.cmdbuild.service.rest.v2.Classes;
import org.cmdbuild.service.rest.v2.DomainAttributes;
import org.cmdbuild.service.rest.v2.Domains;
import org.cmdbuild.service.rest.v2.Impersonate;
import org.cmdbuild.service.rest.v2.LookupTypeValues;
import org.cmdbuild.service.rest.v2.LookupTypes;
import org.cmdbuild.service.rest.v2.Menu;
import org.cmdbuild.service.rest.v2.ProcessAttributes;
import org.cmdbuild.service.rest.v2.ProcessInstanceActivities;
import org.cmdbuild.service.rest.v2.ProcessInstances;
import org.cmdbuild.service.rest.v2.ProcessStartActivities;
import org.cmdbuild.service.rest.v2.Processes;
import org.cmdbuild.service.rest.v2.ProcessesConfiguration;
import org.cmdbuild.service.rest.v2.Relations;
import org.cmdbuild.service.rest.v2.Sessions;
import org.cmdbuild.service.rest.v2.cxf.AllInOneCardAttachments;
import org.cmdbuild.service.rest.v2.cxf.AllInOneProcessInstanceAttachments;
import org.cmdbuild.service.rest.v2.cxf.AttachmentsHelper;
import org.cmdbuild.service.rest.v2.cxf.AttachmentsManagement;
import org.cmdbuild.service.rest.v2.cxf.CxfAttachmentsConfiguration;
import org.cmdbuild.service.rest.v2.cxf.CxfCardAttachments;
import org.cmdbuild.service.rest.v2.cxf.CxfCards;
import org.cmdbuild.service.rest.v2.cxf.CxfClassAttributes;
import org.cmdbuild.service.rest.v2.cxf.CxfClassPrivileges;
import org.cmdbuild.service.rest.v2.cxf.CxfClasses;
import org.cmdbuild.service.rest.v2.cxf.CxfDomainAttributes;
import org.cmdbuild.service.rest.v2.cxf.CxfDomains;
import org.cmdbuild.service.rest.v2.cxf.CxfImpersonate;
import org.cmdbuild.service.rest.v2.cxf.CxfLookupTypeValues;
import org.cmdbuild.service.rest.v2.cxf.CxfLookupTypes;
import org.cmdbuild.service.rest.v2.cxf.CxfMenu;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessAttributes;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceActivities;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstanceAttachments;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessInstances;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessStartActivities;
import org.cmdbuild.service.rest.v2.cxf.CxfProcesses;
import org.cmdbuild.service.rest.v2.cxf.CxfProcessesConfiguration;
import org.cmdbuild.service.rest.v2.cxf.CxfRelations;
import org.cmdbuild.service.rest.v2.cxf.CxfSessions;
import org.cmdbuild.service.rest.v2.cxf.CxfSessions.AuthenticationLogicAdapter;
import org.cmdbuild.service.rest.v2.cxf.CxfSessions.LoginHandler;
import org.cmdbuild.service.rest.v2.cxf.DefaultEncoding;
import org.cmdbuild.service.rest.v2.cxf.DefaultProcessStatusHelper;
import org.cmdbuild.service.rest.v2.cxf.ErrorHandler;
import org.cmdbuild.service.rest.v2.cxf.ProcessStatusHelper;
import org.cmdbuild.service.rest.v2.cxf.TranslatingAttachmentsHelper;
import org.cmdbuild.service.rest.v2.cxf.TranslatingAttachmentsHelper.Encoding;
import org.cmdbuild.service.rest.v2.cxf.WebApplicationExceptionErrorHandler;
import org.cmdbuild.service.rest.v2.cxf.service.InMemoryOperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.InMemorySessionStore;
import org.cmdbuild.service.rest.v2.cxf.service.OperationUserStore;
import org.cmdbuild.service.rest.v2.cxf.service.SessionStore;
import org.cmdbuild.service.rest.v2.cxf.service.SimpleTokenGenerator;
import org.cmdbuild.service.rest.v2.cxf.service.TokenGenerator;
import org.cmdbuild.service.rest.v2.logging.LoggingSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

@Component
public class ServicesV2 implements LoggingSupport {

	@Autowired
	private ApplicationContextHelperV2 helper;

	@Bean
	public AttachmentsConfiguration v2_attachmentsConfiguration() {
		final CxfAttachmentsConfiguration service = new CxfAttachmentsConfiguration(helper.dmsLogic());
		return proxy(AttachmentsConfiguration.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public AllInOneCardAttachments v2_cardAttachments() {
		final CxfCardAttachments service = new CxfCardAttachments(v2_errorHandler(), helper.systemDataAccessLogic(),
				v2_attachmentsHelper());
		return proxy(AllInOneCardAttachments.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Cards v2_cards() {
		final CxfCards service = new CxfCards(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Cards.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassAttributes v2_classAttributes() {
		final CxfClassAttributes service = new CxfClassAttributes(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ClassAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ClassPrivileges v2_classPrivileges() {
		final CxfClassPrivileges service = new CxfClassPrivileges(v2_errorHandler(), helper.authenticationLogic(),
				helper.securityLogic(), helper.userDataAccessLogic());
		return proxy(ClassPrivileges.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Classes v2_classes() {
		final CxfClasses service = new CxfClasses(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Classes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public DomainAttributes v2_domainAttributes() {
		final CxfDomainAttributes service = new CxfDomainAttributes(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(DomainAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Relations v2_relations() {
		final CxfRelations service = new CxfRelations(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Relations.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Domains v2_domains() {
		final CxfDomains service = new CxfDomains(v2_errorHandler(), helper.userDataAccessLogic());
		return proxy(Domains.class, service);
	}

	@Bean
	public Impersonate v2_impersonate() {
		final CxfImpersonate service = new CxfImpersonate(v2_errorHandler(), v2_loginHandler(), v2_sessionStore(),
				v2_impersonateSessionStore(), v2_operationUserStore(), v2_operationUserAllowed());
		return proxy(Impersonate.class, service);
	}

	@Bean
	protected Predicate<OperationUser> v2_operationUserAllowed() {
		return new Predicate<OperationUser>() {

			@Override
			public boolean apply(final OperationUser input) {
				final AuthenticatedUser authenticatedUser = input.getAuthenticatedUser();
				return input.hasAdministratorPrivileges() || authenticatedUser.isService()
						|| authenticatedUser.isPrivileged();
			}

		};
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypes v2_lookupTypes() {
		final CxfLookupTypes service = new CxfLookupTypes(v2_errorHandler(), helper.lookupLogic());
		return proxy(LookupTypes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public LookupTypeValues v2_lookupTypeValues() {
		final CxfLookupTypeValues service = new CxfLookupTypeValues(v2_errorHandler(), helper.lookupLogic());
		return proxy(LookupTypeValues.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Menu v2_menu() {
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
	public ProcessAttributes v2_processAttributes() {
		final CxfProcessAttributes service = new CxfProcessAttributes(v2_errorHandler(), helper.userDataAccessLogic(),
				helper.systemDataView(), helper.metadataStoreFactory(), helper.lookupLogic());
		return proxy(ProcessAttributes.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public Processes v2_processes() {
		final CxfProcesses service = new CxfProcesses(v2_errorHandler(), helper.userWorkflowLogic(),
				v2_processStatusHelper());
		return proxy(Processes.class, service);
	}

	@Bean
	public ProcessesConfiguration v2_processesConfiguration() {
		final CxfProcessesConfiguration service = new CxfProcessesConfiguration(v2_processStatusHelper());
		return proxy(ProcessesConfiguration.class, service);
	}

	@Bean
	protected ProcessStatusHelper v2_processStatusHelper() {
		return new DefaultProcessStatusHelper(helper.lookupHelper());
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstanceActivities v2_processInstanceActivities() {
		final CxfProcessInstanceActivities service = new CxfProcessInstanceActivities(v2_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessInstanceActivities.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public AllInOneProcessInstanceAttachments v2_processInstanceAttachments() {
		final CxfProcessInstanceAttachments service = new CxfProcessInstanceAttachments(v2_errorHandler(),
				helper.userWorkflowLogic(), v2_attachmentsHelper());
		return proxy(AllInOneProcessInstanceAttachments.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessInstances v2_processInstances() {
		final CxfProcessInstances service = new CxfProcessInstances(v2_errorHandler(), helper.userWorkflowLogic(),
				helper.lookupHelper());
		return proxy(ProcessInstances.class, service);
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	public ProcessStartActivities v2_processStartActivities() {
		final CxfProcessStartActivities service = new CxfProcessStartActivities(v2_errorHandler(),
				helper.userWorkflowLogic());
		return proxy(ProcessStartActivities.class, service);
	}

	@Bean
	public Sessions v2_sessions() {
		final CxfSessions service = new CxfSessions(v2_errorHandler(), v2_tokenGenerator(), v2_sessionStore(),
				v2_loginHandler(), v2_operationUserStore());
		return proxy(Sessions.class, service);
	}

	@Bean
	protected LoginHandler v2_loginHandler() {
		return new AuthenticationLogicAdapter(helper.authenticationLogic());
	}

	@Bean
	protected TokenGenerator v2_tokenGenerator() {
		return new SimpleTokenGenerator();
	}

	@Bean
	public SessionStore v2_sessionStore() {
		return new InMemorySessionStore();
	}

	@Bean
	protected SessionStore v2_impersonateSessionStore() {
		return new InMemorySessionStore();
	}

	@Bean
	public OperationUserStore v2_operationUserStore() {
		return new InMemoryOperationUserStore();
	}

	@Bean
	@Scope(value = SCOPE_REQUEST, proxyMode = TARGET_CLASS)
	protected AttachmentsHelper v2_attachmentsHelper() {
		return new TranslatingAttachmentsHelper(new AttachmentsManagement(helper.dmsLogic(), helper.userStore()),
				v2_encoding());
	}

	@Bean
	protected Encoding v2_encoding() {
		return new DefaultEncoding();
	}

	private <T> T proxy(final Class<T> type, final T service) {
		final InvocationHandler serviceWithAnnounces = AnnouncingInvocationHandler.of(service, v2_announceable());
		return newProxy(type, serviceWithAnnounces);
	}

	@Bean
	protected Announceable v2_announceable() {
		return new LoggingAnnounceable();
	}

	private static final class LoggingAnnounceable implements Announceable {

		@Override
		public void announce(final Method method, final Object[] args) {
			logger.info("invoking method '{}' with arguments '{}'", method, args);
		}

	}

	@Bean
	protected ErrorHandler v2_errorHandler() {
		return new WebApplicationExceptionErrorHandler();
	}

}
