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
		final JSONArray domainArray = new JSONArray();
		for (DomainInfo di : response) {
			final JSONArray relationArray = new JSONArray();
			for (RelationInfo ri : di) {
				JSONObject relation = new JSONObject();
				relation.put("_description", ri.getTargetDescription());
				relationArray.put(relation);
			}
			final JSONObject domain = new JSONObject();
			domain.put("_description", di.getDescription());
			if (relationArray.length() <= domainLimit) {
				domain.put("relations", relationArray);
			}
			domain.put("relations_size", relationArray.length());
			domainArray.put(domain);
		}

		final JSONObject jsonResponse;
		if (domainArray.length() > 1) {
			jsonResponse = new JSONObject();
			jsonResponse.put("domains", domainArray);
		} else {
			jsonResponse = domainArray.getJSONObject(0);
		}
		return jsonResponse;
	}
}