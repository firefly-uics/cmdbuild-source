package org.cmdbuild.logic.email;

import org.cmdbuild.logic.Logic;
import org.cmdbuild.scheduler.command.Command;

public interface EmailQueueLogic extends Command, Logic {

	interface Configuration {

		long time();

	}

	boolean running();

	void start();

	void stop();

	Configuration configuration();

	void configure(Configuration configuration);

}
