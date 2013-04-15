package org.cmdbuild.logic;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.fileupload.FileItem;
import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.GisProperties;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.logic.LogicDTO.DomainWithSource;
import org.cmdbuild.logic.commands.AbstractGetRelation.RelationInfo;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.model.domainTree.DomainTreeCardNode;
import org.cmdbuild.model.domainTree.DomainTreeNode;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.gis.GeoFeature;
import org.cmdbuild.services.gis.GeoFeatureLayer.GeoType;
import org.cmdbuild.services.gis.GeoFeatureQuery;
import org.cmdbuild.services.gis.geoserver.GeoServerService;
import org.cmdbuild.services.store.DBDomainTreeStore;
import org.cmdbuild.services.store.DBLayerMetadataStore;
import org.cmdbuild.utils.OrderingUtils;
import org.cmdbuild.utils.OrderingUtils.PositionHandler;
import org.json.JSONObject;

@OldDao
public class GISLogic implements Logic {

	private static final GeoServerService geoServerService = new GeoServerService();
	private static final DBLayerMetadataStore layerMetadataStore = new DBLayerMetadataStore();
	private static final DBDomainTreeStore domainTreeStore = new DBDomainTreeStore();

	private static final String DOMAIN_TREE_TYPE = "gisnavigation";
	private static final String MASTER_ATTRIBUTE = "Master";
	private static final String GEOMETRY_ATTRIBUTE = "Geometry";
	private static final String GEOSERVER = "_Geoserver";
	private static final String GEO_TABLESPACE = "gis";
	private static final String GEO_TABLE_NAME_FORMAT = GEO_TABLESPACE + ".Detail_%s_%s";

	private final UserOperations userOperations;

	public GISLogic(final UserContext userContext) {
		this.userOperations = UserOperations.from(userContext);
	}

	/* Geo attributes */

	public boolean isGisEnabled() {
		return GisProperties.getInstance().isEnabled();
	}

	public LayerMetadata createGeoAttribute(final ITable master, final LayerMetadata layerMetaData) throws Exception {
		ensureGisIsEnabled();
		final ITable geometryTable = createGeoAttributeTable(master, layerMetaData);
		layerMetaData.setFullName(geometryTable.getName());

		return layerMetadataStore.createLayer(layerMetaData);
	}

	public LayerMetadata modifyGeoAttribute(final ITable targetTable, final String name, final String description,
			final int minimumZoom, final int maximumZoom, final String style) throws Exception {
		ensureGisIsEnabled();

		return modifyLayerMetadata(targetTable.getName(), name, description, minimumZoom, maximumZoom, style, null);
	}

	public void deleteGeoAttribute(final String masterTableName, final String name) throws Exception {
		ensureGisIsEnabled();

		final String fullName = fullName(masterTableName, name);
		final ITable geoTable = userOperations.tables().get(fullName);
		geoTable.delete();
		layerMetadataStore.deleteLayer(fullName);
	}

	public GeoFeature getFeature(final ICard card) throws Exception {
		ensureGisIsEnabled();

		final List<LayerMetadata> layers = layerMetadataStore.list(card.getSchema().getName());
		GeoFeature geoFeature = null;

		if (layers.size() > 0) {
			final LayerMetadata layer = layers.get(0);
			final GeoFeatureQuery gfq = new GeoFeatureQuery(layer);
			try {
				geoFeature = gfq.master(card).get();
			} catch (final Exception e) {
				// There are no feature for this card
			}
		}

		return geoFeature;
	}

