package org.cmdbuild.logic;

import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.model.DomainTreeNode;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.gis.CompositeLayerService;
import org.cmdbuild.services.gis.GeoFeatureType;
import org.cmdbuild.services.gis.GeoFeatureType.GeoType;
import org.cmdbuild.services.gis.GeoTable;
import org.cmdbuild.services.store.DBDomainTreeStore;

public class GISLogic {

	private final UserContext userContext;
//	private static final GeoServerService geoServerService = new GeoServerService();
	private static final CompositeLayerService layerService = new  CompositeLayerService();
	private static final DBDomainTreeStore domainTreeStore = new DBDomainTreeStore();
	private static final String DOMAIN_TREE_TYPE = "gisnavigation";

	public GISLogic() {
		this(null);
	}

	public GISLogic(UserContext userCtx) {
		userContext = userCtx;
	}

	public GeoFeatureType addGeoAttribute(ITable table, String name, String description, String geoType,
			int minZoom, int maxZoom, String style) {

		return layerService.createGeoFeatureType(table, name, description, GeoType.valueOf(geoType),
				minZoom, maxZoom, style.toString());
	}

	public GeoFeatureType modifyGeoAttribute(ITable table, String name, String description,
			int minZoom, int maxZoom, String style) {

		GeoTable geoMasterClass = new GeoTable(table);
		GeoFeatureType geoFeatureType = geoMasterClass.getGeoFeatureType(name);
		geoFeatureType.setDescription(description);
		geoFeatureType.setMinZoom(minZoom);
		geoFeatureType.setMaxZoom(maxZoom);
		geoFeatureType.setStyle(style);
		geoFeatureType.save();

		return geoFeatureType;
	}

	public void deleteGeoAttribute(ITable table, String name) {
		GeoTable geoMasterClass = new GeoTable(table);
		geoMasterClass.getGeoFeatureType(name).delete();
	}

	/*DomainTreeNavigation*/

	public void saveGisTreeNavigation(DomainTreeNode root) {
		domainTreeStore.createOrReplaceTree(DOMAIN_TREE_TYPE, root);
	}

	public void removeGisTreeNavigation() {
		domainTreeStore.removeTree(DOMAIN_TREE_TYPE);
	}

	public DomainTreeNode getGisTreeNavigation() {
		return domainTreeStore.getDomainTree(DOMAIN_TREE_TYPE);
	}
}
