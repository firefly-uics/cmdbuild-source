package unit;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeTypeVisitor;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.cmdbuild.dao.query.QuerySpecsImpl;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.junit.Test;

public class SimpleFunctionQueryCreatorTest {

	static private final Long USELESS_FUNCTION_ID = null;

	private static class IdentityAttributeType implements CMAttributeType<Object> {

		@Override
		public Object convertValue(Object value) {
			return value;
		}

		@Override
		public void accept(CMAttributeTypeVisitor visitor) {
			throw new UnsupportedOperationException();
		}

	}

	DBFunction setFunc = new DBFunction("func", USELESS_FUNCTION_ID, true);
	Alias f = Alias.as("f");

	@Test
	public void withAttributeListAndNoParameters() {
		setFunc.addOutputParameter("o1", new UndefinedAttributeType());
		setFunc.addOutputParameter("o2", new UndefinedAttributeType());
		QuerySpecsImpl querySpecs = new QuerySpecsImpl(call(setFunc), f);
		querySpecs.addSelectAttribute(attribute(f, "o2"));
		querySpecs.addSelectAttribute(attribute(f, "o1"));

		String sql = new QueryCreator(querySpecs).getQuery();
		assertThat(sql, is("SELECT f.o2,f.o1 FROM func() AS f  ")); // Extra spaces
	}

	@Test
	public void withAttributeListAndParameters() {
		setFunc.addInputParameter("i1", new IdentityAttributeType());
		setFunc.addInputParameter("i2", new IdentityAttributeType());
		setFunc.addInputParameter("i3", new IdentityAttributeType());
		QuerySpecsImpl querySpecs = new QuerySpecsImpl(call(setFunc, "12", 34, null), f);
		querySpecs.addSelectAttribute(attribute(f, "o"));

		QueryCreator queryCreator = new QueryCreator(querySpecs);
		assertThat(queryCreator.getQuery(), is("SELECT f.o FROM func(?,?,?) AS f  ")); // Extra spaces
		assertThat(queryCreator.getParams()[0], is((Object)"12"));
		assertThat(queryCreator.getParams()[1], is((Object)34));
		assertThat(queryCreator.getParams()[2], is(nullValue()));
	}
}
