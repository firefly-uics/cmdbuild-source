package org.cmdbuild.services.gis.geoserver.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.services.gis.GeoLayer;
import org.cmdbuild.services.gis.geoserver.GeoServerLayer;
import org.cmdbuild.utils.Command;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.io.ReaderInputStream;

public class ModifyLayer extends AbstractGeoCommand implements Command<Void> {

	private final GeoLayer layer;

	public static Void exec(final GeoLayer layer) {
		return new ModifyLayer(layer).run();
	}

	private ModifyLayer(final GeoLayer layer) {
		super();
		this.layer = layer;
	}

	@Override
	public Void run() {
		try {
			final String url = String.format("%s/rest/layers/%s", getGeoServerURL(), layer.getName());
			Reader layerReader = new StringReader(layerToXml(layer).asXML());
			InputStream data = new ReaderInputStream(layerReader, CharacterSet.UTF_8);
			put(data, url, MediaType.TEXT_XML);
		} catch (IOException e) {
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
		return null;
	}

	private Document layerToXml(final GeoLayer layer) {
		Document xmlLayer = DocumentHelper.createDocument();
		Element layerEl = xmlLayer.addElement("layer");
		layerEl.addElement("enabled").addText(Boolean.toString(layer.isEnabled()));
		Element metadataEl = layerEl.addElement("metadata");
		metadataEl.addElement("entry").addAttribute("key", GeoServerLayer.DESCRIPTION_META).addText(layer.getDescription());
		metadataEl.addElement("entry").addAttribute("key", GeoServerLayer.INDEX_META).addText(Integer.toString(layer.getIndex()));
		metadataEl.addElement("entry").addAttribute("key", GeoServerLayer.MIN_ZOOM_META).addText(Integer.toString(layer.getMinZoom()));
		metadataEl.addElement("entry").addAttribute("key", GeoServerLayer.MAX_ZOOM_META).addText(Integer.toString(layer.getMaxZoom()));
		metadataEl.addElement("entry").addAttribute("key", GeoServerLayer.VISIBILITY_META).addText(layer.getVisibilityAsString());
		return xmlLayer;
	}
}
