package org.cmdbuild.dao.driver.postgres.query;

import static org.cmdbuild.dao.driver.postgres.Utils.aliasForSystemAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.aliasForUserAttribute;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAlias;
import static org.cmdbuild.dao.driver.postgres.Utils.quoteAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cmdbuild.dao.driver.postgres.Const;
import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.clause.AnyAttribute;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.QueryDomain;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Holds the information about which attribute to query for every alias and
 * entry type of that alias. Also it is used to keep a mapping between the alias
 * attributes and the position in the select clause.
 */
public class ColumnMapper implements LoggingSupport {

	public static class EntryTypeAttribute {

		public final String name;
		public final Alias alias;
		public final Integer index;
		public final SqlType sqlType;
		public final String sqlTypeString;

		private EntryTypeAttribute(final String name, final Alias alias, final Integer index, final SqlType sqlType,
				final String sqlTypeString) {
			this.name = name;
			this.alias = alias;
			this.index = index;
			this.sqlType = sqlType;
			this.sqlTypeString = sqlTypeString;
		}

		@Override
		public String toString() {
			return String.format("[%s,%s,%s]", name, alias, sqlType);
		}

	}

	private static class AliasAttributes {

		private final Map<CMEntryType, List<EntryTypeAttribute>> map;

		public AliasAttributes(final Iterable<? extends CMEntryType> types) {
			map = Maps.newHashMap();
			for (final CMEntryType type : types) {
				map.put(type, Lists.<EntryTypeAttribute> newArrayList());
			}
		}

		/*
		 * Adds the attribute to the specified type
		 */
		public void addAttribute(final String attributeName, final Alias attributeAlias, final Integer index,
				final CMEntryType type) {
			final String sqlTypeString = getSqlTypeString(type, attributeName);
			final SqlType sqlType = getSqlType(type, attributeName);
			for (final CMEntryType currentType : map.keySet()) {
				final String currentName = (attributeAlias == null || currentType.equals(type)) ? attributeName : null;
				final EntryTypeAttribute eta = new EntryTypeAttribute(currentName, attributeAlias, index, sqlType,
						sqlTypeString);
				map.get(currentType).add(eta);
			}
		}

		private SqlType getSqlType(final CMEntryType type, final String attributeName) {
			final CMAttributeType<?> attributeType = safeAttributeTypeFor(type, attributeName);
			return SqlType.getSqlType(attributeType);
		}

		private String getSqlTypeString(final CMEntryType type, final String attributeName) {
			final CMAttributeType<?> attributeType = safeAttributeTypeFor(type, attributeName);
			return SqlType.getSqlTypeString(attributeType);
		}

		private CMAttributeType<?> safeAttributeTypeFor(final CMEntryType type, final String attributeName) {
			final CMAttributeType<?> attributeType;
			if (type != null) {
				final CMAttribute attribute = type.getAttribute(attributeName);
				if (attribute != null) {
					attributeType = attribute.getType();
				} else {
					attributeType = new UndefinedAttributeType();
				}
			} else {
				attributeType = new UndefinedAttributeType();
			}
			return attributeType;
		}

		public Iterable<EntryTypeAttribute> getAttributes(final CMEntryType type) {
			final Iterable<EntryTypeAttribute> entryTypeAttributes = map.get(type);
			logger.debug("getting all attributes for type '{}':", //
					type.getName(), Iterables.toString(entryTypeAttributes));
			return entryTypeAttributes;
		}

		public Iterable<CMEntryType> getEntryTypes() {
			return map.keySet();
		}

		@Override
		public String toString() {
			return map.toString();
		}

	}

	private static class AliasStore {

		private final Map<Alias, AliasAttributes> map;

		public AliasStore() {
			map = Maps.newHashMap();
		}

		public void addAlias(final Alias alias, final Iterable<? extends CMEntryType> aliasClasses) {
			map.put(alias, new AliasAttributes(aliasClasses));
		}

		public AliasAttributes getAliasAttributes(final Alias entryTypeAlias) {
			return map.get(entryTypeAlias);
		}

		public Set<Alias> getAliases() {
			return map.keySet();
		}

		@Override
		public String toString() {
			return map.toString();
		}

	}

	private final AliasStore cardSourceAliases = new AliasStore();
	private final AliasStore functionCallAliases = new AliasStore();
	private final AliasStore domainAliases = new AliasStore();

	private Integer currentIndex;
	private final Collection<String> attributesExpressionsForSelect;

	ColumnMapper(final QuerySpecs query) {
		currentIndex = 0;
		attributesExpressionsForSelect = Lists.newArrayList();
		fillAliases(query);
	}

	private void fillAliases(final QuerySpecs query) {
		logger.debug("filling aliases");
		final CMEntryType from = query.getFromType();
		// FIXME: Use a visitor!
		if (from instanceof CMClass) {
			logger.debug("from is a '{}'", CMClass.class);
			final CMClass fromClass = CMClass.class.cast(from);
			addClassAlias(query.getFromAlias(), fromClass.getLeaves());
			for (final JoinClause jc : query.getJoins()) {
				addDomainAlias(jc.getDomainAlias(), jc.getQueryDomains());
				addClassAlias(jc.getTargetAlias(), jc.getTargets());
			}
		} else if (from instanceof CMFunctionCall) {
			logger.debug("from is a '{}'", CMFunctionCall.class);
			final CMFunctionCall fromFunctionCall = CMFunctionCall.class.cast(from);
			addFunctionCallAlias(query.getFromAlias(), fromFunctionCall);
		}
	}

