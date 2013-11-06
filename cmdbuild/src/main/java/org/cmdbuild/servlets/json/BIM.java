package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_PROJECTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILE_IFC;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;
import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTE;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_LAYER;
import static org.cmdbuild.servlets.json.ComunicationConstants.VALUE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.model.bim.BimProjectInfo;
import org.cmdbuild.servlets.json.serializers.BimProjectSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class BIM extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONObject read( //
			final @Parameter(value = START) int start, //
			final @Parameter(value = LIMIT) int limit //
	) throws JSONException, CMDBException {
		final List<BimProjectInfo> projects = bimLogic().readBimProjectInfo();
		final JSONArray jsonProjects = BimProjectSerializer.toClient(projects);
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

		response.put("project", //
				BimProjectSerializer.toClient( //
						bimLogic().createBimProjectInfo(project, fileFromFileItem(fileIFC))));

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

		bimLogic().updateBimProjectInfo(project, fileFromFileItem(fileIFC));
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

	@JSONExported
	public JSONObject getPoidForCardId( //
			final @Parameter("cardId") Long cardId //
	) throws JSONException {
		final JSONObject out = new JSONObject();
		out.put("POID", bimLogic().getPoidForCardId(cardId));
		return out;
	}

	@JSONExported
	public JSONObject readBimLayer() throws JSONException {
		final List<BimLayer> layerList = bimLogic().readBimLayer();
		final JSONArray jsonLayerList = BimLayerSerializer.toClient(layerList);
		final JSONObject response = new JSONObject();

		response.put(BIM_LAYER, jsonLayerList);

		return response;
	}

	@JSONExported
	public void saveBimLayer( //
			final @Parameter(CLASS_NAME) String className, //
			final @Parameter(ATTRIBUTE) String attribute, //
			final @Parameter(VALUE) String value) throws Exception {
		bimLogic().updateBimLayer(className, attribute, value);
	}

	private File fileFromFileItem(final FileItem fileIFC) throws Exception {
		File output = null;
		if (fileIFC != null && fileIFC.getSize() > 0) {
			output = File.createTempFile("theIFCToUpload", ".ifc", FileUtils.getTempDirectory());
			fileIFC.write(output);
		}

		return output;
	}

	@JSONExported
	public void bindProjectToCards( //
			final @Parameter("idProjectCard") String projectCardId, //
			final @Parameter("cardsToBind") JSONArray jsonCardsId) throws Exception {

		ArrayList<String> cardIdList = Lists.newArrayList();
		for (int i = 0; i <= jsonCardsId.length(); i++) {
			JSONObject cardId = jsonCardsId.getJSONObject(i);
			cardIdList.add(cardId.toString());
		}
		bimLogic().bindProjectToCards(projectCardId, cardIdList);
	}

	@JSONExported
	public void importIfc( //
			final @Parameter("projectId") String projectId //
	) {
		bimLogic().importIfc(projectId);
	}

	@JSONExported
	public void exportIfc( //
			final @Parameter("projectId") String projectId //
	) {
		bimLogic().exportIfc(projectId);
	}

	@JSONExported
	public void download( //
			final @Parameter("projectId") String projectId //
	) {
		bimLogic().download(projectId);
	}

}
