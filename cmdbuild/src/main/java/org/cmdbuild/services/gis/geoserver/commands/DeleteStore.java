package org.cmdbuild.services.gis.geoserver.commands;

import org.cmdbuild.services.gis.geoserver.GeoServerStore;
import org.cmdbuild.utils.Command;

public class DeleteStore extends AbstractGeoCommand implements Command<Void> {

	private final GeoServerStore store;

	public static Void exec(final GeoServerStore store) {
		return new DeleteStore(store).run();
	}

	private DeleteStore(final GeoServerStore store) {
		super();
		this.store = store;
	}

	@Override
	public Void run() {
		final String url = String.format("%s/rest/workspaces/%s/%ss/%s",
				getGeoServerURL(), getGeoServerWorkspace(), store.getStoreType().toLowerCase(), store.getName());
		delete(url);
		return null;
	}
}