package org.cmdbuild.dao.driver.postgres.query;

import org.cmdbuild.dao.driver.postgres.quote.AliasQuoter;
import org.cmdbuild.dao.query.QuerySpecs;

public class FromPartCreator extends PartCreator {

	public FromPartCreator(final QuerySpecs query) {
		super();
		sb.append("FROM ");
		/*
		 * TODO check if this is really needed
		 * 
		 * if (query.getFromType().holdsHistory()) { sb.append("ONLY "); }
		 */
		sb.append(quoteType(query.getFromType())).append(" AS ").append(AliasQuoter.quote(query.getFromAlias()));
	}

}