	private void addClassAlias(final Alias alias, final Iterable<? extends CMClass> aliasClasses) {
		logger.debug("adding classes '{}' for alias '{}'", namesOf(aliasClasses), alias);
		cardSourceAliases.addAlias(alias, aliasClasses);
	}

	private void addDomainAlias(final Alias alias, final Set<QueryDomain> aliasQueryDomains) {
		final Set<CMDomain> aliasDomains = Sets.newHashSet();
		for (final QueryDomain qd : aliasQueryDomains) {
			aliasDomains.add(qd.getDomain());
		}
		domainAliases.addAlias(alias, aliasDomains);
	}

	private void addFunctionCallAlias(final Alias alias, final CMFunctionCall functioncallAlias) {
		final List<CMFunctionCall> i = Lists.newArrayListWithCapacity(1);
		i.add(functioncallAlias);
		functionCallAliases.addAlias(alias, i);
	}

	public Set<Alias> getClassAliases() {
		return cardSourceAliases.getAliases();
	}

	public Set<Alias> getDomainAliases() {
		return domainAliases.getAliases();
	}

	public Set<Alias> getFunctionCallAliases() {
		return functionCallAliases.getAliases();
	}

	public Iterable<EntryTypeAttribute> getEntryTypeAttributes(final Alias alias, final CMEntryType type) {
		return aliasAttributesFor(alias).getAttributes(type);
	}

	Iterable<String> getAttributeExpressionsForSelect() {
		return attributesExpressionsForSelect;
	}

	void addSystemAttributeForSelect(final Alias typeAlias, final Const.SystemAttributes systemAttribute) {
		logger.debug("adding system attribute '{}' to alias '{}'", systemAttribute, typeAlias);
		appendToSelectStatement( //
				typeAlias, //
				systemAttribute.getDBName(), //
				systemAttribute.getCastSuffix(), //
				aliasForSystemAttribute(typeAlias, systemAttribute));
	}

	void addAllUserAttributesForSelect(final Iterable<QueryAliasAttribute> attributes) {
		for (final QueryAliasAttribute a : attributes) {
			addUserAttributeForSelect(a);
		}
	}

	void addUserAttributeForSelect(final QueryAliasAttribute qa) {
		logger.debug("adding attribute '{}' to alias '{}'", qa.getName(), qa.getEntryTypeAlias());

		final Alias typeAlias = qa.getEntryTypeAlias();
		final AliasAttributes aliasAttributes = aliasAttributesFor(typeAlias);
		if (qa instanceof AnyAttribute) {
			logger.debug("any attribute required");
			for (final CMEntryType type : aliasAttributes.getEntryTypes()) {
				logger.debug("adding attributes for type '{}'", type.getName());
				for (final CMAttribute attribute : type.getAttributes()) {
					logger.debug("adding attribute '{}'", attribute.getName());
					final String attributeName = attribute.getName();
					final Alias attributeAlias = aliasForUserAttribute(typeAlias, attributeName);
					/*
					 * TODO don't add attributes if already added
					 * 
					 * happens if querying for any attribute over a superclass
					 */
					final Integer usedIndex = appendToSelectStatement(typeAlias, attributeName, sqlCastFor(attribute),
							attributeAlias);
					aliasAttributes.addAttribute(attributeName, attributeAlias, usedIndex, type);
				}
			}
		} else {
			final String attributeName = qa.getName();
			/*
			 * FIXME IT SHOULD NOT TAKE THE FIRST ONE IF MORE THAN ONE but it
			 * does not work if we take them all
			 * 
			 * we trust it works
			 */
			final CMEntryType type = aliasAttributes.getEntryTypes().iterator().next();
			final Integer usedIndex = appendToSelectStatement(typeAlias, attributeName,
					sqlCastFor(type.getAttribute(attributeName)), null);
			aliasAttributes.addAttribute(attributeName, null, usedIndex, type);
		}
	}

	private String sqlCastFor(final CMAttribute attribute) {
		return SqlType.getSqlType(attribute.getType()).sqlCast();
	}

	private AliasAttributes aliasAttributesFor(final Alias alias) {
		logger.debug("getting '{}' for alias '{}'...", AliasAttributes.class, alias);
		AliasAttributes out;
		logger.debug("... is a class!");
		out = cardSourceAliases.getAliasAttributes(alias);
		if (out == null) {
			logger.debug("... no is a domain!");
			out = domainAliases.getAliasAttributes(alias);
		}
		if (out == null) {
			logger.debug("... no is a function!");
			out = functionCallAliases.getAliasAttributes(alias);
		}
		return out;
	}

	private Integer appendToSelectStatement(final Alias typeAlias, final String attributeName, final String cast,
			final Alias attributeAlias) {
		final StringBuffer sb = new StringBuffer(quoteAttribute(typeAlias, attributeName));
		if (cast != null) {
			sb.append("::").append(cast);
		}
		if (attributeAlias != null) {
			sb.append(" AS ").append(quoteAlias(attributeAlias));
		}
		final String toBeAppended = sb.toString();
		logger.debug("appends '{}' to select statement", toBeAppended);
		attributesExpressionsForSelect.add(toBeAppended);
		return ++currentIndex;
	}

	@Override
	public String toString() {
		return String.format("[Classes=%s,Domains=%s,Functions=%s]", cardSourceAliases, domainAliases,
				functionCallAliases);
	}

	private static Iterable<String> namesOf(final Iterable<? extends CMEntryType> aliasClasses) {
		return Iterables.transform(aliasClasses, new Function<CMEntryType, String>() {

			@Override
			public String apply(final CMEntryType input) {
				return input.getName();
			}

		});
	}

}
