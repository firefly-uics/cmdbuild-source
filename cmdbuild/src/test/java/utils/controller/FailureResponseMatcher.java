package utils.controller;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.services.json.dto.JsonError;
import org.cmdbuild.services.json.dto.JsonFailureResponse;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class FailureResponseMatcher extends TypeSafeMatcher<JsonResponse> {

	private final String expectedReason;

	private String errorDescription = StringUtils.EMPTY;

	private FailureResponseMatcher(String failureCode) {
		this.expectedReason = failureCode;
	}

	@Override
	public boolean matchesSafely(JsonResponse response) {
		if (response.isSuccess()) {
			errorDescription = "is not a failure";
			return false;
		}
		if (response instanceof JsonFailureResponse) {
			JsonFailureResponse failureResponse = (JsonFailureResponse) response;
			JsonError errors[] = failureResponse.getErrors();
			if (errors.length != 1) {
				errorDescription = "contains more than one error condition";
				return false;
			}
			final String reason = errors[0].getReason(); 
			if (!expectedReason.equals(reason)) {
				errorDescription = String.format("is '%s' instead of ''", reason, expectedReason);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	public void describeTo(Description description) {
		description.appendText(errorDescription);
	}

	@Factory
	public static <T> Matcher<JsonResponse> failureResponse(final String failureCode) {
		return new FailureResponseMatcher(failureCode);
	}
}