package utils;

import static org.mockito.Mockito.mock;

import org.cmdbuild.workflow.CMEventManager;
import org.cmdbuild.workflow.SharkEventsDelegator;

public class MockEventsDelegator extends SharkEventsDelegator {

	public static final CMEventManager mock;

	static {
		mock = mock(CMEventManager.class);
	}

	public MockEventsDelegator() {
		super();
		setEventManager(mock);
	}

}
