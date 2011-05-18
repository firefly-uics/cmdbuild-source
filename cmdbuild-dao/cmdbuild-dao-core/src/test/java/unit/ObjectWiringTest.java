package unit;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.junit.Test;

public class ObjectWiringTest {

	@Test
	public void attributesAreBoundToEntryTypes() {
		final DBAttribute a = new DBAttribute("_id");
		final Collection<DBAttribute> attributes = new ArrayList<DBAttribute>();
		attributes.add(a);
		final CMEntryType et = new DBClass("A", 42, attributes);
		
		assertThat(a.getOwner(), is(et));
	}
}
