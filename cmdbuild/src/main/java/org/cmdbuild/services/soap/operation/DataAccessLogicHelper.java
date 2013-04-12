package org.cmdbuild.services.soap.operation;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.services.soap.operation.SerializationStuff.Functions.toAttributeSchema;

import java.util.List;
import java.util.Map;


import org.cmdbuild.dao.CardStatus;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.RelationDTO;
import org.cmdbuild.model.data.Card;
import org.cmdbuild.services.soap.structure.AttributeSchema;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.Relation;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataAccessLogicHelper implements SoapLogicHelper {

	private static final Marker marker = MarkerFactory.getMarker(DataAccessLogicHelper.class.getName());

	private final CMDataView dataView;
	private final DataAccessLogic dataAccessLogic;

	public DataAccessLogicHelper(final CMDataView dataView, final DataAccessLogic datAccessLogic) {
		this.dataView = dataView;
		this.dataAccessLogic = datAccessLogic;
	}

	public AttributeSchema[] getAttributeList(final String className) {
		logger.info(marker, "getting attributes schema for class '{}'", className);
		return from(dataAccessLogic.findClass(className).getActiveAttributes()) //
				.transform(toAttributeSchema()) //
				.toArray(AttributeSchema.class);
	}

	public int createCard(final org.cmdbuild.services.soap.types.Card card) {
		return dataAccessLogic.createCard(transform(card)).intValue();
	}

	public boolean updateCard(final org.cmdbuild.services.soap.types.Card card) {
		dataAccessLogic.updateCard(transform(card));
		return true;
	}

	public boolean deleteCard(final String className, final int cardId) {
		dataAccessLogic.deleteCard(className, Long.valueOf(cardId));
		return true;
	}

	public boolean createRelation(final Relation relation) {
		dataAccessLogic.createRelations(transform(relation));
		return true;
	}

	public boolean deleteRelation(final Relation relation) {
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final DomainWithSource dom = DomainWithSource.create(domain.getId(), Source._1.toString());
		final Card srcCard = Card.newInstance() //
				.withClassName(relation.getClass1Name()) //
				.withId(Long.valueOf(relation.getCard1Id())) //
				.build();
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		for (DomainInfo domainInfo : response) {
			for (RelationInfo relationInfo : domainInfo) {
				if (relationInfo.getTargetId().equals(Long.valueOf(relation.getCard2Id()))) {
					RelationDTO relationToDelete = transform(relation);
					relationToDelete.relationId = relationInfo.getRelationId();
					dataAccessLogic.deleteRelation(relationToDelete);
				}
			}
		}
		return true;
	}

	private Card transform(final org.cmdbuild.services.soap.types.Card card) {
		final Card _card = Card.newInstance() //
				.withClassName(card.getClassName()) //
				.withId(Long.valueOf(card.getId())) //
				.withAllAttributes(transform(card.getAttributeList())) //
				.build();
		return _card;
	}

	private static Map<String, Object> transform(final List<Attribute> attributes) {
		final Map<String, Object> keysAndValues = Maps.newHashMap();
		for (final Attribute attribute : attributes) {
			final String name = attribute.getName();
			final String value = attribute.getValue();
			if (value != null) {
				keysAndValues.put(name, value);
			}
		}
		return keysAndValues;
	}

	private RelationDTO transform(final Relation relation) {
		final RelationDTO relationDTO = new RelationDTO();
		relationDTO.domainName = relation.getDomainName();
		relationDTO.master = Source._1.toString();
		relationDTO.addSourceCardToClass(Long.valueOf(relation.getCard1Id()), relation.getClass1Name());
		relationDTO.addDestinationCardToClass(Long.valueOf(relation.getCard2Id()), relation.getClass2Name());
		return relationDTO;
	}

	private Relation transform(final RelationInfo relationInfo, final CMDomain domain) {
		Relation relation = new Relation();
		relation.setBeginDate(relationInfo.getRelationBeginDate().toGregorianCalendar());
		relation.setEndDate(relationInfo.getRelationEndDate().toGregorianCalendar());
		relation.setStatus(CardStatus.ACTIVE.value());
		relation.setDomainName(domain.getIdentifier().getLocalName());
		relation.setClass1Name(domain.getClass1().getIdentifier().getLocalName());
		relation.setClass2Name(domain.getClass2().getIdentifier().getLocalName());
		relation.setCard1Id(relationInfo.getRelation().getCard1Id().intValue());
		relation.setCard2Id(relationInfo.getRelation().getCard2Id().intValue());
		return relation;
	}

	public List<Relation> getRelations(final String className, final String domainName, final Long cardId) {
		final CMDomain domain = dataView.findDomain(domainName);
		final DomainWithSource dom;
		if (domain.getClass1().getIdentifier().getLocalName().equals(className)) {
			dom = DomainWithSource.create(domain.getId(), Source._1.toString());
		} else {
			dom = DomainWithSource.create(domain.getId(), Source._2.toString());
		}
		final Card srcCard = buildCard(cardId, className);
		final GetRelationListResponse response = dataAccessLogic.getRelationList(srcCard, dom);
		final List<Relation> relations = Lists.newArrayList();
		for (DomainInfo domainInfo : response) {
			for (RelationInfo relationInfo : domainInfo) {
				relations.add(transform(relationInfo, domain));
			}
		}
		return relations;
	}

	private Card buildCard(final Long cardId, final String className) {
		final Card card = Card.newInstance() //
				.withClassName(className) //
				.withId(cardId) //
				.build();
		return card;
	}

	public Relation[] getRelationHistory(final Relation relation) {
		final List<Relation> historicRelations = Lists.newArrayList();
		final CMDomain domain = dataView.findDomain(relation.getDomainName());
		final Card srcCard = buildCard(Long.valueOf(relation.getCard1Id()), relation.getClass1Name());
		GetRelationHistoryResponse response = dataAccessLogic.getRelationHistory(srcCard, domain);
		for (RelationInfo relationInfo : response) {
			if (relationInfo.getRelation().getCard1Id().equals(Long.valueOf(relation.getCard1Id()))
					&& relationInfo.getRelation().getCard2Id().equals(Long.valueOf(relation.getCard2Id()))) {
				historicRelations.add(transform(relationInfo, domain));
			}
		}
		return historicRelations.toArray(new Relation[historicRelations.size()]);
	}

}
