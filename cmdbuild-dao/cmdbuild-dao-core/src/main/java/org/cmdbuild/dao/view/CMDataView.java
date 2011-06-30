package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

public interface CMDataView {

//	public CMClassDefinition newClass(final String name);
	public CMClass findClassById(final Object id);
	public CMClass findClassByName(final String name);
	public Iterable<? extends CMClass> findAllClasses();

	public Iterable<? extends CMDomain> findAllDomains();
	public Iterable<? extends CMDomain> findDomains(CMClass type);
	public CMDomain findDomainById(final Object id);

	public CMCardDefinition newCard(final CMClass type);
	public CMCardDefinition modifyCard(final CMCard type);
	public QuerySpecsBuilder select(Object... attrDef);
}
