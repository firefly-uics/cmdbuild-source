package org.cmdbuild.servlets.json.schema;

import java.io.File;

import javax.servlet.ServletException;

import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.cmdbuild.utils.tree.CNode;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ModWorkflow extends JSONBase {
	private static final String ps = File.separator;
	private static final String UPLOADED_FILE_RELATIVE_PATH = "images"+ps+"workflow"+ps;
	private static final CustomFilesStore customFileStore = new CustomFilesStore();
	
	@JSONExported
	public JSONArray tree(
			@Parameter(value="active", required=false) boolean active,
			@Parameter(value="configured", required=false) boolean onlyConfigured,
			UserContext userCtx
		) throws ServletException {
		if(onlyConfigured && !WorkflowService.getInstance().isEnabled()) {
			return new JSONArray();
		}
		TableTree tree = userCtx.processTypes().tree();
		if (active) {
			tree.active();
		}
        try {
        	JSONObject serializer;
        	CNode<ITable> treeRoot = tree.getRootElement();
        	if (treeRoot != null) {
        		try {
        			serializer = Serializer.serializeTableTree(treeRoot, onlyConfigured);
        		} catch (CMDBWorkflowException e) {
        			serializer = new JSONObject();
        		}
        	} else {
        		serializer = new JSONObject();
        	}
        	if(serializer.has("children")) {
        		JSONArray out = new JSONArray();
        		out.put(serializer);
        		return out;
        	} else {
        		return new JSONArray();
        	}
		} catch (JSONException e) {
			Log.OTHER.error("error serializing table tree", e);
			throw new ServletException(e.getMessage());
		}
	}
	
	@JSONExported
	public JSONObject getSuperClasses(
			JSONObject serializer,
			UserContext userCtx ) throws JSONException, AuthException {
		for (ITable table : userCtx.processTypes().tree().superclasses()){
			JSONObject element = new JSONObject();
			element.put("name", table.getId());
			element.put("value", table.getDescription());
			serializer.append("superclasses", element);
		}
		return serializer;
	}
	
	@JSONExported
	public JSONObject getClasses(
			JSONObject serializer,
			UserContext userCtx ) throws JSONException, AuthException {
		for(ITable table: userCtx.processTypes().tree()){
			JSONObject element = new JSONObject();
			element.put("name", table.getId());
			element.put("value", table.getDescription());
			serializer.append("classes", element);
		}
		return serializer;
	}

	@Admin
	@JSONExported
	public JSONObject removeAllInconsistentProcesses(
			JSONObject serializer,
			SharkFacade sharkFacade) throws JSONException, CMDBException {
		sharkFacade.removeAllInconsistentProcesses();
		return serializer;
	}

	public static String getSketchURL(ITable table) throws JSONException {
		String filterPattern = table.getName() + ".*";
		String[] processImages = customFileStore.list(UPLOADED_FILE_RELATIVE_PATH, filterPattern);
		if (processImages.length > 0) {
			return customFileStore.getRelativeRootDirectory()
					+ UPLOADED_FILE_RELATIVE_PATH + processImages[0];
		}
		return null;
	}
}
