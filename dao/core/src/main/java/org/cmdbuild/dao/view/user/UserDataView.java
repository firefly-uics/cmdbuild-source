package org.cmdbuild.dao.view.user;

import static org.cmdbuild.common.collect.Iterables.filterNotNull;
import static org.cmdbuild.common.collect.Iterables.map;

import org.cmdbuild.auth.user.OperationUser;
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
import org.cmdbuild.dao.entrytype.DBAttribute;
import org.cmdbuild.dao.entrytype.DBClass;
import org.cmdbuild.dao.entrytype.DBDomain;
import org.cmdbuild.dao.entrytype.DBEntryType;
import org.cmdbuild.dao.entrytype.DBEntryTypeVisitor;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.QueryExecutorDataView;

public class UserDataView extends QueryExecutorDataView {

	private final DBDataView dbView;
	private final OperationUser operationUser;

	public UserDataView(final DBDataView view, final OperationUser user) {
		this.dbView = view;
		this.operationUser = user;
	}

	public OperationUser getOperationUser() {
		return operationUser;
	}

	@Override
	public UserClass findClassById(final Long id) {
		return UserClass.newInstance(this, dbView.findClassById(id));
	}

	@Override
	public UserClass findClassByName(final String name) {
		return UserClass.newInstance(this, dbView.findClassByName(name));
	}

	/**
	 * Returns the active classes for which the user has read access.
	 * 
	 * @return active classes
	 */
	@Override
	public Iterable<UserClass> findClasses() {
		return proxyClasses(dbView.findClasses());
	}

	/**
	 * Returns all (active and inactive) classes if the user has Database
	 * Designer privileges, otherwise it falls back to {@link findClasses()}.
	 * 
	 * @return all classes (active and inactive)
	 */
	@Override
	public Iterable<UserClass> findAllClasses() {
		return proxyClasses(dbView.findAllClasses());
	}

	@Override
	public UserClass createClass(final CMClassDefinition definition) {
		return UserClass.newInstance(this, dbView.createClass(definition));
	}

	@Override
	public UserClass updateClass(final CMClassDefinition definition) {
		return UserClass.newInstance(this, dbView.updateClass(definition));
	}

	@Override
	public void deleteClass(final CMClass clazz) {
		dbView.deleteClass(clazz);
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
	public void deleteAttribute(final CMAttribute attribute) {
		dbView.deleteAttribute(attribute);
	}

	@Override
	public UserDomain findDomainById(final Long id) {
		return UserDomain.newInstance(this, dbView.findDomainById(id));
	}

	@Override
	public UserDomain findDomainByName(final String name) {
		return UserDomain.newInstance(this, dbView.findDomainByName(name));
	}

	/**
	 * Returns the active domains for which the user has read access.
	 * 
	 * @return active domains
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

	/**
	 * Returns all (active and inactive) domains if the user has Database
	 * Designer privileges, otherwise it falls back to {@link findDomains()}.
	 * 
	 * @return all domains (active and inactive)
	 */
	@Override
	public Iterable<UserDomain> findAllDomains() {
		return proxyDomains(dbView.findAllDomains());
	}

	@Override
	public UserDomain createDomain(final CMDomainDefinition definition) {
		return UserDomain.newInstance(this, dbView.createDomain(definition));
	}

	@Override
	public UserDomain updateDomain(final CMDomainDefinition definition) {
		return UserDomain.newInstance(this, dbView.updateDomain(definition));
	}

	@Override
	public void deleteDomain(final CMDomain domain) {
		dbView.deleteDomain(domain);
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
	public CMCardDefinition newCard(final CMClass type) {
		// TODO
		return dbView.newCard(type);
	}

	@Override
	public CMCardDefinition modifyCard(final CMCard card) {
		// TODO: check privileges.....
		// user.hasWriteAccess(card.getType());
		return dbView.modifyCard(card);
	}

	@Override
	public CMQueryResult executeNonEmptyQuery(final QuerySpecs querySpecs) {
		return dbView.executeNonEmptyQuery(querySpecs);
	}

	/*
	 * Proxy helpers
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
	public CMRelationDefinition newRelation(final CMDomain domain) {
		// TODO check privileges
		return dbView.newRelation(domain);
	}

	@Override
	public CMRelationDefinition modifyRelation(final CMRelation relation) {
		// TODO check privileges
		return dbView.modifyRelation(relation);
	}
}
