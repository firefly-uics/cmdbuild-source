package integration;

import static org.cmdbuild.dao.query.clause.alias.Alias.as;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.cmdbuild.dao.driver.postgres.query.QueryCreator;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.EntryTypeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.query.QuerySpecsImpl;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;

public class QueryCreatorTest {

	private static class QuerySpecsDouble extends QuerySpecsImpl {
		
		QuerySpecsDouble() {
			super();
		}
	}

	private QuerySpecsDouble qs = new QuerySpecsDouble();

	private final CMClass sc;
	private final CMClass a;
	private final CMClass b;
	private final CMClass c;

	@SuppressWarnings("unchecked")
	public QueryCreatorTest() {
		sc = mock(CMClass.class);
		a = mock(CMClass.class);
		b = mock(CMClass.class);
		c = mock(CMClass.class);
		when(sc.getName()).thenReturn("nsc");
		when(a.getName()).thenReturn("na");
		when(b.getName()).thenReturn("nb");
		when(c.getName()).thenReturn("nc");
		when((Iterable<CMClass>)sc.getLeaves()).thenReturn(Lists.newArrayList(a, b, c));
		when((Iterable<CMClass>)a.getLeaves()).thenReturn(Lists.newArrayList(a));
		when((Iterable<CMClass>)b.getLeaves()).thenReturn(Lists.newArrayList(b));
		when((Iterable<CMClass>)c.getLeaves()).thenReturn(Lists.newArrayList(c));
	}

