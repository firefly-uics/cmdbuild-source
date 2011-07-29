package org.cmdbuild.dao.view;

import static com.google.common.collect.Iterables.filter;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

import com.google.common.base.Predicate;

public class UserDataView implements CMDataView {

	final DBDataView view;
	final CMAccessControlManager acm;

	public UserDataView(final DBDataView view, final CMAccessControlManager acm) {
		this.view = view;
		this.acm = acm;
	}

	@Override
	public CMClass findClass(Object idOrName) {
		// TODO
		return view.findClass(idOrName);
	}

	@Override
	public CMClass findClassById(Object id) {
		// TODO
		return view.findClassById(id);
	}

	@Override
	public CMClass findClassByName(String name) {
		// TODO
		return view.findClassByName(name);
	}

	/**
	 * Returns the active classes for which the user has read access.
	 * 
	 * @return active classes
	 */
	@Override
	public Iterable<? extends CMClass> findClasses() {
		return filterReadAccess(view.findClasses());
	}

	/**
	 * Returns all (active and inactive) classes if the user has Database
	 * Designer privileges, otherwise it falls back to {@link findClasses()}.
	 * 
	 * @return all classes (active and inactive)
	 */
	@Override
	public Iterable<? extends CMClass> findAllClasses() {
		if (acm.hasDatabaseDesignerPrivileges()) {
			return view.findAllClasses();
		} else {
			return findClasses();
		}
	}

	@Override
	public CMDomain findDomain(Object idOrName) {
		// TODO
		return view.findDomain(idOrName);
	}

	@Override
	public CMDomain findDomainById(Object id) {
		// TODO
		return view.findDomainById(id);
	}

	@Override
	public CMDomain findDomainByName(String name) {
		// TODO
		return view.findDomainByName(name);
	}

	/**
	 * Returns the active domains for which the user has read access.
	 * 
	 * @return active domains
	 */
	@Override
	public Iterable<? extends CMDomain> findDomains() {
		return filterReadAccess(view.findDomains());
	}

	/**
	 * Returns the active domains for a class for which the user has read
	 * access.
	 * 
	 * @param type the class i'm requesting the domains for
	 * 
	 * @return active domains for that class
	 */
	@Override
	public Iterable<? extends CMDomain> findDomainsFor(CMClass type) {
		return filterReadAccess(view.findDomainsFor(type));
	}

	/**
	 * Returns all (active and inactive) domains if the user has Database
	 * Designer privileges, otherwise it falls back to {@link findDomains()}.
	 * 
	 * @return all domains (active and inactive)
	 */
	@Override
	public Iterable<? extends CMDomain> findAllDomains() {
		if (acm.hasDatabaseDesignerPrivileges()) {
			return view.findAllDomains();
		} else {
			return findDomains();
		}
	}

	@Override
	public CMCardDefinition newCard(CMClass type) {
		// TODO
		return view.newCard(type);
	}

	@Override
	public CMCardDefinition modifyCard(CMCard card) {
		// TODO
		return view.modifyCard(card);
	}

	@Override
	public QuerySpecsBuilder select(Object... attrDef) {
		return view.select(attrDef);
	}


	private <T extends CMEntryType> Iterable<T> filterReadAccess(Iterable<T> unfiltered) {
		return filter(unfiltered, new Predicate<CMEntryType>() {
			@Override
			public boolean apply(CMEntryType type) {
				return acm.hasReadAccess(type);
			}
		});
	}
}
