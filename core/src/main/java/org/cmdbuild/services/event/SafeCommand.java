package org.cmdbuild.services.event;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class SafeCommand extends ForwardingCommand {

	static final Logger logger = Log.PERSISTENCE;
	static final Marker marker = MarkerFactory.getMarker(SafeCommand.class.getName());

	public SafeCommand(final Command delegate) {
		super(delegate);
	}

}
