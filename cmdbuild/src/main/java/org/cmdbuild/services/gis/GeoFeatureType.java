package org.cmdbuild.services.gis;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.elements.AttributeImpl;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.IAttribute;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.services.meta.MetadataMap;
import org.cmdbuild.services.meta.MetadataService;

public class GeoFeatureType extends AbstractGeoLayer {

	static final String MASTER_ATTRIBUTE = "Master";
	static final String GEOMETRY_ATTRIBUTE = "Geometry";

	private static final String GEO_TABLESPACE;
	private static final Pattern GEO_TABLE_NAME_MATCHER;
	private static final String GEO_TABLE_NAME_FORMAT;

	private static final String GIS_META_PREFIX;
	private static final String GIS_META_MINZOOM;
	private static final String GIS_META_MAXZOOM;
	private static final String GIS_META_STYLE;
	private static final String GIS_META_VISIBILITY;
	private static final String GIS_META_INDEX;

	static {
		GEO_TABLESPACE = "gis";
		GEO_TABLE_NAME_MATCHER = Pattern.compile("^" + GEO_TABLESPACE + ".Detail_([^_]+)_([^_]+)$");
		GEO_TABLE_NAME_FORMAT = GEO_TABLESPACE + ".Detail_%s_%s";
		GIS_META_PREFIX = MetadataService.SYSTEM_PREFIX + ".gis";
		GIS_META_MINZOOM = GIS_META_PREFIX + ".minzoom";
		GIS_META_MAXZOOM = GIS_META_PREFIX + ".maxzoom";
		GIS_META_STYLE = GIS_META_PREFIX + ".style";
		GIS_META_VISIBILITY = GIS_META_PREFIX + ".visibility";
		GIS_META_INDEX = GIS_META_PREFIX + ".index";
	}

	public enum GeoType {
		POINT(AttributeType.POINT), LINESTRING(AttributeType.LINESTRING), POLYGON(AttributeType.POLYGON);

		private final AttributeType attributeType;

		private GeoType(final AttributeType attributeType) {
			this.attributeType = attributeType;
		}

		public static GeoType valueOf(final AttributeType attributeType) {
			for (final GeoType gt : GeoType.values()) {
				if (gt.attributeType.equals(attributeType)) {
					return gt;
				}
			}
			throw new IllegalArgumentException();
		}

		public AttributeType getAttributeType() {
			return attributeType;
		}
	}

	private final ITable geoAttributeTable;

	private String style;

	private GeoFeatureType(final ITable geoAttributeTable, final String name) {
		super(name);
		this.geoAttributeTable = geoAttributeTable;
		loadMeta();
	}

	public static GeoFeatureType fromGeoTable(final ITable geoAttributeTable) {
		GeoFeatureType geoFeatureType;
		final String geoFeatureName = getGeoFeatureFromTable(geoAttributeTable);
		if (geoFeatureName != null) {
			geoFeatureType = new GeoFeatureType(geoAttributeTable, geoFeatureName);
		} else {
			geoFeatureType = null;
		}
		return geoFeatureType;
	}

	private static String getGeoFeatureFromTable(final ITable geoAttributeTable) {
		String geoFeatureName;
		final Matcher geoNameMatcher = GEO_TABLE_NAME_MATCHER.matcher(geoAttributeTable.getName());
		if (geoNameMatcher.matches()) {
			geoFeatureName = geoNameMatcher.group(2);
		} else {
			geoFeatureName = null;
		}
		return geoFeatureName;
	}

	public static List<GeoFeatureType> list() {
		final List<GeoFeatureType> gftList = new ArrayList<GeoFeatureType>();
		for (final ITable t : UserOperations.from(UserContext.systemContext()).tables().list(CMTableType.SIMPLECLASS)) {
			final GeoFeatureType gft = fromGeoTable(t);
			if (gft != null) {
				gftList.add(gft);
			}
		}
		return gftList;
	}

