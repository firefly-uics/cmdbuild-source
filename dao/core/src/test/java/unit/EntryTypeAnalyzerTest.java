package unit;

import static org.cmdbuild.dao.entrytype.EntryTypeAnalyzer.inspect;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.junit.Test;

import com.google.common.collect.Lists;

public class EntryTypeAnalyzerTest {
	
	
	@Test(expected = IllegalArgumentException.class)
	public void shouldNotCreateAEntryTypeAnalyzerWithNullEntryType() throws Exception {
		inspect(null);
	}
	
	@Test
	public void shouldReturnFalseIfTheEntryTypeDoesNotHaveExternalReferenceActiveAndNotReservedAttributes() throws Exception {
		//given
		final CMAttribute integerAttribute = mockAttribute(new IntegerAttributeType(), false, true);
		final CMAttribute booleanAttribute = mockAttribute(new BooleanAttributeType(), false, true);
		final List<CMAttribute> entryTypeAttributes = Lists.newArrayList();
		entryTypeAttributes.add(integerAttribute);
		entryTypeAttributes.add(booleanAttribute);
		final CMClass clazz = mockClass("foo", entryTypeAttributes);
		
		//when
		boolean b = inspect(clazz).hasExternalReferences();
		
		//then
		assertFalse(b);
	}
	
	@Test
	public void shouldNotConsiderInactiveAttributes() throws Exception {
		
	}
	
	@SuppressWarnings("rawtypes")
	private CMAttribute mockAttribute(final CMAttributeType<?> attributeType, final boolean isSystem, final boolean isActive) {
		final CMAttribute attribute = mock(CMAttribute.class);
		when(attribute.isSystem()).thenReturn(isSystem);
		when(attribute.isActive()).thenReturn(isActive);
		when((CMAttributeType)attribute.getType()).thenReturn(attributeType);
		return attribute;
	}
	
	@SuppressWarnings("unchecked")
	private CMClass mockClass(final String name, final Iterable<CMAttribute> attributes) {
		final CMClass clazz = mock(CMClass.class);
		when(clazz.getName()).thenReturn("foo");
		when((Iterable<CMAttribute>)clazz.getActiveAttributes()).thenReturn(attributes);
		return clazz;
	}
	
}
