package integrationNotWorking.database.matcher;

import java.sql.SQLException;

import org.cmdbuild.dao.backend.postgresql.PGCMBackend.CMSqlException;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class SqlExceptionMatcher extends TypeSafeMatcher<SQLException> {

	private String expectedType;

	public SqlExceptionMatcher(String expectedType) {
		this.expectedType = expectedType;
	}

	public boolean matchesSafely(SQLException e) {
		return e.getMessage().contains(expectedType);
	}

	public void describeTo(Description description) {
		description.appendText("has type ").appendValue(expectedType);
	}

	@Factory
	public static <T> Matcher<SQLException> hasType(String expectedType) {
		return new SqlExceptionMatcher(expectedType);
	}

	@Factory
	public static <T> Matcher<SQLException> hasType(CMSqlException expectedType) {
		return new SqlExceptionMatcher(expectedType.toString());
	}
}
