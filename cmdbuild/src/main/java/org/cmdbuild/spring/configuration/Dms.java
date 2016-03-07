package org.cmdbuild.spring.configuration;

import static com.google.common.reflect.Reflection.newProxy;
import static java.util.Optional.ofNullable;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.common.collect.ChainablePutMap;
import org.cmdbuild.dms.CachedDmsService;
import org.cmdbuild.dms.DefaultDocumentCreatorFactory;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.ForwardingDmsService;
import org.cmdbuild.dms.LoggedDmsService;
import org.cmdbuild.dms.alfresco.AlfrescoDmsService;
import org.cmdbuild.dms.cmis.CmisDmsService;
import org.cmdbuild.logic.dms.DefaultDmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic.DefaultDmsPrivileges;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic.DmsPrivileges;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Dms {

	private static final class MappedDmsService extends ForwardingDmsService {

		private static final DmsService UNSUPPORTED = newProxy(DmsService.class, unsupported("unsupported"));

		private final DmsConfiguration configuration;
		private final Map<String, DmsService> delegates;

		public MappedDmsService(final DmsConfiguration configuration, final Map<String, DmsService> delegates) {
			this.configuration = configuration;
			this.delegates = delegates;
		}

		@Override
		protected DmsService delegate() {
			final String value = configuration.getService();
			return ofNullable(delegates.get(value)).orElse(UNSUPPORTED);
		}

	}

	@Autowired
	private Data data;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private Properties properties;

	@Bean
	@Qualifier(DEFAULT)
	public DmsService dmsService() {
		return new CachedDmsService(loggedDmsService());
	}

	@Bean
	protected DmsService loggedDmsService() {
		return new LoggedDmsService(mappedDmsService());
	}

	@Bean
	protected DmsService mappedDmsService() {
		return new MappedDmsService(properties.dmsProperties(), //
				ChainablePutMap.of(new HashMap<String, DmsService>()) //
						.chainablePut("alfresco", alfrescoDmsService()) //
						.chainablePut("cmis", cmisDmsService()));
	}

	@Bean
	protected DmsService alfrescoDmsService() {
		return new AlfrescoDmsService(properties.dmsProperties());
	}

	@Bean
	protected DmsService cmisDmsService() {
		return new CmisDmsService(properties.dmsProperties());
	}

	@Bean
	public DocumentCreatorFactory documentCreatorFactory() {
		return new DefaultDocumentCreatorFactory();
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(DEFAULT)
	public PrivilegedDmsLogic privilegedDmsLogic() {
		return new PrivilegedDmsLogic( //
				defaultDmsLogic(), //
				defaultDmsPrivileges() //
		);
	}

	@Bean
	@Scope(PROTOTYPE)
	public DmsPrivileges defaultDmsPrivileges() {
		return new DefaultDmsPrivileges(data.systemDataView(), privilegeManagement.userPrivilegeContext());
	}

	@Bean
	public DefaultDmsLogic defaultDmsLogic() {
		return new DefaultDmsLogic( //
				dmsService(), //
				data.systemDataView(), //
				properties.dmsProperties(), //
				documentCreatorFactory(), //
				data.lookupStore() //
		);
	}

}
