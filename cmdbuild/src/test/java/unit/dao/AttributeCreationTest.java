package unit.dao;

import static org.junit.Assert.assertEquals;

import org.cmdbuild.dao.attribute.BinaryAttribute;
import org.cmdbuild.dao.attribute.BooleanAttribute;
import org.cmdbuild.dao.attribute.CharAttribute;
import org.cmdbuild.dao.attribute.DateAttribute;
import org.cmdbuild.dao.attribute.DateTimeAttribute;
import org.cmdbuild.dao.attribute.DecimalAttribute;
import org.cmdbuild.dao.attribute.DoubleAttribute;
import org.cmdbuild.dao.attribute.ForeignKeyAttribute;
import org.cmdbuild.dao.attribute.IPAddressAttribute;
import org.cmdbuild.dao.attribute.IntArrayAttribute;
import org.cmdbuild.dao.attribute.IntegerAttribute;
import org.cmdbuild.dao.attribute.LookupAttribute;
import org.cmdbuild.dao.attribute.ReferenceAttribute;
import org.cmdbuild.dao.attribute.RegclassAttribute;
import org.cmdbuild.dao.attribute.StringArrayAttribute;
import org.cmdbuild.dao.attribute.StringAttribute;
import org.cmdbuild.dao.attribute.TextAttribute;
import org.cmdbuild.dao.attribute.TimeAttribute;
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

	private Object createdClassFor(final AttributeType type) {
		return AttributeImpl.create(null, null, type, null).getClass();
	}

}
