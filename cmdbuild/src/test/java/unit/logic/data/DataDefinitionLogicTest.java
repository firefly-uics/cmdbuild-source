package unit.logic.data;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMClassDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.ClassDTO;
import org.cmdbuild.logic.data.ClassDTO.ClassDTOBuilder;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.junit.Before;
import org.junit.Test;

public class DataDefinitionLogicTest {

	private static final String CLASS_NAME = "foo";

	private CMDataView dataView;
	private DataDefinitionLogic dataDefinitionLogic;

	@Before
	public void createDataDefinitionLogic() throws Exception {
		dataView = mock(CMDataView.class);
		dataDefinitionLogic = new DataDefinitionLogic(dataView);
	}

	@Test
	public void createUnexistingClass() {
		// given
		final CMClass createdClass = mockClass(CLASS_NAME);
		when(dataView.findClassByName(CLASS_NAME)) //
				.thenReturn(null, createdClass);
		when(dataView.createClass(any(CMClassDefinition.class))) //
				.thenReturn(createdClass);

		// when
		final CMClass returnedClass = dataDefinitionLogic.createOrUpdateClass(a(newClass(CLASS_NAME)));

		// then
		assertThat(returnedClass.getName(), equalTo(createdClass.getName()));
		verify(dataView).findClassByName(CLASS_NAME);
		verify(dataView).createClass(any(CMClassDefinition.class));
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void updateExistingClass() {
		final CMClass existingClass = mockClass(CLASS_NAME);
		when(dataView.findClassByName(CLASS_NAME)) //
				.thenReturn(existingClass);

		// when
		dataDefinitionLogic.createOrUpdateClass(a(newClass(CLASS_NAME)));

		// then
		verify(dataView).findClassByName(CLASS_NAME);
		verify(dataView).updateClass(any(CMClassDefinition.class));
		verifyNoMoreInteractions(dataView);
	}

	/*
	 * Utilities
	 */

	private CMClass mockClass(final String name) {
		final CMClass mockClass = mock(CMClass.class);
		when(mockClass.getName()) //
				.thenReturn(name);
		return mockClass;
	}

	private ClassDTOBuilder newClass(final String name) {
		return ClassDTO.newClassDTO() //
				.withName(name);
	}

	private static ClassDTO a(final ClassDTOBuilder classDTO) {
		return classDTO.build();
	}

}
