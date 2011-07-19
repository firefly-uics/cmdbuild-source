package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationHistory.GetRelationHistoryResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonGetRelationHistoryResponse extends AbstractJsonResponseSerializer implements JsonSerializable {

	private final GetRelationHistoryResponse inner;

	public JsonGetRelationHistoryResponse(final GetRelationHistoryResponse inner) {
		this.inner = inner;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject jsonResponse;
		final JSONArray relationHistoryArray = relationHistoryToJson();
		jsonResponse = new JSONObject();
		jsonResponse.put("rows", relationHistoryArray);
		return jsonResponse;
	}

	private JSONArray relationHistoryToJson() throws JSONException {
		final JSONArray jsonRelationArray = new JSONArray();
		for (RelationInfo ri : inner) {
			jsonRelationArray.put(relationToJson(ri));
		}
		return jsonRelationArray;
	}

	// FIXME It's an awful legacy way of serializing the relation
	private JSONObject relationToJson(RelationInfo ri) throws JSONException {
		JSONObject jsonRelation = new JSONObject();
		CMRelation relation = ri.getRelation();
		jsonRelation.put("_RelHist", true);
		jsonRelation.put("DomainDesc", relation.getType().getDescription());
		jsonRelation.put("User", relation.getUser());
		jsonRelation.put("BeginDate", formatDate(relation.getBeginDate()));
		jsonRelation.put("EndDate", formatDate(relation.getEndDate()));
		jsonRelation.put("Attr", historyRelationAttributesToJson(ri));
		jsonRelation.put("Class", ri.getTargetType().getName());
		jsonRelation.put("CardCode", ri.getTargetCode());
		jsonRelation.put("CardDescription", ri.getTargetDescription());
		return jsonRelation;
	}

	/*
	 * Note: it is different from the relation query since this gives the
	 *       attribute description and it preserves the correct order
	 */
	private JSONArray historyRelationAttributesToJson(RelationInfo ri) throws JSONException {
		final JSONArray jsonAttr = new JSONArray();
		final CMRelation relation = ri.getRelation();
		for (CMAttribute attr : relation.getType().getAttributes()) {
			final JSONObject jsonAttrValue = new JSONObject();
			jsonAttrValue.put("d", attr.getDescription());
			jsonAttrValue.put("v", javaToJsonValue(relation.get(attr.getName())));
			//jsonAttrValue.put("c", TODO: CHANGED);
			jsonAttr.put(jsonAttrValue);
		}
		return jsonAttr;
	}
}
