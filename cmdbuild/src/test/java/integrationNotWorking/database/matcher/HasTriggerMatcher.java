package integrationNotWorking.database.matcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.cmdbuild.services.DBService;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class HasTriggerMatcher extends TypeSafeMatcher<String> {

	private final String triggerName;

	public HasTriggerMatcher(final String triggerName) {
		this.triggerName = triggerName;
	}

	@Override
	public boolean matchesSafely(final String className) {
		boolean matches = false;
		try {
			final Connection dbConnection = DBService.getConnection();
			final PreparedStatement ps = dbConnection.prepareStatement("SELECT pg_trigger.oid"
					+ " FROM pg_trigger JOIN pg_class ON pg_trigger.tgrelid = pg_class.oid"
					+ " WHERE pg_trigger.tgname::text = ? AND pg_class.relname::text = ?");
			ps.setString(1, triggerName);
			ps.setString(2, className);
			matches = ps.executeQuery().next();
		} catch (final SQLException e) {
		}
		return matches;
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("trigger ").appendValue(triggerName);
	}

	@Factory
	public static <T> Matcher<String> hasTrigger(final String className) {
		return new HasTriggerMatcher(className);
	}
}
