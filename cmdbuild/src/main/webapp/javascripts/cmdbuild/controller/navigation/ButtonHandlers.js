(function() {

	Ext.define('CMDBuild.controller.navigation.ButtonHandlers', {

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 */
		navigationChronologyButtonHandler: function(record) {
			switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
				case 'class':
					return this.navigationChronologyButtonHandlerClass(record);

				case 'custompage':
					return this.navigationChronologyButtonHandlerDefault(record);

				case 'dashboard':
					return this.navigationChronologyButtonHandlerDefault(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getDataView():
					return this.navigationChronologyButtonHandlerDefault(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getReport():
					return this.navigationChronologyButtonHandlerDefault(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getWorkflow():
					return this.navigationChronologyButtonHandlerWorkflow(record);
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @private
		 */
		navigationChronologyButtonHandlerClass: function(record) {
			if (!Ext.isEmpty(record) && !record.isEmpty()) {
				if (
					!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
				) {
					_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).expand();

					_CMMainViewportController.openCard({
						Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
						IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
						activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
					});

					if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
						_CMMainViewportController.panelControllers[record.get(CMDBuild.core.constants.Proxy.MODULE_ID)].gridController.view.getStore().on('load', function() {
							_CMMainViewportController.viewport.findModuleByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).cardTabPanel.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });

					if (!record.isEmpty([CMDBuild.core.constants.Proxy.SUB_SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
						_CMMainViewportController.panelControllers[record.get(CMDBuild.core.constants.Proxy.MODULE_ID)].gridController.view.getStore().on('load', function() {
							_CMMainViewportController.panelControllers[record.get(CMDBuild.core.constants.Proxy.MODULE_ID)].mdController.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SUB_SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });
				} else if (!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])) {
					_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).expand();
					_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).selectNodeById(
						record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					);
				}
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @private
		 */
		navigationChronologyButtonHandlerDefault: function(record) {
			if (
				!Ext.isEmpty(record) && !record.isEmpty()
				&& !record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
			) {
				_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).expand();
				_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).selectNodeById(
					record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
				);
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @private
		 */
		navigationChronologyButtonHandlerWorkflow: function(record) {
			if (!Ext.isEmpty(record) && !record.isEmpty()) {
				if (
					!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
				) {
					_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).expand();

					_CMMainViewportController.openCard({
						Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
						IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
						activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
					});

					if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
						_CMMainViewportController.panelControllers[record.get(CMDBuild.core.constants.Proxy.MODULE_ID)].gridController.view.getStore().on('load', function() {
							_CMMainViewportController.viewport.findModuleByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).cardTabPanel.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });
				} else if (!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])) {
					_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).expand();
					_CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).selectNodeById(
						record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					);
				}
			}
		}
	});

})();