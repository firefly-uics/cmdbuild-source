package utils.matchers;

import java.util.List;

import org.cmdbuild.services.soap.Attribute;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class AttributeListMatcher extends TypeSafeMatcher<List<Attribute>> {

	private final String name;
	private final String value;

	public AttributeListMatcher(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public void describeTo(final Description description) {
		description //
				.appendText(" contains an attribute with name ") //
				.appendValue(name) //
				.appendText(" and value ") //
				.appendValue(value);
	}

	@Override
	public boolean matchesSafely(final List<Attribute> attributes) {
		for (final Attribute attribute : attributes) {
			final boolean found = name.equals(attribute.getName()) && value.equals(attribute.getValue());
			if (found) {
				return true;
			}
		}
		return false;
	}

	public static Matcher<List<Attribute>> containsAttribute(final String name, final String value) {
		return new AttributeListMatcher(name, value);
	}

}
