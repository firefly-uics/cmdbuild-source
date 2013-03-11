package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.utils.Command;

public class DeleteLayer extends AbstractGeoCommand implements Command<Void> {

	private final LayerMetadata layer;

	public static Void exec(final LayerMetadata layer) {
		return new DeleteLayer(layer).run();
	}

	private DeleteLayer(final LayerMetadata layer) {
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
