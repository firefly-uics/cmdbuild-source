package org.cmdbuild.servlets.json.schema;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.elements.Lookup;
import org.cmdbuild.elements.LookupType;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.operation.schema.LookupTypeOperation;
import org.cmdbuild.services.SchemaCache;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.utils.tree.CTree;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class Lookup
 */
public class ModLookup extends JSONBase {

	@JSONExported
	public JSONArray tree() throws JSONException {
		Iterable<LookupType> lookupTypes = SchemaCache.getInstance().getLookupTypeList();
		JSONArray jsonLookupTypes = new JSONArray();
		
		for (LookupType lookupType: lookupTypes) {
			JSONObject jsonLookupType = Serializer.serializeLookupTable(lookupType);
			jsonLookupTypes.put(jsonLookupType);
		}
		
		return jsonLookupTypes;
	}
	
	@JSONExported
	public JSONObject getLookupTypeList(JSONObject serializer) throws JSONException {
		CTree<LookupType> tree = SchemaCache.getInstance().getLookupTypeTree();
		for(LookupType lt : tree.getLeaves()) {
			if (lt == tree.getRootElement().getData())
				continue;
			JSONObject jlt = new JSONObject();
			jlt.put("type", lt.getType());
			serializer.append("rows", jlt);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getLookupType(
			JSONObject serializer,
			UserContext userCtx,
			@Parameter("type") String type ) throws JSONException {
		LookupTypeOperation lo = new LookupTypeOperation(userCtx);
		LookupType lookupType = lo.getLookupType(type);

		serializer.put("data", Serializer.serializeLookupType(lookupType));
		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject saveLookupType(JSONObject serializer,
			@Parameter("description") String type,
			@Parameter("orig_type") String originalType,
			@Parameter(value="parent", required=false) String parentType,
			LookupTypeOperation lo) throws JSONException {
		if(parentType==null) parentType="";
		LookupType lookupType = lo.saveLookupType(type, originalType, parentType);
		JSONObject jsonLookupType = Serializer.serializeLookupTable(lookupType);
		serializer.put("lookup", jsonLookupType);
		if (!"".equals(originalType)) {
			jsonLookupType.put("oldId", originalType);
		} else {
			serializer.put("isNew", true);
		}
		return serializer;
	}

	@JSONExported
	public JSONObject getLookupList(
			JSONObject serializer,
			LookupOperation lo,
			@Parameter("type") String lookupType,
			@Parameter(value="start", required=false) int start,
			@Parameter(value="limit", required=false) int limit,
			@Parameter("active") boolean active,
			@Parameter(value="short", required=false) boolean shortForm) throws JSONException {
    	List<Lookup> list = lo.getLookupList(lookupType);

    	//order by number
    	Collections.sort(list, new Comparator<Lookup>() {			
			public int compare(Lookup l1, Lookup l2) {
				if (l1.getNumber() > l2.getNumber()) {
					return 1;
				} else if (l1.getNumber() < l2.getNumber()) {
					return -1;
				}
				return 0;
			}
		});
    	
		if (list == null) {
			throw NotFoundExceptionType.LOOKUP_TYPE_NOTFOUND.createException(lookupType);
		}
		int i = 0;
		int end = limit > 0 ? limit+start : Integer.MAX_VALUE;
		for (Lookup lookup: list) {
			if (!active || lookup.getStatus().isActive()) {
				if (i >= start && i < end) {
					serializer.append("rows", Serializer.serializeLookup(lookup, shortForm));
				}
				++i;
			}
		}
		serializer.put("total", i);
		return serializer;
	}
	
	@JSONExported
	public JSONObject getParentList(
			JSONObject serializer,
			LookupOperation lo,
			@Parameter(value="type", required=false) String type) throws JSONException, AuthException {
       	if(type!=null && !type.equals("")){
       		LookupType lookupType = SchemaCache.getInstance().getLookupType(type);
       		String parentType = "";
       		if(lookupType!=null)
       			parentType = lookupType.getParentType();
       		if(parentType!=null && !(parentType.trim().equals(""))){
       			Iterable<Lookup> list = lo.getLookupList(parentType);
       			if (list == null)
       				throw NotFoundExceptionType.LOOKUP_NOTFOUND.createException(parentType);
       			// Serialize result
       			for(Lookup lookup: list){
       				serializer.append("rows", Serializer.serializeLookupParent(lookup));
       			}
       		}
   		}
		return serializer;
	}
	
	@JSONExported
	public JSONObject getLookup(
			JSONObject serializer,
			LookupOperation lo,
			@Parameter("Id") int id ) throws JSONException {
		Lookup lookup = null;
		if (id>0)
			lookup=lo.getLookupById(id);
		serializer.put("lookup", Serializer.serializeLookup(lookup));
		return serializer;
	}

	@JSONExported
	@Admin
	public void disableLookup(
			@Parameter("Id") int id,
			LookupOperation lo) throws JSONException {
		if (id>0)
			lo.disableLookup(id);
	}
	
	@JSONExported
	@Admin
	public void enableLookup(
			@Parameter("Id") int id,
			LookupOperation lo) throws JSONException {
		if (id>0)
			lo.enableLookup(id);
	}
	
	@JSONExported
	@Admin
	public JSONObject saveLookup(
			JSONObject serializer,
			LookupOperation lo,
			@Parameter("Type") String type,
			@Parameter("Code") String code,
			@Parameter("Description") String description,
			@Parameter("Id") int id,
			@Parameter("ParentId") int parentId,
			@Parameter("Notes") String notes,
			@Parameter("Default") boolean isDefault,
			@Parameter("Active") boolean isActive,
			@Parameter("Number") int number ) throws JSONException {
		Lookup lookup;
		if (id==0) {
			lookup = lo.createLookup(type, code, description, notes, parentId, number, isDefault, isActive);
		} else {
			lookup = lo.updateLookup(id, type, code, description, notes, parentId, number, isDefault, isActive);
		}
		serializer.put("lookup", Serializer.serializeLookup(lookup));
		return serializer;
	}

	@JSONExported
	@Admin
	public void reorderLookup(
			@Parameter("type") String lookupType,
			LookupOperation lo,
			@Parameter("lookuplist") JSONArray decoder) throws JSONException, AuthException {
		Map<Integer, Integer> lookupPositions = new HashMap<Integer, Integer>();
		for(int i = 0; i < decoder.length(); i++) {
			JSONObject jattr = decoder.getJSONObject(i);
			int lookupId = jattr.getInt("id");
			int lookupIndex = jattr.getInt("index");
			lookupPositions.put(lookupId, lookupIndex);
		}
		lo.reorderLookup(lookupType, lookupPositions);
	}
}
