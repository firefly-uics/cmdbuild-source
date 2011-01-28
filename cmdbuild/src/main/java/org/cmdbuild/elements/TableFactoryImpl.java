package org.cmdbuild.elements;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.proxy.TableProxy;
import org.cmdbuild.elements.proxy.TableTreeProxy;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.auth.UserContext;

public class TableFactoryImpl implements ITableFactory {
	UserContext userCtx;

	public TableFactoryImpl(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public ITable create() {
		if (!userCtx.privileges().isAdmin())
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		ITable realTable = new TableImpl(); 
		return new TableProxy(realTable, userCtx);
	}

	public ITable get(int classId) throws NotFoundException {
		ITable realTable = TableImpl.get(classId);
		userCtx.privileges().assureReadPrivilege(realTable);
		return new TableProxy(realTable, userCtx);
	}

	public ITable get(String className) throws NotFoundException {
		ITable realTable = TableImpl.get(className);
		userCtx.privileges().assureReadPrivilege(realTable);
		return new TableProxy(realTable, userCtx);
	}

	public Iterable<ITable> list() {
		return list(null);
	}

	public Iterable<ITable> list(CMTableType type) {
		List<ITable> list = new LinkedList<ITable>();
		for (ITable realTable : TableImpl.list()) {
			if ((type == null || type.equals(realTable.getTableType())) &&
					userCtx.privileges().hasReadPrivilege(realTable)) {
				list.add(new TableProxy(realTable, userCtx));
			}
		}
		return list;
	}

	public TableTree tree() {
		TableTree realTree = fullTree().exclude(ITable.BaseTable);
		return new TableTreeProxy(realTree, userCtx);
	}

	public TableTree fullTree() {
		TableTree realTree = TableImpl.tree();
		return new TableTreeProxy(realTree, userCtx);
	}
}
