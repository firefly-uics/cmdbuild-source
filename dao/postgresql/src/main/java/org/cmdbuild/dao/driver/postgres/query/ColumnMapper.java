package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.dao.driver.postgres.Utils.aliasForUserAttribute;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.dao.driver.postgres.SqlType;
import org.cmdbuild.dao.driver.postgres.logging.LoggingSupport;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
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

		public EntryTypeAttribute(final String name, final Alias alias, final Integer index, final SqlType sqlType,
				final String sqlTypeString) {
			this.name = name;
			this.alias = alias;
			this.index = index;
			this.sqlType = sqlType;
			this.sqlTypeString = sqlTypeString;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
					.append(name) //
					.append(alias) //
					.append(sqlType) //
					.toString();
		}
	}

	private static class AliasAttributes {

		private final Map<CMEntryType, List<EntryTypeAttribute>> map;

		public AliasAttributes(final Iterable<? extends CMEntryType> types) {
			map = newHashMap();
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
			logger.debug("getting all attributes for type '{}': {}", //
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
			map = newHashMap();
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

	private final SelectAttributesHolder selectAttributesHolder;

	public ColumnMapper(final QuerySpecs query, final SelectAttributesHolder holder) {
		selectAttributesHolder = holder;
		currentIndex = 0;
		fillAliases(query);
	}

	private void fillAliases(final QuerySpecs query) {
		logger.debug("filling aliases");
		query.getFromType().accept(new CMEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				addClasses(query.getFromAlias(), type.getLeaves());
				for (final JoinClause joinClause : query.getJoins()) {
					addDomainAlias(joinClause.getDomainAlias(), joinClause.getQueryDomains());
					addClasses(joinClause.getTargetAlias(), joinClause.getTargets());
				}
			}

			private void addClasses(final Alias alias, final Iterable<? extends CMClass> classes) {
				add(cardSourceAliases, alias, classes);
			}

			private void addDomainAlias(final Alias alias, final Iterable<QueryDomain> queryDomains) {
				add(domainAliases, alias, newHashSet(transform(queryDomains, //
						new Function<QueryDomain, CMEntryType>() {
							@Override
							public CMEntryType apply(final QueryDomain input) {
								return input.getDomain();
							}
						})));
			}

			@Override
			public void visit(final CMDomain type) {
				throw new IllegalArgumentException("domain is an illegal 'from' type");
			}

			@Override
			public void visit(final CMFunctionCall type) {
				add(functionCallAliases, query.getFromAlias(), newArrayList(type));
			}

			private void add(final AliasStore store, final Alias alias, final Iterable<? extends CMEntryType> entryTypes) {
				logger.debug("adding '{}' for alias '{}'", namesOfEntryTypes(entryTypes), alias);
				store.addAlias(alias, entryTypes);
			}

		});
	}

	public Iterable<Alias> getClassAliases() {
		return cardSourceAliases.getAliases();
	}

	public Iterable<Alias> getDomainAliases() {
		return domainAliases.getAliases();
	}

	public Iterable<Alias> getFunctionCallAliases() {
		return functionCallAliases.getAliases();
	}

	public Iterable<EntryTypeAttribute> getAttributes(final Alias alias, final CMEntryType type) {
		return aliasAttributesFor(alias).getAttributes(type);
	}

	public void addAllAttributes(final Iterable<QueryAliasAttribute> attributes) {
		for (final QueryAliasAttribute a : attributes) {
			addAttribute(a);
		}
	}

	public void addAttribute(final QueryAliasAttribute attribute) {
		logger.debug("adding attribute '{}' to alias '{}'", attribute.getName(), attribute.getEntryTypeAlias());

		final Alias typeAlias = attribute.getEntryTypeAlias();
		final AliasAttributes aliasAttributes = aliasAttributesFor(typeAlias);
		if (attribute instanceof AnyAttribute) {
			logger.debug("any attribute required");
			for (final CMEntryType type : aliasAttributes.getEntryTypes()) {
				logger.debug("adding attributes for type '{}'", type.getName());
				for (final CMAttribute _attribute : type.getAttributes()) {
					logger.debug("adding attribute '{}'", _attribute.getName());
					final String attributeName = _attribute.getName();
					final Alias attributeAlias = aliasForUserAttribute(typeAlias, attributeName);
					/*
					 * TODO don't add attributes if already added
					 * 
					 * happens if querying for any attribute over a superclass
					 */
					selectAttributesHolder.add(typeAlias, attributeName, sqlCastFor(_attribute), attributeAlias);
					aliasAttributes.addAttribute(attributeName, attributeAlias, ++currentIndex, type);
				}
			}
		} else {
			final String attributeName = attribute.getName();
			/*
			 * FIXME IT SHOULD NOT TAKE THE FIRST ONE IF MORE THAN ONE but it
			 * does not work if we take them all
			 * 
			 * we trust it works
			 */
			final CMEntryType type = aliasAttributes.getEntryTypes().iterator().next();
			selectAttributesHolder.add(typeAlias, attributeName, sqlCastFor(type.getAttribute(attributeName)), null);
			aliasAttributes.addAttribute(attributeName, null, ++currentIndex, type);
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

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("Classes", cardSourceAliases) //
				.append("Domains", domainAliases) //
				.append("Functions", functionCallAliases) //
				.toString();
	}

	private static Iterable<String> namesOfEntryTypes(final Iterable<? extends CMEntryType> aliasClasses) {
		return transform(aliasClasses, new Function<CMEntryType, String>() {

			@Override
			public String apply(final CMEntryType input) {
				return input.getName();
			}

		});
	}

}
