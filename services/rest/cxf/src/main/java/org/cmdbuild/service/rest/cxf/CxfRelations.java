package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.Relations;
import org.cmdbuild.service.rest.dto.Card;
import org.cmdbuild.service.rest.dto.DetailResponseMetadata;
import org.cmdbuild.service.rest.dto.ListResponse;
import org.cmdbuild.service.rest.dto.Relation;

import com.google.common.base.Function;

public class CxfRelations implements Relations {

	private static final Function<RelationInfo, Relation> RELATION_INFO_TO_RELATION = new Function<RelationInfo, Relation>() {

		@Override
		public Relation apply(final RelationInfo input) {
			final CMClass sourceType = input.getSourceType();
			final CMClass targetType = input.getTargetType();
			return Relation.newInstance() //
					.withId(input.getRelationId()) //
					.withType(input.getQueryDomain().getDomain().getName()) //
					.withSource(Card.newInstance() //
							.withType(sourceType.getName()) //
							.withId(input.getSourceId()) //
							.withValue(sourceType.getCodeAttributeName(), input.getSourceCode()) //
							.withValue(sourceType.getDescriptionAttributeName(), input.getSourceDescription()) //
							.build()) //
					.withDestination(Card.newInstance() //
							.withType(targetType.getName()) //
							.withId(input.getTargetId()) //
							.withValue(targetType.getCodeAttributeName(), input.getTargetCode()) //
							.withValue(targetType.getDescriptionAttributeName(), input.getTargetDescription()) //
							.build()) //
					// TODO date -> input.getRelationBeginDate()
					// TODO attributes date ->
					// relationAttributeSerializer.toClient(input)
					.build();
		}

	};

	private final ErrorHandler errorHandler;
	private final DataAccessLogic dataAccessLogic;

	public CxfRelations(final ErrorHandler errorHandler, final DataAccessLogic dataAccessLogic) {
		this.errorHandler = errorHandler;
		this.dataAccessLogic = dataAccessLogic;
	}

	@Override
	public ListResponse<Relation> read(final String type, final String className, final Long cardId,
			final String domainSource, final Integer limit, final Integer offset) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(type);
		if (targetDomain == null) {
			errorHandler.domainNotFound(type);
		}
		final org.cmdbuild.model.data.Card src = org.cmdbuild.model.data.Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		final DomainWithSource dom = DomainWithSource.create(targetDomain.getId(), domainSource);
		try {
			final GetRelationListResponse response = dataAccessLogic.getRelationListEmptyForWrongId(src, dom);
			final List<Relation> elements = newArrayList();
			for (final DomainInfo domainInfo : response) {
				addAll(elements, from(domainInfo) //
						.transform(RELATION_INFO_TO_RELATION));
			}
			return ListResponse.newInstance(Relation.class) //
					.withElements(elements) //
					.withMetadata(DetailResponseMetadata.newInstance() //
							.withTotal(Long.valueOf(response.getTotalNumberOfRelations())) //
							.build()) //
					.build();
		} catch (final Exception e) {
			errorHandler.propagate(e);
		}
		return null;
	}

}