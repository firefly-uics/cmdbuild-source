package org.cmdbuild.logic.email;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.scheduler.Triggers.everyMinute;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;

public class DefaultEmailQueueLogic implements EmailQueueLogic {

	private static final Configuration NULL_CONFIGURATION = new Configuration() {

		@Override
		public long time() {
			return 0;
		}
	};

	private final SchedulerService schedulerService;
	private final Job job;
	private Configuration configuration = NULL_CONFIGURATION;

	public DefaultEmailQueueLogic(final SchedulerService schedulerService, final Command command) {
		this.schedulerService = schedulerService;
		this.job = BuildableCommandBasedJob.newInstance() //
				.withName(DefaultEmailQueueLogic.class.getName()) //
				.withCommand(command) //
				.build();
	}

	@Override
	public boolean running() {
		return schedulerService.isStarted(job);
	}

	@Override
	public void start() {
		// TODO add time check
		schedulerService.add(job, everyMinute());
	}

	@Override
	public void stop() {
		if (running()) {
			schedulerService.remove(job);
		}
	}

	@Override
	public Configuration configuration() {
		return this.configuration;
	}

	@Override
	public void configure(final Configuration configuration) {
		validate(configuration);
		this.configuration = defaultIfNull(configuration, this.configuration);
	}

	private void validate(final Configuration configuration) {
		Validate.isTrue(configuration.time() >= 0, "invalid time");
	}

}
