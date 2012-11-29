package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.utils.Command;

public class DeleteFeatureTypeOrCoverage extends AbstractGeoCommand implements Command<Void> {

	private final GeoServerLayer layer;
	private final GeoServerStore store;

	public static Void exec(final GeoServerLayer layer, final GeoServerStore store) {
		return new DeleteFeatureTypeOrCoverage(layer, store).run();
	}

	private DeleteFeatureTypeOrCoverage(final GeoServerLayer layer, final GeoServerStore store) {
		super();
		this.layer = layer;
		this.store = store;
	}

	@Override
	public Void run() {
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s/%ss/%s",
				getGeoServerURL(), getGeoServerWorkspace(),
				store.getStoreType().toLowerCase(), store.getName(),
				store.getStoreSubtype().toLowerCase(), layer.getName());
		delete(url);
		return null;
	}
}
