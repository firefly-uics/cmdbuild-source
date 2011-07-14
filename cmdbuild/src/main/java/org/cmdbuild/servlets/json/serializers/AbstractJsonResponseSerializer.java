package org.cmdbuild.servlets.json.serializers;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public abstract class AbstractJsonResponseSerializer {

	@Deprecated
	private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yy HH:mm:ss"); // FIXME should be defined in the user session

	protected final String formatDate(final DateTime dateTime) {
		return DATE_TIME_FORMATTER.print(dateTime);
	}
}
