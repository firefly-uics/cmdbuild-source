package integration.scheduler.quartz;

import integration.scheduler.utils.ExecutionListenerJob;
import integration.scheduler.utils.JobExecutionProbe;
import integration.scheduler.utils.SelfRemovingJob;

import java.util.Date;

import org.cmdbuild.exception.SchedulerException;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.CMJob;
import org.cmdbuild.services.scheduler.quartz.QuartzScheduler;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.OneTimeTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import utils.async.Poller;

public class QuartzSchedulerTest {

	private static final long POLLING_TIMEOUT = 1000L;
	private static final long POLLING_INTERVAL = 100L;

	private static final int ONCE = 1;
	private static final int THREE_TIMES = 3;
	private static final long IN_THREE_SECONDS = (3 - 1) * 1000L;
	private static final String EVERY_SECOND_CRON_EXPR = "0/1 * * * * ?";

	private SchedulerService scheduler;

	@Before
	public void initScheduler() {
		scheduler = new QuartzScheduler();
	}

	@After
	public void cleanScheduler() {
		scheduler.stop();
	}

	@Test(expected = SchedulerException.class)
	public void quartzForbidsAddingTheSameJobTwice() throws InterruptedException {
		final CMJob nullJob = createNullJob();
		final JobTrigger trigger = new OneTimeTrigger(new Date());
		scheduler.addJob(nullJob, trigger);
		scheduler.addJob(nullJob, trigger);
	}

	@Test()
	public void quartzAllowsRemovalOfUnexistentJob() throws InterruptedException {
		final CMJob nullJob = createNullJob();
		scheduler.removeJob(nullJob);
	}

	@Test
	public void quartzExecutesAnImmediateJob() throws InterruptedException {
		final CMJob nullJob = createNullJob();
		final JobTrigger immediately = new OneTimeTrigger(new Date());
		assertEventually(nullJob, willBeExecuted(immediately));
	}

	@Test
	public void quartzExecutesADeferredJob() throws InterruptedException {
		final CMJob nullJob = createNullJob();
		final JobTrigger timeout = new OneTimeTrigger(afterMillis(POLLING_INTERVAL / 2));
		assertEventually(nullJob, willBeExecutedAfter(timeout));
	}

	@Test
	public void quartzCanHandleMultipleJobs() {
		final JobTrigger timeout = new OneTimeTrigger(farFarAway());
		scheduler.addJob(createNullJob(), timeout);
		scheduler.addJob(createNullJob(), timeout);
	}

	@Test
	public void quartzExecutesARecurringJob() throws InterruptedException {
		final CMJob nullJob = createNullJob();
		final JobTrigger everySecond = new RecurringTrigger(EVERY_SECOND_CRON_EXPR);
		assertEventually(nullJob, willBeExecutedApproximately(everySecond, THREE_TIMES), IN_THREE_SECONDS);
	}

	@Test
	public void quartzRemovesARecurringJob() throws InterruptedException {
		final CMJob nullJob = createSelfRemovingJob();
		final JobTrigger everySecond = new RecurringTrigger(EVERY_SECOND_CRON_EXPR);
		assertEventually(nullJob, willBeExecuted(everySecond, ONCE), IN_THREE_SECONDS);
	}

	/*
	 * Test helpers
	 */

	private ExecutionListenerJob createNullJob() {
		return new ExecutionListenerJob();
	}

	private ExecutionListenerJob createSelfRemovingJob() {
		return new SelfRemovingJob(scheduler);
	}

	public static Date afterMillis(final long millis) {
		final Date now = new Date();
		return new Date(now.getTime() + millis);
	}

	private Date farFarAway() {
		return afterMillis(1000000L);
	}

	private JobExecutionProbe willBeExecuted(final JobTrigger trigger) {
		return JobExecutionProbe.jobWasExecuted(trigger);
	}

	private JobExecutionProbe willBeExecutedAfter(final JobTrigger timeout) {
		return JobExecutionProbe.jobWasExecutedAfter((OneTimeTrigger) timeout);
	}

	private JobExecutionProbe willBeExecuted(final JobTrigger trigger, final int times) {
		return JobExecutionProbe.jobExecutionCounter((RecurringTrigger) trigger, times, times);
	}

	private JobExecutionProbe willBeExecutedApproximately(final JobTrigger trigger, final int times) {
		return JobExecutionProbe.jobExecutionCounter((RecurringTrigger) trigger, times, times + 1);
	}

	private void assertEventually(final CMJob job, final JobExecutionProbe probe) throws InterruptedException {
		assertEventually(job, probe, POLLING_TIMEOUT);
	}

	private void assertEventually(final CMJob job, final JobExecutionProbe probe, final long timeout)
			throws InterruptedException {
		probe.setJob((ExecutionListenerJob) job);
		scheduler.addJob(job, probe.getTrigger());
		scheduler.start();
		new Poller(timeout, POLLING_INTERVAL).check(probe);
		scheduler.stop();
	}
}
