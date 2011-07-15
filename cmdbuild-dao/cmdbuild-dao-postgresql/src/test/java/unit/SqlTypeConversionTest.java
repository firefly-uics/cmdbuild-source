package unit;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.BooleanAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DecimalAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.junit.Test;

public class SqlTypeConversionTest {

	private static final AttributeMetadata NO_META = new AttributeMetadata();

	@Test
	public void testConversionFromSql() {
		CMAttributeType type;

		type = SqlType.createAttributeType("bool", NO_META);
		assertThat(type, instanceOf(BooleanAttributeType.class));

		type = SqlType.createAttributeType("varchar(20)", NO_META);
		assertThat(type, instanceOf(StringAttributeType.class));
		assertThat(((StringAttributeType) type).length, is(20));

		type = SqlType.createAttributeType("numeric(43,21)", NO_META);
		assertThat(type, instanceOf(DecimalAttributeType.class));
		assertThat(((DecimalAttributeType) type).precision, is(43));
		assertThat(((DecimalAttributeType) type).scale, is(21));

		type = SqlType.createAttributeType("regclass", NO_META);
		assertThat(type, instanceOf(UndefinedAttributeType.class));
	}
}
