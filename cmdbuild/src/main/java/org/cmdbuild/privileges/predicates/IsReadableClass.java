package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;

import com.google.common.base.Predicate;

public class IsReadableClass implements Predicate<CMCard> {

	private final PrivilegeContext privilegeContext;
	private final CMDataView view;

	public IsReadableClass(final CMDataView view, final CMGroup group) {
		privilegeContext = TemporaryObjectsBeforeSpringDI.getPrivilegeContextFactory().buildPrivilegeContext(group);
		this.view = view;
	}

	@Override
	public boolean apply(final CMCard input) {
		final Object idElementClass = input.get(ELEMENT_CLASS_ATTRIBUTE);
		if (idElementClass == null) {
			return false;
		}
		final EntryTypeReference entryTypeReference = (EntryTypeReference) idElementClass;
		final CMClass referencedClass = view.findClass(entryTypeReference.getId());
		if (referencedClass == null) {
			return false;
		}
		return privilegeContext.hasReadAccess(referencedClass);
	}

}
