package org.cmdbuild.dao.view.user;

import static org.cmdbuild.common.collect.Iterables.filterNotNull;
import static org.cmdbuild.common.collect.Iterables.map;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMCard.CMCardDefinition;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entry.CMRelation.CMRelationDefinition;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMClass.CMClassDefinition;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMDomain.CMDomainDefinition;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.DBEntryTypeVisitor;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.QueryExecutorDataView;
import org.cmdbuild.dao.view.user.privileges.RowPrivilegeFetcher;

public class UserDataView extends QueryExecutorDataView {

	private final DBDataView dbView;
	private final PrivilegeContext privilegeContext;
	private final RowPrivilegeFetcher rowPrivilegeFetcher;

	public UserDataView(final DBDataView view, final PrivilegeContext privilegeContext,
			final RowPrivilegeFetcher rowPrivilegeFetcher) {
		this.dbView = view;
		this.privilegeContext = privilegeContext;
		this.rowPrivilegeFetcher = rowPrivilegeFetcher;
	}

	public PrivilegeContext getPrivilegeContext() {
		return privilegeContext;
	}

	@Override
	public UserClass findClass(final Long id) {
		return UserClass.newInstance(this, dbView.findClass(id));
	}

	@Override
	public UserClass findClass(final String name) {
		return UserClass.newInstance(this, dbView.findClass(name));
	}

	/**
	 * Returns the active and not active classes for which the user has read
	 * access. It does not return reserved classes
	 */
	@Override
	public Iterable<UserClass> findClasses() {
		return proxyClasses(dbView.findClasses());
	}

	@Override
	public UserClass create(final CMClassDefinition definition) {
		return UserClass.newInstance(this, dbView.create(definition));
	}

	@Override
	public UserClass update(final CMClassDefinition definition) {
		return UserClass.newInstance(this, dbView.update(definition));
	}

	@Override
	public void delete(final CMClass clazz) {
		dbView.delete(clazz);
	}

	@Override
	public UserAttribute createAttribute(final CMAttributeDefinition definition) {
		return UserAttribute.newInstance(this, dbView.createAttribute(definition));
	}

	@Override
	public UserAttribute updateAttribute(final CMAttributeDefinition definition) {
		return UserAttribute.newInstance(this, dbView.updateAttribute(definition));
	}

	@Override
	public void delete(final CMAttribute attribute) {
		dbView.delete(attribute);
	}

	@Override
	public UserDomain findDomain(final Long id) {
		return UserDomain.newInstance(this, dbView.findDomain(id));
	}

	@Override
	public UserDomain findDomain(final String name) {
		return UserDomain.newInstance(this, dbView.findDomain(name));
	}

	/**
	 * Returns the active and not active domains. It does not return reserved
	 * domains
	 * 
	 * @return all domains (active and non active)
	 */
	@Override
	public Iterable<UserDomain> findDomains() {
		return proxyDomains(dbView.findDomains());
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
	public Iterable<UserDomain> findDomainsFor(final CMClass type) {
		return proxyDomains(dbView.findDomainsFor(type));
	}

	@Override
	public UserDomain create(final CMDomainDefinition definition) {
		return UserDomain.newInstance(this, dbView.create(definition));
	}

	@Override
	public UserDomain update(final CMDomainDefinition definition) {
		return UserDomain.newInstance(this, dbView.update(definition));
	}

	@Override
	public void delete(final CMDomain domain) {
		dbView.delete(domain);
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return dbView.findFunctionByName(name);
	}

	/**
	 * Returns all the defined functions for every user.
	 */
	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return dbView.findAllFunctions();
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		// TODO
		return dbView.createCardFor(type);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return dbView.update(card);
	}

	@Override
	public CMQueryResult executeNonEmptyQuery(final QuerySpecs querySpecs) {
		return dbView.executeNonEmptyQuery(querySpecs);
	}

	public WhereClause getAdditionalFiltersForClass(final CMClass classToFilter) {
		return rowPrivilegeFetcher.fetchPrivilegeFiltersFor(classToFilter);
	}

	/*
	 * Proxy helpers
	 */

	/**
	 * Note that a UserClass is null if the user does not have the privileges to
	 * read the class or if the class is a system class (reserved)
	 * 
	 * @param source
	 * @return
	 */
	Iterable<UserClass> proxyClasses(final Iterable<DBClass> source) {
		return filterNotNull(map(source, new Mapper<DBClass, UserClass>() {
			@Override
			public UserClass map(final DBClass o) {
				return UserClass.newInstance(UserDataView.this, o);
			}
		}));
	}

	Iterable<UserDomain> proxyDomains(final Iterable<DBDomain> source) {
		return filterNotNull(map(source, new Mapper<DBDomain, UserDomain>() {
			@Override
			public UserDomain map(final DBDomain o) {
				return UserDomain.newInstance(UserDataView.this, o);
			}
		}));
	}

	Iterable<UserAttribute> proxyAttributes(final Iterable<DBAttribute> source) {
		return filterNotNull(map(source, new Mapper<DBAttribute, UserAttribute>() {
			@Override
			public UserAttribute map(final DBAttribute o) {
				return UserAttribute.newInstance(UserDataView.this, o);
			}
		}));
	}

	UserEntryType proxy(final DBEntryType unproxed) {
		return new DBEntryTypeVisitor() {
			UserEntryType proxy;

			@Override
			public void visit(final DBClass type) {
				proxy = UserClass.newInstance(UserDataView.this, type);
			}

			@Override
			public void visit(final DBDomain type) {
				proxy = UserDomain.newInstance(UserDataView.this, type);
			}

			UserEntryType proxy() {
				unproxed.accept(this);
				return proxy;
			}
		}.proxy();
	}

	@Override
	public CMRelationDefinition createRelationFor(final CMDomain domain) {
		// TODO check privileges
		return dbView.createRelationFor(domain);
	}

	@Override
	public CMRelationDefinition update(final CMRelation relation) {
		// TODO check privileges
		return dbView.update(relation);
	}

	@Override
	public void clear(final CMEntryType type) {
		dbView.clear(type);
	}

	@Override
	public void delete(final CMCard card) {
		// TODO: check privileges
		dbView.delete(card);
	}

	// TODO reconsider this solution

	@Override
	public CMClass getActivityClass() {
		return UserClass.newInstance(this, (DBClass) dbView.getActivityClass());
	}

	@Override
	public CMClass getReportClass() {
		return UserClass.newInstance(this, (DBClass) dbView.getReportClass());
	}

}
