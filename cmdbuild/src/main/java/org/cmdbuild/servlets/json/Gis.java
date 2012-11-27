package org.cmdbuild.servlets.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.ITableFactory;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.CustomFilesStore;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.gis.CompositeLayerService;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureType;
import org.cmdbuild.services.gis.GeoFeatureType.GeoType;
import org.cmdbuild.services.gis.GeoLayer;
import org.cmdbuild.services.gis.GeoTable;
import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.servlets.json.serializers.GeoJSONSerializer;
import org.cmdbuild.servlets.json.serializers.Serializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Gis extends JSONBase {

	private static final String ps = File.separator;
	private static final String UPLOADED_FILE_RELATIVE_PATH = "images" + ps + "gis" + ps;
	private static final CustomFilesStore iconsFileStore = new CustomFilesStore();

	private static GeoServerService geoServerService = new GeoServerService();
	private static CompositeLayerService layerService = new CompositeLayerService();

	@JSONExported
	public JSONObject getIconsList(final JSONObject serializer) throws JSONException, AuthException {
		final String[] iconsFileList = iconsFileStore.list("images" + ps + "gis");
		final JSONArray rows = new JSONArray();
		for (final String iconFileName : iconsFileList) {
			final JSONObject jsonIcon = new JSONObject();
			jsonIcon.put("name", iconFileName);
			jsonIcon.put("description", iconsFileStore.removeExtension(iconFileName));
			final String path = iconsFileStore.getRelativeRootDirectory() + UPLOADED_FILE_RELATIVE_PATH + iconFileName;
			jsonIcon.put("path", path.replace(File.separator, "/"));
			rows.put(jsonIcon);
		}
		serializer.put("rows", rows);
		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject createIconCard(final JSONObject serializer, final ITableFactory tf,
			@Parameter(value = "file", required = true) final FileItem file,
			@Parameter(value = "description", required = true) final String fileName) throws ORMException,
			FileNotFoundException, IOException {
		final String relativePath = UPLOADED_FILE_RELATIVE_PATH + fileName
				+ iconsFileStore.getExtension(file.getName());

		if (iconsFileStore.isImage(file)) {
			iconsFileStore.save(file, relativePath);
		} else {
			throw ORMExceptionType.ORM_ICONS_UNSUPPORTED_TYPE.createException();
		}
		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject updateIconCard(final JSONObject serializer,
			@Parameter(value = "file", required = false) final FileItem file,
			@Parameter(value = "name", required = true) final String fileName) throws JSONException, AuthException,
			ORMException, IOException {

		if (!"".equals(file.getName())) {
			if (iconsFileStore.isImage(file)) {
				iconsFileStore.remove(UPLOADED_FILE_RELATIVE_PATH + fileName);
				iconsFileStore.save(file, UPLOADED_FILE_RELATIVE_PATH + fileName);
			} else {
				throw ORMExceptionType.ORM_ICONS_UNSUPPORTED_TYPE.createException();
			}
		}

		return serializer;
	}

	@JSONExported
	@Admin
	public JSONObject deleteIconCard(final JSONObject serializer, @Parameter("name") final String fileName)
			throws JSONException {
		iconsFileStore.remove(UPLOADED_FILE_RELATIVE_PATH + fileName);
		return serializer;
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject addGeoAttribute(final ITable table, @Parameter("name") final String name,
			@Parameter("description") final String description, @Parameter("type") final String type,
			@Parameter("minZoom") final int minZoom, @Parameter("maxZoom") final int maxZoom,
			@Parameter("style") final JSONObject jsonStyle, final JSONObject serializer) throws JSONException {
		final GeoFeatureType gft = layerService.createGeoFeatureType(table, name, description, GeoType.valueOf(type),
				minZoom, maxZoom, jsonStyle.toString());
		serializer.put("geoAttribute", Serializer.serializeGeoLayer(gft, table));
		return serializer;
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject modifyGeoAttribute(final ITable table, final JSONObject serializer,
			@Parameter("name") final String name, @Parameter("description") final String description,
			@Parameter("minZoom") final int minZoom, @Parameter("maxZoom") final int maxZoom,
			@Parameter("style") final JSONObject jsonStyle) throws JSONException {
		final GeoTable geoMasterClass = new GeoTable(table);
		final GeoFeatureType geoFeatureType = geoMasterClass.getGeoFeatureType(name);
		geoFeatureType.setDescription(description);
		geoFeatureType.setMinZoom(minZoom);
		geoFeatureType.setMaxZoom(maxZoom);
		geoFeatureType.setStyle(jsonStyle.toString());
		geoFeatureType.save();
		serializer.put("geoAttribute", Serializer.serializeGeoLayer(geoFeatureType));
		return serializer;
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoAttribute(final ITable table, final JSONObject serializer, @Parameter("name") final String name)
			throws JSONException {
		final GeoTable geoMasterClass = new GeoTable(table);
		geoMasterClass.getGeoFeatureType(name).delete();
	}

	@JSONExported
	@SkipExtSuccess
	public JSONObject getGeoCardList(final ITable masterClass,
			@Parameter(value = "bbox", required = true) final String bbox,
			@Parameter(value = "attribute", required = true) final String featureTypeName) throws JSONException {
		final GeoTable geoMasterClass = new GeoTable(masterClass);
		final JSONArray features = new JSONArray();
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		final GeoFeatureType ft = geoMasterClass.getGeoFeatureType(featureTypeName);
		for (final GeoFeature geoFeature : ft.query().bbox(bbox).onlyFrom(masterClass)) {
			features.put(geoSerializer.serialize(geoFeature));
		}
		return geoSerializer.getNewFeatureCollection(features);
	}

	/*
	 * GEO SERVER
	 */

	@JSONExported
	@Admin
	public void addGeoServerLayer(@Parameter("name") final String name,
			@Parameter("description") final String description, @Parameter("type") final String type,
			@Parameter(value = "file", required = true) final FileItem file, @Parameter("minZoom") final int minZoom,
			@Parameter("maxZoom") final int maxZoom) throws IOException {
		layerService.createGeoServerLayer(name, type, file.getInputStream(), minZoom, maxZoom, description);
	}

	@Transacted
	@JSONExported
	@Admin
	public void modifyGeoServerLayer(@Parameter("name") final String name,
			@Parameter("description") final String description,
			@Parameter(required = false, value = "file") final FileItem file, @Parameter("minZoom") final int minZoom,
			@Parameter("maxZoom") final int maxZoom) throws IOException {
		if (file != null && file.getSize() > 0) {
			geoServerService.modifyStoreData(name, file.getInputStream());
		}
		geoServerService.modifyStoreZoomAndDescription(name, minZoom, maxZoom, description);
	}

	@Transacted
	@JSONExported
	@Admin
	public void deleteGeoServerLayer(@Parameter("name") final String name) {
		geoServerService.deleteStore(name);
	}

	@Transacted
	@JSONExported
	@Admin
	public JSONObject getGeoserverLayers(final JSONObject serializer) throws JSONException {
		final List<GeoServerLayer> layers = geoServerService.getLayers();
		serializer.put("layers", Serializer.serializeGeoLayers(layers));
		return serializer;
	}

	/**
	 * It is used to center the map to a specific card
	 * 
	 * @return the feature for the first geometry attribute
	 */
	@JSONExported
	public JSONObject getFeature(final ICard card, final ITableFactory tf) throws JSONException {
		JSONObject feature;
		final GeoJSONSerializer geoSerializer = new GeoJSONSerializer();
		try {
			final GeoFeature geoFeature = getMainGeoFeatureType(card).query().master(card).get();
			feature = geoSerializer.serialize(geoFeature);
		} catch (final NotFoundException e) {
			feature = new JSONObject();
		}
		return feature;
	}

	private GeoFeatureType getMainGeoFeatureType(final ICard card) {
		final GeoTable geoMasterClass = new GeoTable(card.getSchema());
		final Iterator<GeoFeatureType> i = geoMasterClass.getGeoFeatureTypes().iterator();
		if (!i.hasNext()) {
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
		return i.next();
	}

	@Admin
	@JSONExported
	public JSONObject getAllLayers(final JSONObject serializer) throws JSONException {
		final List<? extends GeoLayer> allLayers = layerService.getLayers();
		serializer.put("layers", Serializer.serializeGeoLayers(allLayers));
		return serializer;
	}

	@Admin
	@JSONExported
	public void setLayerVisibility(final UserContext userCtx, final ITable table,
			@Parameter("master") final int masterClassId,
			@Parameter(value = "featureTypeName", required = true) final String name,
			@Parameter("visible") final boolean visible) {
		ITable masterTable = null;
		if (masterClassId > 0) {
			masterTable = UserOperations.from(userCtx).tables().get(masterClassId);
		}
		layerService.setLayerVisibility(name, masterTable, table, visible);
	}

	@Transacted
	@Admin
	@JSONExported
	public void setLayersOrder(@Parameter(value = "oldIndex", required = true) final int oldIndex,
			@Parameter(value = "newIndex", required = true) final int newIndex) {
		layerService.reorderLayers(oldIndex, newIndex);
	}
}