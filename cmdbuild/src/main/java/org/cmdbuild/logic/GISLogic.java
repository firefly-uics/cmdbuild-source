package org.cmdbuild.logic;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.config.GisProperties;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.model.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureLayer.GeoType;
import org.cmdbuild.services.gis.GeoFeatureQuery;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.services.store.DBDomainTreeStore;
import org.cmdbuild.services.store.DBLayerMetadataStore;
import org.cmdbuild.utils.OrderingUtils;
import org.cmdbuild.utils.OrderingUtils.PositionHandler;
import org.json.JSONObject;

public class GISLogic {

	private static final GeoServerService geoServerService = new GeoServerService();
	private static final DBLayerMetadataStore layerMetadataStore = new DBLayerMetadataStore();
	private static final DBDomainTreeStore domainTreeStore = new DBDomainTreeStore();

	private static final String DOMAIN_TREE_TYPE = "gisnavigation";
	private static final String MASTER_ATTRIBUTE = "Master";
	private static final String GEOMETRY_ATTRIBUTE = "Geometry";
	private static final String GEOSERVER = "_Geoserver";
	private static final String GEO_TABLESPACE = "gis";
	private static final String GEO_TABLE_NAME_FORMAT = GEO_TABLESPACE + ".Detail_%s_%s";

	/* Geo attributes */

	public LayerMetadata createGeoAttribute(ITable master, LayerMetadata layerMetaData) throws Exception {
		ensureGisIsEnabled();
		ITable geometryTable = createGeoAttributeTable(master, layerMetaData);
		layerMetaData.setFullName(geometryTable.getName());

		return layerMetadataStore.createLayer(layerMetaData);
	}

	public LayerMetadata modifyGeoAttribute(ITable targetTable, String name, String description,
			int minimumZoom, int maximumZoom, String style) throws Exception {
		ensureGisIsEnabled();

		return modifyLayerMetadata(targetTable.getName(), name, description, minimumZoom, maximumZoom, style);
	}

	public void deleteGeoAttribute(String masterTableName, String name) throws Exception {
		ensureGisIsEnabled();

		String fullName = fullName(masterTableName, name);
		ITable geoTable = UserContext.systemContext().tables().get(fullName);
		geoTable.delete();
		layerMetadataStore.deleteLayer(fullName);
	}

	public GeoFeature getFeature(ICard card) throws Exception {
		ensureGisIsEnabled();

		List<LayerMetadata> layers = layerMetadataStore.list(card.getSchema().getName());
		GeoFeature geoFeature = null;

		if (layers.size() > 0) {
			LayerMetadata layer = layers.get(0);
			GeoFeatureQuery gfq = new GeoFeatureQuery(layer);
			try {
				geoFeature = gfq.master(card).get();
			} catch (Exception e) {
				// There are no feature for this card
			}
		}

		return geoFeature;
	}

	public Iterable<GeoFeature> getFeatures(ITable masterClass, String layerName, String bbox) throws Exception {
		ensureGisIsEnabled();

		String fullName = String.format(GEO_TABLE_NAME_FORMAT, masterClass.getName(), layerName);
		LayerMetadata layerMetadata = layerMetadataStore.get(fullName);
		return new GeoFeatureQuery(layerMetadata).bbox(bbox).onlyFrom(masterClass);
	}

	public void updateFeatures(ICard masterCard, Map<String, String> attributes) throws Exception {
		ensureGisIsEnabled();

		String geoAttributesJsonString = attributes.get("geoAttributes");
		if (geoAttributesJsonString != null) {
			final JSONObject geoAttributesObject = new JSONObject(geoAttributesJsonString);
			final String[] geoAttributesName = JSONObject.getNames(geoAttributesObject);
			final ITable masterTable = masterCard.getSchema();

			if (geoAttributesName != null) {
				for (String name : geoAttributesName) {
					final LayerMetadata layerMetaData = layerMetadataStore.get(fullName(masterTable.getName(), name));
					final GeoFeatureQuery gfq = new GeoFeatureQuery(layerMetaData);
					final String value = geoAttributesObject.getString(name);

					try {
						final GeoFeature geoFeature = gfq.master(masterCard).get();
						if (value != null && !value.trim().isEmpty()) {
							geoFeature.setValue(value);
						} else {
							geoFeature.delete();
						}
					} catch (NotFoundException e) {
						if (value != null && !value.trim().isEmpty()) {
							createGeoFeature(masterCard, layerMetaData, value);
						}
					}

				}
			}
		}
	}

	/* GeoServer */

	public void createGeoServerLayer(LayerMetadata layerMetaData, FileItem file) throws IOException, Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		String geoServerLayerName = geoServerService.createStoreAndLayer(layerMetaData, file.getInputStream());
		if (geoServerLayerName == null) {
			throw new Exception("Geoserver has not create the layer");
		}

		String fullName = String.format(GEO_TABLE_NAME_FORMAT, GEOSERVER, layerMetaData.getName());
		layerMetaData.setFullName(fullName);
		layerMetaData.setGeoServerName(geoServerLayerName);

