package integration.logic.data;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.entrytype.CMClass;
import org.junit.Test;

public class ClassDefinitionTest extends DataDefinitionLogicTest {

	private static final String SUPER_CLASS_NAME = "super";
	private static final String CLASS_NAME = "foo";
	private static final String DESCRIPTION = "description of foo";

	@Test
	public void createStandardClassHasSomeDefauls() {
		// given
		dataDefinitionLogic().createOrUpdateClass(a(newClass(CLASS_NAME)));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.getName(), equalTo(CLASS_NAME));
		assertThat(createdClass.getDescription(), equalTo(CLASS_NAME));
		assertThat(createdClass.isSuperclass(), equalTo(false));
		assertThat(createdClass.isActive(), equalTo(true));
		assertThat(createdClass.holdsHistory(), equalTo(true));
		assertThat(createdClass.getParent(), is(nullValue(CMClass.class)));
	}

	@Test
	public void createClassWithNoDescription() {
		// given
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME)));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.getDescription(), equalTo(CLASS_NAME));
	}

	@Test
	public void createClassWithDescriptionDifferentFromName() {
		// given
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME).withDescription(DESCRIPTION)));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.getDescription(), equalTo(DESCRIPTION));
	}

	@Test
	public void createNonActiveClass() {
		// given
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME).thatIsActive(false)));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.isActive(), equalTo(false));
	}

	@Test
	public void createClassWithNoParent() {
		// given
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME)));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.getParent(), is(nullValue()));
	}

	@Test
	public void createClassWithParent() {
		// given
		final CMClass parent = dataDefinitionLogic().createOrUpdateClass(a(newClass(SUPER_CLASS_NAME)));
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME).withParent(parent.getId())));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.getParent().getName(), is(SUPER_CLASS_NAME));
	}

	@Test
	public void createClassWithNoHistoryAndNoParentAKASimpleClass() {
		// given
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME).thatIsHoldingHistory(false)));

		// when
		final CMClass createdClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(createdClass.holdsHistory(), equalTo(false));
		assertThat(createdClass.getParent(), is(nullValue()));
	}

	@Test
	public void onlyDescriptionAndActiveConditionCanBeChangedDuringUpdate() {
		// given
		final CMClass parent = dataDefinitionLogic().createOrUpdateClass(a(newClass(SUPER_CLASS_NAME)));
		dataDefinitionLogic().createOrUpdateClass( //
				a(newClass(CLASS_NAME).withParent(parent.getId())));
		dataDefinitionLogic().createOrUpdateClass(a(newClass(CLASS_NAME) //
				.withDescription(DESCRIPTION) //
				.withParent(null) //
				.thatIsHoldingHistory(false) //
				.thatIsActive(false)));

		// when
		final CMClass updatedClass = dataView().findClassByName(CLASS_NAME);

		// then
		assertThat(updatedClass.holdsHistory(), equalTo(true));
		assertThat(updatedClass.getParent().getName(), equalTo(parent.getName()));
		assertThat(updatedClass.getDescription(), equalTo(DESCRIPTION));
		assertThat(updatedClass.isActive(), equalTo(false));
	}

}
