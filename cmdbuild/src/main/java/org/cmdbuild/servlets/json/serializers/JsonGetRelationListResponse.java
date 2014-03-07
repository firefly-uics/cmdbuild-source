package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGetRelationListResponse extends AbstractJsonResponseSerializer implements JsonSerializable {

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
		jsonResponse = new JSONObject();
		jsonResponse.put("domains", domainArray);
		return jsonResponse;
	}

	private JSONArray domainListToJson() throws JSONException {
		final JSONArray domainArray = new JSONArray();
		for (DomainInfo di : response) {
			domainArray.put(domainToJson(di));
		}
		return domainArray;
	}

	private JSONObject domainToJson(DomainInfo domainInfo) throws JSONException {
		final JSONObject domain = new JSONObject();
		final JSONArray relationArray = relationListToJson(domainInfo);
		domain.put("id", domainInfo.getQueryDomain().getDomain().getId());
		domain.put("src", domainInfo.getQueryDomain().getQuerySource());
		if (relationArray.length() <= domainLimit) {
			domain.put("relations", relationArray);
		}
		domain.put("relations_size", relationArray.length());
		return domain;
	}

	private JSONArray relationListToJson(DomainInfo domainInfo) throws JSONException {
		final JSONArray relationArray = new JSONArray();
		for (RelationInfo relationInfo : domainInfo) {
			relationArray.put(relationToJson(relationInfo));
		}
		return relationArray;
	}

	private JSONObject relationToJson(RelationInfo relationInfo) throws JSONException {
		JSONObject relation = new JSONObject();
		relation.put("dst_id", relationInfo.getTargetId());
		relation.put("dst_cid", relationInfo.getTargetType().getId());
		relation.put("dst_code", relationInfo.getTargetCode());
		relation.put("dst_desc", relationInfo.getTargetDescription());
		relation.put("rel_id", relationInfo.getRelationId());
		relation.put("rel_date", formatDateTime(relationInfo.getRelationBeginDate()));
		relation.put("rel_attr", RelationAttributeSerializer.toClient(relationInfo));
		return relation;
	}

}