package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_PROJECTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_IFC;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.servlets.json.serializers.BIMProjectSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BIM extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONObject read( //
			final @Parameter(value = START) int start, //
			final @Parameter(value = LIMIT) int limit //
	) throws JSONException, CMDBException {
		final List<BimProjectInfo> projects = bimLogic().read();
		final JSONArray jsonProjects = BIMProjectSerializer.toClient(projects);
		final JSONObject response = new JSONObject();

		response.put(BIM_PROJECTS, jsonProjects);

		return response;
	}

	@JSONExported
	public JSONObject create( //
			final @Parameter(NAME) String name, //
			final @Parameter(DESCRIPTION) String description, //
			final @Parameter(ACTIVE) boolean active, //
			final @Parameter(value = FILE_IFC, required = false) FileItem fileIFC, //
			final Map<String, Object> attributes //
	) throws Exception {

		final JSONObject response = new JSONObject();
		final BimProjectInfo project = new BimProjectInfo();
		project.setActive(active);
		project.setName(name);
		project.setDescription(description);

		if (fileIFC != null)
		response.put("project", //
				BIMProjectSerializer.toClient( //
					bimLogic().create(project, fileFromFileItem(fileIFC))
				)
			);

		return response;
	}

	@JSONExported
	public void update( //
			final @Parameter(ID) String projectId, //
			final @Parameter(DESCRIPTION) String description, //
			final @Parameter(ACTIVE) boolean active, //
			final @Parameter(value = FILE_IFC, required = false) FileItem fileIFC //
		) throws Exception {

		final BimProjectInfo project = new BimProjectInfo();
		project.setProjectId(projectId);
		project.setDescription(description);
		project.setActive(active);

		bimLogic().update(project, fileFromFileItem(fileIFC));
	}

	@JSONExported
	public void enableProject( //
			final @Parameter(ID) String projectId //
		) {

		bimLogic().enableProject(projectId);
	}

	@JSONExported
	public void disableProject( //
			final @Parameter(ID) String projectId //
		) {

		bimLogic().disableProject(projectId);
	}

	private File fileFromFileItem(final FileItem fileIFC) throws Exception {
		File output = null;
		if (fileIFC != null && fileIFC.getSize() > 0) {
			output = File.createTempFile("theIFCToUpload", ".ifc", FileUtils.getTempDirectory());
			fileIFC.write(output);
		}

		return output;
	}
}
