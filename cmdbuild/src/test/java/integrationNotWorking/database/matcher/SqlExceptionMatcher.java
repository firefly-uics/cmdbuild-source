package integrationNotWorking.database.matcher;

import java.sql.SQLException;

import org.cmdbuild.dao.backend.postgresql.PGCMBackend.CMSqlException;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SqlExceptionMatcher extends TypeSafeMatcher<SQLException> {

	private final String expectedType;

	public SqlExceptionMatcher(final String expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public boolean matchesSafely(final SQLException e) {
		return e.getMessage().contains(expectedType);
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("has type ").appendValue(expectedType);
	}

	@Factory
	public static <T> Matcher<SQLException> hasType(final String expectedType) {
		return new SqlExceptionMatcher(expectedType);
	}

	@Factory
	public static <T> Matcher<SQLException> hasType(final CMSqlException expectedType) {
		return new SqlExceptionMatcher(expectedType.toString());
	}
}
