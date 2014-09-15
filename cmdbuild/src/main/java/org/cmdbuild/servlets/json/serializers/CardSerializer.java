package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.logic.translation.DefaultTranslationLogic.DESCRIPTION_FOR_CLIENT;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.CommunicationConstants.RESULTS;
import static org.cmdbuild.servlets.json.CommunicationConstants.*;

import java.util.Map;

import org.cmdbuild.dao.constants.Cardinality;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.translation.ClassTranslation;
import org.cmdbuild.logic.translation.TranslationObject;
import org.cmdbuild.model.data.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class CardSerializer {

	private final DataAccessLogic dataAccessLogic;
	private final RelationAttributeSerializer relationAttributeSerializer;
	private final TranslationFacade translationFacade;
	private final LookupStore lookupStore;

	public CardSerializer( //
			final SystemDataAccessLogicBuilder dataAccessLogicBuilder, //
			final RelationAttributeSerializer relationAttributeSerializer, //
			final TranslationFacade translationFacade, final LookupStore lookupStore) {
		this.dataAccessLogic = dataAccessLogicBuilder.build();
		this.relationAttributeSerializer = relationAttributeSerializer;
		this.translationFacade = translationFacade;
		this.lookupStore = lookupStore;
	}

	/*
	 * TODO continue the implementation, pay attention to lookup and references
	 */

	public JSONObject toClient(final Card card, final String wrapperLabel) throws JSONException {
		final JSONObject json = new JSONObject();

		// add the attributes
		for (final Map.Entry<String, Object> entry : card.getAttributes().entrySet()) {
			final Object output;
			final Object input = entry.getValue();

			if (input instanceof IdAndDescription) {

				if (input instanceof LookupValue) {
					final LookupSerializer lookupSerializer = new LookupSerializer(translationFacade, lookupStore);
					output = lookupSerializer.serializeLookupValue((LookupValue) input);
				} else {
					final IdAndDescription idAndDescription = IdAndDescription.class.cast(input);
					final Map<String, Object> map = Maps.newHashMap();
					map.put(ID, idAndDescription.getId());
					map.put(DESCRIPTION, idAndDescription.getDescription());
					output = map;
				}

			} else {
				output = entry.getValue();
			}

			json.put(entry.getKey(), output);
		}

		// add some required info
		json.put(ID_CAPITAL, card.getId());
		// TODO if IdClass is no more needed, remove getClassId() method too
		json.put(CLASS_ID_CAPITAL, card.getClassId());
		
		json.put(CLASS_NAME, card.getClassName());

		/*
		 * We must serialize the class description Is used listing the card of a
		 * superclass to know the effective class The ugly key is driven by
		 * backward compatibility
		 */
		json.put("IdClass_value_default", card.getClassDescription());

		final TranslationObject classTranslationObject = ClassTranslation.newInstance().withName(card.getClassName())
				.withField(DESCRIPTION_FOR_CLIENT).build();

		final String translatedClassDescription = translationFacade.read(classTranslationObject);
		json.put("IdClass_value", defaultIfNull(translatedClassDescription, card.getClassDescription()));

		// wrap in a JSON object if required
		if (wrapperLabel != null) {
			final JSONObject wrapper = new JSONObject();
			wrapper.put(wrapperLabel, json);
			wrapper.put("referenceAttributes", getReferenceAttributes(card));
			return wrapper;
		} else {
			return json;
		}
	}

	/*
	 * Return a map with the reference attribute names as keys and a map with
	 * name-value of the relation attributes
	 */
	private Map<String, JSONObject> getReferenceAttributes(final Card card) throws JSONException {
		final Map<String, JSONObject> referenceAttributes = Maps.newHashMap();
		final CMClass owner = card.getType();
		if (owner == null) {
			return referenceAttributes;
		}

		for (final String referenceAttributeName : card.getAttributes().keySet()) {
			final CMAttributeType<?> attributeType = owner.getAttribute(referenceAttributeName).getType();
			if (attributeType instanceof ReferenceAttributeType) {
				final String domainName = ((ReferenceAttributeType) attributeType).getDomainName();
				final Long domainId = dataAccessLogic.findDomain(domainName).getId();
				final GetRelationListResponse response;
				if (dataAccessLogic.findDomain(domainName).getCardinality().equals(Cardinality.CARDINALITY_1N.value())) {
					response = dataAccessLogic.getRelationList(card,
							DomainWithSource.create(domainId, Source._2.toString()));
				} else { // CARDINALITY_N1
					response = dataAccessLogic.getRelationList(card,
							DomainWithSource.create(domainId, Source._1.toString()));
				}

				for (final DomainInfo domainInfo : response) {
					for (final RelationInfo relationInfo : domainInfo) {
						referenceAttributes.put(referenceAttributeName,
								relationAttributeSerializer.toClient(relationInfo, true));
					}
				}
			}
		}
		return referenceAttributes;
	}

	public JSONObject toClient(final Card card) throws JSONException {
		return toClient(card, null);
	}

	public JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize //
	) throws JSONException {

		return toClient(cards, totalSize, ROWS);
	}

	public JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize, //
			final String cardsLabel //
	) throws JSONException {

		final JSONObject json = new JSONObject();
		final JSONArray jsonRows = new JSONArray();
		for (final Card card : cards) {
			jsonRows.put(toClient(card));
		}

		json.put(cardsLabel, jsonRows);
		json.put(RESULTS, totalSize);
		return json;
	}

}
