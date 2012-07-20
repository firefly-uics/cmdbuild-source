package org.cmdbuild.services.gis.geoserver.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.utils.Command;
import org.dom4j.Document;
import org.dom4j.XPath;

public class GetLayer extends AbstractGeoCommand implements Command<GeoServerLayer> {

	private final String name;

	private static final Pattern storeNamePattern = java.util.regex.Pattern.compile("/([^/]+)/(featuretype|coverage)s/[^/]+$");

	public static GeoServerLayer exec(final String name) {
		return new GetLayer(name).run();
	}

	private GetLayer(final String name) {
		super();
		this.name = name;
	}

	@Override
	public GeoServerLayer run() {
		final String url = String.format("%s/rest/layers/%s", getGeoServerURL(), name);
		final Document xmlLayer = get(url);
		final String dataStoreName = extractDataStoreName(xmlLayer);
		final String layerDescription = extractMeta(xmlLayer, GeoServerLayer.DESCRIPTION_META);
		final GeoServerLayer l = new GeoServerLayer(name, layerDescription, dataStoreName);
		setIndex(l, xmlLayer);
		setMinZoom(l, xmlLayer);
		setMaxZoom(l, xmlLayer);
		setVisibility(l, xmlLayer);
		return l;
	}

	private String extractDataStoreName(final Document xmlLayer) {
		final XPath xpath = xmlLayer.createXPath("//layer/resource/atom:link/@href");
		xpath.setNamespaceURIs(AbstractGeoCommand.atomNS);
		final String featureTypeUrl = xpath.valueOf(xmlLayer);
		Matcher matcher = storeNamePattern.matcher(featureTypeUrl);
		if (matcher.find()) {
			return matcher.group(1);
		} else {
			return null;
		}
	}

	private void setIndex(GeoServerLayer l, Document xmlLayer) {
		final String meta = extractMeta(xmlLayer, GeoServerLayer.INDEX_META);
		l.setIndex(meta);
	}

	private void setMinZoom(GeoServerLayer l, Document xmlLayer) {
		final String meta = extractMeta(xmlLayer, GeoServerLayer.MIN_ZOOM_META);
		l.setMinZoom(meta);
	}

	private void setMaxZoom(GeoServerLayer l, Document xmlLayer) {
		final String meta = extractMeta(xmlLayer, GeoServerLayer.MAX_ZOOM_META);
		l.setMaxZoom(meta);
	}

	private void setVisibility(GeoServerLayer l, Document xmlLayer) {
		final String meta = extractMeta(xmlLayer, GeoServerLayer.VISIBILITY_META);
		l.setVisibility(meta);
	}

	private String extractMeta(final Document xmlLayer, final String metaName) {
		final String metaXPath = String.format("//layer/metadata/entry[@key=\"%s\"]", metaName);
		return xmlLayer.valueOf(metaXPath);
	}
}
