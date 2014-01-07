package org.cmdbuild.dao.view.user;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.common.collect.Iterables.filterNotNull;
import static org.cmdbuild.common.collect.Iterables.map;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EmptyArrayOperatorAndValue.emptyArray;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.StringArrayOverlapOperatorAndValue.stringArrayOverlap;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.cmdbuild.dao.entrytype.NullEntryTypeVisitor;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.ForwardingQuerySpecs;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.join.DirectJoinClause;
import org.cmdbuild.dao.query.clause.where.AndWhereClause;
import org.cmdbuild.dao.query.clause.where.NullWhereClauseVisitor;
import org.cmdbuild.dao.query.clause.where.OrWhereClause;
import org.cmdbuild.dao.query.clause.where.SimpleWhereClause;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.AbstractDataView;
import org.cmdbuild.dao.view.CMAttributeDefinition;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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

	@Override
	public UserDomain findDomain(final CMIdentifier identifier) {
		return UserDomain.newInstance(this, view.findDomain(identifier));
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
		final WhereClause userWhereClause = whereClauseForUser(querySpecs);
		final Set<DirectJoinClause> directJoins = directJoinClausesForUser(querySpecs, userWhereClause);
		final QuerySpecs forwarder = new ForwardingQuerySpecs(querySpecs) {

			@Override
			public List<DirectJoinClause> getDirectJoins() {
				return Lists.newArrayList(directJoins);
			}

			@Override
			public WhereClause getWhereClause() {
				return userWhereClause;
			}

		};
		return UserQueryResult.newInstance(this, view.executeQuery(forwarder));
	}

	private WhereClause whereClauseForUser(final QuerySpecs querySpecs) {
		final WhereClause userWhereClause;
		if (querySpecs.getFromClause().getType() instanceof CMClass) {
			final CMClass type = (CMClass) querySpecs.getFromClause().getType();

			final WhereClause superClassesWhereClause = filterForSuperclassesOf(type);

			final WhereClause currentClassAndChildrenWhereClause = filterFor(type, type);

			final WhereClause prevExecutorsWhereClause = operationUser.hasAdministratorPrivileges() ? trueWhereClause()
					: addPrevExecutorsWhereClause(type);

			userWhereClause = and( //
					querySpecs.getWhereClause(), //
					prevExecutorsWhereClause, //
					(currentClassAndChildrenWhereClause == null) ? trueWhereClause()
							: currentClassAndChildrenWhereClause, //
					superClassesWhereClause //
			);
		} else {
			userWhereClause = querySpecs.getWhereClause();
		}
		return userWhereClause;
	}

	private Set<DirectJoinClause> directJoinClausesForUser(final QuerySpecs querySpecs,
			final WhereClause userWhereClause) {
		final Set<DirectJoinClause> directJoins = Sets.newHashSet(querySpecs.getDirectJoins());
		final Map<Alias, CMClass> descendantsByAlias = Maps.newHashMap();
		querySpecs.getFromClause().getType().accept(new NullEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				for (final CMClass descendant : type.getDescendants()) {
					final Alias alias = EntryTypeAlias.canonicalAlias(descendant);
					descendantsByAlias.put(alias, descendant);
				}
			}

		});
		userWhereClause.accept(new NullWhereClauseVisitor() {

			@Override
			public void visit(final AndWhereClause whereClause) {
				for (final WhereClause subWhereClause : whereClause.getClauses()) {
					subWhereClause.accept(this);
				}
			}

			@Override
			public void visit(final OrWhereClause whereClause) {
				for (final WhereClause subWhereClause : whereClause.getClauses()) {
					subWhereClause.accept(this);
				}
			}

			@Override
			public void visit(final SimpleWhereClause whereClause) {
				final QueryAliasAttribute attribute = whereClause.getAttribute();
				final Alias alias = attribute.getEntryTypeAlias();
				if (descendantsByAlias.containsKey(alias)) {
					final CMClass type = descendantsByAlias.get(alias);
					final DirectJoinClause clause = DirectJoinClause.newInstance() //
							.leftJoin(type) //
							.as(alias) //
							.on(attribute(alias, "Id")) //
							.equalsTo(attribute(querySpecs.getFromClause().getAlias(), "Id")) //
							.build();
					directJoins.add(clause);
				}
			}

		});
		return directJoins;
	}

	/**
	 * Returns the global {@link WhereClause} for the specified {@link CMClass}
	 * including sub-classes.
	 * 
	 * @param root
	 * @param type
	 * 
	 * @return the global {@link WhereClause} for the specified {@link CMClass}
	 *         or {@code null} if the filter is not available.
	 */
	private WhereClause filterFor(final CMClass root, final CMClass type) {
		try {
			final Iterable<? extends WhereClause> currentWhereClauses = getAdditionalFiltersFor(type);
			final List<WhereClause> childrenWhereClauses = Lists.newArrayList();
			final List<Long> childrenWithNoFilter = Lists.newArrayList();
			for (final CMClass child : type.getChildren()) {
				final WhereClause childWhereClause = filterFor(root, child);
				if (childWhereClause != null) {
					childrenWhereClauses.add(childWhereClause);
				} else {
					childrenWithNoFilter.add(child.getId());
				}
			}
			if (!childrenWithNoFilter.isEmpty()) {
				childrenWhereClauses.add(condition(attribute(root, "IdClass"), in(childrenWithNoFilter.toArray())));
			}
			final WhereClause whereClause;
			if (isEmpty(currentWhereClauses) && isEmpty(childrenWhereClauses)) {
				whereClause = null;
			} else {
				whereClause = and( //
						isEmpty(currentWhereClauses) ? trueWhereClause() : or(currentWhereClauses), //
						isEmpty(childrenWhereClauses) ? trueWhereClause() : or(childrenWhereClauses) //
				);
			}
			return whereClause;
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Returns the global {@link WhereClause} for the super-classes of the
	 * specified {@link CMClass}.
	 * 
	 * @param type
	 * 
	 * @return the global {@link WhereClause} for the specified {@link CMClass}
	 *         or {@link TrueWhereClause} if there is no filter available.
	 */
	private WhereClause filterForSuperclassesOf(final CMClass type) {
		try {
			final List<WhereClause> superClassesWhereClauses = Lists.newArrayList();
			for (CMClass parentType = type.getParent(); parentType != null; parentType = parentType.getParent()) {
				final Iterable<? extends WhereClause> privilegeWhereClause = getAdditionalFiltersFor(parentType, type);
				if (!isEmpty(privilegeWhereClause)) {
					superClassesWhereClauses.add(or(privilegeWhereClause));
				}
			}
			return isEmpty(superClassesWhereClauses) ? trueWhereClause() : and(superClassesWhereClauses);
		} catch (final Exception e) {
			return trueWhereClause();
		}
	}

	/**
	 * Return a where clause to filter the processes: if there is a default
	 * group check that the PrevExecutors is one of the user groups. Otherwise
	 * check for the logged group only
	 * 
	 * @param type
	 * @return
	 */
	private WhereClause addPrevExecutorsWhereClause(final CMClass type) {
		WhereClause prevExecutorsWhereClause = trueWhereClause();
		final CMAttribute prevExecutors = type.getAttribute("PrevExecutors");

		if (prevExecutors != null) {
			final String defaultGroupName = operationUser.getAuthenticatedUser().getDefaultGroupName();
			String userGroupsJoined = "";
			if (defaultGroupName == null || "".equals(defaultGroupName)) {

				userGroupsJoined = operationUser.getPreferredGroup().getName();
			} else {
				userGroupsJoined = Joiner.on(",").join( //
						operationUser.getAuthenticatedUser().getGroupNames() //
						);
			}

			prevExecutorsWhereClause = or( //
					condition(attribute(type, prevExecutors.getName()), stringArrayOverlap(userGroupsJoined)), //
					/*
					 * the or with empty array is necessary because after the
					 * creation of the the process card (before to say to shark
					 * to advance it) the PrevExecutors is empty
					 */
					condition(attribute(type, prevExecutors.getName()), emptyArray()) //
			);
		}

		return prevExecutorsWhereClause;
	}

	@Override
	public Iterable<? extends WhereClause> getAdditionalFiltersFor(final CMEntryType classToFilter) {
		return rowColumnPrivilegeFetcher.fetchPrivilegeFiltersFor(classToFilter);
	}

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

}
