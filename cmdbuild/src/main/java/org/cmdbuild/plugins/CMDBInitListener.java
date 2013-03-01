package org.cmdbuild.plugins;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.PropertyConfigurator;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.SchedulerException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.JobCard;
import org.cmdbuild.services.scheduler.quartz.QuartzScheduler;

public class CMDBInitListener implements ServletContextListener {
	
	public interface CmdbuildModuleLoader {
		public void init(ServletContext ctxt) throws Exception;
	}

	public void contextDestroyed(ServletContextEvent ctxt) {
		stopSchedulerService();
	}

	public void contextInitialized(ServletContextEvent evt) {
        loadConfiguration(evt);
        loadPlugins(evt);
        setupPatchManager();
        setupSchedulerService();
	}

	private void loadConfiguration(ServletContextEvent evt) {
		// we get the fully qualified path to web application
		String path = evt.getServletContext().getRealPath("/");
		String properties = path + "WEB-INF/conf/log4j.conf";
		
		// Next we set the properties for all the servlets and JSP
		// pages in this web application
		PropertyConfigurator.configureAndWatch(properties);
		
		Log.OTHER.info("Loading common configurations for CMDBuild");

        // load single modules config
        String[] modulesList = evt.getServletContext().getInitParameter("modules").split(",");
        Settings settings = Settings.getInstance();
        for(int i=0; i<modulesList.length; i++){
            String module = modulesList[i];
            Log.OTHER.debug("Loading configurations for " + module);
            try {
                settings.load(module, path + "WEB-INF/conf/" + module + ".conf");
            } catch (Throwable e) {
                Log.OTHER.error("Unable to load configuration file for " + module);
            }
        }
        settings.setRootPath(path);
	}

	private void setupPatchManager() {
		PatchManager.reset();
	}

	private void loadPlugins(ServletContextEvent evt) {
		// start all 'configuration loaders'
        String basepack = "org.cmdbuild.plugins.";
        String[] loaders = evt.getServletContext().getInitParameter("moduleLoaders").split(",");
        for(String loader : loaders){
        	try {
        		Log.OTHER.debug("Initialize plugin: " + loader);
				((CmdbuildModuleLoader) Class.forName(basepack + loader).newInstance() ).init(evt.getServletContext());
			} catch (Exception e) {
				Log.OTHER.error("Failed to load '" + loader + "' module!");
				e.printStackTrace();
			}
        }
	}

	private void setupSchedulerService() {
		SchedulerService scheduler = new QuartzScheduler();
        scheduler.start();
        try {
        	Log.OTHER.info("Loading scheduled jobs");
	        for (JobCard jobCard : JobCard.all()) {
	        	Log.OTHER.info("Adding job " + jobCard.getDescription());
	        	try {
	        		scheduler.addJob(jobCard.createJob(), jobCard.createJobTrigger());
	        	} catch (SchedulerException e) {
	        		Log.OTHER.error("Exception occurred scheduling the job", e);
	        	}
	        }
        } catch (CMDBException e) {
        	Log.OTHER.warn("Could not load the scheduled jobs: first start or patch not yet applied?");
        }
	}

	private void stopSchedulerService() {
		SchedulerService scheduler = new QuartzScheduler();
        scheduler.stop();
	}
}