	@Test
	public void systemAttributesAreAlwaysReturned() {
		qs.setFrom(new ClassAlias(c, as("ac")));

		String query = new QueryCreator(qs).getQuery();

		assertThat(query, is(
				"SELECT "
					+ sqlAttribute("ac","IdClass","oid","_ac_ClassId") + ","
					+ sqlAttribute("ac","Id","_ac_Id") + ","
					+ sqlAttribute("ac","User","_ac_User") + ","
					+ sqlAttribute("ac","BeginDate","_ac_BeginDate")
				+ " FROM ONLY " + sqlQuoteIdent("nc") + " AS " + sqlQuoteIdent("ac")
				+ " WHERE " + sqlAttribute("ac","Status") + "=?"
			));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void anyAttributeReturnsAllAttributesAndSystem() {
		final CMAttribute ca = mock(CMAttribute.class); // "c" attribute
		when(ca.getName()).thenReturn("nca");
		when((CMAttributeType)ca.getType()).thenReturn(new UndefinedAttributeType());
		when((Iterable<CMAttribute>)c.getAttributes()).thenReturn(Lists.newArrayList(ca));
		when(c.getAttribute("nca")).thenReturn(ca);

		qs.setFrom(new ClassAlias(c, as("ac")));
		qs.addSelectAttribute(anyAttribute("ac"));

		String query = new QueryCreator(qs).getQuery();

		assertThat(query, is(
				"SELECT "
					+ sqlAttribute("ac","nc","nc#nca") + "," // WRONG!!!!!! IT OUTPUTS sqlAttribute("ac","nc#nca")
					+ sqlAttribute("ac","IdClass","oid","_ac_ClassId") + ","
					+ sqlAttribute("ac","Id","_ac_Id") + ","
					+ sqlAttribute("ac","User","_ac_User") + ","
					+ sqlAttribute("ac","BeginDate","_ac_BeginDate")
				+ " FROM ONLY " + sqlQuoteIdent("nc") + " AS " + sqlQuoteIdent("ac")
				+ " WHERE " + sqlAttribute("ac","Status") + "=?"
			));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void entryTypeAttributesAreCastToOid() {
		final CMAttribute beta = mock(CMAttribute.class); // "b" entry type attribute
		when(beta.getName()).thenReturn("beta");
		when((CMAttributeType)beta.getType()).thenReturn(new EntryTypeAttributeType());
		when((Iterable<CMAttribute>)b.getAttributes()).thenReturn(Lists.newArrayList(beta));
		when(b.getAttribute("beta")).thenReturn(beta);

		qs.setFrom(new ClassAlias(b, as("ab")));
		qs.addSelectAttribute(anyAttribute("ab"));

		String query = new QueryCreator(qs).getQuery();

		assertThat(query, is(
				"SELECT "
					+ sqlAttribute("ab","beta","oid","ab#beta") + "," // WRONG!!!!!! IT OUTPUTS sqlAttribute("ab","nb#beta")
					+ sqlAttribute("ab","IdClass","oid","_ab_ClassId") + ","
					+ sqlAttribute("ab","Id","_ab_Id") + ","
					+ sqlAttribute("ab","User","_ab_User") + ","
					+ sqlAttribute("ab","BeginDate","_ab_BeginDate")
				+ " FROM ONLY " + sqlQuoteIdent("nb") + " AS " + sqlQuoteIdent("ab")
				+ " WHERE " + sqlAttribute("ab","Status") + "=?"
			));
	}

	@Ignore
	@Test
	public void superclassesAreQueriedWithASubquery() {
		qs.setFrom(new ClassAlias(sc, as("asc")));

		String query = new QueryCreator(qs).getQuery();

		assertThat(query, is(
				"SELECT "
					+ sqlAttribute("ac","IdClass","oid","_ac_ClassId") + ","
					+ sqlAttribute("ac","Id","_ac_Id") + ","
					+ sqlAttribute("ac","User","_ac_User") + ","
					+ sqlAttribute("ac","BeginDate","_ac_BeginDate")
				+ " FROM ("
					+ "SELECT "
					+ sqlQuoteIdent("Id") + ","
					+ sqlQuoteIdent("IdClass") + ","
					+ sqlQuoteIdent("User") + ","
					+ sqlQuoteIdent("BeginDate") + ","
					+ "NULL AS " + sqlQuoteIdent("EndDate")
					+ " FROM ONLY " + sqlQuoteIdent("na")
					+ " UNION ALL "
					+ "SELECT "
					+ sqlQuoteIdent("Id") + ","
					+ sqlQuoteIdent("IdClass") + ","
					+ sqlQuoteIdent("User") + ","
					+ sqlQuoteIdent("BeginDate") + ","
					+ "NULL AS " + sqlQuoteIdent("EndDate")
					+ " FROM ONLY " + sqlQuoteIdent("nb")
					+ " UNION ALL "
					+ "SELECT "
					+ sqlQuoteIdent("Id") + ","
					+ sqlQuoteIdent("IdClass") + ","
					+ sqlQuoteIdent("User") + ","
					+ sqlQuoteIdent("BeginDate") + ","
					+ "NULL AS " + sqlQuoteIdent("EndDate")
					+ " FROM ONLY " + sqlQuoteIdent("nc")
				+ ") AS " + sqlQuoteIdent("ac")
				+ " WHERE " + sqlAttribute("ac","Status") + "=?"
			));
	}

	@Ignore
	@Test
	public void joinsWithASubclassOverOneDomainDoNotNeedUnions() {
		// THIS IS NOT TRUE: THE JOIN IS ALWAYS PRINTED
		fail();
	}

	@Ignore
	@Test
	public void joinsWithMultipleDomainsUseUnions() {
		// THIS IS PARTIALLY TRUE: CLASS UNIONS ARE USED EVEN IF THE TARGET IS ONE CLASS
		fail();
	}

	@Ignore
	@Test
	public void joinsWithSuperclassesUseUnions() {
		// THIS IS PARTIALLY TRUE: DOMAIN UNIONS ARE USED EVEN IF OVER ONE DOMAIN ONLY
		fail();
	}

	private String sqlAttribute(final String tableAlias, final String attributeName) {
		return sqlAttribute(tableAlias, attributeName, null, null);
	}

	private String sqlAttribute(final String tableAlias, final String attributeName, final String attributeAlias) {
		return sqlAttribute(tableAlias, attributeName, null, attributeAlias);
	}

	private String sqlAttribute(final String tableAlias, final String attributeName, final String cast, final String attributeAlias) {
		return String.format("%s.%s%s%s",
				sqlQuoteIdent(tableAlias), sqlQuoteIdent(attributeName),
				cast != null ? "::" + cast : "",
				attributeAlias != null ?  " AS " + sqlQuoteIdent(attributeAlias) : "");
	}

	private String sqlEscape(final String ident) {
		return ident.replace("\"", "\\\"");
	}

	private String sqlQuoteIdent(final String ident) {
		return String.format("\"%s\"", sqlEscape(ident));
	}
}
