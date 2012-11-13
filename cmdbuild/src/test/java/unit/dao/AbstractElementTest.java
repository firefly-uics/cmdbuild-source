package unit.dao;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.elements.AbstractElementImpl;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.BaseSchemaImpl;
import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.exception.ORMException;
import org.junit.Test;

public class AbstractElementTest {

	private static BaseSchema FAKE_SCHEMA;
	private static String FAKE_INT_ATTRIBUTE_NAME;

	static {
		FAKE_INT_ATTRIBUTE_NAME = "Fake";
		FAKE_SCHEMA = new BaseSchemaImpl() {
			
			protected boolean isNew() { return false; }
			public void readDataDefinitionMeta(Map<String, String> dataDefinitionMeta) {};
			public Map<String, String> genDataDefinitionMeta() { return null; }
			protected Map<String, IAttribute> loadAttributes() {
				final Map<String, IAttribute> attrs = new HashMap<String, IAttribute>();
				attrs.put("Id", AttributeImpl.create(this, "Id", AttributeType.INTEGER));
				attrs.put(FAKE_INT_ATTRIBUTE_NAME, AttributeImpl.create(this, FAKE_INT_ATTRIBUTE_NAME, AttributeType.INTEGER));
				return attrs;
			}
			public String getPrivilegeId() { return null; }
		};
	}

	private static class AbstractElementTestDouble extends AbstractElementImpl {

		private int timesCreateCalled = 0;
		private int timesModifyCalled = 0;

		private AbstractElementTestDouble() {
			super();
			this.schema = FAKE_SCHEMA;
		}

		@Override
		protected int create() throws ORMException {
			++timesCreateCalled;
			return 42;
		}

		@Override
		protected void modify() throws ORMException {
			++timesModifyCalled;
		}

		@Override
		public String toString() {
			return null;
		}
	}

	@Test
	public void saveDoesNotSaveIfNothingChanged() {
		AbstractElementTestDouble element = new AbstractElementTestDouble();

		element.setValue(FAKE_INT_ATTRIBUTE_NAME, 2);
		element.save();

		assertEquals(1, element.timesCreateCalled);
		assertEquals(0, element.timesModifyCalled);

		element.setValue(FAKE_INT_ATTRIBUTE_NAME, 2);
		element.save();

		assertEquals(1, element.timesCreateCalled);
		assertEquals(0, element.timesModifyCalled);
	}

	@Test
	public void forceSaveAlwaysSaves() {
		AbstractElementTestDouble element = new AbstractElementTestDouble();

		element.setValue(FAKE_INT_ATTRIBUTE_NAME, 2);
		element.forceSave();

		assertEquals(1, element.timesCreateCalled);
		assertEquals(0, element.timesModifyCalled);

		element.setValue(FAKE_INT_ATTRIBUTE_NAME, 2);
		element.forceSave();

		assertEquals(1, element.timesCreateCalled);
		assertEquals(1, element.timesModifyCalled);
	}
}
