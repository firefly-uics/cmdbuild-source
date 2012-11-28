package unit;

import static org.cmdbuild.dao.query.clause.FunctionCall.call;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.dao.driver.postgres.Utils;
import org.cmdbuild.dao.driver.postgres.Utils.ParamAdder;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.StringAttributeType;
import org.cmdbuild.dao.function.DBFunction;
import org.junit.Test;

public class QuotingTest {

	static private final Long USELESS_FUNCTION_ID = null;

	@Test
	public void identStringsAreQuoted() {
		assertThat(Utils.quoteIdent("xy"), is("xy"));
		assertThat(Utils.quoteIdent("x1y"), is("x1y"));
		assertThat(Utils.quoteIdent("x+y"), is("\"x+y\""));
		assertThat(Utils.quoteIdent("x'y"), is("\"x'y\""));
		assertThat(Utils.quoteIdent("x\"y"), is("\"x\"\"y\""));
		assertThat(Utils.quoteIdent("XY"), is("\"XY\""));
		assertThat(Utils.quoteIdent("X\"Y"), is("\"X\"\"Y\""));
	}

	@Test
	public void functionCallsAreQuoted() {
		final List<Object> params = new ArrayList<Object>();
		DBFunction func = new DBFunction("func", USELESS_FUNCTION_ID, true);
		assertThat(Utils.quoteType(call(func), new ParamAdder() {			
			@Override
			public void add(final Object value) {
				params.add(value);
			}
		}), is("func()"));

		func.addInputParameter("i1", new IntegerAttributeType());
		func.addInputParameter("i2", new StringAttributeType());
		func.addInputParameter("i3", new IntegerAttributeType());
		assertThat(Utils.quoteType(call(func, 42, "s", "24"), new ParamAdder() {			
			@Override
			public void add(Object value) {
				params.add(value);
			}
		}), is("func(?,?,?)"));
		assertThat(params.get(0), is((Object)42));
		assertThat(params.get(1), is((Object)"s"));
		assertThat(params.get(2), is((Object)24));
	}
}
