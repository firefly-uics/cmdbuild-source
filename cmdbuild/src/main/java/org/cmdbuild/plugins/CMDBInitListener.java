package org.cmdbuild.plugins;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.SchedulerException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.StartProcessJob;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;
import org.slf4j.Logger;

public class CMDBInitListener implements ServletContextListener {

	private static final Logger logger = Log.CMDBUILD;

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
		setupPatchManager();
		setupSchedulerService();
	}

	private void setupPatchManager() {
		PatchManager.reset();
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
				logger.info("Adding job " + job.getDescription());
				try {
					final StartProcessJob startJob = new StartProcessJob(job.getId());
					startJob.setDetail(job.getDetail());
					startJob.setParams(job.getParams());
					final JobTrigger jobTrigger = new RecurringTrigger(job.getCronExpression());
					scheduler.addJob(startJob, jobTrigger);
				} catch (final SchedulerException e) {
					logger.error("Exception occurred scheduling the job", e);
				}
			}
		} catch (final CMDBException e) {
			logger.warn("Could not load the scheduled jobs: first start or patch not yet applied?");
		}
	}

	private void stopSchedulerService() {
		final SchedulerService scheduler = applicationContext().getBean(SchedulerService.class);
		scheduler.stop();
	}

}
