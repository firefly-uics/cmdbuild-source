package utils;

import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;

public class IsCQL2 extends TypeSafeMatcher<QuerySpecsBuilder> {

	private final String expectedCql2;
	
	private IsCQL2(final String expectedCql2) {
		this.expectedCql2 = expectedCql2;
	}

	@Override
	public boolean matchesSafely(QuerySpecsBuilder query) {
		return query.toCQL2().equals(expectedCql2);
	}

	public void describeTo(Description description) {
		description.appendText(expectedCql2);
	}

	@Factory
	public static <T> Matcher<QuerySpecsBuilder> cql2(final String expectedCql2) {
		return new IsCQL2(expectedCql2);
	}
}