	public Iterable<GeoFeature> getFeatures(final ITable masterClass, final String layerName, final String bbox)
			throws Exception {
		ensureGisIsEnabled();

		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, masterClass.getName(), layerName);
		final LayerMetadata layerMetadata = layerMetadataStore.get(fullName);
		return new GeoFeatureQuery(layerMetadata).bbox(bbox).onlyFrom(masterClass);
	}

	@OldDao
	public void updateFeatures(final ICard masterCard, final Map<String, Object> attributes) throws Exception {
		ensureGisIsEnabled();

		final String geoAttributesJsonString = (String) attributes.get("geoAttributes");
		if (geoAttributesJsonString != null) {
			final JSONObject geoAttributesObject = new JSONObject(geoAttributesJsonString);
			final String[] geoAttributesName = JSONObject.getNames(geoAttributesObject);
			final ITable masterTable = masterCard.getSchema();

			if (geoAttributesName != null) {
				for (final String name : geoAttributesName) {
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
					} catch (final NotFoundException e) {
						if (value != null && !value.trim().isEmpty()) {
							createGeoFeature(masterCard, layerMetaData, value);
						}
					}

				}
			}
		}
	}

	/* GeoServer */

	public void createGeoServerLayer(final LayerMetadata layerMetaData, final FileItem file) throws IOException,
			Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		final String geoServerLayerName = geoServerService.createStoreAndLayer(layerMetaData, file.getInputStream());
		if (geoServerLayerName == null) {
			throw new Exception("Geoserver has not create the layer");
		}

		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, GEOSERVER, layerMetaData.getName());
		layerMetaData.setFullName(fullName);
		layerMetaData.setGeoServerName(geoServerLayerName);

		layerMetadataStore.createLayer(layerMetaData);
	}

	public void modifyGeoServerLayer(final String name, final String description, final int maximumZoom,
			final int minimumZoom, final FileItem file, final Set<String> cardBinding) throws Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		final LayerMetadata layerMetadata = modifyLayerMetadata(GEOSERVER, name, description, minimumZoom, maximumZoom,
				null, cardBinding);

		if (file != null && file.getSize() > 0) {
			geoServerService.modifyStoreData(layerMetadata, file.getInputStream());
		}
	}

	public void deleteGeoServerLayer(final String name) throws Exception {
		ensureGisIsEnabled();
		ensureGeoServerIsEnabled();

		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, GEOSERVER, name);
		final LayerMetadata layer = layerMetadataStore.get(fullName);
		geoServerService.deleteStoreAndLayers(layer);
		layerMetadataStore.deleteLayer(fullName);
	}

	public List<LayerMetadata> getGeoServerLayers() throws Exception {
		ensureGisIsEnabled();

		return layerMetadataStore.list(GEOSERVER);
	}

	public Map<String, ClassMapping> getGeoServerLayerMapping() throws Exception {
		final List<LayerMetadata> geoServerLayers = getGeoServerLayers();
		final Map<String, ClassMapping> mapping = new HashMap<String, ClassMapping>();

		for (final LayerMetadata layer : geoServerLayers) {
			for (final String bindedCard : layer.getCardBinding()) {
				final String[] cardInfo = bindedCard.split("_"); // A cardInfo
				// is
				// ClassName_CardId
				final String className = cardInfo[0];
				final String cardId = cardInfo[1];

				ClassMapping classMapping;
				if (mapping.containsKey(className)) {
					classMapping = mapping.get(className);
				} else {
					classMapping = new ClassMapping();
					mapping.put(className, classMapping);
				}

				classMapping.addCardMapping(cardId, new CardMapping(layer.getName(), layer.getDescription()));
			}
		}

		return mapping;
	}

	/* Common layers methods */

	public List<LayerMetadata> list() throws Exception {
		ensureGisIsEnabled();
		return layerMetadataStore.list();
	}

	public List<LayerMetadata> listGeoAttributesForTable(final ITable table) throws Exception {
		ensureGisIsEnabled();

		return layerMetadataStore.list(table);
	}

	public void setLayerVisisbility(final String layerFullName, final String visibleTable, final boolean visible)
			throws Exception {
		ensureGisIsEnabled();

		layerMetadataStore.updateLayerVisibility(layerFullName, visibleTable, visible);
	}

	public void reorderLayers(final int oldIndex, final int newIndex) throws Exception {
		ensureGisIsEnabled();

		OrderingUtils.alterPosition(list(), oldIndex, newIndex, new PositionHandler<LayerMetadata>() {
			@Override
			public int getPosition(final LayerMetadata l) {
				return l.getIndex();
			}

			@Override
			public void setPosition(final LayerMetadata l, final int p) {
				layerMetadataStore.setLayerIndex(l, p);
			}
		});
	}

	/* DomainTreeNavigation */

	public void saveGisTreeNavigation(final DomainTreeNode root) {
		domainTreeStore.createOrReplaceTree(DOMAIN_TREE_TYPE, root);
	}

	public void removeGisTreeNavigation() {
		domainTreeStore.removeTree(DOMAIN_TREE_TYPE);
	}

	public DomainTreeNode getGisTreeNavigation() {
		return domainTreeStore.getDomainTree(DOMAIN_TREE_TYPE);
	}

	public DomainTreeCardNode expandDomainTree(final DataAccessLogic dataAccesslogic) {
		final Map<String, Long> domainIds = getDomainIds(dataAccesslogic);
		final Map<Long, DomainTreeCardNode> nodes = new HashMap<Long, DomainTreeCardNode>();

		final DomainTreeNode root = this.getGisTreeNavigation();
		final DomainTreeCardNode rootCardNode = new DomainTreeCardNode();

		if (root != null) {
			rootCardNode.setText(root.getTargetClassDescription());
			rootCardNode.setExpanded(true);
			rootCardNode.setLeaf(false);

			nodes.put(rootCardNode.getCardId(), rootCardNode);

			final ITable table = userOperations.tables().get(root.getTargetClassName());

			for (final ICard card : table.cards().list()) {
				final DomainTreeCardNode node = new DomainTreeCardNode();
				node.setText(card.getDescription());
				node.setClassName(card.getSchema().getName());
				node.setClassId(new Long(card.getSchema().getId()));
				node.setCardId(new Long(card.getId()));
				node.setLeaf(false);

				rootCardNode.addChild(node);
				nodes.put(node.getCardId(), node);
			}

			fetchRelationsByDomain(dataAccesslogic, domainIds, root, nodes);
			rootCardNode.sortByText();
			setDefaultCheck(nodes);
		}

		return rootCardNode;
	}

	// the default check is that:
	// identify the base nodes, AKA the nodes created expanding the base domain
	// this nodes represents the base level, and we want that only the first
	// child
	// of a siblings group was checked. Also all the ancestors of this node must
	// be checked
	private void setDefaultCheck(final Map<Long, DomainTreeCardNode> nodes) {
		final Map<Object, DomainTreeCardNode> visitedNodes = new HashMap<Object, DomainTreeCardNode>();

		for (final DomainTreeCardNode node : nodes.values()) {
			if (node.isBaseNode()) {
				final DomainTreeCardNode parent = node.parent();
				if (parent != null && !visitedNodes.containsKey(parent.getCardId())) {

					parent.getChildren().get(0).setChecked(true, true, true);
					visitedNodes.put(parent.getCardId(), parent);
				}
			}
		}
	}

	private void fetchRelationsByDomain(final DataAccessLogic dataAccesslogic, final Map<String, Long> domainIds,
			final DomainTreeNode root, final Map<Long, DomainTreeCardNode> nodes) {

		final Map<Object, Map<Object, List<RelationInfo>>> relationsByDomain = new HashMap<Object, Map<Object, List<RelationInfo>>>();

		for (final DomainTreeNode domainTreeNode : root.getChildNodes()) {
			final Long domainId = domainIds.get(domainTreeNode.getDomainName());
			final String querySource = domainTreeNode.isDirect() ? "_1" : "_2";
			final DomainWithSource dom = DomainWithSource.create(domainId, querySource);
			final Map<Object, List<RelationInfo>> relations = dataAccesslogic.relationsBySource(
					root.getTargetClassName(), dom);
			relationsByDomain.put(domainId, relations);
			final boolean leaf = domainTreeNode.getChildNodes().size() == 0;
			final boolean baseNode = domainTreeNode.isBaseNode();
			fillNodes(nodes, relationsByDomain, leaf, baseNode);
			fetchRelationsByDomain(dataAccesslogic, domainIds, domainTreeNode, nodes);
		}
	}

	private void fillNodes(final Map<Long, DomainTreeCardNode> nodes,
			final Map<Object, Map<Object, List<RelationInfo>>> relationsByDomain, final boolean leaf,
			final boolean baseNode) {

		for (final Map<Object, List<RelationInfo>> relationsBySource : relationsByDomain.values()) {
			for (final Object sourceCardId : relationsBySource.keySet()) {
				final DomainTreeCardNode parent = nodes.get(sourceCardId);

				if (parent == null) {
					continue;
				}

				for (final RelationInfo ri : relationsBySource.get(sourceCardId)) {
					final DomainTreeCardNode child = new DomainTreeCardNode();
					String text = ri.getTargetDescription();
					if (text == null || text.equals("")) {
						text = ri.getTargetCode();
					}

					child.setText(text);
					child.setCardId(ri.getTargetId());
					child.setClassId(ri.getTargetType().getId());
					child.setClassName(ri.getTargetType().getName());
					child.setLeaf(leaf);
					child.setBaseNode(baseNode);

					parent.addChild(child);
					nodes.put(child.getCardId(), child);
				}
			}
		}
	}

	/* private methods */

	private ITable createGeoAttributeTable(final ITable masterTable, final LayerMetadata layerMetadata) {
		final ITable geoAttributeTable = userOperations.tables().create();
		geoAttributeTable.setTableType(CMTableType.SIMPLECLASS);
		geoAttributeTable.setMode(Mode.RESERVED.toString());
		geoAttributeTable.setName(String.format(GEO_TABLE_NAME_FORMAT, masterTable.getName(), layerMetadata.getName()));
		geoAttributeTable.setDescription(layerMetadata.getDescription());
		geoAttributeTable.save();

		final IAttribute masterAttribute = AttributeImpl.create(geoAttributeTable, MASTER_ATTRIBUTE,
				AttributeType.FOREIGNKEY);
		masterAttribute.setFKTargetClass(masterTable.getName());
		masterAttribute.setMode(Mode.RESERVED.toString());
		masterAttribute.save();

		final IAttribute geometryAttribute = AttributeImpl.create(geoAttributeTable, GEOMETRY_ATTRIBUTE, GeoType
				.valueOf(layerMetadata.getType()).getAttributeType());
		geometryAttribute.setMode(Mode.RESERVED.toString());
		geometryAttribute.save();

		return geoAttributeTable;
	}

	private LayerMetadata modifyLayerMetadata(final String targetTableName, final String name,
			final String description, final int minimumZoom, final int maximumZoom, final String style,
			final Set<String> cardBinding) {

		final String fullName = fullName(targetTableName, name);
		final LayerMetadata changes = new LayerMetadata();
		changes.setDescription(description);
		changes.setMinimumZoom(minimumZoom);
		changes.setMaximumzoom(maximumZoom);
		changes.setMapStyle(style);
		changes.setCardBinding(cardBinding);

		return layerMetadataStore.updateLayer(fullName, changes);
	}

	private void ensureGisIsEnabled() throws Exception {
		if (!isGisEnabled()) {
			throw new Exception("GIS Module is non enabled");
		}
	}

	private void ensureGeoServerIsEnabled() throws Exception {
		if (!GisProperties.getInstance().isGeoServerEnabled()) {
			throw new Exception("GEOServer is non enabled");
		}
	}

	private void createGeoFeature(final ICard card, final LayerMetadata layerMetadata, final String value) {
		final ITable featureTable = getFeatureTable(layerMetadata.getFullName());
		final ICard geoCard = featureTable.cards().create();
		geoCard.setValue(MASTER_ATTRIBUTE, card.getId());
		geoCard.setValue(GEOMETRY_ATTRIBUTE, value);
		geoCard.save();
	}

	private ITable getFeatureTable(final String tableName) {
		return userOperations.tables().get(tableName);
	}

	private String fullName(final String masterTableName, final String name) {
		final String fullName = String.format(GEO_TABLE_NAME_FORMAT, masterTableName, name);
		return fullName;
	}

	private Map<String, Long> getDomainIds(final DataAccessLogic dataAccessLogic) {
		final Map<String, Long> domainIds = new HashMap<String, Long>();

		for (final CMDomain d : dataAccessLogic.findActiveDomains()) {
			domainIds.put(d.getName(), new Long(d.getId()));
		}

		return domainIds;
	}

	public class ClassMapping {
		private final Map<String, CardMapping> map;

		public ClassMapping() {
			map = new HashMap<String, CardMapping>();
		}

		public void addCardMapping(final String cardId, final CardMapping mapping) {
			map.put(cardId, mapping);
		}

		public Set<String> cards() {
			return map.keySet();
		}

		public CardMapping get(final String cardId) {
			return map.get(cardId);
		}
	}

	public class CardMapping {
		private final String name;
		private final String description;

		public CardMapping(final String name, final String description) {
			this.name = name;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public String getDesription() {
			return description;
		}
	}
}
