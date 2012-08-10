package org.cmdbuild.elements;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessFactory;
import org.cmdbuild.elements.interfaces.ProcessType;
import org.cmdbuild.elements.proxy.TableProxy;
import org.cmdbuild.services.auth.UserContext;

public class ProcessTypeImpl extends TableProxy implements ProcessType {

	public ProcessTypeImpl(ITable table, UserContext userCtx) {
		super(table, userCtx);
	}

	public ProcessFactory cards() {
		return new ProcessFactoryImpl(this, userCtx);
	}

}
