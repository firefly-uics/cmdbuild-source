package org.cmdbuild.dao.view.user;

import static org.cmdbuild.common.collect.Iterables.filterNotNull;
import static org.cmdbuild.common.collect.Iterables.map;

import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.DBEntryTypeVisitor;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.auth.CMAccessControlManager;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.QueryExecutorDataView;

public class UserDataView extends QueryExecutorDataView {

	private final DBDataView view;
	private final CMAccessControlManager acm;

	public UserDataView(final DBDataView view, final CMAccessControlManager acm) {
		this.view = view;
		this.acm = acm;
	}

	public CMAccessControlManager getAccessControlManager() {
		return acm;
	}

	@Override
	public UserClass findClass(Object idOrName) {
		return UserClass.create(this, view.findClass(idOrName));
	}

	@Override
	public UserClass findClassById(Object id) {
		return UserClass.create(this, view.findClassById(id));
	}

	@Override
	public UserClass findClassByName(String name) {
		return UserClass.create(this, view.findClassByName(name));
	}

	/**
	 * Returns the active classes for which the user has read access.
	 * 
	 * @return active classes
	 */
	@Override
	public Iterable<UserClass> findClasses() {
		return proxyClasses(view.findClasses());
	}

	/**
	 * Returns all (active and inactive) classes if the user has Database
	 * Designer privileges, otherwise it falls back to {@link findClasses()}.
	 * 
	 * @return all classes (active and inactive)
	 */
	@Override
	public Iterable<UserClass> findAllClasses() {
		return proxyClasses(view.findAllClasses());
	}

	@Override
	public UserDomain findDomain(Object idOrName) {
		return UserDomain.create(this, view.findDomain(idOrName));
	}

	@Override
	public UserDomain findDomainById(Object id) {
		return UserDomain.create(this, view.findDomainById(id));
	}

	@Override
	public UserDomain findDomainByName(String name) {
		return UserDomain.create(this, view.findDomainByName(name));
	}

	/**
	 * Returns the active domains for which the user has read access.
	 * 
	 * @return active domains
	 */
	@Override
	public Iterable<UserDomain> findDomains() {
		return proxyDomains(view.findDomains());
	}

	/**
	 * Returns the active domains for a class for which the user has read
	 * access.
	 * 
	 * @param type
	 *            the class i'm requesting the domains for
	 * 
	 * @return active domains for that class
	 */
	@Override
	public Iterable<UserDomain> findDomainsFor(CMClass type) {
		return proxyDomains(view.findDomainsFor(type));
	}

	/**
	 * Returns all (active and inactive) domains if the user has Database
	 * Designer privileges, otherwise it falls back to {@link findDomains()}.
	 * 
	 * @return all domains (active and inactive)
	 */
	@Override
	public Iterable<UserDomain> findAllDomains() {
		return proxyDomains(view.findAllDomains());
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
	public CMQueryResult executeNonEmptyQuery(final QuerySpecs querySpecs) {
		return view.executeNonEmptyQuery(querySpecs);
	}

	/*
	 * Proxy helpers
	 */

	Iterable<UserClass> proxyClasses(Iterable<DBClass> source) {
		return filterNotNull(map(source, new Mapper<DBClass, UserClass>() {
			@Override
			public UserClass map(DBClass o) {
				return UserClass.create(UserDataView.this, o);
			}
		}));
	}

	Iterable<UserDomain> proxyDomains(Iterable<DBDomain> source) {
		return filterNotNull(map(source, new Mapper<DBDomain, UserDomain>() {
			@Override
			public UserDomain map(DBDomain o) {
				return UserDomain.create(UserDataView.this, o);
			}
		}));
	}

	Iterable<UserAttribute> proxyAttributes(Iterable<DBAttribute> source) {
		return filterNotNull(map(source, new Mapper<DBAttribute, UserAttribute>() {
			@Override
			public UserAttribute map(DBAttribute o) {
				return UserAttribute.create(UserDataView.this, o);
			}
		}));
	}

	UserEntryType proxy(final DBEntryType unproxed) {
		return new DBEntryTypeVisitor() {
			UserEntryType proxy;

			@Override
			public void visit(DBClass type) {
				proxy = UserClass.create(UserDataView.this, type);
			}

			@Override
			public void visit(DBDomain type) {
				proxy = UserDomain.create(UserDataView.this, type);
			}

			UserEntryType proxy() {
				unproxed.accept(this);
				return proxy;
			}
		}.proxy();
	}
}
