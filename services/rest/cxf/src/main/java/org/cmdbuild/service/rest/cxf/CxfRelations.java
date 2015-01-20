package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_DESTINATION_ID;
import static org.cmdbuild.service.rest.constants.Serialization.UNDERSCORED_SOURCE_ID;
import static org.cmdbuild.service.rest.cxf.util.Json.safeJsonObject;
import static org.cmdbuild.service.rest.model.Models.newCard;
import static org.cmdbuild.service.rest.model.Models.newMetadata;
import static org.cmdbuild.service.rest.model.Models.newRelation;
import static org.cmdbuild.service.rest.model.Models.newResponseMultiple;
import static org.cmdbuild.service.rest.model.Models.newResponseSingle;

import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.service.rest.Relations;
import org.cmdbuild.service.rest.model.Relation;
import org.cmdbuild.service.rest.model.ResponseMultiple;
import org.cmdbuild.service.rest.model.ResponseSingle;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class CxfRelations implements Relations {

	private static class RelationInfoToRelation implements Function<RelationInfo, Relation> {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<RelationInfoToRelation> {

			private boolean includeValues;

			private Builder() {
				// use factory method
			}

			@Override
			public RelationInfoToRelation build() {
				return new RelationInfoToRelation(this);
			}

			public Builder includeValues(final boolean includeValues) {
				this.includeValues = includeValues;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private static final Map<String, Object> NO_VALUES = emptyMap();

		private final boolean includeValues;

		private RelationInfoToRelation(final Builder builder) {
			this.includeValues = builder.includeValues;
		}

		@Override
		public Relation apply(final RelationInfo input) {
			final CMClass sourceType = input.getSourceType();
			final CMClass targetType = input.getTargetType();
			return newRelation() //
					.withId(input.getRelationId()) //
					.withType(input.getQueryDomain().getDomain().getName()) //
					.withSource(newCard() //
							.withType(sourceType.getName()) //
							.withId(input.getSourceId()) //
							.withValue(sourceType.getDescriptionAttributeName(), input.getSourceDescription()) //
							.build()) //
					.withDestination(newCard() //
							.withType(targetType.getName()) //
							.withId(input.getTargetId()) //
							.withValue(targetType.getDescriptionAttributeName(), input.getTargetDescription()) //
							.build()) //
					.withValues(includeValues ? input.getRelationAttributes() : NO_VALUES.entrySet()) //
					.build();
		}

	};

	private static final RelationInfoToRelation BASIC_DETAILS = RelationInfoToRelation.newInstance() //
			.includeValues(false) //
			.build();

	private static final RelationInfoToRelation FULL_DETAILS = RelationInfoToRelation.newInstance() //
			.includeValues(true) //
			.build();

	private final ErrorHandler errorHandler;
	private final DataAccessLogic dataAccessLogic;

	public CxfRelations(final ErrorHandler errorHandler, final DataAccessLogic dataAccessLogic) {
		this.errorHandler = errorHandler;
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ResponseSingle<Long> create(final String domainId, final Relation relation) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			final RelationDTO relationDTO = new RelationDTO();
			relationDTO.domainName = targetDomain.getName();
			relationDTO.master = "_1";
			relationDTO.addSourceCard(relation.getSource().getId(), relation.getSource().getType());
			relationDTO.addDestinationCard(relation.getDestination().getId(), relation.getDestination().getType());
			relationDTO.relationAttributeToValue = relation.getValues();
			final Long created = from(dataAccessLogic.createRelations(relationDTO)).first().get();
			return newResponseSingle(Long.class) //
					.withElement(created) //
					.build();
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public ResponseMultiple<Relation> read(final String domainId, final String filter, final Integer limit,
			final Integer offset) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			String _filter = defaultString(filter);
			// TODO do it better
			// <<<<<
			final String regex_1 = "\"attribute\"[\\w]*:[\\w]*\"" + UNDERSCORED_SOURCE_ID + "\"";
			final String replacement_1 = "\"attribute\":\"IdObj1\"";
			_filter = _filter.replaceAll(regex_1, replacement_1);

			final String regex_2 = "\"attribute\"[\\w]*:[\\w]*\"" + UNDERSCORED_DESTINATION_ID + "\"";
			final String replacement_2 = "\"attribute\":\"IdObj2\"";
			_filter = _filter.replaceAll(regex_2, replacement_2);
			// <<<<<
			final QueryOptions queryOptions = QueryOptions.newQueryOption() //
					.filter(safeJsonObject(_filter)) //
					.limit(limit) //
					.offset(offset) //
					.build();
			final GetRelationListResponse response = dataAccessLogic.getRelationList(targetDomain, queryOptions);
			final List<Relation> elements = newArrayList();
			for (final DomainInfo domainInfo : response) {
				addAll(elements, from(domainInfo) //
						.transform(BASIC_DETAILS));
			}
			return newResponseMultiple(Relation.class) //
					.withElements(elements) //
					.withMetadata(newMetadata() //
							.withTotal(Long.valueOf(response.getTotalNumberOfRelations())) //
							.build()) //
					.build();
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return null;
	}

	@Override
	public ResponseSingle<Relation> read(final String domainId, final Long relationId) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		final Optional<RelationInfo> relation = getRelation(relationId, targetDomain);
		if (!relation.isPresent()) {
			errorHandler.relationNotFound(relationId);
		}
		final Relation element = FULL_DETAILS.apply(relation.get());
		return newResponseSingle(Relation.class) //
				.withElement(element) //
				.build();
	}

	private Optional<RelationInfo> getRelation(final Long relationId, final CMDomain targetDomain) {
		try {
			return dataAccessLogic.getRelation(targetDomain, relationId);
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return Optional.absent();
	}

}
