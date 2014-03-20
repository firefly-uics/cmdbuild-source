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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
	public Iterable<Layer> readLayers() {
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
		return Iterables.transform(out, STORABLE_TO_LOGIC_LAYER);
	}

	@Override
	public void updateBimLayer(final String className, final String attributeName, final String value) {

		final BimDataModelCommandFactory factory = new BimDataModelCommandFactory(bimPersistence, //
				bimDataModelManager);
		final BimDataModelCommand dataModelCommand = factory.create(attributeName);
		dataModelCommand.execute(className, value);
	}

	@Override
	public Layer getRootLayer() {
		final BimLayer rootLayer = bimPersistence.findRoot();
		return STORABLE_TO_LOGIC_LAYER.apply(rootLayer);
	}

	private static final Function<BimLayer, Layer> STORABLE_TO_LOGIC_LAYER = new Function<BimLayer, Layer>() {
		@Override
		public Layer apply(final BimLayer input) {
			return new LayerWrapper(input);
		}
	};

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

	@Override
	public Iterable<Layer> getActiveLayers() {
		Iterable<BimLayer> storedLayers = bimPersistence.listLayers();
		Iterable<BimLayer> filtered = Iterables.filter(storedLayers, ACTIVE_FILTER);
		return Iterables.transform(filtered, STORABLE_TO_LOGIC_LAYER);
	}

	private static final Predicate<BimLayer> ACTIVE_FILTER = new Predicate<BimLayer>() {
		@Override
		public boolean apply(BimLayer input) {
			return input.isActive();
		}
	};

	private static class LayerWrapper implements Layer {

		private final BimLayer delegate;

		public LayerWrapper(final BimLayer delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getClassName() {
			return delegate.getClassName();
		}

		@Override
		public boolean isRoot() {
			return delegate.isRoot();
		}

		@Override
		public boolean isContainer() {
			return delegate.isContainer();
		}

		@Override
		public String getRootReference() {
			return delegate.getRootReference();
		}

		@Override
		public boolean isExport() {
			return delegate.isExport();
		}

		@Override
		public boolean isActive() {
			return delegate.isActive();
		}

		@Override
		public String getDescription() {
			return delegate.getDescription();
		}

	}

}
