package integration.database.matcher;

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

	private String childTable;

	public IsParentTableOfMatcher(String childTable) {
		this.childTable = childTable;
	}

	public boolean matchesSafely(String expectedParentTable) {
		boolean matches = false;
		try {
			Connection dbConnection = DBService.getConnection();
			PreparedStatement ps = dbConnection.prepareStatement(
					"SELECT _cm_cmtable(_cm_parent_id(_cm_table_id(?)))"
				);
			ps.setString(1, childTable);
			ResultSet rs = ps.executeQuery();
			rs.next();
			String realParent = rs.getString(1);
			matches = ((expectedParentTable != null && expectedParentTable.equals(realParent)) || realParent == null);
		} catch (SQLException e) {
		}
		return matches;
	}

	public void describeTo(Description description) {
		description.appendText("superclass of ").appendValue(childTable);
	}

	@Factory
	public static <T> Matcher<String> isParentTableOf(String childTable) {
		return new IsParentTableOfMatcher(childTable);
	}
}
