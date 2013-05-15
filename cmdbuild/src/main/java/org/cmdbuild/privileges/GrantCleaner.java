package org.cmdbuild.privileges;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.*;
import static org.cmdbuild.dao.query.clause.AnyAttribute.*;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.*;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.*;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;

/**
 * The goal of this class is to keep clean the Grant table, in which are stored
 * grants for all privileged objects and groups. It is useful when I remove a
 * View or a Filter from its table.
 */
public class GrantCleaner {

	private final CMDataView view;

	public GrantCleaner(final CMDataView view) {
		this.view = view;
	}

	public void deleteGrantReferingTo(final Long privilegedObjectId) {
		final CMClass grantClass = view.findClass(GRANT_CLASS_NAME);
		CMQueryResult result = view.select(anyAttribute(grantClass)) //
				.from(grantClass) //
				.where(condition(attribute(grantClass, PRIVILEGED_OBJECT_ID_ATTRIBUTE), eq(privilegedObjectId))) //
				.run();
		for (CMQueryRow row : result) {
			final CMCard cardToDelete = row.getCard(grantClass);
			view.delete(cardToDelete);
		}
	}

}
