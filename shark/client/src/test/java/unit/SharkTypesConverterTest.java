package unit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMLookupType;
import org.cmdbuild.dao.reference.CardReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.workflow.SharkTypesConverter;
import org.cmdbuild.workflow.TypesConverter;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.joda.time.DateTime;
import org.junit.Test;


public class SharkTypesConverterTest {

	final CMDataView dataView = mock(CMDataView.class);
	final TypesConverter converter = new SharkTypesConverter(dataView);

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
		long instant = 123456L;
		assertConverted(new DateTime(instant), new Date(instant));
	}

	@Test
	public void bigDecimalAreConvertedToDouble() {
		assertConverted(new BigDecimal(1.5d), 1.5d);
	}

	@Test
	public void lookupsAreConvertedToLookupTypeDTOs() {
		CMLookup src = mock(CMLookup.class);
		CMLookupType type = mock(CMLookupType.class);
		when(type.getName()).thenReturn("t");
		when(src.getType()).thenReturn(type);
		when(src.getId()).thenReturn(42L);
		when(src.getCode()).thenReturn("c");
		when(src.getDescription()).thenReturn("d");
		
		LookupType dst = LookupType.class.cast(converter.toWorkflowType(src));

		assertThat(dst.getType(), is("t"));
		assertThat(dst.getId(), is(42));
		assertThat(dst.getCode(), is("c"));
		assertThat(dst.getDescription(), is("d"));
	}

	@Test
	public void cardReferencesAreConvertedToReferenceTypeDTOs() {
		CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(12);
		CardReference src = CardReference.newInstance(srcClass.getName(), 42, null);
		when(dataView.findClassByName(srcClass.getName())).thenReturn(srcClass);
		
		ReferenceType dst = ReferenceType.class.cast(converter.toWorkflowType(src));

		assertThat(dst.getId(), is(42));
		assertThat(dst.getIdClass(), is(srcClass.getId()));
		assertThat(dst.getDescription(), is(StringUtils.EMPTY));
	}

	@Test
	public void cardReferenceArraysAreConvertedToReferenceTypeDTOArrays() {
		CMClass srcClass = mock(CMClass.class);
		when(srcClass.getName()).thenReturn("CN");
		when(srcClass.getId()).thenReturn(12);
		CardReference src0 = CardReference.newInstance(srcClass.getName(), 42, null);
		when(dataView.findClassByName(srcClass.getName())).thenReturn(srcClass);
		CardReference[] src = new CardReference[] { src0 };

		ReferenceType[] dst = ReferenceType[].class.cast(converter.toWorkflowType(src));

		assertThat(dst.length, is(src.length));
		ReferenceType dst0 = dst[0];
		assertThat(dst0.getId(), is(42));
		assertThat(dst0.getIdClass(), is(srcClass.getId()));
		assertThat(dst0.getDescription(), is(StringUtils.EMPTY));
	}

	@Test
	public void lookupTypesAreConvertedToInteger() {
		LookupType src = mock(LookupType.class);

		when(src.getType()).thenReturn("t");
		when(src.getId()).thenReturn(42);
		when(src.getCode()).thenReturn("c");
		when(src.getDescription()).thenReturn("d");

		Integer dst = Integer.class.cast(converter.fromWorkflowType(src));

		assertThat(dst, is(42));
	}

	@Test
	public void referenceTypesAreConvertedToInteger() {
		ReferenceType src = mock(ReferenceType.class);

		when(src.getId()).thenReturn(42);
		Integer dst = Integer.class.cast(converter.fromWorkflowType(src));
		assertThat(dst, is(42));
	}

	/*
	 * Utils
	 */

	private void assertNotConverted(Object src) {
		assertThat(converter.toWorkflowType(src), is(sameInstance(src)));
	}

	private void assertConverted(Object src, Object dst) {
		assertThat(converter.toWorkflowType(src), is(dst));
	}

}
