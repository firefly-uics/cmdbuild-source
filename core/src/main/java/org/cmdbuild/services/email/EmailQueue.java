package org.cmdbuild.services.email;

import org.cmdbuild.logger.Log;
import org.cmdbuild.scheduler.command.Command;
import org.slf4j.Logger;

public interface EmailQueue extends Command {

	Logger logger = Log.EMAIL;

}
