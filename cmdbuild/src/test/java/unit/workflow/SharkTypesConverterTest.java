package unit.workflow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.cmdbuild.common.Constants;
import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMLookupType;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.workflow.SharkTypesConverter;
import org.cmdbuild.workflow.WorkflowTypesConverter;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.joda.time.DateTime;
import org.junit.Test;

public class SharkTypesConverterTest {

	final CMDataView dataView = mock(CMDataView.class);
	final WorkflowTypesConverter converter = new SharkTypesConverter(dataView);

	@Test
	public void returnsTheSameObjectIfConversionNotNeeded() {
		assertThat(converter.toWorkflowType(null), is(nullValue()));
		assertNotConverted("Fifteen");
		assertNotConverted(true);
		assertNotConverted(false);
	}

	@Test
	public void integersAreConvertedToLong() {
		assertConverted(15, 15L);
	}

	@Test
	public void jodaDateTimesAreConvertedToJavaDates() {
		final long instant = 123456L;
		assertConverted(new DateTime(instant), new Date(instant));
	}

	@Test
	public void bigDecimalAreConvertedToDouble() {
		assertConverted(new BigDecimal(1.5d), 1.5d);
	}

	@Test
	public void lookupsAreConvertedToLookupTypeDTOs() {
		final CMLookup src = mock(CMLookup.class);
		final CMLookupType type = mock(CMLookupType.class);
		when(type.getName()).thenReturn("t");
		when(src.getType()).thenReturn(type);
		when(src.getId()).thenReturn(42L);
		when(src.getCode()).thenReturn("c");
		when(src.getDescription()).thenReturn("d");

		final LookupType dst = LookupType.class.cast(converter.toWorkflowType(src));

		assertThat(dst.getType(), is("t"));
		assertThat(dst.getId(), is(42));
		assertThat(dst.getCode(), is("c"));
		assertThat(dst.getDescription(), is("d"));
	}

	@Test
	public void cardReferencesAreConvertedToReferenceTypeDTOs() {
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(12L);
		final CardReference src = CardReference.newInstance(srcClass.getName(), 42L, "D");
		when(dataView.findClassByName(srcClass.getName())).thenReturn(srcClass);

		final ReferenceType dst = ReferenceType.class.cast(converter.toWorkflowType(src));

		assertThat(dst.getId(), is(42));
		assertThat(dst.getIdClass(), is(12));
		assertThat(dst.getDescription(), is("D"));
	}

	@Test
	public void cardReferenceArraysAreConvertedToReferenceTypeDTOArrays() {
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(12L);
		final CardReference src0 = CardReference.newInstance(srcClass.getName(), 42L, "D");
		when(dataView.findClassByName(srcClass.getName())).thenReturn(srcClass);
		final CardReference[] src = new CardReference[] { src0 };

		final ReferenceType[] dst = ReferenceType[].class.cast(converter.toWorkflowType(src));

		assertThat(dst.length, is(src.length));
		final ReferenceType dst0 = dst[0];
		assertThat(dst0.getId(), is(42));
		assertThat(dst0.getIdClass(), is(12));
		assertThat(dst0.getDescription(), is("D"));
	}

	@Test
	public void lookupTypesAreConvertedToInteger() {
		final LookupType src = new LookupType();
		src.setType("t");
		src.setId(42);
		src.setCode("c");
		src.setDescription("d");

		final Integer dst = Integer.class.cast(converter.fromWorkflowType(src));

		assertThat(dst, is(42));

		assertThat(converter.fromWorkflowType(new LookupType()), is(nullValue()));
	}

	@Test
	public void referenceTypesAreConvertedToCardReferenceWithoutDescription() {
		final ReferenceType src = new ReferenceType();
		src.setIdClass(666);
		src.setId(42);
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(666L);
		when(dataView.findClassById(666L)).thenReturn(srcClass);

		final CardReference dst = CardReference.class.cast(converter.fromWorkflowType(src));
		assertThat(dst.getId(), is(42L));
		assertThat(dst.getClassName(), is("CN"));

		verify(dataView, times(1)).findClassById(666L);
		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void illegalReferenceTypesAreConvertedToNull() {
		assertThat(converter.fromWorkflowType(new ReferenceType()), is(nullValue()));

		verifyNoMoreInteractions(dataView);
	}

	@Test
	public void referenceTypesWithIllegalClassIdAreFetchedFromTheDataStore() {
		final ReferenceType src = new ReferenceType();
		src.setIdClass(666);
		src.setId(42);
		final CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(666L);
		when(dataView.findClassById(666L)).thenReturn(null);

		final CardReference dst = CardReference.class.cast(converter.fromWorkflowType(src));
		assertThat(dst.getId(), is(42L));
		assertThat(dst.getClassName(), is(Constants.BASE_CLASS_NAME));

		verify(dataView, times(1)).findClassById(666L);
		verifyNoMoreInteractions(dataView);
	}

	/*
	 * Utils
	 */

	private void assertNotConverted(final Object src) {
		assertThat(converter.toWorkflowType(src), is(sameInstance(src)));
	}

	private void assertConverted(final Object src, final Object dst) {
		assertThat(converter.toWorkflowType(src), is(dst));
	}

}
