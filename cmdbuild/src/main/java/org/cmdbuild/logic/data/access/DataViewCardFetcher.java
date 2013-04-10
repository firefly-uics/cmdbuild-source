package org.cmdbuild.logic.data.access;

import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.alias.EntryTypeAlias.canonicalAlias;
import static org.cmdbuild.dao.query.clause.join.Over.over;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.cmdbuild.common.Builder;
import org.cmdbuild.common.collect.Mapper;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.function.CMFunction;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.FunctionCall;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.cmdbuild.logic.mapping.json.JsonSorterMapper;
import org.json.JSONArray;

import com.google.common.collect.Lists;

public class DataViewCardFetcher {

	private static final String DEFAULT_SORTING_ATTRIBUTE_NAME = "Description";

	private static abstract class AbstractQuerySpecsBuilderBuilder implements Builder<QuerySpecsBuilder> {

		protected CMDataView dataView;
		protected QueryOptions queryOptions;

		public AbstractQuerySpecsBuilderBuilder withDataView(final CMDataView value) {
			dataView = value;
			return this;
		}

		public AbstractQuerySpecsBuilderBuilder withQueryOptions(final QueryOptions value) {
			queryOptions = value;
			return this;
		}

		protected void addJoinOptions(final QuerySpecsBuilder querySpecsBuilder, final QueryOptions options,
				final Iterable<FilterMapper.JoinElement> joinElements) {
			if (!isEmpty(joinElements)) {
				querySpecsBuilder.distinct();
			}
			for (final FilterMapper.JoinElement joinElement : joinElements) {
				final CMDomain domain = dataView.findDomain(joinElement.domain);
				final CMClass clazz = dataView.findClass(joinElement.destination);
				if (joinElement.left) {
					querySpecsBuilder.leftJoin(clazz, canonicalAlias(clazz), over(domain));
				} else {
					querySpecsBuilder.join(clazz, canonicalAlias(clazz), over(domain));
				}
			}
		}

