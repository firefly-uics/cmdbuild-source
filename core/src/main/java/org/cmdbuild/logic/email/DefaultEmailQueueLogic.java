package org.cmdbuild.logic.email;

import static org.cmdbuild.scheduler.Triggers.everyMinute;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;

public class DefaultEmailQueueLogic implements EmailQueueLogic {

	private final EmailConfiguration configuration;
	private final SchedulerService schedulerService;
	private final Job job;

	public DefaultEmailQueueLogic(final EmailConfiguration configuration, final SchedulerService schedulerService,
			final Command command) {
		this.configuration = configuration;
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
		return new Configuration() {

			@Override
			public long time() {
				return configuration.getQueueTime();
			}

		};
	}

	@Override
	public void configure(final Configuration configuration) {
		validate(configuration);
		this.configuration.setQueueTime(configuration.time());
		this.configuration.save();
	}

	private void validate(final Configuration configuration) {
		Validate.notNull(configuration, "missing configuration");
		Validate.isTrue(configuration.time() >= 0, "invalid time");
	}

}
