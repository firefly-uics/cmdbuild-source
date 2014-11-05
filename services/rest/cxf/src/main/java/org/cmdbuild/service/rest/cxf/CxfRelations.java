package org.cmdbuild.service.rest.cxf;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.addAll;
import static com.google.common.collect.Lists.newArrayList;
import static org.cmdbuild.service.rest.model.Builders.newCard;
import static org.cmdbuild.service.rest.model.Builders.newMetadata;
import static org.cmdbuild.service.rest.model.Builders.newRelation;
import static org.cmdbuild.service.rest.model.Builders.newResponseMultiple;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.service.rest.Relations;
import org.cmdbuild.service.rest.model.Relation;
import org.cmdbuild.service.rest.model.ResponseMultiple;

import com.google.common.base.Function;

public class CxfRelations implements Relations {

	private static final Function<RelationInfo, Relation> RELATION_INFO_TO_RELATION = new Function<RelationInfo, Relation>() {

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
							.withValue(sourceType.getCodeAttributeName(), input.getSourceCode()) //
							.withValue(sourceType.getDescriptionAttributeName(), input.getSourceDescription()) //
							.build()) //
					.withDestination(newCard() //
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
	public ResponseMultiple<Relation> read(final String domainId, final Integer limit, final Integer offset) {
		final CMDomain targetDomain = dataAccessLogic.findDomain(domainId);
		if (targetDomain == null) {
			errorHandler.domainNotFound(domainId);
		}
		try {
			final GetRelationListResponse response = dataAccessLogic.getRelationList(targetDomain);
			final List<Relation> elements = newArrayList();
			for (final DomainInfo domainInfo : response) {
				addAll(elements, from(domainInfo) //
						.transform(RELATION_INFO_TO_RELATION));
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

}
