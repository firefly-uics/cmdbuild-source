package unit.services.email;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.cmdbuild.services.email.DefaultSubjectParser;
import org.cmdbuild.services.email.SubjectParser.ParsedSubject;
import org.junit.Test;

public class DefaultSubjectParserTest {

	private ParsedSubject parse(final String subject) {
		return new DefaultSubjectParser().parse(subject);
	}

	@Test
	public void hasExpectedFormat() {
		assertThat(parse("[bar 42]").hasExpectedFormat(), is(true));
		assertThat(parse("[42 42]").hasExpectedFormat(), is(true));
		assertThat(parse("[bar 42] foo").hasExpectedFormat(), is(true));
		assertThat(parse("foo [bar 42]").hasExpectedFormat(), is(true));
		assertThat(parse("baz [bar 42] foo").hasExpectedFormat(), is(true));
	}

	@Test
	public void doesNotHaveExpectedFormat() {
		assertThat(parse("[]").hasExpectedFormat(), is(false));
		assertThat(parse("[bar]").hasExpectedFormat(), is(false));
		assertThat(parse("[42]").hasExpectedFormat(), is(false));
		assertThat(parse("[bar baz]").hasExpectedFormat(), is(false));
		assertThat(parse("foo").hasExpectedFormat(), is(false));
		assertThat(parse("[bar baz 42]").hasExpectedFormat(), is(false));
	}

	@Test
	public void activityClassNameExtracted() throws Exception {
		assertThat(parse("[bar 42]").getActivityClassName(), equalTo("bar"));
		assertThat(parse("[123 42]").getActivityClassName(), equalTo("123"));
	}

	@Test
	public void activityIdExtracted() throws Exception {
		assertThat(parse("[bar 42]").getActivityId(), equalTo(42));
	}

	@Test
	public void realSubjectExtracted() throws Exception {
		assertThat(parse("[bar 42]").getRealSubject(), equalTo(""));
		assertThat(parse("[bar 42] ").getRealSubject(), equalTo(""));
		assertThat(parse("[bar 42] foo").getRealSubject(), equalTo("foo"));
		assertThat(parse("baz [bar 42]").getRealSubject(), equalTo(""));
	}

}
