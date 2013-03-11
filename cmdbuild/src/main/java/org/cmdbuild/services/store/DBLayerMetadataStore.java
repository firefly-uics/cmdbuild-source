package org.cmdbuild.services.store;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.elements.filters.AttributeFilter.AttributeFilterType;
import org.cmdbuild.elements.interfaces.ICard;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.gis.LayerMetadata;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;

public class DBLayerMetadataStore {
	private enum Attributes {
		CARDS_BINDING("CardsBinding"),
		DESCRIPTION("Description"),
		FULL_NAME("FullName"),
		GEO_SERVER_NAME("GeoServerName"),
		INDEX("Index"),
		MINIMUM_ZOOM("MinimumZoom"),
		MAXIMUM_ZOOM("MaximumZoom"),
		MAP_STYLE("MapStyle"),
		NAME("Name"),
		TYPE("Type"),
		VISIBILITY("Visibility");

		private String name;

		Attributes(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private static final ITable table = UserOperations.from(UserContext.systemContext()).tables().get("_Layer");
	private static final String TARGET_TABLE_FORMAT = LayerMetadata.TARGET_TABLE_FORMAT;

	public LayerMetadata createLayer(LayerMetadata layer) {
		ICard c = table.cards().create();
		c.setValue(Attributes.FULL_NAME.getName(), layer.getFullName());
		c.setValue(Attributes.NAME.getName(), layer.getName());
		c.setValue(Attributes.DESCRIPTION.getName(), layer.getDescription());
		c.setValue(Attributes.INDEX.getName(), layer.getIndex());
		c.setValue(Attributes.MINIMUM_ZOOM.getName(), layer.getMinimumZoom());
		c.setValue(Attributes.MAXIMUM_ZOOM.getName(), layer.getMaximumzoom());
		c.setValue(Attributes.MAP_STYLE.getName(), layer.getMapStyle());
		c.setValue(Attributes.TYPE.getName(), layer.getType());
		c.setValue(Attributes.INDEX.getName(), getMaxIndex() + 1);
		c.setValue(Attributes.VISIBILITY.getName(), layer.getVisibilityAsString());
		c.setValue(Attributes.GEO_SERVER_NAME.getName(), layer.getGeoServerName());
		c.setValue(Attributes.CARDS_BINDING.getName(), layer.getCardBindingAsString());
		c.save();

		return cardToLayerMetadata(c);
	}

	public LayerMetadata get(String fullName) {
		ICard card = getLayerMetadataCard(fullName);
		return cardToLayerMetadata(card);
	}

	public LayerMetadata updateLayer(String fullName, LayerMetadata changes) {
		ICard c = getLayerMetadataCard(fullName);
		c.setValue(Attributes.DESCRIPTION.getName(), changes.getDescription());
		c.setValue(Attributes.MINIMUM_ZOOM.getName(), changes.getMinimumZoom());
		c.setValue(Attributes.MAXIMUM_ZOOM.getName(), changes.getMaximumzoom());
		c.setValue(Attributes.MAP_STYLE.getName(), changes.getMapStyle());
		c.setValue(Attributes.CARDS_BINDING.getName(), changes.getCardBindingAsString());
		c.save();

		return cardToLayerMetadata(c);
	}

	public void updateLayerVisibility(String fullName, String tableName, boolean isVisible) {
		ICard card = getLayerMetadataCard(fullName);
		LayerMetadata layer = cardToLayerMetadata(card);
		if (isVisible) {
			layer.addVisibility(tableName);
		} else {
			layer.removeVisibility(tableName);
		}

		card.setValue(Attributes.VISIBILITY.getName(), layer.getVisibilityAsString());
		card.save();
	}

	public void setLayerIndex(LayerMetadata layerMetadata, int index) {
		ICard c = getLayerMetadataCard(layerMetadata.getFullName());
		c.setValue(Attributes.INDEX.getName(), index);
		c.save();
	}

	public void deleteLayer(String fullName) {
		ICard layerCard = getLayerMetadataCard(fullName);
		layerCard.delete();
	}

	public List<LayerMetadata> list() {
		List<LayerMetadata> layers = new LinkedList<LayerMetadata>();
		for (ICard c:table.cards().list()) {
			layers.add(cardToLayerMetadata(c));
		}

		return layers;
	}

	public List<LayerMetadata> list(ITable masterTable) {
		return list(table.getName());
	}

	public List<LayerMetadata> list(String tableName) {
		List<LayerMetadata> layers = new LinkedList<LayerMetadata>();
		for (ICard c:table.cards().list().filter(Attributes.FULL_NAME.getName(), AttributeFilterType.BEGIN, String.format(TARGET_TABLE_FORMAT, tableName))) {
			layers.add(cardToLayerMetadata(c));
		}

		return layers;
	}

	private LayerMetadata cardToLayerMetadata(ICard card) {
		LayerMetadata layer = new LayerMetadata();
		layer.setDescription((String) card.getValue(Attributes.DESCRIPTION.getName()));
		layer.setFullName((String) card.getValue(Attributes.FULL_NAME.getName()));
		layer.setIndex((Integer) card.getValue(Attributes.INDEX.getName()));
		layer.setMinimumZoom((Integer) card.getValue(Attributes.MINIMUM_ZOOM.getName()));
		layer.setMaximumzoom((Integer) card.getValue(Attributes.MAXIMUM_ZOOM.getName()));
		layer.setMapStyle((String) card.getValue(Attributes.MAP_STYLE.getName()));
		layer.setName((String) card.getValue(Attributes.NAME.getName()));
		layer.setType((String) card.getValue(Attributes.TYPE.getName()));
		layer.setVisibilityFromString((String) card.getValue(Attributes.VISIBILITY.getName()));
		layer.setGeoServerName((String) card.getValue(Attributes.GEO_SERVER_NAME.getName()));
		layer.setCardBindingFromString((String) card.getValue(Attributes.CARDS_BINDING.getName()));

		return layer;
	}

	private ICard getLayerMetadataCard(String fullName) throws ORMException {
		Iterator<ICard> iterator = table.cards().list().filter(Attributes.FULL_NAME.getName(), AttributeFilterType.EQUALS, fullName).iterator();

		if (iterator.hasNext()) {
			return iterator.next();
		} else {
			Log.PERSISTENCE.debug("The layer " + fullName + " was not found");
			throw ORMExceptionType.ORM_ERROR_CARD_SELECT.createException();
		}
	}

	private Integer getMaxIndex() {
		Integer max = new Integer(0);

		for (ICard c:table.cards().list()) {
			Integer index = (Integer) c.getValue(Attributes.INDEX.getName());
			if (index > max) {
				max = index;
			}
		}

		return max;
	}
}