package org.cmdbuild.services.bim;

public class BimDataModelCommandFactory {

	private final BimPersistence dataPersistence;
	private final BimDataModelManager dataModelManager;

	public BimDataModelCommandFactory(BimPersistence dataPersistence, BimDataModelManager dataModelManager) {
		this.dataPersistence = dataPersistence;
		this.dataModelManager = dataModelManager;
	}

	public BimDataModelCommand create(String attributeName) {
		return MapperInfoUpdater.of(attributeName).create(dataPersistence, dataModelManager);
	}

	private static enum MapperInfoUpdater {
		active {
			@Override
			public BimDataModelCommand create(BimPersistence dataPersistence, BimDataModelManager dataModelManager) {
				return new BimActiveCommand(dataPersistence, dataModelManager);
			}
		}, //
		root {
			@Override
			public BimDataModelCommand create(BimPersistence bimDataPersistence,
					BimDataModelManager dataModelManager) {
				return new BimRootCommand(bimDataPersistence, dataModelManager);
			}
		}, //
		export{
			@Override
			public BimDataModelCommand create(BimPersistence bimDataPersistence,
					BimDataModelManager dataModelManager) {
				return new BimExportCommand(bimDataPersistence, dataModelManager);
			}
		},//
		container{
			@Override
			public BimDataModelCommand create(BimPersistence bimDataPersistence,
					BimDataModelManager dataModelManager) {
				return new BimContainerCommand(bimDataPersistence, dataModelManager);
			}
		},//
		unknown {
			@Override
			public BimDataModelCommand create(BimPersistence bimDataPersistence,
					BimDataModelManager dataModelManager) {
				return new BimDataModelCommand(bimDataPersistence, dataModelManager) {

					@Override
					public void execute(String className, String value) {
						// TODO Auto-generated method stub
					}
				};
			}
		}, //
		;

		public static MapperInfoUpdater of(final String attributeName) {
			for (final MapperInfoUpdater attribute : values()) {
				if (attribute.name().equals(attributeName)) {
					return attribute;
				}
			}
			return unknown;
		}

		public abstract BimDataModelCommand create(BimPersistence dataPersistence,
				BimDataModelManager dataModelManager);
	}

}