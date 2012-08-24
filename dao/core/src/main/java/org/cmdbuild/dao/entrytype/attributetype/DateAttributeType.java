package org.cmdbuild.dao.entrytype.attributetype;

import org.cmdbuild.common.Constants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class DateAttributeType extends AbstractDateAttributeType {

	public DateAttributeType() {
	}

	@Override
	public void accept(CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	protected DateTimeFormatter[] getFormatters() {
		return new DateTimeFormatter[] {
			DateTimeFormat.forPattern(Constants.DATE_PARSING_PATTERN),
			DateTimeFormat.forPattern(SOAP_DATETIME_FORMAT)
		};
	}
}
