package unit.view;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBClass.ClassMetadata;
import org.cmdbuild.dao.entrytype.DBEntryType.EntryTypeMetadata;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.DBDataView;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

// TODO complete tests checking data translation
public class DBDataViewTest {

	private static final String CLASS_NAME = "className";
	private static final Long ID = 42L;

	private static final String NOT_ACTIVE_CLASS_NAME = "notActive";
	private static final Long NOT_ACTIVE_ID = 123L;

	private static final String DOMAIN_NAME = "domainName";

	private static final String FUNCTION_NAME = "functionName";

	private DBDriver driver;
	private DBDataView view;

	@Before
	public void setUp() throws Exception {
		driver = mock(DBDriver.class);
		view = new DBDataView(driver);
	}

	@Test
	public void classFoundById() throws Exception {
		when(driver.findClassById(ID)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final DBClass dbClass = view.findClassById(ID);

		assertThat(dbClass.getId(), equalTo(ID));

		verify(driver).findClassById(ID);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void classFoundByName() throws Exception {
		when(driver.findClassByName(CLASS_NAME)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final DBClass dbClass = view.findClassByName(CLASS_NAME);

		assertThat(dbClass.getId(), equalTo(ID));
		assertThat(dbClass.getName(), equalTo(CLASS_NAME));

		verify(driver).findClassByName(CLASS_NAME);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void activeClassesFound() throws Exception {
		when(driver.findAllClasses()) //
				.thenReturn(allClasses());

		final Iterable<DBClass> allClasses = view.findClasses();
		assertThat(sizeOf(allClasses), equalTo(1));
		assertThat(allClasses, hasItem(anActiveClass(CLASS_NAME, ID)));

		verify(driver).findAllClasses();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void allClassesFound() throws Exception {
		when(driver.findAllClasses()) //
				.thenReturn(allClasses());

		final Iterable<DBClass> allClasses = view.findAllClasses();
		assertThat(sizeOf(allClasses), equalTo(2));
		assertThat(allClasses, hasItem(anActiveClass(CLASS_NAME, ID)));
		assertThat(allClasses, hasItem(aNotActiveClass(NOT_ACTIVE_CLASS_NAME, NOT_ACTIVE_ID)));

		verify(driver).findAllClasses();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void activeDomainsFound() throws Exception {
		view.findDomains();

		verify(driver).findAllDomains();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void allDomainsFound() throws Exception {
		view.findAllDomains();

		verify(driver).findAllDomains();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void findDomainsFor() throws Exception {
		final CMClass cmClass = mock(CMClass.class);

		view.findDomainsFor(cmClass);

		verify(driver).findAllDomains();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void domainFoundById() throws Exception {
		view.findDomainById(ID);

		verify(driver).findDomainById(ID);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void domainFoundByName() throws Exception {
		view.findDomainByName(DOMAIN_NAME);

		verify(driver).findDomainByName(DOMAIN_NAME);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void allFunctionsFound() throws Exception {
		view.findAllFunctions();

		verify(driver).findAllFunctions();
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void functionFoundByName() throws Exception {
		view.findFunctionByName(FUNCTION_NAME);

		verify(driver).findFunctionByName(FUNCTION_NAME);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void newCardCreatedButNotSaved() throws Exception {
		when(driver.findClassById(any(Long.class))) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass mockClass = mock(CMClass.class);
		when(mockClass.getId()).thenReturn(ID);

		view.createCardFor(mockClass);

		verify(driver).findClassById(ID);
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void newCardCreatedAndSaved() throws Exception {
		when(driver.findClassById(any(Long.class))) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass mockClass = mock(CMClass.class);
		when(mockClass.getId()).thenReturn(ID);

		view.createCardFor(mockClass).save();

		verify(driver).findClassById(ID);
		verify(driver).create(any(DBEntry.class));
		verifyNoMoreInteractions(driver);
	}

	@Test
	public void cardModifiedButNotSaved() throws Exception {
		// given
		when(driver.findClassByName(CLASS_NAME)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass clazz = mock(CMClass.class);
		when(clazz.getName()).thenReturn(CLASS_NAME);

		final CMCard card = mock(CMCard.class);
		when(card.getType()).thenReturn(clazz);
		when(card.getValues()).thenReturn(Maps.<String, Object> newHashMap().entrySet());

		// when
		view.update(card);

		// then
		verify(driver).findClassByName(CLASS_NAME);
		verifyNoMoreInteractions(driver);

		verify(card).getType();
		verify(card).getId();
		verify(card).getValues();
		verifyNoMoreInteractions(card);
	}

	@Test
	public void cardModifiedAndSaved() throws Exception {
		// given
		when(driver.findClassByName(CLASS_NAME)) //
				.thenReturn(anActiveClass(CLASS_NAME, ID));

		final CMClass clazz = mock(CMClass.class);
		when(clazz.getName()).thenReturn(CLASS_NAME);

		final CMCard card = mock(CMCard.class);
		when(card.getType()).thenReturn(clazz);
		when(card.getValues()).thenReturn(Maps.<String, Object> newHashMap().entrySet());

		// when
		view.update(card).save();

		// then
		verify(driver).findClassByName(CLASS_NAME);
		verify(driver).update(any(DBEntry.class));
		verifyNoMoreInteractions(driver);

		verify(card).getType();
		verify(card).getId();
		verify(card).getValues();
		verifyNoMoreInteractions(card);
	}

	@Test
	public void queryExecuted() throws Exception {
		final QuerySpecs querySpecs = mock(QuerySpecs.class);

		view.executeNonEmptyQuery(querySpecs);

		verify(driver).query(querySpecs);
		verifyNoMoreInteractions(driver);
	}

	/*
	 * Utilities
	 */

	private List<DBClass> allClasses() {
		return Arrays.asList( //
				anActiveClass(CLASS_NAME, ID), //
				aNotActiveClass(NOT_ACTIVE_CLASS_NAME, NOT_ACTIVE_ID));
	}

	private DBClass anActiveClass(final String className, final Long id) {
		return aClass(className, id, true);
	}

	private DBClass aNotActiveClass(final String className, final Long id) {
		return aClass(className, id, false);
	}

	private DBClass aClass(final String className, final Long id, final boolean active) {
		final ClassMetadata classMetadata = new ClassMetadata();
		classMetadata.put(EntryTypeMetadata.ACTIVE, Boolean.valueOf(active).toString());
		return DBClass.newClass() //
				.withName(className) //
				.withId(id) //
				.withAllMetadata(classMetadata) //
				.withAllAttributes(Collections.<DBAttribute> emptyList()) //
				.build();
	}

	private int sizeOf(final Iterable<?> iterable) {
		return Iterators.size(iterable.iterator());
	}

}
