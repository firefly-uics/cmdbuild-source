package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_ID_CAPITAL;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID_CAPITAL;
import static org.cmdbuild.servlets.json.ComunicationConstants.RESULTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.ROWS;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.dao.constants.Cardinality;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.query.clause.QueryDomain.Source;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.data.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

public class CardSerializer {

	// TODO continue the implementation,
	// pay attention to lookup and references

	public static JSONObject toClient(final Card card, final String wrapperLabel) throws JSONException {
		final JSONObject json = new JSONObject();

		// add the attributes
		for (final Map.Entry<String, Object> entry : card.getAttributes().entrySet()) {
			json.put(entry.getKey(), entry.getValue());
		}

		// add some required info
		json.put(ID_CAPITAL, card.getId());
		// TODO if IdClass is no more needed, remove getClassId() method too
		json.put(CLASS_ID_CAPITAL, card.getClassId());

		// We must serialize the class description
		// Is used listing the card of a superclass to
		// know the effective class
		// The ugly key is driven by backward compatibility
		json.put("IdClass_value", card.getClassDescription());

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

	private static Map<String, Map<String, Object>> getReferenceAttributes(final Card card) {
		final DataAccessLogic dataAccessLogic = TemporaryObjectsBeforeSpringDI.getSystemDataAccessLogic();
		final Map<String, Map<String, Object>> referenceAttributes = Maps.newHashMap();
		final CMClass owner = card.getType();
		if (owner == null) {
			return referenceAttributes;
		}
		for (String referenceAttributeName : card.getAttributes().keySet()) {
			final CMAttributeType<?> attributeType = owner.getAttribute(referenceAttributeName).getType();
			if (attributeType instanceof ReferenceAttributeType) {
				String domainName = ((ReferenceAttributeType) attributeType).getDomainName();
				Long domainId = dataAccessLogic.findDomain(domainName).getId();
				final GetRelationListResponse response;
				if (dataAccessLogic.findDomain(domainName).getCardinality().equals(Cardinality.CARDINALITY_1N.value())) {
					response = dataAccessLogic.getRelationList(card, DomainWithSource.create(domainId, Source._2.toString()));
				} else { // CARDINALITY_N1
					response = dataAccessLogic.getRelationList(card, DomainWithSource.create(domainId, Source._1.toString()));
				}
				Map<String, Object> inner = Maps.newHashMap();
				for (DomainInfo domainInfo : response) {
					for (RelationInfo relationInfo : domainInfo) {
						for (Entry<String, Object> entry : relationInfo.getRelationAttributes()) {
							inner.put(entry.getKey(), entry.getValue());
						}
					}
				}
				referenceAttributes.put(referenceAttributeName, inner);
			}
		}
		return referenceAttributes;
	}

	public static JSONObject toClient(final Card card) throws JSONException {
		return toClient(card, null);
	}

	public static JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize //
	) throws JSONException {

		return toClient(cards, totalSize, ROWS);
	}

	public static JSONObject toClient( //
			final Iterable<Card> cards, //
			final int totalSize, //
			final String cardsLabel //
	) throws JSONException {

		final JSONObject json = new JSONObject();
		final JSONArray jsonRows = new JSONArray();
		for (final Card card : cards) {
			jsonRows.put(CardSerializer.toClient(card));
		}

		json.put(cardsLabel, jsonRows);
		json.put(RESULTS, totalSize);
		return json;
	}

}
