package utils;

import org.cmdbuild.workflow.SharkEventsDelegator;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class LoggerEventsDelegator extends SharkEventsDelegator {

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		setEventManager(new LoggerEventManager(cus));
	}

}
