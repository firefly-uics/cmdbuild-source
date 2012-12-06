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

	private String triggerName;

	public HasTriggerMatcher(String triggerName) {
		this.triggerName = triggerName;
	}

	public boolean matchesSafely(String className) {
		boolean matches = false;
		try {
			Connection dbConnection = DBService.getConnection();
			PreparedStatement ps = dbConnection.prepareStatement(
					"SELECT pg_trigger.oid"
					+" FROM pg_trigger JOIN pg_class ON pg_trigger.tgrelid = pg_class.oid"
					+" WHERE pg_trigger.tgname::text = ? AND pg_class.relname::text = ?"
				);
			ps.setString(1, triggerName);
			ps.setString(2, className);
			matches = ps.executeQuery().next();
		} catch (SQLException e) {
		}
		return matches;
	}

	public void describeTo(Description description) {
		description.appendText("trigger ").appendValue(triggerName);
	}

	@Factory
	public static <T> Matcher<String> hasTrigger(String className) {
		return new HasTriggerMatcher(className);
	}
}
