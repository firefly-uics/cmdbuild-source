package org.cmdbuild.services.store;

import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.LinkedList;
import java.util.List;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.exception.ORMException;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.model.gis.LayerMetadata;

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

	private static final String TARGET_TABLE_FORMAT = LayerMetadata.TARGET_TABLE_FORMAT;
	private static final String LAYER_TABLE_NAME = "_Layer";

	private CMClass layerTable = null;
	private CMDataView dataView = null;

	public DBLayerMetadataStore(CMDataView dataView) {
		this.dataView = dataView;
	}

	public LayerMetadata createLayer(LayerMetadata layer) {

		final CMCard card = dataView.createCardFor(getLayerTable())
			.set(Attributes.FULL_NAME.getName(), layer.getFullName())
			.set(Attributes.NAME.getName(), layer.getName())
			.set(Attributes.DESCRIPTION.getName(), layer.getDescription())
			.set(Attributes.INDEX.getName(), layer.getIndex())
			.set(Attributes.MINIMUM_ZOOM.getName(), layer.getMinimumZoom())
			.set(Attributes.MAXIMUM_ZOOM.getName(), layer.getMaximumzoom())
			.set(Attributes.MAP_STYLE.getName(), layer.getMapStyle())
			.set(Attributes.TYPE.getName(), layer.getType())
			.set(Attributes.INDEX.getName(), getMaxIndex() + 1)
			.set(Attributes.VISIBILITY.getName(), layer.getVisibilityAsString())
			.set(Attributes.GEO_SERVER_NAME.getName(), layer.getGeoServerName())
			.set(Attributes.CARDS_BINDING.getName(), layer.getCardBindingAsString())
			.save();

		return cardToLayerMetadata(card);
	}

	public LayerMetadata get(String fullName) {
		CMCard card = getLayerMetadataCard(fullName);
		return cardToLayerMetadata(card);
	}

	public LayerMetadata updateLayer(String fullName, LayerMetadata changes) {
		CMCard cardToUpdate = getLayerMetadataCard(fullName);
		CMCard updatedCard = dataView.update(cardToUpdate)
		.set(Attributes.DESCRIPTION.getName(), changes.getDescription())
		.set(Attributes.MINIMUM_ZOOM.getName(), changes.getMinimumZoom())
		.set(Attributes.MAXIMUM_ZOOM.getName(), changes.getMaximumzoom())
		.set(Attributes.MAP_STYLE.getName(), changes.getMapStyle())
		.set(Attributes.CARDS_BINDING.getName(), changes.getCardBindingAsString())
		.save();

		return cardToLayerMetadata(updatedCard);
	}

	public void updateLayerVisibility(String fullName, String tableName, boolean isVisible) {
		CMCard cardToUpdate = getLayerMetadataCard(fullName);
		LayerMetadata layer = cardToLayerMetadata(cardToUpdate);

		if (isVisible) {
			layer.addVisibility(tableName);
		} else {
			layer.removeVisibility(tableName);
		}

		dataView.update(cardToUpdate)
			.set(Attributes.VISIBILITY.getName(), layer.getVisibilityAsString())
			.save();
	}

	public void setLayerIndex(LayerMetadata layerMetadata, int index) {
		CMCard cardToUpdate = getLayerMetadataCard(layerMetadata.getFullName());
		dataView.update(cardToUpdate)
			.set(Attributes.INDEX.getName(), index)
			.save();
	}

	public void deleteLayer(String fullName) {
		CMCard layerCard = getLayerMetadataCard(fullName);
		dataView.delete(layerCard);
	}

	public List<LayerMetadata> list() {
		List<LayerMetadata> layers = new LinkedList<LayerMetadata>();

		for (CMQueryRow layerAsQueryRow: getLayersAsQueryResult()) {
			layers.add(cardToLayerMetadata(layerAsQueryRow.getCard(getLayerTable())));
		}

		return layers;
	}

	public List<LayerMetadata> list(String tableName) {
		List<LayerMetadata> layers = new LinkedList<LayerMetadata>();
		CMQueryResult layersAsQueryResutl = dataView.select(anyAttribute(getLayerTable())) //
				.from(getLayerTable()) //
				.where(condition( //
						attribute(getLayerTable(), Attributes.FULL_NAME.getName()),
							beginsWith(String.format(TARGET_TABLE_FORMAT, tableName)) //
						)//
					) //
				.run();

		for (CMQueryRow layerAsQueryRow: layersAsQueryResutl) {
			layers.add(cardToLayerMetadata(layerAsQueryRow.getCard(getLayerTable())));
		}

		return layers;
	}

	private LayerMetadata cardToLayerMetadata(CMCard card) {
		LayerMetadata layer = new LayerMetadata();
		layer.setDescription((String) card.get(Attributes.DESCRIPTION.getName()));
		layer.setFullName((String) card.get(Attributes.FULL_NAME.getName()));
		layer.setIndex((Integer) card.get(Attributes.INDEX.getName()));
		layer.setMinimumZoom((Integer) card.get(Attributes.MINIMUM_ZOOM.getName()));
		layer.setMaximumzoom((Integer) card.get(Attributes.MAXIMUM_ZOOM.getName()));
		layer.setMapStyle((String) card.get(Attributes.MAP_STYLE.getName()));
		layer.setName((String) card.get(Attributes.NAME.getName()));
		layer.setType((String) card.get(Attributes.TYPE.getName()));
		layer.setVisibilityFromString((String) card.get(Attributes.VISIBILITY.getName()));
		layer.setGeoServerName((String) card.get(Attributes.GEO_SERVER_NAME.getName()));
		layer.setCardBindingFromString((String) card.get(Attributes.CARDS_BINDING.getName()));

		return layer;
	}

	private CMClass getLayerTable() {
		if (layerTable == null) {
			layerTable = dataView.findClass(LAYER_TABLE_NAME);
		}

		return layerTable;
	}

	private CMQueryResult getLayersAsQueryResult() {
		return dataView.select(anyAttribute(getLayerTable())) //
		.from(getLayerTable()) //
		.run();
	}

	private CMCard getLayerMetadataCard(String fullName) throws ORMException {

		CMQueryRow queryRow = dataView.select(anyAttribute(getLayerTable())) //
				.from(getLayerTable()) //
				.where(condition(attribute(getLayerTable(), Attributes.FULL_NAME.getName()), eq(fullName))) //
				.run().getOnlyRow();

		if (queryRow != null) {
			return queryRow.getCard(getLayerTable());
		} else {
			Log.PERSISTENCE.debug("The layer " + fullName + " was not found");
			throw ORMExceptionType.ORM_ERROR_CARD_SELECT.createException();
		}
	}

	private Integer getMaxIndex() {
		Integer max = new Integer(0);

		CMQueryResult layersRawData = getLayersAsQueryResult();

		for (CMQueryRow layerQueryRow: layersRawData) {
			CMCard layerCard = layerQueryRow.getCard(getLayerTable());
			Integer index = layerCard.get(Attributes.INDEX.getName(), Integer.class);
			if (index > max) {
				max = index;
			}
		}

		return max;
	}
}