	public static GeoFeatureType create(final ITable masterTable, final String name, final String description,
			final GeoType geoType, final int minZoom, final int maxZoom, final String style, final int position) {
		final ITable geoAttributeTable = createGeoAttributeTable(masterTable, name, description, geoType, minZoom,
				maxZoom, style);
		final GeoFeatureType geoFeatureType = fromGeoTable(geoAttributeTable);
		geoFeatureType.setMinZoom(minZoom);
		geoFeatureType.setMaxZoom(maxZoom);
		geoFeatureType.setStyle(style);
		geoFeatureType.setIndex(position);
		for (final ITable t : masterTable.treeBranch()) {
			geoFeatureType.setVisibility(t, true);
		}
		geoFeatureType.saveMeta();
		return geoFeatureType;
	}

	private static ITable createGeoAttributeTable(final ITable masterTable, final String name,
			final String description, final GeoType geoType, final int minZoom, final int maxZoom, final String style) {
		final ITable geoAttributeTable = UserOperations.from(UserContext.systemContext()).tables().create();
		geoAttributeTable.setTableType(CMTableType.SIMPLECLASS);
		geoAttributeTable.setMode(Mode.RESERVED.toString());
		geoAttributeTable.setName(String.format(GEO_TABLE_NAME_FORMAT, masterTable.getName(), name));
		geoAttributeTable.setDescription(description);
		geoAttributeTable.save();
		final IAttribute masterAttribute = AttributeImpl.create(geoAttributeTable, MASTER_ATTRIBUTE,
				AttributeType.FOREIGNKEY);
		masterAttribute.setFKTargetClass(masterTable.getName());
		masterAttribute.setMode(Mode.RESERVED.toString());
		masterAttribute.save();
		final IAttribute geometryAttribute = AttributeImpl.create(geoAttributeTable, GEOMETRY_ATTRIBUTE,
				geoType.getAttributeType());
		geometryAttribute.setMode(Mode.RESERVED.toString());
		geometryAttribute.save();
		return geoAttributeTable;
	}

	@Override
	public String getDescription() {
		return geoAttributeTable.getDescription();
	}

	public void setDescription(final String description) {
		this.geoAttributeTable.setDescription(description);
	}

	public ITable getMasterTable() {
		return geoAttributeTable.getAttribute(MASTER_ATTRIBUTE).getFKTargetClass();
	}

	@Override
	public String getTypeName() {
		return geoAttributeTable.getAttribute(GEOMETRY_ATTRIBUTE).getType().toString();
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(final String style) {
		if (style == null) {
			this.style = "{}";
		} else {
			this.style = style;
		}
	}

	public GeoFeatureQuery query() {
		return new GeoFeatureQuery(this);
	}

	ITable getGeoAttributeTable() {
		return geoAttributeTable;
	}

	public void delete() {
		geoAttributeTable.delete();
	}

	public void save() {
		geoAttributeTable.save();
		saveMeta();
	}

	private void loadMeta() {
		final MetadataMap meta = MetadataService.getMetadata(geoAttributeTable);
		setMinZoom((String) meta.get(GIS_META_MINZOOM));
		setMaxZoom((String) meta.get(GIS_META_MAXZOOM));
		setStyle((String) meta.get(GIS_META_STYLE));
		setVisibility((String) meta.get(GIS_META_VISIBILITY));
		setIndex((String) meta.get(GIS_META_INDEX));
	}

	private void saveMeta() {
		MetadataService.updateMetadata(geoAttributeTable, GIS_META_MINZOOM, String.valueOf(getMinZoom()));
		MetadataService.updateMetadata(geoAttributeTable, GIS_META_MAXZOOM, String.valueOf(getMaxZoom()));
		MetadataService.updateMetadata(geoAttributeTable, GIS_META_STYLE, style);
		MetadataService.updateMetadata(geoAttributeTable, GIS_META_VISIBILITY, getVisibilityAsString());
		MetadataService.updateMetadata(geoAttributeTable, GIS_META_INDEX, String.valueOf(getIndex()));
	}

	public boolean isActive() {
		return getMasterTable().getStatus().isActive();
	}

	public void create(final ICard card, final String value) {
		final ICard geoCard = getGeoAttributeTable().cards().create();
		geoCard.setValue(MASTER_ATTRIBUTE, card.getId());
		geoCard.setValue(GEOMETRY_ATTRIBUTE, value);
		geoCard.save();
	}

	@Override
	public boolean isLocal(final ITable table) {
		return getMasterTable().treeBranch().contains(table);
	}

	@Override
	public boolean isEnabled() {
		return geoAttributeTable.getStatus().isActive();
	}
}