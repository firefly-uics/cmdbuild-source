package unit.logic.data.access.lock;

import static org.cmdbuild.logic.data.access.lock.Lockables.card;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.access.lock.LockManager.Lockable;
import org.junit.Test;

public class LockablesTest {

	@Test
	public void loackableCardsDiffersWhenIdIsDifferent() {
		// given
		final Lockable first = card(1L);
		final Lockable second = card(2L);
		final Lockable sameAsFirst = card(1L);

		// then
		assertThat(first, equalTo(sameAsFirst));
		assertThat(first.hashCode(), equalTo(sameAsFirst.hashCode()));
		assertThat(first, not(equalTo(second)));
		assertThat(first.hashCode(), not(equalTo(second.hashCode())));
	}

}
