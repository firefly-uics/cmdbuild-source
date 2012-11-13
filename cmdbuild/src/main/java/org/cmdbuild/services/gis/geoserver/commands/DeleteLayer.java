package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.services.gis.GeoLayer;
import org.cmdbuild.utils.Command;

public class DeleteLayer extends AbstractGeoCommand implements Command<Void> {

	private final GeoLayer layer;

	public static Void exec(final GeoLayer layer) {
		return new DeleteLayer(layer).run();
	}

	private DeleteLayer(final GeoLayer layer) {
		super();
		this.layer = layer;
	}

	@Override
	public Void run() {
		final String url = String.format("%s/rest/layers/%s", getGeoServerURL(), layer.getName());
		delete(url);
		return null;
	}
}
