package org.cmdbuild.logic.scheduler;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.logic.scheduler.Constants.*;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logic.data.Utils;
import org.cmdbuild.logic.scheduler.DefaultScheduledJob.ScheduledJobBuilder;
import org.cmdbuild.services.scheduler.SchedulerService;
import org.cmdbuild.services.scheduler.job.CMJob;
import org.cmdbuild.services.scheduler.job.StartProcessJob;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DefaultSchedulerLogic implements SchedulerLogic {

	private final CMDataView view;
	private final SchedulerService schedulerService;

	public DefaultSchedulerLogic(final CMDataView view, final SchedulerService schedulerService) {
		this.view = view;
		this.schedulerService = schedulerService;
	}

	@Override
	public Iterable<ScheduledJob> findAllScheduledJobs() {
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
		final CMCard createdJobCard = jobToCreate.set(DETAIL_ATTRIBUTE_NAME, scheduledJob.getDetail()) //
				.setDescription(scheduledJob.getDescription()) //
				.set(CRON_EXP_ATTRIBUTE_NAME, scheduledJob.getCronExpression()) //
				.set(NOTES_ATTRIBUTE_NAME, fromParamsMapToString(scheduledJob.getParams())) //
				.set(JOB_TYPE_ATTRIBUTE_NAME, scheduledJob.getJobType().toString())
				.set(RUNNING_ATTRIBUTE_NAME, scheduledJob.isRunning())
				.save();

		final ScheduledJob createdScheduledJob = createScheduledJobFrom(createdJobCard);
		addJobToSchedulerService(createdScheduledJob);
		return createdScheduledJob;
	}

	@Override
	public String fromParamsMapToString(final Map<String, String> params) {
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
		final CMJob job = CMJobFactory.from(scheduledJob);
		final JobTrigger jobTrigger = new RecurringTrigger(scheduledJob.getCronExpression());
		schedulerService.addJob(job, jobTrigger);
	}

	@Override
	@Transactional
	public void update(final ScheduledJob jobToUpdate) {
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
		final CMClass schedulerClass = view.findClass(SCHEDULER_CLASS_NAME);
		final CMCard cardToDelete = view.select(anyAttribute(schedulerClass)) //
				.from(schedulerClass) //
				.where(condition(attribute(schedulerClass, "Id"), eq(jobId))) //
				.run().getOnlyRow().getCard(schedulerClass);
		view.delete(cardToDelete);
		schedulerService.removeJob(new StartProcessJob(jobId));
	}

}
