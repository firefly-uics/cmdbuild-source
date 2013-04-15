package org.cmdbuild.servlets.utils.builder;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.elements.interfaces.BaseSchema;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class BaseSchemaParameter extends AbstractParameterBuilder<BaseSchema> {

	public static final String TABLE_ID_PARAMETER = "tableId";

	@Override
	public BaseSchema build(final HttpServletRequest r) throws Exception {
		BaseSchema table;

		final int tableId = parameter(Integer.TYPE, TABLE_ID_PARAMETER, r);

		if (tableId > 0) {
			table = getTableFromId(tableId);
		} else {
			table = null;
		}
		return table;
	}

	private BaseSchema getTableFromId(final int tableId) {
		BaseSchema table;
		final UserContext userCtx = new SessionVars().getCurrentUserContext();
		try {
			table = UserOperations.from(userCtx).tables().get(tableId);
		} catch (final Exception e1) {
			try {
				table = UserOperations.from(userCtx).domains().get(tableId);
			} catch (final Exception e2) {
				table = null;
			}
		}
		return table;
	}
}
