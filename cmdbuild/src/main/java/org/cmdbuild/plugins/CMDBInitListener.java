package org.cmdbuild.plugins;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.SchedulerException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.StartProcessJob;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;
import org.springframework.context.ApplicationContext;

public class CMDBInitListener implements ServletContextListener {

	private static ApplicationContext applicationContext = applicationContext();

	public interface CmdbuildModuleLoader {
		public void init(ServletContext ctxt) throws Exception;
	}

	@Override
	public void contextDestroyed(final ServletContextEvent ctxt) {
		stopSchedulerService();
	}

	@Override
	public void contextInitialized(final ServletContextEvent evt) {
		loadConfiguration(evt);
		loadPlugins(evt);
		setupPatchManager();
		setupSchedulerService();
	}

	private void loadConfiguration(final ServletContextEvent evt) {
		// we get the fully qualified path to web application
		final String path = evt.getServletContext().getRealPath("/");
		final String properties = path + "WEB-INF/conf/log4j.conf";

		// Next we set the properties for all the servlets and JSP
		// pages in this web application
		PropertyConfigurator.configureAndWatch(properties);

		Log.CMDBUILD.info("Loading common configurations for CMDBuild");

		// load single modules config
		final String[] modulesList = evt.getServletContext().getInitParameter("modules").split(",");
		final Settings settings = Settings.getInstance();
		for (int i = 0; i < modulesList.length; i++) {
			final String module = modulesList[i];
			Log.CMDBUILD.debug("Loading configurations for " + module);
			try {
				settings.load(module, path + "WEB-INF/conf/" + module + ".conf");
			} catch (final Throwable e) {
				Log.CMDBUILD.error("Unable to load configuration file for " + module);
			}
		}
		settings.setRootPath(path);
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
				Log.CMDBUILD.debug("Initialize plugin: " + loader);
				((CmdbuildModuleLoader) Class.forName(basepack + loader).newInstance()).init(evt.getServletContext());
			} catch (final Exception e) {
				Log.CMDBUILD.error("Failed to load '" + loader + "' module!");
				e.printStackTrace();
			}
		}
	}

	private void setupSchedulerService() {
		final SchedulerLogic schedulerLogic = applicationContext.getBean(SchedulerLogic.class);
		final SchedulerService scheduler = applicationContext.getBean(SchedulerService.class);
		scheduler.start();
		if (!DatabaseProperties.getInstance().isConfigured()) {
			return;
		}
		try {
			Log.CMDBUILD.info("Loading scheduled jobs");
			final Iterable<ScheduledJob> scheduledJobs = schedulerLogic.findAllScheduledJobs();
			for (final ScheduledJob job : scheduledJobs) {
				Log.CMDBUILD.info("Adding job " + job.getDescription());
				try {
					final StartProcessJob startJob = new StartProcessJob(job.getId());
					startJob.setDetail(job.getDetail());
					startJob.setParams(job.getParams());
					final JobTrigger jobTrigger = new RecurringTrigger(job.getCronExpression());
					scheduler.addJob(startJob, jobTrigger);
				} catch (final SchedulerException e) {
					Log.CMDBUILD.error("Exception occurred scheduling the job", e);
				}
			}
		} catch (final CMDBException e) {
			Log.CMDBUILD.warn("Could not load the scheduled jobs: first start or patch not yet applied?");
		}
	}

	private void stopSchedulerService() {
		final SchedulerService scheduler = applicationContext.getBean(SchedulerService.class);
		scheduler.stop();
	}
}
