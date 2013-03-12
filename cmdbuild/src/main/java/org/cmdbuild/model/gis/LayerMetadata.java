package org.cmdbuild.model.gis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Joiner;

public class LayerMetadata {
	private static final String TARGET_TABLE_PREFIX = "gis.Detail_";
	public static final String TARGET_TABLE_FORMAT = TARGET_TABLE_PREFIX + "%s";
	private String name, fullName, description, mapStyle, type;
	private Integer index, minimumZoom, maximumzoom;
	private Set<String> visibility;

	/**
	 * The name of the store that GEOServer
	 * has created to hosts the layer
	 */
	private String storeName;

	/**
	 * The name that geoServer return as layer name (Is the name of the files
	 * within the .zip file)
	 */
	private String geoServerName;

	/**
	 * Is a configuration string with that form
	 * ClassName_CardId,ClassName2_CardId2,...
	 * Used to bind show the layer near to the binded
	 * cards in the GISNavigationTree
	 */
	private Set<String> cardBinding;

	public LayerMetadata() {
		this(null, null, null, 0, 0, 0, null, null);
	}

	public LayerMetadata(String name) {
		this(name, null, null, 0, 0, 0, null, null);
	}

	public LayerMetadata(String name, String description, String type,
			Integer minimumZoom, Integer maximumzoom, Integer index,
			String mapStyle, Set<String> visibility) {

		this.description = description;
		this.index = index;
		this.minimumZoom = minimumZoom;
		this.maximumzoom = maximumzoom;
		this.mapStyle = mapStyle;
		this.name = name;
		this.type = type;
		this.setVisibility(visibility);
		this.setCardBinding(new HashSet<String>());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String setStoreName(String storeName) {
		return this.storeName = storeName;
	}

	public String getStoreName() {
		return storeName;
	}

	public String toString() {
		return getStoreName() + "/" + getName();
	}

	public String getGeoServerName() {
		return geoServerName;
	}

	public void setGeoServerName(String geoServerName) {
		this.geoServerName = geoServerName;
	}

	public Set<String> getCardBinding() {
		return cardBinding;
	}

	public Object getCardBindingAsString() {
		return Joiner.on(",").join(getCardBinding());
	}

	public void setCardBinding(Set<String> cardBinding) {
		if (cardBinding == null) {
			this.cardBinding = new HashSet<String>();
		} else {
			this.cardBinding = cardBinding;
		}
	}

	public void setCardBindingFromString(String cardBinding) {
		if (cardBinding != null) {
			String[] items = cardBinding.split("\\s*,\\s*");
			this.cardBinding = new HashSet<String>(Arrays.asList(items));
		} else {
			this.cardBinding = new HashSet<String>();
		}
	}

	public void addCardToBinding(String card) {
		this.cardBinding.add(card);
	}
}
