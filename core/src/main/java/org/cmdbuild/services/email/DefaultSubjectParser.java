package org.cmdbuild.services.email;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;
import static org.apache.commons.lang.StringUtils.trim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public class DefaultSubjectParser implements SubjectParser {

	private static final int ACTIVITY_CLASSNAME_GROUP = 1;
	private static final int ACTIVITY_ID_GROUP = 2;
	private static final int NOTIFICATION_GROUP = 4;
	private static final int REAL_SUBJECT_GROUP = 5;

	private static final Logger logger = Log.EMAIL;

	private static final String PATTERN = "[^\\[]*\\[(\\w+)\\s+(\\d+)(\\s+(\\w+)?)?\\](.*)";

	@Override
	public ParsedSubject parse(final String subject) {
		logger.debug("parsing subject '{}'", subject);
		final Pattern pattern = Pattern.compile(PATTERN);
		final Matcher matcher = pattern.matcher(defaultIfBlank(trim(subject), EMPTY));
		matcher.find();
		return new ParsedSubject() {

			@Override
			public boolean hasExpectedFormat() {
				return matcher.matches();
			}

			@Override
			public String getActivityClassName() {
				Validate.isTrue(hasExpectedFormat(), "invalid format");
				return getTrimmed(ACTIVITY_CLASSNAME_GROUP);
			}

			@Override
			public Integer getActivityId() {
				Validate.isTrue(hasExpectedFormat(), "invalid format");
				return Integer.parseInt(getTrimmed(ACTIVITY_ID_GROUP));
			}

			@Override
			public String getNotification() {
				Validate.isTrue(hasExpectedFormat(), "invalid format");
				return getTrimmed(NOTIFICATION_GROUP);
			}

			@Override
			public String getRealSubject() {
				Validate.isTrue(hasExpectedFormat(), "invalid format");
				return getTrimmed(REAL_SUBJECT_GROUP);
			}

			private String getTrimmed(final int group) {
				return trim(matcher.group(group));
			}

		};
	}

}
