package org.cmdbuild.elements;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.proxy.TableTreeProxy;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.services.auth.UserContext;

public class ProcessTypeFactoryImpl implements ProcessTypeFactory {
	UserContext userCtx;

	public ProcessTypeFactoryImpl(UserContext userCtx) {
		this.userCtx = userCtx;
	}

	public ProcessType create() {
		if (!userCtx.privileges().isAdmin())
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		ITable realTable = new TableImpl();
		return new ProcessTypeImpl(realTable, userCtx);
	}

	public ProcessType get(int classId) throws NotFoundException {
		ITable realTable = TableImpl.get(classId);
		userCtx.privileges().assureReadPrivilege(realTable);
		return new ProcessTypeImpl(realTable, userCtx);
	}

	public ProcessType get(String className) throws NotFoundException {
		ITable realTable = TableImpl.get(className);
		userCtx.privileges().assureReadPrivilege(realTable);
		return new ProcessTypeImpl(realTable, userCtx);
	}

	public Iterable<ProcessType> list() {
		List<ProcessType> list = new LinkedList<ProcessType>();
		for (ITable realTable : TableImpl.list()) {
			list.add(new ProcessTypeImpl(realTable, userCtx));
		}
		return list;
	}

	public TableTree tree() {
		TableTree realTree = fullTree().branch(ProcessType.BaseTable);
		return new TableTreeProxy(realTree, userCtx);
	}

	public TableTree fullTree() {
		TableTree realTree = TableImpl.tree();
		return new TableTreeProxy(realTree, userCtx);
	}
}
