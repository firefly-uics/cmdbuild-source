package org.cmdbuild.logic.scheduler;

import java.util.Map;

import org.cmdbuild.logic.Logic;

public interface SchedulerLogic extends Logic {

	enum ScheduledJobType {
		workflow,
		emailService
	}

	interface ScheduledJob {

		Long getId();

		String getDescription();

		String getCronExpression();

		String getDetail();

		Map<String, String> getParams();

		ScheduledJobType getJobType();

		boolean isRunning();
	}

	Iterable<ScheduledJob> findAllScheduledJobs();

	Iterable<ScheduledJob> findJobsByDetail(String detail);

	ScheduledJob findJobById(Long jobId);

	ScheduledJob createAndStart(ScheduledJob scheduledJob);

	String fromParamsMapToString(Map<String, String> params);

	void update(ScheduledJob jobToUpdate);

	void delete(Long jobId);

}
