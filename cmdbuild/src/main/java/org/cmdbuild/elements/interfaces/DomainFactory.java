package org.cmdbuild.elements.interfaces;

import org.cmdbuild.exception.NotFoundException;

public interface DomainFactory {

	IDomain create();
	IDomain get(int domainId) throws NotFoundException;
	IDomain get(String domainName) throws NotFoundException;

	DomainQuery list(ITable table);
	Iterable<IDomain> list();
}
