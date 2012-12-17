package org.cmdbuild.servlets.json.serializers;

import java.util.List;

import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DomainTreeNodeJSONMapper {

	public static final String 
		BASE_NODE = "baseNode",
		CHILD_NODES = "childNodes",
		DIRECT = "direct",
		DOMAIN_NAME = "domainName",
		ID = "id",
		ID_PARENT = "idParent",
		ID_GROUP = "idGroup",
		TARGET_CLASS_NAME = "targetClassName",
		TARGET_CLASS_DESCRIPTION = "targetClassDescription",
		TYPE = "type";

	public static DomainTreeNode deserialize(JSONObject jsonTreeNode) throws JSONException {
		DomainTreeNode treeNode = new DomainTreeNode();

		treeNode.setBaseNode(readBooleanOrFalse(jsonTreeNode, BASE_NODE));
		treeNode.setDirect(readBooleanOrFalse(jsonTreeNode, DIRECT));
		treeNode.setTargetClassName(readStringOrNull(jsonTreeNode, TARGET_CLASS_NAME));
		treeNode.setTargetClassDescription(readStringOrNull(jsonTreeNode, TARGET_CLASS_DESCRIPTION));
		treeNode.setDomainName(readStringOrNull(jsonTreeNode,DOMAIN_NAME));
		treeNode.setType(readStringOrNull(jsonTreeNode,TYPE));
		treeNode.setId(readLongOrNull(jsonTreeNode,ID));
		treeNode.setIdParent(readLongOrNull(jsonTreeNode,ID_PARENT));
		treeNode.setIdGroup(readLongOrNull(jsonTreeNode,ID_GROUP));

		JSONArray jsonChildNodes = new JSONArray();
		if (jsonTreeNode.has(CHILD_NODES)) {
			jsonChildNodes = (JSONArray) jsonTreeNode.get(CHILD_NODES);
		}

		for (int i=0, l=jsonChildNodes.length(); i<l; ++i) {
			JSONObject jsonChild = (JSONObject) jsonChildNodes.get(i);
			treeNode.addChildNode(deserialize(jsonChild));
		}

		return treeNode;
	}

	public static JSONObject serialize(DomainTreeNode treeNode, Boolean deeply) throws JSONException {
		JSONObject jsonTreeNode = new JSONObject();
		if (treeNode == null) {
			return jsonTreeNode;
		}

		jsonTreeNode.put(BASE_NODE, treeNode.isBaseNode());
		jsonTreeNode.put(DIRECT, treeNode.isDirect());
		jsonTreeNode.put(TARGET_CLASS_NAME, treeNode.getTargetClassName());
		jsonTreeNode.put(TARGET_CLASS_DESCRIPTION, treeNode.getTargetClassDescription());
		jsonTreeNode.put(DOMAIN_NAME, treeNode.getDomainName());
		jsonTreeNode.put(TYPE, treeNode.getType());
		jsonTreeNode.put(ID, treeNode.getId());
		jsonTreeNode.put(ID_PARENT, treeNode.getIdParent());
		jsonTreeNode.put(ID_GROUP, treeNode.getIdGroup());

		if (deeply) {
			jsonTreeNode.put(CHILD_NODES, serialize(treeNode.getChildNodes(), deeply));
		}

		return jsonTreeNode;
	}

	public static JSONArray serialize(List<DomainTreeNode> nodes, Boolean deeply) throws JSONException {
		JSONArray jsonChildNodes = new JSONArray();
		for (DomainTreeNode child:nodes) {
			jsonChildNodes.put(serialize(child, deeply));
		}

		return jsonChildNodes;
	}

	private static Boolean readBooleanOrFalse(JSONObject src, String key) throws JSONException {
		if (src.has(key)) {
			return src.getBoolean(key);
		} else {
			return false;
		}
	}

	private static String readStringOrNull(JSONObject src, String key) throws JSONException {
		if (src.has(key)) {
			return src.getString(key);
		} else {
			return null;
		}
	}

	private static Long readLongOrNull(JSONObject src, String key) throws JSONException {
		if (src.has(key)) {
			return src.getLong(key);
		} else {
			return null;
		}
	}
}
