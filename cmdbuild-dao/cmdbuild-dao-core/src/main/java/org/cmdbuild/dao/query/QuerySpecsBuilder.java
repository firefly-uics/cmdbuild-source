package org.cmdbuild.dao.query;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.AnyClass.anyClass;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.NamedAttribute;
import org.cmdbuild.dao.query.clause.NamedClass;
import org.cmdbuild.dao.view.CMDataView;

public class QuerySpecsBuilder {

	private List<CMAttribute> attributes;
	private CMClass fromClass;

//	private final DataView dataView;

	public QuerySpecsBuilder(CMDataView dataView) {
//		this.dataView = dataView;
		select(anyAttribute());
		from(anyClass());
	}

	public QuerySpecsBuilder select(final List<CMAttribute> attributes) {
		this.attributes = attributes;
		return this;
	}

	public QuerySpecsBuilder select(final CMAttribute a) {
		final List<CMAttribute> attributes = new ArrayList<CMAttribute>(1);
		attributes.add(a);
		return select(attributes);
	}

	public QuerySpecsBuilder select(final String... attributeNames) {
		final List<CMAttribute> attributes = new ArrayList<CMAttribute>(attributeNames.length);
		for (String name : attributeNames) {
			attributes.add(new NamedAttribute(name));
		}
		return select(attributes);
	}


	public QuerySpecsBuilder from(CMClass fromClass) {
		this.fromClass = fromClass;
		return this;
	}

	public QuerySpecsBuilder from(String className) {
		return from(new NamedClass(className));
	}


	public String toCQL2() {
		String[] queryParts = { toCql2Select(), toCql2From() };
		return StringUtils.join(queryParts, " ");
	}

	private String toCql2Select() {
		final List<String> attributeNames = new ArrayList<String>(attributes.size());
		for (CMAttribute a : attributes) {
			attributeNames.add(a.getName());
		}
		return "SELECT " + StringUtils.join(attributeNames, ", ");
	}

	private String toCql2From() {
		return "FROM " + fromClass.getName();
	}

	public String toString() {
		return toCQL2();
	}

	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

//	private QuerySpecs prepare() {
//		return new QuerySpecs();
//	}
}
