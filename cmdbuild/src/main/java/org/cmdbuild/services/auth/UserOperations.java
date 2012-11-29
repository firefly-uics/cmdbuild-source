package org.cmdbuild.services.auth;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.DomainFactoryImpl;
import org.cmdbuild.elements.ProcessTypeFactoryImpl;
import org.cmdbuild.elements.RelationFactoryImpl;
import org.cmdbuild.elements.TableFactoryImpl;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;

@Legacy("temporary, must be removed")
public class UserOperations {

	private final UserContext context;

	private UserOperations(final UserContext context) {
		this.context = context;
	}

	public ProcessTypeFactory processTypes() {
		return new ProcessTypeFactoryImpl(context);
	}

	public RelationFactory relations() {
		return new RelationFactoryImpl(context);
	}

	public ITableFactory tables() {
		return new TableFactoryImpl(context);
	}

	public DomainFactory domains() {
		return new DomainFactoryImpl(context);
	}
	
	public static UserOperations from(UserContext context) {
		return new UserOperations(context);
	}

}
