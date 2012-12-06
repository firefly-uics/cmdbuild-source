package unit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBAttribute.AttributeMetadata;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.junit.Test;

public class ObjectWiringTest {

	@Test
	public void attributesAreBoundToEntryTypes() {
		final DBAttribute a = new DBAttribute("X", new IntegerAttributeType(), new AttributeMetadata());
		final List<DBAttribute> attributes = new ArrayList<DBAttribute>();
		attributes.add(a);
		final CMEntryType et = new DBClass("A", 42L, attributes);

		assertThat(a.getOwner(), is(et));
	}
}
