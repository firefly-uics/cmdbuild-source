package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Map;

import org.cmdbuild.dao.entry.CardReference;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.data.store.Store.Storable;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.commands.GetRelationList.DomainInfo;
import org.cmdbuild.logic.commands.GetRelationList.GetRelationListResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonGetRelationListResponse extends AbstractJsonResponseSerializer implements JsonSerializable {

	private final GetRelationListResponse response;
	private final int domainLimit;
	private final LookupStore lookupStore;

	public JsonGetRelationListResponse(final GetRelationListResponse inner, final int domainLimitOrZero) {
		this.response = inner;
		this.domainLimit = domainLimitOrZero > 0 ? domainLimitOrZero : Integer.MAX_VALUE;
		this.lookupStore = applicationContext().getBean(LookupStore.class);
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
		relation.put("rel_attr", relationAttributesToJson(relationInfo));
		return relation;
	}

	private JSONObject relationAttributesToJson(RelationInfo relationInfo) throws JSONException {
		final JSONObject jsonAttr = new JSONObject();
		final CMDomain domain = relationInfo.getRelation().getType();
		for (Map.Entry<String, Object> attr : relationInfo.getRelationAttributes()) {
			CMAttributeType<?> type = domain.getAttribute(attr.getKey()).getType();
			final Object value = attr.getValue();
			if (type instanceof LookupAttributeType //
					&& value != null) { //
				final CardReference cardReference = CardReference.class.cast(value);
				Lookup lookup = null;
				if (cardReference.getId() != null) {
					lookup = lookupStore.read(createFakeStorableFrom((cardReference.getId())));
				}
				if (lookup != null) {
					String lookupDescription = lookup.description;
					attr.setValue(lookupDescription);
				}
			}
			// end of bug fixing

			jsonAttr.put(attr.getKey(), javaToJsonValue(type, attr.getValue()));
		}
		return jsonAttr;
	}

	private Storable createFakeStorableFrom(final Long storableId) {
		return new Storable() {
			@Override
			public String getIdentifier() {
				return storableId.toString();
			}
		};
	}
}