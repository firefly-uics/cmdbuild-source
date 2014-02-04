package org.cmdbuild.dao.view.user;

import static org.cmdbuild.common.collect.Iterables.filterNotNull;
import static org.cmdbuild.common.collect.Iterables.map;

import java.util.Map;

import org.cmdbuild.auth.acl.PrivilegeContext;
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
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.CMIdentifier;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.AbstractDataView;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;

public class UserDataView extends AbstractDataView {

	private final CMDataView view;
	private final PrivilegeContext privilegeContext;
	private final RowAndColumnPrivilegeFetcher rowColumnPrivilegeFetcher;
	private final OperationUser operationUser;

	public UserDataView( //
			final CMDataView view, //
			final PrivilegeContext privilegeContext, //
			final RowAndColumnPrivilegeFetcher rowPrivilegeFetcher, //
			final OperationUser operationUser //
	) {
		this.view = view;
		this.privilegeContext = privilegeContext;
		this.rowColumnPrivilegeFetcher = rowPrivilegeFetcher;
		this.operationUser = operationUser;
	}

	@Override
	protected CMDataView viewForBuilder() {
		return view;
	}

	public PrivilegeContext getPrivilegeContext() {
		return privilegeContext;
	}

	@Override
	public UserClass findClass(final Long id) {
		return UserClass.newInstance(this, view.findClass(id));
	}

	@Override
	public UserClass findClass(final String name) {
		return UserClass.newInstance(this, view.findClass(name));
	}

	@Override
	public UserClass findClass(final CMIdentifier identifier) {
		return UserClass.newInstance(this, view.findClass(identifier));
	}

	/**
	 * Returns the active and not active classes for which the user has read
	 * access. It does not return reserved classes
	 */
	@Override
	public Iterable<UserClass> findClasses() {
		return proxyClasses(view.findClasses());
	}

	@Override
	public UserClass create(final CMClassDefinition definition) {
		return UserClass.newInstance(this, view.create(definition));
	}

	@Override
	public UserClass update(final CMClassDefinition definition) {
		return UserClass.newInstance(this, view.update(definition));
	}

	@Override
	public UserAttribute createAttribute(final CMAttributeDefinition definition) {
		return UserAttribute.newInstance(this, view.createAttribute(definition));
	}

	@Override
	public UserAttribute updateAttribute(final CMAttributeDefinition definition) {
		return UserAttribute.newInstance(this, view.updateAttribute(definition));
	}

	@Override
	public void delete(final CMAttribute attribute) {
		view.delete(attribute);
	}

	@Override
	public UserDomain findDomain(final Long id) {
		return UserDomain.newInstance(this, view.findDomain(id));
	}

	@Override
	public UserDomain findDomain(final String name) {
		return UserDomain.newInstance(this, view.findDomain(name));
	}

	/**
	 * Returns the active and not active domains. It does not return reserved
	 * domains
	 * 
	 * @return all domains (active and non active)
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
	public Iterable<UserDomain> findDomainsFor(final CMClass type) {
		return proxyDomains(view.findDomainsFor(type));
	}

	@Override
	public UserDomain create(final CMDomainDefinition definition) {
		return UserDomain.newInstance(this, view.create(definition));
	}

	@Override
	public UserDomain update(final CMDomainDefinition definition) {
		return UserDomain.newInstance(this, view.update(definition));
	}

	@Override
	public CMFunction findFunctionByName(final String name) {
		return view.findFunctionByName(name);
	}

	/**
	 * Returns all the defined functions for every user.
	 */
	@Override
	public Iterable<? extends CMFunction> findAllFunctions() {
		return view.findAllFunctions();
	}

	@Override
	public void delete(final CMEntryType entryType) {
		view.delete(entryType);
	}

	@Override
	public CMCardDefinition createCardFor(final CMClass type) {
		// TODO
		return view.createCardFor(type);
	}

	@Override
	public CMCardDefinition update(final CMCard card) {
		return view.update(card);
	}

	@Override
	public UserQueryResult executeQuery(final QuerySpecs querySpecs) {
		return UserQueryResult.newInstance(this, view.executeQuery(querySpecs));
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter) {
		return rowColumnPrivilegeFetcher.fetchPrivilegeFiltersFor(classToFilter);
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter,
			final CMEntryType classForClauses) {
		return rowColumnPrivilegeFetcher.fetchPrivilegeFiltersFor(classToFilter, classForClauses);
	}

	@Override
	public Map<String, String> getAttributesPrivilegesFor(final CMEntryType entryType) {
		return rowColumnPrivilegeFetcher.fetchAttributesPrivilegesFor(entryType);
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
	Iterable<UserClass> proxyClasses(final Iterable<? extends CMClass> source) {
		return filterNotNull(map(source, new Mapper<CMClass, UserClass>() {
			@Override
			public UserClass map(final CMClass o) {
				return UserClass.newInstance(UserDataView.this, o);
			}
		}));
	}

	Iterable<UserDomain> proxyDomains(final Iterable<? extends CMDomain> source) {
		return filterNotNull(map(source, new Mapper<CMDomain, UserDomain>() {
			@Override
			public UserDomain map(final CMDomain o) {
				return UserDomain.newInstance(UserDataView.this, o);
			}
		}));
	}

	Iterable<UserAttribute> proxyAttributes(final Iterable<? extends CMAttribute> source) {
		return filterNotNull(map(source, new Mapper<CMAttribute, UserAttribute>() {
			@Override
			public UserAttribute map(final CMAttribute inner) {
				return UserAttribute.newInstance(UserDataView.this, inner);
			}
		}));
	}

	UserEntryType proxy(final CMEntryType unproxed) {
		return new CMEntryTypeVisitor() {
			UserEntryType proxy;

			@Override
			public void visit(final CMClass type) {
				proxy = UserClass.newInstance(UserDataView.this, type);
			}

			@Override
			public void visit(final CMDomain type) {
				proxy = UserDomain.newInstance(UserDataView.this, type);
			}

			@Override
			public void visit(final CMFunctionCall type) {
				proxy = UserFunctionCall.newInstance(UserDataView.this, type);
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
		return view.createRelationFor(domain);
	}

	@Override
	public CMRelationDefinition update(final CMRelation relation) {
		// TODO check privileges
		return view.update(relation);
	}

	@Override
	public void delete(final CMRelation relation) {
		// TODO: check privileges
		view.delete(relation);
	}

	@Override
	public void clear(final CMEntryType type) {
		view.clear(type);
	}

	@Override
	public void delete(final CMCard card) {
		// TODO: check privileges
		view.delete(card);
	}

	// TODO reconsider this solution

	@Override
	public CMClass getActivityClass() {
		return UserClass.newInstance(this, view.getActivityClass());
	}

	@Override
	public CMClass getReportClass() {
		return UserClass.newInstance(this, view.getReportClass());
	}

	@Override
	public QuerySpecsBuilder select(final Object... attrDef) {
		return UserQuerySpecsBuilder.newInstance(view.select(attrDef), this, operationUser);
	}

}
