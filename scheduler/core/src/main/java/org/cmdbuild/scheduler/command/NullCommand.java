package org.cmdbuild.scheduler.command;

public class NullCommand implements Command {

	private static final NullCommand INSTANCE = new NullCommand();
	
	public static NullCommand nullCommand() {
		return INSTANCE;
	}
	
	private NullCommand() {
		// use factory method
	}

	@Override
	public void execute() {
		// nothing to do
	}

}
