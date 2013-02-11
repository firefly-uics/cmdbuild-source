package org.cmdbuild.servlets.json.management;

import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.interfaces.RelationFactory;
import org.cmdbuild.elements.interfaces.RelationQuery;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModGraph extends JSONBase {

	@OldDao
	@JSONExported
	public JSONObject getRelationTree(
			JSONObject serializer,
			ICard card,
			@Parameter("levels") int levels,
			RelationFactory rf ) throws JSONException, CMDBException {
		JSONArray nodes = new JSONArray();
		JSONArray archs = new JSONArray();

		Set<ICard> cards = new HashSet<ICard>();
		cards.add(card);
		RelationQuery relations = getRelations(rf, cards, levels);
		/*
		 * Now cards contains all the cards, and relations all the relations between
		 * those cards. You should build the graph you need, instead of serializing
		 * the relations
		 */
		for(IRelation relation : relations) {
			archs.put(serializeArch(relation));
			cards.add(relation.getCard2()); // add the outer cards
		}
		for(ICard _card : cards) {
			nodes.put(serializeNode(_card));
		}
		serializer.put("nodes", nodes);
		serializer.put("archs", archs);
		return serializer;
	}

	/*
	 * This is not optimized at all, but for now it should suffice
	 */
	RelationQuery getRelations(RelationFactory rf, Set<ICard> cards, int levels) {
		// straightened() means that the cards we requested are always on the left side
		RelationQuery relationQuery = rf.list().straightened();
		for (ICard card : cards) {
			relationQuery.card(card);
		}
		if (levels > 1) {
			for (IRelation relation : relationQuery) {
				cards.add(relation.getCard2());
			}
			return getRelations(rf, cards, levels-1);
		} else {
			return relationQuery;
		}
	}

	private JSONObject serializeNode(ICard card) {
		JSONObject serializer = new JSONObject();
		try {
			// these are used for the label
			serializer.put("className", card.getSchema().getName());
			serializer.put("description", card.getDescription());
			// these are used for the engine
			serializer.put("Id", card.getId());
			serializer.put("IdClass", card.getIdClass());
			serializer.put("nodeKey", getUniqueKey(card));
		} catch(JSONException e){
			Log.JSONRPC.error("Error serializing card", e);
		}
		return serializer;
	}

	private JSONObject serializeArch(IRelation relation) {
		JSONObject serializer = new JSONObject();
		try {
			// these are used for the label
			serializer.put("domainName", relation.getSchema().getName());
			serializer.put("description", relation.getSchema().getDescriptionDirect());
			// these are used for the engine
			serializer.put("source", getUniqueKey(relation.getCard1()));
			serializer.put("target", getUniqueKey(relation.getCard2()));
		} catch(JSONException e){
			Log.JSONRPC.error("Error serializing card", e);
		}
		return serializer;
	}
	
	private String getUniqueKey(ICard card) {
		return card.getIdClass() + "_" + card.getId();
	}

}
