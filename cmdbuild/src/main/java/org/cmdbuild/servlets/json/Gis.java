package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.servlets.json.serializers.DomainTreeNodeJSONMapper;
import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Gis extends JSONBase {

	@Transacted
	@JSONExported
	@Admin
	public void addGeoAttribute(
			ITable table,
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("type") String type,
			@Parameter("minZoom") int minimumZoom,
			@Parameter("maxZoom") int maximumzoom,
			@Parameter("style") JSONObject mapStyle) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final LayerMetadata layerMetaData= new LayerMetadata(name, description, type, minimumZoom,
				maximumzoom, 0, mapStyle.toString(), null);
		layerMetaData.addVisibility(table.getName());
		logic.createGeoAttribute(table, layerMetaData);
	}

	@Transacted
	@JSONExported
	@Admin
	public void modifyGeoAttribute(
			ITable table,
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("minZoom") int minimumZoom,
			@Parameter("maxZoom") int maximumzoom,
			@Parameter("style") JSONObject jsonStyle) throws JSONException, Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.modifyGeoAttribute(table, name, description, minimumZoom, maximumzoom, jsonStyle.toString());
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoAttribute(
			@Parameter("masterTableName") String masterTableName,
			@Parameter("name") String name) throws JSONException, Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.deleteGeoAttribute(masterTableName, name);
	}

	@JSONExported
	@SkipExtSuccess
	public JSONObject getGeoCardList(
			ITable masterClass,
			@Parameter(value="bbox", required=true) String bbox,
			@Parameter(value="attribute", required=true) String layerName) throws JSONException, Exception {
		JSONArray features = new JSONArray();
		GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();

		for (GeoFeature geoFeature: logic.getFeatures(masterClass, layerName, bbox)) {
			features.put(geoSerializer.serialize(geoFeature));
		}

		return geoSerializer.getNewFeatureCollection(features);
	}

	/**
	 * It is used to center the map to a specific card
	 * 
	 * @return the feature for the first geometry attribute
	 */
	@JSONExported 
	public JSONObject getFeature(
			ICard card,
			ITableFactory tf) throws JSONException, Exception {

		JSONObject jsonFeature = new JSONObject();
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		GeoFeature feature = logic.getFeature(card);
		if (feature != null) {
			jsonFeature = geoSerializer.serialize(feature);
		}

		return jsonFeature;
	}

	@Admin
	@JSONExported
	public JSONObject getAllLayers(
			JSONObject serializer) throws JSONException, Exception {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		List<LayerMetadata> layers = logic.list();
		serializer.put("layers", GeoJSONSerializer.serializeGeoLayers(layers));
		return serializer;
	}

	@Admin
	@JSONExported
	public void setLayerVisibility(
			@Parameter("layerFullName") String layerFullName,
			@Parameter("tableName") String visibleTableName,
			@Parameter("visible") boolean visible) throws Exception  {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.setLayerVisisbility(layerFullName, visibleTableName, visible);
	}

	@Transacted
	@Admin
	@JSONExported
	public void setLayersOrder(
			@Parameter(value="oldIndex", required=true) int oldIndex,
			@Parameter(value="newIndex", required=true) int newIndex) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.reorderLayers(oldIndex, newIndex);
	}

	/* DomainTreeNavigation*/

	@Admin
	@JSONExported
	public void saveGISTreeNavigation(
		@Parameter("structure") String jsonConfiguraiton) throws JSONException {

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
		DomainTreeNode root = logic.getGisTreeNavigation();
		JSONObject response = new JSONObject();
		if (root != null) {
			response.put("root", DomainTreeNodeJSONMapper.serialize(root));
			response.put("geoServerLayersMapping",
				GeoJSONSerializer.serialize(logic.getGeoServerLayerMapping()));
		}

		return response;
	}

	/*
	 * GEO SERVER 
	 */

	@JSONExported
	@Admin
	public void addGeoServerLayer(
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("cardBinding") JSONArray cardBindingString,
			@Parameter("type") String type,
			@Parameter("minZoom") int minimumZoom,
			@Parameter("maxZoom") int maximumzoom,
			@Parameter(value="file", required=true) FileItem file) throws IOException, Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		final LayerMetadata layerMetaData = new LayerMetadata(name, description, type, minimumZoom, maximumzoom,
				0, null, null);
		layerMetaData.setCardBinding(fromJsonToSet(cardBindingString));
		logic.createGeoServerLayer(layerMetaData, file);
	}

	@Transacted
	@JSONExported
	@Admin
	public void modifyGeoServerLayer(
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("cardBinding") JSONArray cardBindingString,
			@Parameter("minZoom") int minimumZoom,
			@Parameter("maxZoom") int maximumZoom,
			@Parameter(required=false, value="file") FileItem file) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.modifyGeoServerLayer(name, description, maximumZoom, minimumZoom, file, fromJsonToSet(cardBindingString));
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoServerLayer(
			@Parameter("name") String name) throws Exception {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.deleteGeoServerLayer(name);
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject getGeoserverLayers(JSONObject serializer) throws Exception {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		serializer.put("layers", GeoJSONSerializer.serializeGeoLayers(logic.getGeoServerLayers()));
		return serializer;
	}

	private Set<String> fromJsonToSet(JSONArray json) throws JSONException {
		HashSet<String> out = new HashSet<String>();
		for (int i=0, l=json.length(); i<l; ++i) {
			JSONObject jsonCardBinding = (JSONObject) json.get(i);
			out.add(jsonCardBinding.getString("className") + "_" + jsonCardBinding.getString("idCard"));
		}
		return out;
	}
}