package org.cmdbuild.elements.proxy;

import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.services.auth.UserContext;

public class CardProxy extends CardForwarder {
	protected org.cmdbuild.services.auth.UserContext userCtx;

	public CardProxy(ICard card, UserContext userCtx) {
		super(card);
		this.userCtx = userCtx;
	}

	@Override
	public ITable getSchema() {
		ITable table =  super.getSchema();
		if (!(table instanceof TableProxy))
			table = new TableProxy(table, userCtx);
		return table;
	}

	@Override
	public void delete() {
		userCtx.privileges().assureWritePrivilege(c.getSchema());
		super.delete();
	}

	@Override
	public void save() throws ORMException {
		if (c.isNew()) {
			userCtx.privileges().assureCreatePrivilege(c.getSchema());
		} else {
			userCtx.privileges().assureWritePrivilege(c.getSchema());
		}
		c.setValue("User", userCtx.getUsername());
		super.save();
	}
}
