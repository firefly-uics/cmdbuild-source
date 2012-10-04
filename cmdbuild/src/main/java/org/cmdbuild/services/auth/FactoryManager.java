package org.cmdbuild.services.auth;

import org.cmdbuild.elements.ProcessTypeFactoryImpl;
import org.cmdbuild.elements.DomainFactoryImpl;
import org.cmdbuild.elements.RelationFactoryImpl;
import org.cmdbuild.elements.TableFactoryImpl;
import org.cmdbuild.elements.interfaces.ProcessTypeFactory;
import org.cmdbuild.elements.interfaces.DomainFactory;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.ITableFactory;

class FactoryManager {

	final ITableFactory tableFactory;
	final DomainFactory domainFactory;
	final RelationFactory relationFactory;
	final ProcessTypeFactory processTypeFactory;

	FactoryManager(UserContext userCtx) {
		this.tableFactory = new TableFactoryImpl(userCtx);
		this.domainFactory = new DomainFactoryImpl(userCtx);
		this.relationFactory = new RelationFactoryImpl(userCtx);
		this.processTypeFactory = new ProcessTypeFactoryImpl(userCtx);
	}

	protected ITableFactory getTableFactory() {
		return tableFactory;
	}

	protected DomainFactory getDomainFactory() {
		return domainFactory;
	}

	protected RelationFactory getRelationFactory() {
		return relationFactory;
	}

	protected ProcessTypeFactory getProcessTypeFactory() {
		return processTypeFactory;
	}
}
