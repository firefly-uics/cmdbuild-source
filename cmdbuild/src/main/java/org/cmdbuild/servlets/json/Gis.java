package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.GISLogic.ClassMapping;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.servlets.json.serializers.DomainTreeNodeJSONMapper;
import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Gis extends JSONBase {

	@OldDao
	@Transacted
	@JSONExported
	@Admin
	public void addGeoAttribute(final ITable table, @Parameter("name") final String name,
			@Parameter("description") final String description, @Parameter("type") final String type,
			@Parameter("minZoom") final int minimumZoom, @Parameter("maxZoom") final int maximumzoom,
			@Parameter("style") final JSONObject mapStyle) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final LayerMetadata layerMetaData = new LayerMetadata(name, description, type, minimumZoom, maximumzoom, 0,
				mapStyle.toString(), null);
		layerMetaData.addVisibility(table.getName());
		logic.createGeoAttribute(table, layerMetaData);
	}

	@OldDao
	@Transacted
	@JSONExported
	@Admin
	public void modifyGeoAttribute(final ITable table, @Parameter("name") final String name,
			@Parameter("description") final String description, @Parameter("minZoom") final int minimumZoom,
			@Parameter("maxZoom") final int maximumzoom, @Parameter("style") final JSONObject jsonStyle)
			throws JSONException, Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.modifyGeoAttribute(table, name, description, minimumZoom, maximumzoom, jsonStyle.toString());
	}

	
	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoAttribute(@Parameter("masterTableName") final String masterTableName,
			@Parameter("name") final String name) throws JSONException, Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.deleteGeoAttribute(masterTableName, name);
	}

	@OldDao
	@JSONExported
	@SkipExtSuccess
	public JSONObject getGeoCardList(final ITable masterClass,
			@Parameter(value = "bbox", required = true) final String bbox,
			@Parameter(value = "attribute", required = true) final String layerName) throws JSONException, Exception {
		final JSONArray features = new JSONArray();
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();

		for (final GeoFeature geoFeature : logic.getFeatures(masterClass, layerName, bbox)) {
			features.put(geoSerializer.serialize(geoFeature));
		}

		return geoSerializer.getNewFeatureCollection(features);
	}

	/**
	 * It is used to center the map to a specific card
	 * 
	 * @return the feature for the first geometry attribute
	 */
	@OldDao
	@JSONExported
	public JSONObject getFeature(final ICard card, final ITableFactory tf) throws JSONException, Exception {

		JSONObject jsonFeature = new JSONObject();
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final GeoFeature feature = logic.getFeature(card);
		if (feature != null) {
			jsonFeature = geoSerializer.serialize(feature);
		}

		return jsonFeature;
	}

	@Admin
	@JSONExported
	public JSONObject getAllLayers(final JSONObject serializer) throws JSONException, Exception {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final List<LayerMetadata> layers = logic.list();
		serializer.put("layers", GeoJSONSerializer.serializeGeoLayers(layers));
		return serializer;
	}

	@Admin
	@JSONExported
	public void setLayerVisibility(@Parameter("layerFullName") final String layerFullName,
			@Parameter("tableName") final String visibleTableName, @Parameter("visible") final boolean visible)
			throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.setLayerVisisbility(layerFullName, visibleTableName, visible);
	}

	@Transacted
	@Admin
	@JSONExported
	public void setLayersOrder(@Parameter(value = "oldIndex", required = true) final int oldIndex,
			@Parameter(value = "newIndex", required = true) final int newIndex) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.reorderLayers(oldIndex, newIndex);
	}

	/* DomainTreeNavigation */

	@Admin
	@JSONExported
	public void saveGISTreeNavigation(@Parameter("structure") final String jsonConfiguraiton) throws JSONException {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final JSONObject structure = new JSONObject(jsonConfiguraiton);
		final DomainTreeNode root = DomainTreeNodeJSONMapper.deserialize(structure);

		logic.saveGisTreeNavigation(root);
	}

	@Admin
	@JSONExported
	public void removeGISTreeNavigation() {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.removeGisTreeNavigation();
	}

	@JSONExported
	public JSONObject getGISTreeNavigation() throws Exception {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		DomainTreeNode root = null;
		Map<String, ClassMapping> geoServerLayerMapping = null;
		if (logic.isGisEnabled()) {
			root = logic.getGisTreeNavigation();
			geoServerLayerMapping = logic.getGeoServerLayerMapping();
		}

		final JSONObject response = new JSONObject();
		if (root != null) {
			response.put("root", DomainTreeNodeJSONMapper.serialize(root, true));
		}
		if (geoServerLayerMapping != null) {
			response.put("geoServerLayersMapping", GeoJSONSerializer.serialize(geoServerLayerMapping));
		}

		return response;
	}

	@JSONExported
	public JSONObject expandDomainTree(final UserContext userCtx) throws JSONException {
		final JSONObject response = new JSONObject();
		final DataAccessLogic dataAccesslogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic(userCtx);
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();

		response.put("root", new JSONObject(logic.expandDomainTree(dataAccesslogic)));
		return response;
	}

	/*
	 * GEO SERVER
	 */

	@JSONExported
	@Admin
	public void addGeoServerLayer(@Parameter("name") final String name,
			@Parameter("description") final String description,
			@Parameter("cardBinding") final JSONArray cardBindingString, @Parameter("type") final String type,
			@Parameter("minZoom") final int minimumZoom, @Parameter("maxZoom") final int maximumzoom,
			@Parameter(value = "file", required = true) final FileItem file) throws IOException, Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final LayerMetadata layerMetaData = new LayerMetadata(name, description, type, minimumZoom, maximumzoom, 0,
				null, null);
		layerMetaData.setCardBinding(fromJsonToSet(cardBindingString));
		logic.createGeoServerLayer(layerMetaData, file);
	}

	@Transacted
	@JSONExported
	@Admin
	public void modifyGeoServerLayer(@Parameter("name") final String name,
			@Parameter("description") final String description,
			@Parameter("cardBinding") final JSONArray cardBindingString, @Parameter("minZoom") final int minimumZoom,
			@Parameter("maxZoom") final int maximumZoom,
			@Parameter(required = false, value = "file") final FileItem file) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.modifyGeoServerLayer(name, description, maximumZoom, minimumZoom, file, fromJsonToSet(cardBindingString));
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoServerLayer(@Parameter("name") final String name) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.deleteGeoServerLayer(name);
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject getGeoserverLayers(final JSONObject serializer) throws Exception {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		serializer.put("layers", GeoJSONSerializer.serializeGeoLayers(logic.getGeoServerLayers()));
		return serializer;
	}

	private Set<String> fromJsonToSet(final JSONArray json) throws JSONException {
		final HashSet<String> out = new HashSet<String>();
		for (int i = 0, l = json.length(); i < l; ++i) {
			final JSONObject jsonCardBinding = (JSONObject) json.get(i);
			out.add(jsonCardBinding.getString("className") + "_" + jsonCardBinding.getString("idCard"));
		}
		return out;
	}
}