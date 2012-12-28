package integrationNotWorking.database.matcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cmdbuild.services.DBService;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class IsParentTableOfMatcher extends TypeSafeMatcher<String> {

	private final String childTable;

	public IsParentTableOfMatcher(final String childTable) {
		this.childTable = childTable;
	}

	@Override
	public boolean matchesSafely(final String expectedParentTable) {
		boolean matches = false;
		try {
			final Connection dbConnection = DBService.getConnection();
			final PreparedStatement ps = dbConnection
					.prepareStatement("SELECT _cm_cmtable(_cm_parent_id(_cm_table_id(?)))");
			ps.setString(1, childTable);
			final ResultSet rs = ps.executeQuery();
			rs.next();
			final String realParent = rs.getString(1);
			matches = ((expectedParentTable != null && expectedParentTable.equals(realParent)) || realParent == null);
		} catch (final SQLException e) {
		}
		return matches;
	}

	@Override
	public void describeTo(final Description description) {
		description.appendText("superclass of ").appendValue(childTable);
	}

	@Factory
	public static <T> Matcher<String> isParentTableOf(final String childTable) {
		return new IsParentTableOfMatcher(childTable);
	}
}
