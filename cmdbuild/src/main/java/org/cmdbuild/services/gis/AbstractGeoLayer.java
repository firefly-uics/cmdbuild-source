package org.cmdbuild.services.gis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.cmdbuild.elements.interfaces.ITable;

public abstract class AbstractGeoLayer implements GeoLayer {

	private static final int DEFAULT_INDEX = -1;
	private static final int DEFAULT_MIN_ZOOM = 0;
	private static final int DEFAULT_MAX_ZOOM = 25;
	private static final String EMPTY_VISIBILITY = null;

	private final String name;
	private int index;
	private int minZoom;
	private int maxZoom;
	private Set<String> visibility;

	protected AbstractGeoLayer(String name) {
		this.name = name;
		setIndex(DEFAULT_INDEX);
		setMaxZoom(DEFAULT_MAX_ZOOM);
		setMinZoom(DEFAULT_MIN_ZOOM);
		setVisibility(EMPTY_VISIBILITY);
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public final int getIndex() {
		return index;
	}

	@Override
	public final int getMaxZoom() {
		return maxZoom;
	}

	@Override
	public final int getMinZoom() {
		return minZoom;
	}

	public final void setIndex(int index) {
		this.index = index;
	}

	@Override
	public final void setMaxZoom(int maxZoom) {
		this.maxZoom = maxZoom;
	}

	@Override
	public final void setMinZoom(int minZoom) {
		this.minZoom = minZoom;
	}

	public void setIndex(final String index) {
		try {
			setIndex(Integer.valueOf(index));
		} catch (Exception e) {
			setIndex(DEFAULT_INDEX);
		}
	}

	@Override
	public void setMaxZoom(final String maxZoom) {
		try {
			setMaxZoom(Integer.valueOf(maxZoom));
		} catch (Exception e) {
			setMaxZoom(DEFAULT_MAX_ZOOM);
		}
	}

	@Override
	public void setMinZoom(final String minZoom) {
		try {
			setMinZoom(Integer.valueOf(minZoom));
		} catch (Exception e) {
			setMinZoom(DEFAULT_MIN_ZOOM);
		}
	}

	@Override
	public void addVisibility(ITable table) {
		visibility.add(table.getName());
	}

	@Override
	public void removeVisibility(ITable table) {
		visibility.remove(table.getName());
	}

	@Override
	public boolean isVisible(ITable table) {
		return visibility.contains(table.getName());
	}

	@Override
	public void setVisibility(ITable table, boolean visible) {
		if (visible) {
			addVisibility(table);
		} else {
			removeVisibility(table);
		}
	}

	@Override
	public final String getVisibilityAsString() {
		return StringUtils.join(visibility, ',');
	}

	public final void setVisibility(String csString) {
		visibility = new HashSet<String>();
		if (csString != null && !csString.isEmpty()) {
			visibility.addAll(Arrays.asList(csString.split(",")));
		}
	}
}
