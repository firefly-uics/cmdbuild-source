package org.cmdbuild.servlets.json.schema;

import java.io.File;
import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.ServletException;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.TableTree;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ProcessType;
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
import org.cmdbuild.workflow.CmdbuildProcessInfo;
import org.cmdbuild.workflow.operation.SharkFacade;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("restriction")
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
	
	@JSONExported
	public JSONObject xpdlInfo(
			ProcessType processType,
			JSONObject out) throws Exception {
		JSONObject row = new JSONObject();
		row.put("idClass", processType.getId());
		row.put("name", processType.getName());

		CmdbuildProcessInfo procInfo = processType.getProcInfo();
		row.put("userstoppable", procInfo != null ? procInfo.isUserStoppable() : false);

		Integer[] versions = processType.getPackageVersions();
		row.put("configured", versions.length != 0);
		row.put("versions", versions);

		out.put("data", row);
		return out;
	}
	
	@Admin
	@JSONExported
	public JSONObject uploadXPDL(
			@Parameter(value="xpdlfile", required=false) FileItem xpdlFile,
			@Parameter(value="imgfile",required=false) FileItem image,
			ProcessType processType,
			JSONObject serializer,
			@Parameter("userstoppable") boolean userStoppable) throws IOException, JSONException {
		JSONArray messages = new JSONArray();	
		if (!"".equals(xpdlFile.getName())) {
			processType.getXPDLManager().upload(xpdlFile.getInputStream(), userStoppable);
			messages.put("saved_xpdl");
		}
		
		if (!"".equals(image.getName())) {
			saveImage(image, processType);
			messages.put("saved_image");
		} else {
			removeImage(processType);
			messages.put("deleted_image");
		}
		serializer.put("messages", messages);
		return serializer;
	}
	
	private void saveImage(FileItem image, ProcessType processType) throws IOException {
		String filterPattern = processType.getName()+".*";
		String[] processImages = customFileStore.list(UPLOADED_FILE_RELATIVE_PATH, filterPattern);
		String relativeUploadPath = UPLOADED_FILE_RELATIVE_PATH+processType.getName()+customFileStore.getExtension(image.getName());
		if (processImages.length > 0) {
			customFileStore.remove(UPLOADED_FILE_RELATIVE_PATH+processImages[0]);
		}
		customFileStore.save(image, relativeUploadPath);
	}
	
	private void removeImage(ProcessType processType) {
		String filterPattern = processType.getName()+".*";
		String[] processImages = customFileStore.list(UPLOADED_FILE_RELATIVE_PATH, filterPattern);
		if (processImages.length > 0) {
			customFileStore.remove(UPLOADED_FILE_RELATIVE_PATH+processImages[0]);
		}
	}

	@Admin
	@JSONExported
	public DataHandler downloadXPDL(
			ProcessType processType,
			@Parameter("version") int version ) throws CMDBWorkflowException {
		byte[] contents = processType.getXPDLManager().download(version);
		ByteArrayDataSource ds = new ByteArrayDataSource(contents,"application/xpdl");
		ds.setName(processType.getName() + "_" + version + ".xpdl");
		return new DataHandler(ds);
	}

	@Admin
	@JSONExported
	public JSONObject removeAllInconsistentProcesses(
			JSONObject serializer,
			SharkFacade sharkFacade) throws JSONException, CMDBException {
		sharkFacade.removeAllInconsistentProcesses();
		return serializer;
	}

	@Admin
	@JSONExported
	public JSONObject removeInconsistentProcesses(
			JSONObject serializer,
			ITable classTable,
			SharkFacade sharkFacade) throws JSONException, CMDBException {
		sharkFacade.removeInconsistentProcesses(classTable);
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
