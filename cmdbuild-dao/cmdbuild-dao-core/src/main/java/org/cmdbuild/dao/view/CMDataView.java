package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

public interface CMDataView {

//	CMClassDefinition newClass(String name);
	CMClass findClass(Object idOrName);
	CMClass findClassById(Object id);
	CMClass findClassByName(String name);
	Iterable<? extends CMClass> findAllClasses();

	Iterable<? extends CMDomain> findAllDomains();
	Iterable<? extends CMDomain> findDomains(CMClass type);
	CMDomain findDomain(Object idOrName);
	CMDomain findDomainById(Object id);
	DBDomain findDomainByName(String name);

	CMCardDefinition newCard(CMClass type);
	CMCardDefinition modifyCard(CMCard type);
	QuerySpecsBuilder select(Object... attrDef);
}
