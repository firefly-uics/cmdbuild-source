package org.cmdbuild.dao.view;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;

public interface CMDataView {

//	public CMClassDefinition newClass(final String name);
	public CMClass findClassById(final Object id);
	public CMClass findClassByName(final String name);
	public Iterable<? extends CMClass> findAllClasses();

	public CMCardDefinition newCard(final CMClass type);
	public CMCardDefinition modifyCard(final CMCard type);
//	public CMQuery select(String attributeName);
}
