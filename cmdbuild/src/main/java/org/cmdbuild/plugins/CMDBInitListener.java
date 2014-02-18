package org.cmdbuild.plugins;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.DocumentSearch;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.SchedulerException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.scheduler.CMJobFactory;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.CMJob;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;
import org.slf4j.Logger;

public class CMDBInitListener implements ServletContextListener {

	private static final Logger logger = Log.CMDBUILD;

	private static final Iterable<String> ROOT = Collections.emptyList();

	public interface CmdbuildModuleLoader {
		public void init(ServletContext ctxt) throws Exception;
	}

	@Override
	public void contextDestroyed(final ServletContextEvent ctxt) {
		stopSchedulerService();
	}

	@Override
	public void contextInitialized(final ServletContextEvent evt) {
		loadPlugins(evt);
		setupSchedulerService();
		clearDmsTemporary();
	}

	private void loadPlugins(final ServletContextEvent evt) {
		// start all 'configuration loaders'
		final String basepack = "org.cmdbuild.plugins.";
		final String[] loaders = evt.getServletContext().getInitParameter("moduleLoaders").split(",");
		for (final String loader : loaders) {
			try {
				logger.debug("Initialize plugin: " + loader);
				((CmdbuildModuleLoader) Class.forName(basepack + loader).newInstance()).init(evt.getServletContext());
			} catch (final Exception e) {
				logger.error("Failed to load '" + loader + "' module!");
				e.printStackTrace();
			}
		}
	}

	private void setupSchedulerService() {
		final SchedulerLogic schedulerLogic = applicationContext().getBean(SchedulerLogic.class);
		final SchedulerService scheduler = applicationContext().getBean(SchedulerService.class);
		scheduler.start();

		if (!DatabaseProperties.getInstance().isConfigured()) {
			return;
		}

		try {
			logger.info("Loading scheduled jobs");
			final Iterable<ScheduledJob> scheduledJobs = schedulerLogic.findAllScheduledJobs();
			for (final ScheduledJob job : scheduledJobs) {

				if (!job.isRunning()) {
					continue;
				}

				try {
					final CMJob theJob = CMJobFactory.from(job);
					if (theJob != null) {
						final JobTrigger jobTrigger = new RecurringTrigger(job.getCronExpression());
						scheduler.addJob(theJob, jobTrigger);
					}
				} catch (final SchedulerException e) {
					logger.error("Exception occurred scheduling the job", e);
				}
			}

		} catch (final CMDBException e) {
			logger.warn("Could not load the scheduled jobs: first start or patch not yet applied?");
		}
	}

	private void clearDmsTemporary() {
		try {
			logger.info("clearing DMS temporary");

			/*
			 * we need to call it now, even if not used, because DmsService will
			 * be configured when injected inside DmsLogic
			 */
			applicationContext().getBean(DmsLogic.class);

			final DmsConfiguration dmsConfiguration = applicationContext().getBean(DmsConfiguration.class);
			if (dmsConfiguration.isEnabled()) {
				final DmsService dmsService = applicationContext().getBean("dmsService", DmsService.class);
				final DocumentCreatorFactory documentCreatorFactory = applicationContext().getBean(
						DocumentCreatorFactory.class);
				final DocumentSearch all = documentCreatorFactory.createTemporary(ROOT) //
						.createDocumentSearch(null, null);
				dmsService.delete(all);
			}
		} catch (final Throwable e) {
			logger.warn("error clearing DMS temporary", e);
		}
	}

	private void stopSchedulerService() {
		final SchedulerService scheduler = applicationContext().getBean(SchedulerService.class);
		scheduler.stop();
	}

}