		layerMetadataStore.createLayer(layerMetaData);
	}

	public void modifyGeoServerLayer(String name, String description, int maximumZoom, int minimumZoom, FileItem file) throws Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		LayerMetadata layerMetadata = modifyLayerMetadata(GEOSERVER, name, description, minimumZoom, maximumZoom, null);

		if (file != null && file.getSize() > 0) {
			geoServerService.modifyStoreData(layerMetadata, file.getInputStream());
		}
	}

	public void deleteGeoServerLayer(String name) throws Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		String fullName = String.format(GEO_TABLE_NAME_FORMAT, GEOSERVER, name);
		LayerMetadata layer = layerMetadataStore.get(fullName);
		geoServerService.deleteStoreAndLayers(layer);
		layerMetadataStore.deleteLayer(fullName);
	}

	public List<LayerMetadata> getGeoServerLayers() throws Exception {
		ensureGisIsEnabled();

		return layerMetadataStore.list(GEOSERVER);
	}

	/* Common layers methods */

	public List<LayerMetadata> list() throws Exception {
		ensureGisIsEnabled();
		return layerMetadataStore.list();
	}

	public List<LayerMetadata> listGeoAttributesForTable(ITable table) throws Exception {
		ensureGisIsEnabled();

		return layerMetadataStore.list(table);
	}

	public void setLayerVisisbility(String layerFullName, String visibleTable, boolean visible) throws Exception {
		ensureGisIsEnabled();

		layerMetadataStore.updateLayerVisibility(layerFullName, visibleTable, visible);
	}

	public void reorderLayers(int oldIndex, int newIndex) throws Exception {
		ensureGisIsEnabled();

		OrderingUtils.alterPosition(list(), oldIndex, newIndex, new PositionHandler<LayerMetadata>() {
			@Override
			public int getPosition(LayerMetadata l) {
				return l.getIndex();
			}

			@Override
			public void setPosition(LayerMetadata l, int p) {
				layerMetadataStore.setLayerIndex(l, p);
			}
		});
	}

	/* DomainTreeNavigation */

	public void saveGisTreeNavigation(DomainTreeNode root) {
		domainTreeStore.createOrReplaceTree(DOMAIN_TREE_TYPE, root);
	}

	public void removeGisTreeNavigation() {
		domainTreeStore.removeTree(DOMAIN_TREE_TYPE);
	}

	public DomainTreeNode getGisTreeNavigation() {
		return domainTreeStore.getDomainTree(DOMAIN_TREE_TYPE);
	}

	/* private methods */

	private static ITable createGeoAttributeTable(ITable masterTable, LayerMetadata layerMetadata) {
		ITable geoAttributeTable = UserContext.systemContext().tables().create();
		geoAttributeTable.setTableType(CMTableType.SIMPLECLASS);
		geoAttributeTable.setMode(Mode.RESERVED.toString());
		geoAttributeTable.setName(String.format(GEO_TABLE_NAME_FORMAT, masterTable.getName(), layerMetadata.getName()));
		geoAttributeTable.setDescription(layerMetadata.getDescription());
		geoAttributeTable.save();

		IAttribute masterAttribute = AttributeImpl.create(geoAttributeTable, MASTER_ATTRIBUTE, AttributeType.FOREIGNKEY);
		masterAttribute.setFKTargetClass(masterTable.getName());
		masterAttribute.setMode(Mode.RESERVED.toString());
		masterAttribute.save();

		IAttribute geometryAttribute = AttributeImpl.create(geoAttributeTable, GEOMETRY_ATTRIBUTE, 
		GeoType.valueOf(layerMetadata.getType()).getAttributeType());
		geometryAttribute.setMode(Mode.RESERVED.toString());
		geometryAttribute.save();

		return geoAttributeTable;
	}

	private LayerMetadata modifyLayerMetadata(String targetTableName, String name, String description,
			int minimumZoom, int maximumZoom, String style) {

		String fullName = fullName(targetTableName, name);
		LayerMetadata changes = new LayerMetadata();
		changes.setDescription(description);
		changes.setMinimumZoom(minimumZoom);
		changes.setMaximumzoom(maximumZoom);
		changes.setMapStyle(style);

		return layerMetadataStore.updateLayer(fullName, changes);
	}

	private void ensureGisIsEnabled() throws Exception {
		if (!GisProperties.getInstance().isEnabled()) {
			throw new Exception("GEOServer is non enabled");
		}
	}

	private void ensureGeoServerIsEnabled() throws Exception {
		if (!GisProperties.getInstance().isGeoServerEnabled()) {
			throw new Exception("GEOServer is non enabled");
		}
	}

	private void createGeoFeature(ICard card, LayerMetadata layerMetadata, String value) {
		ITable featureTable = getFeatureTable(layerMetadata.getFullName());
		ICard geoCard = featureTable.cards().create();
		geoCard.setValue(MASTER_ATTRIBUTE, card.getId());
		geoCard.setValue(GEOMETRY_ATTRIBUTE, value);
		geoCard.save();
	}

	private  ITable getFeatureTable(String tableName) {
		return UserContext.systemContext().tables().get(tableName);
	}

	private String fullName(String masterTableName, String name) {
		String fullName = String.format(GEO_TABLE_NAME_FORMAT, masterTableName, name);
		return fullName;
	}
}