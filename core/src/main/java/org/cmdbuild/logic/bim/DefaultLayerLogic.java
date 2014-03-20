package org.cmdbuild.logic.bim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.model.bim.BimLayer;
import org.cmdbuild.services.bim.BimDataModelCommand;
import org.cmdbuild.services.bim.BimDataModelCommandFactory;
import org.cmdbuild.services.bim.BimDataModelManager;
import org.cmdbuild.services.bim.BimDataView;
import org.cmdbuild.services.bim.BimPersistence;

public class DefaultLayerLogic implements LayerLogic {

	private final BimPersistence bimPersistence;
	private final BimDataModelManager bimDataModelManager;
	private final BimDataView bimDataView;

	public DefaultLayerLogic( //
			final BimPersistence bimPersistence, //
			final BimDataView bimDataView, //
			final BimDataModelManager bimDataModelManager) {
		this.bimPersistence = bimPersistence;
		this.bimDataView = bimDataView;
		this.bimDataModelManager = bimDataModelManager;
	}

	@Override
	public List<BimLayer> readLayers() {
		final List<BimLayer> out = new LinkedList<BimLayer>();
		final Map<String, BimLayer> storedLayers = bimLayerMap();
		final Iterable<? extends CMClass> allClasses = bimDataView.findClasses();
		for (final CMClass cmdbuildClass : allClasses) {
			if (cmdbuildClass.isSystem() || cmdbuildClass.isBaseClass()) {
				continue;
			}

			final String layerName = cmdbuildClass.getName();
			final String layerDescription = cmdbuildClass.getDescription();

			BimLayer layerToPut = null;
			if (storedLayers.containsKey(layerName)) {
				layerToPut = storedLayers.get(layerName);
			} else {
				layerToPut = new BimLayer(layerName);
			}

			layerToPut.setDescription(layerDescription);
			out.add(layerToPut);
		}

		return out;
	}

	@Override
	public void updateBimLayer(final String className, final String attributeName, final String value) {

		final BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimPersistence, //
				bimDataModelManager);
		final BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	@Override
	public BimLayer getRootLayer() {
		return bimPersistence.findRoot();
	}

	@Override
	public boolean isActive(final String classname) {
		return bimPersistence.isActiveLayer(classname);
	}

	private Map<String, BimLayer> bimLayerMap() {
		final Map<String, BimLayer> out = new HashMap<String, BimLayer>();
		final List<BimLayer> storedLayers = (List<BimLayer>) bimPersistence.listLayers();
		for (final BimLayer layer : storedLayers) {
			out.put(layer.getClassName(), layer);
		}
		return out;
	}

}
