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
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.model.data.Attribute;
import org.cmdbuild.model.data.Class;
import org.cmdbuild.model.data.Attribute.AttributeBuilder;
import org.cmdbuild.model.data.Class.ClassBuilder;
import org.junit.Before;
import org.junit.Test;

public class DataDefinitionLogicTest {

	private static final String CLASS_NAME = "foo";
	private static final Long CLASS_ID = 42L;
	private static final String ATTRIBUTE_NAME = "bar";

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
		final CMClass returnedClass = dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// then
		assertThat(returnedClass.getName(), equalTo(createdClass.getName()));
		verify(dataView).findClassByName(CLASS_NAME);
		verify(dataView).createClass(any(CMClassDefinition.class));
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void updateExistingClass() {
		// given
		final CMClass existingClass = mockClass(CLASS_NAME);
		when(dataView.findClassByName(CLASS_NAME)) //
				.thenReturn(existingClass);

		// when
		dataDefinitionLogic.createOrUpdate(a(newClass(CLASS_NAME)));

		// then
		verify(dataView).findClassByName(CLASS_NAME);
		verify(dataView).updateClass(any(CMClassDefinition.class));
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void deletingUnexistingAttributeDoesNothing() throws Exception {
		// given
		final CMClass existingClass = mockClass(CLASS_NAME);
		when(existingClass.getId()) //
				.thenReturn(CLASS_ID);
		when(dataView.findClassById(CLASS_ID)) //
				.thenReturn(existingClass);

		// when
		dataDefinitionLogic.deleteOrDeactivate( //
				a(newAttribute(ATTRIBUTE_NAME) //
						.withOwner(existingClass.getId())));

		// then
		verify(dataView.findClassById(CLASS_ID)).getAttribute(ATTRIBUTE_NAME);
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

	private ClassBuilder newClass(final String name) {
		return Class.newClass() //
				.withName(name);
	}

	private AttributeBuilder newAttribute(final String name) {
		return Attribute.newAttribute() //
				.withName(name);
	}

	private static Class a(final ClassBuilder classBuilder) {
		return classBuilder.build();
	}

	private static Attribute a(final AttributeBuilder attributeBuilder) {
		return attributeBuilder.build();
	}

}
