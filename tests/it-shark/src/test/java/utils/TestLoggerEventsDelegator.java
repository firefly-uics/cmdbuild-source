package utils;

import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.SharkEventsDelegator;

public class TestLoggerEventsDelegator extends SharkEventsDelegator {

	private static final CMEventManager eventManager = new TestLoggerEventManager();

	public TestLoggerEventsDelegator() {
		super(eventManager);
	}

}
