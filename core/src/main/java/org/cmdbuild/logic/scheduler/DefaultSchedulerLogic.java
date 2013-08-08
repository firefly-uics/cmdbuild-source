package org.cmdbuild.logic.scheduler;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.logic.scheduler.Constants.CRON_EXP_ATTRIBUTE_NAME;
import static org.cmdbuild.logic.scheduler.Constants.DESCRIPTION_ATTRIBUTE_NAME;
import static org.cmdbuild.logic.scheduler.Constants.DETAIL_ATTRIBUTE_NAME;
import static org.cmdbuild.logic.scheduler.Constants.JOB_TYPE_ATTRIBUTE_NAME;
import static org.cmdbuild.logic.scheduler.Constants.NOTES_ATTRIBUTE_NAME;
import static org.cmdbuild.logic.scheduler.Constants.RUNNING_ATTRIBUTE_NAME;
import static org.cmdbuild.logic.scheduler.Constants.SCHEDULER_CLASS_NAME;

import java.util.List;
import java.util.Map;

import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.exception.SchedulerException;
import org.cmdbuild.logic.data.Utils;
import org.cmdbuild.logic.scheduler.DefaultScheduledJob.ScheduledJobBuilder;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.SchedulerTrigger;
import org.cmdbuild.services.scheduler.StartProcessJob;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultSchedulerLogic implements SchedulerLogic {

	private final CMDataView view;
	private final SchedulerService schedulerService;
	private DatabaseConfiguration databaseConfiguration;

	public DefaultSchedulerLogic( //
			final CMDataView view, //
			final SchedulerService schedulerService, //
			final DatabaseConfiguration databaseConfiguration //
	) {
		this.view = view;
		this.schedulerService = schedulerService;
		this.databaseConfiguration = databaseConfiguration;
	}

	@Override
	public Iterable<ScheduledJob> findAllScheduledJobs() {
		logger.info("finding all scheduled jobs");
		final List<ScheduledJob> scheduledJobs = Lists.newArrayList();
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMQueryResult result = view.select(anyAttribute(schedulerClass)) //
				.from(schedulerClass) //
				.run();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(schedulerClass);
			scheduledJobs.add(createScheduledJobFrom(card));
		}
		return scheduledJobs;
	}

	@Override
	public Iterable<ScheduledJob> findJobsByDetail(final String detail) {
		logger.info("finding all jobs with detail '{}'", detail);
		final List<ScheduledJob> scheduledJobs = Lists.newArrayList();
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMQueryResult result = view.select(anyAttribute(schedulerClass)) //
				.from(schedulerClass) //
				.where(condition(attribute(schedulerClass, DETAIL_ATTRIBUTE_NAME), eq(detail))) //
				.run();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(schedulerClass);
			scheduledJobs.add(createScheduledJobFrom(card));
		}
		return scheduledJobs;
	}

	private DefaultScheduledJob createScheduledJobFrom(final CMCard jobCard) {

		String description = "";
		final Object objDescription = jobCard.get(DESCRIPTION_ATTRIBUTE_NAME);
		if (objDescription != null) {
			description = (String) objDescription;
		}

		final String type = Utils.readString(jobCard, JOB_TYPE_ATTRIBUTE_NAME);
		final boolean running = Utils.readBoolean(jobCard, RUNNING_ATTRIBUTE_NAME);

		final ScheduledJobBuilder jobBuilder = DefaultScheduledJob.newScheduledJob(type, running) //
				.withCronExpression((String) jobCard.get(CRON_EXP_ATTRIBUTE_NAME)) //
				.withDetail((String) jobCard.get(DETAIL_ATTRIBUTE_NAME)) //
				.withId(jobCard.getId()) //
				.withParams(fromStringToParamsMap(jobCard)) //
				.withDescription(description);

		return jobBuilder.build();
	}

	private Map<String, String> fromStringToParamsMap(final CMCard jobCard) {
		final Map<String, String> params = Maps.newHashMap();
		final Object paramBlockObject = jobCard.get(NOTES_ATTRIBUTE_NAME);
		if (paramBlockObject != null) {
			final String paramBlock = (String) paramBlockObject;
			for (final String paramLine : paramBlock.split("\n")) {
				final String[] paramArray = paramLine.split("=", 2);
				if (paramArray.length == 2) {
					params.put(paramArray[0], paramArray[1]);
				}
			}
		}
		return params;
	}

	@Override
	public ScheduledJob findJobById(final Long jobId) {
		logger.info("finding job with id '{}'", jobId);
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMCard fetchedCard = view.select(anyAttribute(schedulerClass)) //
				.from(schedulerClass) //
				.where(condition(attribute(schedulerClass, "Id"), eq(jobId))) //
				.run().getOnlyRow().getCard(schedulerClass);
		return createScheduledJobFrom(fetchedCard);
	}

	@Override
	@Transactional
	public ScheduledJob createAndStart(final ScheduledJob scheduledJob) {
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMCardDefinition jobToCreate = view.createCardFor(schedulerClass);
		final CMCard createdJobCard = jobToCreate //
				.set(DETAIL_ATTRIBUTE_NAME, scheduledJob.getDetail()) //
				.setDescription(scheduledJob.getDescription()) //
				.set(CRON_EXP_ATTRIBUTE_NAME, scheduledJob.getCronExpression()) //
				.set(NOTES_ATTRIBUTE_NAME, fromParamsMapToString(scheduledJob.getParams())) //
				.set(JOB_TYPE_ATTRIBUTE_NAME, scheduledJob.getJobType().toString()) //
				.set(RUNNING_ATTRIBUTE_NAME, scheduledJob.isRunning()) //
				.save();

		final ScheduledJob createdScheduledJob = createScheduledJobFrom(createdJobCard);
		addJobToSchedulerService(createdScheduledJob);
		return createdScheduledJob;
	}

	private String fromParamsMapToString(final Map<String, String> params) {
		final StringBuilder paramBlock = new StringBuilder();
		if (params != null) {
			for (final String key : params.keySet()) {
				final String value = params.get(key);
				if (value == null || value.contains("\n")) {
					throw ORMExceptionType.ORM_TYPE_ERROR.createException();
				}
				paramBlock.append(key).append("=").append(value).append("\n");
			}
		}
		return paramBlock.toString();
	}

	private void addJobToSchedulerService(final ScheduledJob scheduledJob) {
		final SchedulerJob job = CMJobFactory.from(scheduledJob);
		final SchedulerTrigger jobTrigger = new RecurringTrigger(scheduledJob.getCronExpression());
		schedulerService.addJob(job, jobTrigger);
	}

	@Override
	@Transactional
	public void update(final ScheduledJob jobToUpdate) {
		logger.info("updating job '{}'", jobToUpdate.getId());
		schedulerService.removeJob(new StartProcessJob(jobToUpdate.getId()));
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMCard cardToUpdate = view.select(anyAttribute(schedulerClass)) //
				.from(schedulerClass) //
				.where(condition(attribute(schedulerClass, "Id"), eq(jobToUpdate.getId()))) //
				.run().getOnlyRow().getCard(schedulerClass);
		final CMCardDefinition mutableCard = view.update(cardToUpdate);
		mutableCard.set(CRON_EXP_ATTRIBUTE_NAME, jobToUpdate.getCronExpression()) //
				.setDescription(jobToUpdate.getDescription()) //
				.set(NOTES_ATTRIBUTE_NAME, fromParamsMapToString(jobToUpdate.getParams())) //
				.save();
		addJobToSchedulerService(jobToUpdate);
	}

	@Override
	@Transactional
	public void delete(final Long jobId) {
		logger.info("deleting job '{}'", jobId);
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMCard cardToDelete = view.select(anyAttribute(schedulerClass)) //
				.from(schedulerClass) //
				.where(condition(attribute(schedulerClass, "Id"), eq(jobId))) //
				.run().getOnlyRow().getCard(schedulerClass);
		view.delete(cardToDelete);
		schedulerService.removeJob(new StartProcessJob(jobId));
	}

	@Override
	public void startScheduler() {
		logger.info("starting scheduler");
		schedulerService.start();
	}

	@Override
	public void stopScheduler() {
		logger.info("stopping scheduler");
		schedulerService.stop();
	}

	@Override
	public void addAllScheduledJobs() {
		logger.info("adding all scheduled jobs");

		if (!databaseConfiguration.isConfigured()) {
			logger.warn("database not configured");
			return;
		}

		try {
			final Iterable<ScheduledJob> scheduledJobs = findAllScheduledJobs();
			for (final ScheduledJob job : scheduledJobs) {

				if (!job.isRunning()) {
					continue;
				}

				try {
					final SchedulerJob theJob = CMJobFactory.from(job);
					if (theJob != null) {
						final SchedulerTrigger jobTrigger = new RecurringTrigger(job.getCronExpression());
						schedulerService.addJob(theJob, jobTrigger);
					}
				} catch (final SchedulerException e) {
					logger.error("Exception occurred scheduling the job", e);
				}
			}
		} catch (final CMDBException e) {
			logger.warn("could not load scheduled jobs: first start or patch not yet applied?");
		}
	}

}
