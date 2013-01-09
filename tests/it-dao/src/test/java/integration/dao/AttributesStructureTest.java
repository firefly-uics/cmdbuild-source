package integration.dao;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static utils.IntegrationTestUtils.newClass;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Ignore;
import org.junit.Test;

import utils.IntegrationTestBase;

public class AttributesStructureTest extends IntegrationTestBase {

	@Test
	public void nullValueIsReturnedWhenFetchingUnexistingAttribute() {
		// given
		dbDataView().createClass(newClass("foo"));

		// when
		final DBAttribute attribute = dbDataView().findClassByName("foo").getAttribute("unexisting");

		// then
		assertThat(attribute, nullValue());
	}

	@Ignore
	@Test
	public void reservedAttributeAreNotFetched() {
		// given
		dbDataView().findClassByName("Grant");

		// when
		final CMAttribute attribute = dbDataView().findClassByName("foo").getAttribute("IdGrantedClass");

		// then
		assertThat(attribute, nullValue());
	}

	@Test
	public void defaultClassAttributesCanBeFetchedWithoutReloadingClass() {
		// given
		final DBClass clazz = dbDataView().createClass(newClass("foo"));

		// when

		// then
		assertThat(clazz.getAttribute("Code"), notNullValue());
		assertThat(clazz.getAttribute("Description"), notNullValue());
		assertThat(clazz.getAttribute("Notes"), notNullValue());
	}

	@Test
	public void defaultClassAttributesCanBeFetchedAfterReloadingClass() {
		// given
		dbDataView().createClass(newClass("foo"));

		// when
		final DBClass clazz = dbDataView().findClassByName("foo");

		// then
		assertThat(clazz.getAttribute("Code"), notNullValue());
		assertThat(clazz.getAttribute("Description"), notNullValue());
		assertThat(clazz.getAttribute("Notes"), notNullValue());
	}

}
