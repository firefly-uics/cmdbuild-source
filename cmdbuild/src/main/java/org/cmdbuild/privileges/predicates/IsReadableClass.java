package org.cmdbuild.privileges.predicates;

import static org.cmdbuild.services.store.menu.MenuConstants.ELEMENT_CLASS_ATTRIBUTE;
import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Predicate;

public class IsReadableClass implements Predicate<CMCard> {

	private final PrivilegeContext privilegeContext;
	private final CMDataView view;

	public IsReadableClass(final CMDataView view, final CMGroup group) {
		privilegeContext = applicationContext().getBean(PrivilegeContextFactory.class).buildPrivilegeContext(group);
		this.view = view;
	}

	@Override
	public boolean apply(final CMCard input) {
		final Long idElementClass = input.get(ELEMENT_CLASS_ATTRIBUTE, Long.class);
		if (idElementClass == null) {
			return false;
		}
		final CMClass referencedClass = view.findClass(idElementClass);
		if (referencedClass == null) {
			return false;
		}
		return privilegeContext.hasReadAccess(referencedClass);
	}

}
