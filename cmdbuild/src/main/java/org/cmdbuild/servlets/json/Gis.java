package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.model.DomainTreeNode;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.gis.CompositeLayerService;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureType;
import org.cmdbuild.services.gis.GeoLayer;
import org.cmdbuild.services.gis.GeoTable;
import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.servlets.json.serializers.DomainTreeNodeJSONMapper;
import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Gis extends JSONBase {

	private static final GeoServerService geoServerService = new GeoServerService();
	private static final CompositeLayerService layerService = new  CompositeLayerService();

	@Transacted
	@JSONExported
	@Admin
	public JSONObject addGeoAttribute(
			ITable table,
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("type") String type,
			@Parameter("minZoom") int minZoom,
			@Parameter("maxZoom") int maxZoom,
			@Parameter("style") JSONObject jsonStyle,
			JSONObject serializer) throws JSONException {
		
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		GeoFeatureType gft = logic.addGeoAttribute(table, name, description, type,
				minZoom, maxZoom, jsonStyle.toString());
		serializer.put("geoAttribute", Serializer.serializeGeoLayer(gft, table));
		return serializer;
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject modifyGeoAttribute(
			ITable table,
			JSONObject serializer,
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter("minZoom") int minZoom,
			@Parameter("maxZoom") int maxZoom,
			@Parameter("style") JSONObject jsonStyle) throws JSONException {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		GeoFeatureType gft = logic.modifyGeoAttribute(table, name, description, minZoom, maxZoom, jsonStyle.toString());
		serializer.put("geoAttribute", Serializer.serializeGeoLayer(gft));
		return serializer;
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoAttribute(
			ITable table,
			JSONObject serializer,
			@Parameter("name") String name) throws JSONException {

		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		logic.deleteGeoAttribute(table, name);
	}

	@JSONExported
	@SkipExtSuccess
	public JSONObject getGeoCardList(
			ITable masterClass,
			@Parameter(value="bbox", required=true) String bbox,
			@Parameter(value="attribute", required=true) String featureTypeName) throws JSONException {
		GeoTable geoMasterClass = new GeoTable(masterClass);
		JSONArray features = new JSONArray();
		GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GeoFeatureType ft = geoMasterClass.getGeoFeatureType(featureTypeName);
		for (GeoFeature geoFeature : ft.query().bbox(bbox).onlyFrom(masterClass)) {
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
    		ITableFactory tf) throws JSONException {
    	JSONObject feature;
		GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		try {
			GeoFeature geoFeature = getMainGeoFeatureType(card).query().master(card).get();
			feature = geoSerializer.serialize(geoFeature);
		} catch (NotFoundException e) {
			feature = new JSONObject();
		}
    	return feature; 
    }

	private GeoFeatureType getMainGeoFeatureType(ICard card) {
		GeoTable geoMasterClass = new GeoTable(card.getSchema());
		Iterator<GeoFeatureType> i = geoMasterClass.getGeoFeatureTypes().iterator();
		if (!i.hasNext()) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return i.next();
	}

	@Admin
	@JSONExported
	public JSONObject getAllLayers(
			JSONObject serializer) throws JSONException {
		List<? extends GeoLayer> allLayers = layerService.getLayers();
		serializer.put("layers", Serializer.serializeGeoLayers(allLayers));
		return serializer;
	}

	@Admin
	@JSONExported
	public void setLayerVisibility(
			UserContext userCtx,
			ITable table,
			@Parameter("master") int masterClassId,
			@Parameter(value="featureTypeName", required=true) String name,
			@Parameter("visible") boolean visible) {
		ITable masterTable = null;
		if (masterClassId > 0) {
			masterTable = userCtx.tables().get(masterClassId);
		}
		layerService.setLayerVisibility(name, masterTable, table, visible);
	}

	@Transacted
	@Admin
	@JSONExported
	public void setLayersOrder(
			@Parameter(value="oldIndex", required=true) int oldIndex,
			@Parameter(value="newIndex", required=true) int newIndex) {
		layerService.reorderLayers(oldIndex, newIndex);
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
	public JSONObject getGISTreeNavigation() throws JSONException {
		final GISLogic logic = TemporaryObjectsBeforeSpringDI.getGISLogic();
		DomainTreeNode root = logic.getGisTreeNavigation();
		JSONObject response = new JSONObject();
		if (root != null) {
			response.put("root", DomainTreeNodeJSONMapper.serialize(root));
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
			@Parameter("type") String type,
			@Parameter(value="file", required=true) FileItem file,
			@Parameter("minZoom") int minZoom,
			@Parameter("maxZoom") int maxZoom) throws IOException {
		layerService.createGeoServerLayer(name, type, file.getInputStream(), minZoom, maxZoom, description);
	}

	@Transacted
	@JSONExported
	@Admin
	public void modifyGeoServerLayer(
			@Parameter("name") String name,
			@Parameter("description") String description,
			@Parameter(required=false, value="file") FileItem file,
			@Parameter("minZoom") int minZoom,
			@Parameter("maxZoom") int maxZoom) throws IOException {
		if (file != null && file.getSize() > 0) {
			geoServerService.modifyStoreData(name, file.getInputStream());
		}
		geoServerService.modifyStoreZoomAndDescription(name, minZoom, maxZoom, description);
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoServerLayer(
			@Parameter("name") String name) {
		geoServerService.deleteStore(name);
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject getGeoserverLayers(JSONObject serializer) throws JSONException {
		List<GeoServerLayer> layers = geoServerService.getLayers();
		serializer.put("layers", Serializer.serializeGeoLayers(layers));
		return serializer;
	}
}