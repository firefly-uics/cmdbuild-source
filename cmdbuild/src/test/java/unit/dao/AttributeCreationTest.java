package unit.dao;

import static org.junit.Assert.*;

import org.cmdbuild.dao.attribute.*;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.junit.Test;


public class AttributeCreationTest {

	@Test
	public void createsTheCorrectAttributeType() {
		assertEquals(BinaryAttribute.class, createdClassFor(AttributeType.BINARY));
		assertEquals(BooleanAttribute.class, createdClassFor(AttributeType.BOOLEAN));
		assertEquals(CharAttribute.class, createdClassFor(AttributeType.CHAR));
		assertEquals(DateAttribute.class, createdClassFor(AttributeType.DATE));
		assertEquals(DecimalAttribute.class, createdClassFor(AttributeType.DECIMAL));
		assertEquals(DoubleAttribute.class, createdClassFor(AttributeType.DOUBLE));
		assertEquals(ForeignKeyAttribute.class, createdClassFor(AttributeType.FOREIGNKEY));
		assertEquals(IPAddressAttribute.class, createdClassFor(AttributeType.INET));
		assertEquals(IntArrayAttribute.class, createdClassFor(AttributeType.INTARRAY));
		assertEquals(IntegerAttribute.class, createdClassFor(AttributeType.INTEGER));
		assertEquals(LookupAttribute.class, createdClassFor(AttributeType.LOOKUP));
		assertEquals(ReferenceAttribute.class, createdClassFor(AttributeType.REFERENCE));
		assertEquals(RegclassAttribute.class, createdClassFor(AttributeType.REGCLASS));
		assertEquals(StringAttribute.class, createdClassFor(AttributeType.STRING));
		assertEquals(StringArrayAttribute.class, createdClassFor(AttributeType.STRINGARRAY));
		assertEquals(TextAttribute.class, createdClassFor(AttributeType.TEXT));
		assertEquals(TimeAttribute.class, createdClassFor(AttributeType.TIME));
		assertEquals(DateTimeAttribute.class, createdClassFor(AttributeType.TIMESTAMP));
	}

	private Object createdClassFor(AttributeType type) {
		return AttributeImpl.create(null, null, type, null).getClass();
	}

}
