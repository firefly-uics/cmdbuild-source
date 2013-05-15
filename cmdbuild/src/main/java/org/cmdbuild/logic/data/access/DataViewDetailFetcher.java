package org.cmdbuild.logic.data.access;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.join.Over.over;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ANY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.QuerySpecsBuilder;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.alias.NameAlias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.mapping.json.JsonFullTextQueryBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class DataViewDetailFetcher {

	private final CMDataView _dataView;

	public DataViewDetailFetcher(final CMDataView dataView) {
		_dataView = dataView;
	}

	public PagedElements<CMCard> fetch( //
			final String detailClassName, //
			final QueryOptions queryOptions //
			) {

		final CMClass detailClass = _dataView.findClass(detailClassName);
		final QuerySpecsBuilder querySpecsBuilder = _dataView
				.select(anyAttribute(detailClass))
				.from(detailClass);

		final FilterAnalizer filterAnalizer = new FilterAnalizer();
		filterAnalizer.fillQuerySpecsBuilderWithJSONFilter( //
				querySpecsBuilder, //
				queryOptions.getFilter(), //
				detailClass //
			);

		querySpecsBuilder.offset(queryOptions.getOffset());
		querySpecsBuilder.limit(queryOptions.getLimit());

		final CMQueryResult result = querySpecsBuilder.run();
		final List<CMCard> filteredCards = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(_dataView.findClass(detailClassName));
			filteredCards.add(card);
		}

		return new PagedElements<CMCard>(filteredCards, result.totalSize());
	}

	private class FilterAnalizer {

		private final List<WhereClause> _whereClauses;
		private final SecureRandom _random;

		public FilterAnalizer() {
			_whereClauses = new LinkedList<WhereClause>();
			_random = new SecureRandom();
		}

		public void fillQuerySpecsBuilderWithJSONFilter( //
				final QuerySpecsBuilder querySpecsBuilder, //
				final JSONObject jsonFilter, //
				final CMClass entryType
				) {

			try {
				manageFilterOverRelations(querySpecsBuilder, jsonFilter, entryType);
				manageFullTextQuery(querySpecsBuilder, jsonFilter, entryType);
			} catch (JSONException e) {
				throw new IllegalArgumentException("The filter is malformed");
			}

			fillQuerySpecsBuilderWhereCondition(querySpecsBuilder);
		}

		private void manageFilterOverRelations( //
				final QuerySpecsBuilder querySpecsBuilder, //
				final JSONObject jsonFilter, //
				final CMClass entryType //
				) throws JSONException {

			if (jsonFilter.has(RELATION_KEY)) {
				final JSONArray conditions = jsonFilter.getJSONArray(RELATION_KEY);

				for (int i = 0; i < conditions.length(); i++) {
					final JSONObject condition = conditions.getJSONObject(i);
					final String domainName = condition.getString(RELATION_DOMAIN_KEY);
					final CMDomain domain = _dataView.findDomain(domainName);

					final String destinationName = condition.getString(RELATION_DESTINATION_KEY);
					final CMClass destinationClass = _dataView.findClass(destinationName);

					final boolean left = condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE);
					final Alias destinationAlias = NameAlias.as(String.format("DST-%s-%s", destinationName, randomString()));

					// Add the join
					if (left) {
						querySpecsBuilder.leftJoin(destinationClass, destinationAlias, over(domain));
					} else {
						querySpecsBuilder.join(destinationClass, destinationAlias, over(domain));
					}

					// Add the where
					final String conditionType = condition.getString(RELATION_TYPE_KEY);

					if (conditionType.equals(RELATION_TYPE_ONEOF)) {
						final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);
						final List<Long> ids = new LinkedList<Long>();

						for (int j = 0; j < cards.length(); j++) {
							final JSONObject card = cards.getJSONObject(j);
							ids.add(card.getLong(RELATION_CARD_ID_KEY));
						}

						_whereClauses.add( //
								condition( //
									QueryAliasAttribute.attribute(destinationAlias, Id.getDBName()), //
									in(ids.toArray()) //
									)
								);

					} else if (conditionType.equals(RELATION_TYPE_NOONE)) {
						// TODO
					} else if (conditionType.equals(RELATION_TYPE_ANY)) {
						// TODO
					}
				}
			}
		}

		private void manageFullTextQuery( //
				final QuerySpecsBuilder querySpecsBuilder, //
				final JSONObject jsonFilter, //
				final CMClass entryType //
				) {
			String fullTextQuery = readFullTextQuery(jsonFilter);

			if (fullTextQuery != null) {
				final JsonFullTextQueryBuilder jsonFullTextQueryBuilder = new JsonFullTextQueryBuilder( //
						fullTextQuery, //
						entryType, //
						null //
					);

				_whereClauses.add(jsonFullTextQueryBuilder.build());
			}
		}

		private String readFullTextQuery(final JSONObject jsonFilter) {
			String fullTextQuery = null;
			try {
				fullTextQuery = jsonFilter.getString(FULL_TEXT_QUERY_KEY);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return fullTextQuery;
		}

		private void fillQuerySpecsBuilderWhereCondition(final QuerySpecsBuilder querySpecsBuilder) {
			querySpecsBuilder.where(and(_whereClauses));
		}

		public String randomString() {
			return new BigInteger(130, _random).toString(32);
		}
	}
}
