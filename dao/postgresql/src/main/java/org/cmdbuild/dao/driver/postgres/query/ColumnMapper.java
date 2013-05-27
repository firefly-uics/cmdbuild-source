package org.cmdbuild.dao.driver.postgres.query;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.cmdbuild.dao.driver.postgres.Utils.nameForUserAttribute;
import static org.cmdbuild.dao.query.clause.alias.NameAlias.as;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.cmdbuild.dao.query.clause.alias.EntryTypeAlias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

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
	private final JoinHolder joinHolder;

	public ColumnMapper(final QuerySpecs query, final SelectAttributesHolder holder, final JoinHolder joinHolder) {
		this.selectAttributesHolder = holder;
		this.joinHolder = joinHolder;
		this.currentIndex = 0;
		fillAliases(query);
	}

	private void fillAliases(final QuerySpecs querySpecs) {
		logger.debug("filling aliases");
		querySpecs.getFromClause().getType().accept(new CMEntryTypeVisitor() {

			@Override
			public void visit(final CMClass type) {
				final List<CMClass> classes = Lists.newArrayList(type.getLeaves());
				// Add also the super class, to be able to
				// sort the result by the super class's attributes
				if (type.isSuperclass()) {
					classes.add(type);
				}

				addClasses(querySpecs.getFromClause().getAlias(), classes);
				for (final JoinClause joinClause : querySpecs.getJoins()) {
					addDomainAlias(joinClause.getDomainAlias(), joinClause.getQueryDomains());
					addClasses(joinClause.getTargetAlias(), from(joinClause.getTargets()) //
							.transform(new Function<Entry<CMClass, WhereClause>, CMClass>() {
								@Override
								public CMClass apply(final Entry<CMClass, WhereClause> input) {
									return input.getKey();
								}
							}));
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
				add(functionCallAliases, querySpecs.getFromClause().getAlias(), newArrayList(type));
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

	public void addAllAttributes(final Iterable<? extends QueryAliasAttribute> attributes) {
		for (final QueryAliasAttribute a : attributes) {
			addAttribute(a);
		}
	}

	private void addAttribute(final QueryAliasAttribute attribute) {
		logger.debug("adding attribute '{}' to alias '{}'", attribute.getName(), attribute.getEntryTypeAlias());

		final Alias typeAlias = attribute.getEntryTypeAlias();
		final AliasAttributes aliasAttributes = aliasAttributesFor(typeAlias);
		if (attribute instanceof AnyAttribute) {
			logger.debug("any attribute required");
			for (final CMEntryType type : aliasAttributes.getEntryTypes()) {
				logger.debug("adding attributes for type '{}'", type.getIdentifier().getLocalName());
				final Alias _typeAlias = new CMEntryTypeVisitor() {

					private Alias _typeAlias;

					@Override
					public void visit(CMClass type) {
						_typeAlias = EntryTypeAlias.canonicalAlias(type);
					}

					@Override
					public void visit(CMDomain type) {
						_typeAlias = typeAlias;
					}

					@Override
					public void visit(CMFunctionCall type) {
						_typeAlias = EntryTypeAlias.canonicalAlias(type);
					}

					public Alias typeAlias() {
						type.accept(this);
						return _typeAlias;
					}

				}.typeAlias();

				for (final CMAttribute _attribute : type.getAllAttributes()) {
					logger.debug("adding attribute '{}'", _attribute.getName());
					final String attributeName = _attribute.getName();
					Alias attributeAlias = as(nameForUserAttribute(_typeAlias, attributeName));

					if (type instanceof CMDomain) {
						/**
						 * The alias is updated. Bug fix for domains that have
						 * an attribute with the same name
						 */
						attributeAlias = as(nameForUserAttribute(_typeAlias, attributeName + "##" + currentIndex));
						selectAttributesHolder.add(sqlCastFor(_attribute), attributeAlias);
					} else {
						selectAttributesHolder.add(_typeAlias, attributeName, sqlCastFor(_attribute), attributeAlias);
					}

					joinHolder.add(_typeAlias, typeAlias);
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
			final Alias attributeAlias = as(nameForUserAttribute(typeAlias, attributeName));
			selectAttributesHolder.add(typeAlias, attributeName, sqlCastFor(type.getAttribute(attributeName)),
					attributeAlias);
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
