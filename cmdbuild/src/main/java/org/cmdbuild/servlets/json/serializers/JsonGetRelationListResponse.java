package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.cmdbuild.logic.commands.GetRelationList.RelationInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGetRelationListResponse implements JsonSerializable {
	private final GetRelationListResponse response;
	private final int domainLimit;

	public JsonGetRelationListResponse(final GetRelationListResponse inner, final int domainLimitOrZero) {
		this.response = inner;
		this.domainLimit = domainLimitOrZero > 0 ? domainLimitOrZero : Integer.MAX_VALUE;
	}

	@Override
	public JSONObject toJson() throws JSONException {
		final JSONObject jsonResponse;
		final JSONArray domainArray = domainListToJson();
		if (domainArray.length() > 1) {
			jsonResponse = new JSONObject();
			jsonResponse.put("domains", domainArray);
		} else {
			jsonResponse = domainArray.getJSONObject(0);
		}
		return jsonResponse;
	}

	private JSONArray domainListToJson() throws JSONException {
		final JSONArray domainArray = new JSONArray();
		for (DomainInfo di : response) {
			domainArray.put(domainToJson(di));
		}
		return domainArray;
	}

	private JSONObject domainToJson(DomainInfo di) throws JSONException {
		final JSONObject domain = new JSONObject();
		final JSONArray relationArray = relationListToJson(di);
		domain.put("id", di.getQueryDomain().getDomain().getId());
		if (relationArray.length() <= domainLimit) {
			domain.put("relations", relationArray);
		}
		domain.put("relations_size", relationArray.length());
		return domain;
	}

	private JSONArray relationListToJson(DomainInfo di) throws JSONException {
		final JSONArray relationArray = new JSONArray();
		for (RelationInfo ri : di) {
			relationArray.put(relationToJson(ri));
		}
		return relationArray;
	}

	private JSONObject relationToJson(RelationInfo ri) throws JSONException {
		JSONObject relation = new JSONObject();
		relation.put("dst_id", ri.getTargetId());
		relation.put("dst_cid", ri.getTargetType().getId());
		relation.put("dst_code", ri.getTargetCode());
		relation.put("dst_desc", ri.getTargetDescription());
		relation.put("rel_id", ri.getRelationId());
		relation.put("rel_date", ri.getRelationLastModified());
		relation.put("rel_attr", ri.getRelationAttributes());
		return relation;
	}
}