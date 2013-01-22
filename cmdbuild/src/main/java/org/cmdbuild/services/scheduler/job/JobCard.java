package org.cmdbuild.services.scheduler.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.proxy.LazyCard;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.scheduler.trigger.JobTrigger;
import org.cmdbuild.services.scheduler.trigger.RecurringTrigger;

@Deprecated
@OldDao
public class JobCard extends LazyCard implements JobFactory {
	private static final long serialVersionUID = 1L;

	public static final String JOB_CLASS_NAME = "Scheduler";
	public static final String JOB_CRON_EXPRESSION_ATTRIBUTE = "CronExpression";
	public static final String JOB_TYPE_ATTRIBUTE = ICard.CardAttributes.Code.toString();
	public static final String JOB_DETAIL_ATTRIBUTE = "Detail";
	public static final String JOB_PARAMS_ATTRIBUTE = ICard.CardAttributes.Notes.toString();

	private static final String START_PROCESS_TYPE = "StartProcess";

	public JobCard(final int id) {
		super(JobCard.getJobClass(), id);
		setType(START_PROCESS_TYPE);
	}

	public JobCard() {
		super(JobCard.getJobClass().cards().create());
		setType(START_PROCESS_TYPE);
	}

	public JobCard(final ICard card) {
		super(card);
		setType(START_PROCESS_TYPE);
	}

	private static ITable getJobClass() {
		return UserOperations.from(UserContext.systemContext()).tables().get(JOB_CLASS_NAME);
	}

	private void setType(final String jobType) {
		getAttributeValue(JOB_TYPE_ATTRIBUTE).setValue(jobType);
	}

	public String getDetail() {
		return getAttributeValue(JOB_DETAIL_ATTRIBUTE).getString();
	}

	public void setDetail(final String detail) {
		getAttributeValue(JOB_DETAIL_ATTRIBUTE).setValue(detail);
	}

	public String getCronExpression() {
		return getAttributeValue(JOB_CRON_EXPRESSION_ATTRIBUTE).getString();
	}

	public void setCronExpression(final String cronExpression) {
		getAttributeValue(JOB_CRON_EXPRESSION_ATTRIBUTE).setValue(cronExpression);
	}

	public Map<String, String> getParams() {
		final Map<String, String> params = new HashMap<String, String>();
		final String paramBlock = getAttributeValue(JOB_PARAMS_ATTRIBUTE).getString();
		if (paramBlock != null) {
			for (final String paramLine : paramBlock.split("\n")) {
				final String[] paramArray = paramLine.split("=", 2);
				if (paramArray.length == 2) {
					params.put(paramArray[0], paramArray[1]);
				}
			}
		}
		return params;
	}

	public void setParams(final Map<String, String> params) {
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
		getAttributeValue(JOB_PARAMS_ATTRIBUTE).setValue(paramBlock.toString());
	}

	public static Iterable<JobCard> allForDetail(final String detail) {
		return convertCardListToJobCardList(JobCard.getJobClass().cards().list()
				.filter(JOB_DETAIL_ATTRIBUTE, AttributeFilterType.EQUALS, detail));
	}

	public static Iterable<JobCard> all() {
		return convertCardListToJobCardList(JobCard.getJobClass().cards().list());
	}

	private static Iterable<JobCard> convertCardListToJobCardList(final Iterable<ICard> cardList) {
		final List<JobCard> list = new ArrayList<JobCard>();
		for (final ICard card : cardList) {
			list.add(new JobCard(card));
		}
		return list;
	}

	public String getType() {
		return START_PROCESS_TYPE;
	}

	@Override
	public Job createJob() {
		if (isNew()) {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		final AbstractJob job = new StartProcessJob(getId()); // TODO
		job.setDetail(getDetail());
		job.setParams(getParams());
		return job;
	}

	public JobTrigger createJobTrigger() {
		return new RecurringTrigger(getCronExpression());
	}
}