		protected static void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder,
				final Iterable<OrderByClause> clauses) {
			for (final OrderByClause clause : clauses) {
				querySpecsBuilder.orderBy(clause.getAttribute(), clause.getDirection());
			}
		}

	}

	public static class QuerySpecsBuilderBuilder extends AbstractQuerySpecsBuilderBuilder {

		private CMClass fetchedClass;

		@Override
		public QuerySpecsBuilder build() {
			final FilterMapper filterMapper = new JsonFilterMapper(fetchedClass, queryOptions.getFilter(), dataView);
			final WhereClause whereClause = filterMapper.whereClause();
			final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();
			final Mapper<JSONArray, List<QueryAliasAttribute>> attributeSubsetMapper = new JsonAttributeSubsetMapper(
					fetchedClass);
			final List<QueryAliasAttribute> attributeSubsetForSelect = attributeSubsetMapper.map(queryOptions
					.getAttributes());
			final QuerySpecsBuilder querySpecsBuilder = newQuerySpecsBuilder(attributeSubsetForSelect, fetchedClass);
			querySpecsBuilder.from(fetchedClass) //
					.where(whereClause) //
					.limit(queryOptions.getLimit()) //
					.offset(queryOptions.getOffset());

			addJoinOptions(querySpecsBuilder, queryOptions, joinElements);
			addSortingOptions(querySpecsBuilder, queryOptions, fetchedClass);
			return querySpecsBuilder;
		}

		private QuerySpecsBuilder newQuerySpecsBuilder(final List<QueryAliasAttribute> attributeSubsetForSelect,
				final CMEntryType entryType) {
			if (attributeSubsetForSelect.isEmpty()) {
				return dataView.select(anyAttribute(entryType));
			}
			final Object[] attributesArray = new QueryAliasAttribute[attributeSubsetForSelect.size()];
			attributeSubsetForSelect.toArray(attributesArray);
			return dataView.select(attributesArray);
		}

		public static void addSortingOptions(final QuerySpecsBuilder querySpecsBuilder, final QueryOptions options,
				final CMClass clazz) {
			final SorterMapper sorterMapper = new JsonSorterMapper(clazz, options.getSorters());
			final List<OrderByClause> clauses = sorterMapper.deserialize();
			/*
			 * if no sorting rules are defined sort by description (if the class
			 * has a description)
			 */
			if (clauses.isEmpty()) {
				if (clazz.getAttribute(DEFAULT_SORTING_ATTRIBUTE_NAME) != null) {
					querySpecsBuilder.orderBy(attribute(clazz, DEFAULT_SORTING_ATTRIBUTE_NAME), Direction.ASC);
				}
			} else {
				addSortingOptions(querySpecsBuilder, clauses);
			}
		}

		@Override
		public QuerySpecsBuilderBuilder withDataView(final CMDataView value) {
			return (QuerySpecsBuilderBuilder) super.withDataView(value);
		}

		@Override
		public QuerySpecsBuilderBuilder withQueryOptions(final QueryOptions value) {
			return (QuerySpecsBuilderBuilder) super.withQueryOptions(value);
		}

		public QuerySpecsBuilderBuilder withClass(final CMClass value) {
			fetchedClass = value;
			return this;
		}

	}

	public static class SqlQuerySpecsBuilderBuilder extends AbstractQuerySpecsBuilderBuilder {

		private CMFunction fetchedFunction;
		private Alias functionAlias;

		@Override
		public QuerySpecsBuilder build() {
			final FunctionCall functionCall = FunctionCall.call(fetchedFunction, new HashMap<String, Object>());
			final FilterMapper filterMapper = new JsonFilterMapper(functionCall, queryOptions.getFilter(), dataView,
					functionAlias);
			final WhereClause whereClause = filterMapper.whereClause();
			final Iterable<FilterMapper.JoinElement> joinElements = filterMapper.joinElements();
			final QuerySpecsBuilder querySpecsBuilder = dataView //
					.select(anyAttribute(fetchedFunction, functionAlias)) //
					.from(functionCall, functionAlias) //
					.where(whereClause) //
					.limit(queryOptions.getLimit()) //
					.offset(queryOptions.getOffset());
			addJoinOptions(querySpecsBuilder, queryOptions, joinElements);
			addSortingOptions(querySpecsBuilder, queryOptions, functionCall, functionAlias);
			return querySpecsBuilder;
		}

		private void addSortingOptions( //
				final QuerySpecsBuilder querySpecsBuilder, //
				final QueryOptions options, //
				final FunctionCall functionCall, //
				final Alias alias) { //

			final SorterMapper sorterMapper = new JsonSorterMapper(functionCall, options.getSorters(), alias);
			final List<OrderByClause> clauses = sorterMapper.deserialize();

			addSortingOptions(querySpecsBuilder, clauses);
		}

		@Override
		public SqlQuerySpecsBuilderBuilder withDataView(final CMDataView value) {
			return (SqlQuerySpecsBuilderBuilder) super.withDataView(value);
		}

		@Override
		public SqlQuerySpecsBuilderBuilder withQueryOptions(final QueryOptions value) {
			return (SqlQuerySpecsBuilderBuilder) super.withQueryOptions(value);
		}

		public SqlQuerySpecsBuilderBuilder withFunction(final CMFunction value) {
			fetchedFunction = value;
			return this;
		}

		public SqlQuerySpecsBuilderBuilder withAlias(final Alias value) {
			functionAlias = value;
			return this;
		}

	}

	public static class DataViewCardFetcherBuilder implements Builder<DataViewCardFetcher> {

		private CMDataView dataView;
		private String className;
		private QueryOptions queryOptions;

		@Override
		public DataViewCardFetcher build() {
			return new DataViewCardFetcher(this);
		}

		public DataViewCardFetcherBuilder withDataView(final CMDataView value) {
			dataView = value;
			return this;
		}

		public DataViewCardFetcherBuilder withClassName(final String value) {
			className = value;
			return this;
		}

		public DataViewCardFetcherBuilder withQueryOptions(final QueryOptions value) {
			queryOptions = value;
			return this;
		}

	}

	public static DataViewCardFetcherBuilder newInstance() {
		return new DataViewCardFetcherBuilder();
	}

	private static final PagedElements<CMCard> EMPTY = new PagedElements<CMCard>(Collections.<CMCard> emptyList(), 0);

	private final CMDataView dataView;
	private final String className;
	private final QueryOptions queryOptions;

	public DataViewCardFetcher(final DataViewCardFetcherBuilder builder) {
		this.dataView = builder.dataView;
		this.className = builder.className;
		this.queryOptions = builder.queryOptions;
	}

	public PagedElements<CMCard> fetch() {
		final CMClass fetchedClass = dataView.findClass(className);
		if (fetchedClass == null) {
			return EMPTY;
		}

		final CMQueryResult result = new QuerySpecsBuilderBuilder() //
				.withDataView(dataView) //
				.withClass(fetchedClass) //
				.withQueryOptions(queryOptions) //
				.build() //
				.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(fetchedClass);
			filteredCards.add(card);
		}

		return new PagedElements<CMCard>(filteredCards, result.totalSize());
	}

}
