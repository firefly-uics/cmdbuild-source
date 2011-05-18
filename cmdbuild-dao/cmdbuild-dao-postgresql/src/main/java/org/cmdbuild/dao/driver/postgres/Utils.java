package org.cmdbuild.dao.driver.postgres;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBEntryType;

public class Utils {

	public static final String ID_ATTRIBUTE = "Id";
	public static final String CLASS_ID_ATTRIBUTE = "IdClass";
	public static final String CODE_ATTRIBUTE = "Code";
	public static final String DESCRIPTION_ATTRIBUTE = "Description";

	static final String HISTORY_SUFFIX = "_history";

	private Utils() {};

	static String quoteIdent(final String name) {
		return String.format("\"%s\"", name.replace("\"", "\"\""));
	}

	static String quoteType(final CMEntryType type) {
		return quoteIdent(type.getName());
	}

	static String quoteTypeAndHistory(DBEntryType type) {
		return quoteType(type) + ", " + quoteIdent(type.getName() + HISTORY_SUFFIX);
	}

	static String quoteAttribute(final CMAttribute attribute) {
		//TODO return String.format("%s.%s", quoteType(attribute.getOwner()), quoteIdent(attribute.getName()));
		return quoteIdent(attribute.getName());
	}

	static String quoteAndJoin(final Iterable<CMAttribute> attributes) {
		final List<String> quoted = new ArrayList<String>();
		quoted.add(quoteIdent(CLASS_ID_ATTRIBUTE)+"::oid");
		for (CMAttribute a : attributes) {
			quoted.add(quoteAttribute(a));
		}
		return StringUtils.join(quoted, ",");
	}
}
