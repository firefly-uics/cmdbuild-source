package org.cmdbuild.spring.configuration;

import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.config.DmsProperties;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.config.EmailProperties;
import org.cmdbuild.config.GisConfiguration;
import org.cmdbuild.config.GisProperties;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.data.access.lock.DefaultLockCardConfiguration;
import org.cmdbuild.logic.data.access.lock.LockCardManager.LockCardConfiguration;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Properties {

	@Bean
	protected AuthProperties authConf() {
		return AuthProperties.getInstance();
	}

	@Bean
	public CmdbuildConfiguration cmdbuildProperties() {
		return CmdbuildProperties.getInstance();
	}

	@Bean
	public DatabaseConfiguration databaseProperties() {
		return DatabaseProperties.getInstance();
	}

	@Bean
	public DmsConfiguration dmsProperties() {
		return DmsProperties.getInstance();
	}

	@Bean
	public EmailConfiguration emailProperties() {
		return EmailProperties.getInstance();
	}

	@Bean
	public GisConfiguration gisProperties() {
		return GisProperties.getInstance();
	}

	@Bean
	public GraphProperties graphProperties() {
		return GraphProperties.getInstance();
	}

	@Bean
	public LockCardConfiguration lockCardConfiguration() {
		return new DefaultLockCardConfiguration(cmdbuildProperties());
	}

	@Bean
	public WorkflowConfiguration workflowProperties() {
		return WorkflowProperties.getInstance();
	}

}
