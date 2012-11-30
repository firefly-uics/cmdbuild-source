package unit.logic.data;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.cmdbuild.logic.data.ClassDTO;
import org.cmdbuild.logic.data.ClassDTO.ClassDTOBuilder;
import org.junit.Test;

public class ClassDTOTest {

	private static final String NAME = "foo";
	private static final String DESCRIPTION = "foo's description";
	private static final String BLANK = " \t";
	private static final String NAME_WITH_BLANKS = BLANK + NAME + BLANK;

	private static final Long NEGATIVE_ID = -42L;
	private static final Long ZERO_ID = 0L;
	private static final Long VALID_ID = 42L;

	@Test(expected = IllegalArgumentException.class)
	public void missingNameThowsException() {
		a(newClassWithNoNameSpecified());
	}

	@Test
	public void nameIsTheOnlyRequiredAtribute() {
		assertThat(a(newValidClass()).getName(), equalTo(NAME));
	}

	@Test
	public void nameIsTrimmed() {
		assertThat(a(newClassWithNoNameSpecified().withName(NAME_WITH_BLANKS)).getName(), equalTo(NAME));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameCannotBeEmpty() {
		a(newClassWithNoNameSpecified().withName(EMPTY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void nameCannotBeBlank() {
		a(newClassWithNoNameSpecified().withName(BLANK));
	}

	@Test
	public void whenDescriptionIsNotSpecifiedOrEmptyOrBlankThenNameValueIsReturned() {
		assertThat(a(newValidClass()).getDescription(), equalTo(NAME));
		assertThat(a(newValidClass().withDescription(EMPTY)).getDescription(), equalTo(NAME));
		assertThat(a(newValidClass().withDescription(BLANK)).getDescription(), equalTo(NAME));
	}

	@Test
	public void whenDescriptionIsSpecifiedItsValueCanBeRead() {
		assertThat(a(newValidClass().withDescription(DESCRIPTION)).getDescription(), equalTo(DESCRIPTION));
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenParentIdIsSpecifiedItMustNotBeLowerThatZero() throws Exception {
		a(newValidClass().withParent(NEGATIVE_ID));
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenParentIdIsSpecifiedItMustNotBeZero() throws Exception {
		a(newValidClass().withParent(ZERO_ID));
	}

	@Test
	public void whenParentIdIsNotSpecifiedThenNullValueIsReturned() throws Exception {
		assertThat(a(newValidClass()).getParentId(), is(nullValue(Long.class)));
	}

	@Test
	public void whenParentIdIsSpecifiedThenItsValueIsReturned() throws Exception {
		assertThat(a(newValidClass().withParent(VALID_ID)).getParentId(), equalTo(VALID_ID));
	}

	@Test
	public void whenSuperClassConditionIsNotSpecifiedThenIsConsideredFalse() throws Exception {
		assertThat(a(newValidClass()).isSuperClass(), equalTo(false));
	}

	@Test
	public void whenSuperClassConditionIsSpecifiedThenItsValueIsReturned() throws Exception {
		assertThat(a(newValidClass().thatIsSuperClass(false)).isSuperClass(), equalTo(false));
		assertThat(a(newValidClass().thatIsSuperClass(true)).isSuperClass(), equalTo(true));
	}

	@Test
	public void whenProcessConditionIsNotSpecifiedThenIsConsideredFalse() throws Exception {
		assertThat(a(newValidClass()).isProcess(), equalTo(false));
	}

	@Test
	public void whenProcessConditionIsSpecifiedThenItsValueIsReturned() throws Exception {
		assertThat(a(newValidClass().thatIsProcess(false)).isProcess(), equalTo(false));
		assertThat(a(newValidClass().thatIsProcess(true)).isProcess(), equalTo(true));
	}

	@Test
	public void whenUserStoppableConditionIsNotSpecifiedThenIsConsideredFalse() throws Exception {
		assertThat(a(newValidClass()).isUserStoppable(), equalTo(false));
	}

	@Test
	public void whenUserStoppableConditionIsSpecifiedThenItsValueIsReturned() throws Exception {
		assertThat(a(newValidClass().thatIsUserStoppable(false)).isUserStoppable(), equalTo(false));
		assertThat(a(newValidClass().thatIsUserStoppable(true)).isUserStoppable(), equalTo(true));
	}

	@Test
	public void whenHistoryConditionIsNotSpecifiedThenIsConsideredTrue() throws Exception {
		assertThat(a(newValidClass()).isHoldingHistory(), equalTo(true));
	}

	@Test
	public void whenHistoryConditionIsSpecifiedThenItsValueIsReturned() throws Exception {
		assertThat(a(newValidClass().thatIsHoldingHistory(false)).isHoldingHistory(), equalTo(false));
		assertThat(a(newValidClass().thatIsHoldingHistory(true)).isHoldingHistory(), equalTo(true));
	}

	@Test
	public void whenActiveConditionIsNotSpecifiedThenIsConsideredTrue() throws Exception {
		assertThat(a(newValidClass()).isActive(), equalTo(true));
	}

	@Test
	public void whenActiveConditionIsSpecifiedThenItsValueIsReturned() throws Exception {
		assertThat(a(newValidClass().thatIsActive(false)).isActive(), equalTo(false));
		assertThat(a(newValidClass().thatIsActive(true)).isActive(), equalTo(true));
	}

	/*
	 * Utilities
	 */

	private static ClassDTO a(final ClassDTOBuilder classDTOBuilder) {
		return classDTOBuilder.build();
	}

	private ClassDTOBuilder newValidClass() {
		return newClassWithNoNameSpecified().withName(NAME);
	}

	private ClassDTOBuilder newClassWithNoNameSpecified() {
		return ClassDTO.newClassDTO();
	}

}
