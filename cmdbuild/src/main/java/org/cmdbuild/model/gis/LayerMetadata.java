package org.cmdbuild.model.gis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;

public class LayerMetadata {
	private static final String TARGET_TABLE_PREFIX = "gis.Detail_";
	public static final String TARGET_TABLE_FORMAT = TARGET_TABLE_PREFIX + "%s";
	private String name, fullName, description, mapStyle, type, geoServerName;
	private Integer index, minimumZoom, maximumzoom;
	private Set<String> visibility;

	public LayerMetadata() {
		this(null, null, null, 0, 0, 0, null, null, null);
	}

	public LayerMetadata(String name, String description, String type,
			Integer minimumZoom, Integer maximumzoom, Integer index,
			String mapStyle, Set<String> visibility, String geoServerName) {

		this.description = description;
		this.index = index;
		this.minimumZoom = minimumZoom;
		this.maximumzoom = maximumzoom;
		this.mapStyle = mapStyle;
		this.name = name;
		this.type = type;
		this.geoServerName = null;
		this.setVisibility(visibility);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGeoServerName() {
		return geoServerName;
	}

	public void setGeoServerName(String geoServerName) {
		this.geoServerName = geoServerName;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMapStyle() {
		return mapStyle;
	}

	public void setMapStyle(String mapStyle) {
		if (mapStyle != null) {
			this.mapStyle = mapStyle;
		} else {
			this.mapStyle = "{}";
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getMinimumZoom() {
		return minimumZoom;
	}

	public void setMinimumZoom(int minimumZoom) {
		this.minimumZoom = minimumZoom;
	}

	public int getMaximumzoom() {
		return maximumzoom;
	}

	public void setMaximumzoom(int maximumzoom) {
		this.maximumzoom = maximumzoom;
	}

	public Set<String> getVisibility() {
		return visibility;
	}

	public String getVisibilityAsString() {
		return Joiner.on(",").join(getVisibility());
	}

	public void setVisibility(Set<String> visibility) {
		if (visibility != null) {
			this.visibility = visibility;
		} else {
			this.visibility = new HashSet<String>();
		}
	}

	public void setVisibilityFromString(String visibility) {
		if (visibility != null) {
			String[] items = visibility.split("\\s*,\\s*");
			this.visibility = new HashSet<String>(Arrays.asList(items));
		} else {
			this.visibility = new HashSet<String>();
		}
	}

	public void addVisibility(String tableName) {
		this.visibility.add(tableName);
	}

	public void removeVisibility(String tableName) {
		this.visibility.remove(tableName);
	}

	public boolean isVisible(String tableName) {
		return this.visibility.contains(tableName);
	}

	public String getMasterTableName() {
		String fullName = getFullName();
		String name = getName();
		return fullName.substring(TARGET_TABLE_PREFIX.length(), fullName.length()-(name.length()+1));
	}
}
