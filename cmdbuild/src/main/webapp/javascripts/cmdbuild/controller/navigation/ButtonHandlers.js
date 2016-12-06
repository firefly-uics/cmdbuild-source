(function () {

	Ext.define('CMDBuild.controller.navigation.ButtonHandlers', {

		requires: [
			'CMDBuild.core.constants.ModuleIdentifiers',
			'CMDBuild.core.constants.Proxy'
		],

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {Void}
		 */
		navigationChronologyButtonHandler: function (record) {
			switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
				case 'class':
					return this.buttonHandlerClass(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getCustomPage():
					return this.buttonHandlerDefault(record);

				case 'dashboard':
					return this.buttonHandlerDefault(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getDataView():
					return this.buttonHandlerDefault(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getReport():
					return this.buttonHandlerDefault(record);

				case CMDBuild.core.constants.ModuleIdentifiers.getWorkflow():
					return this.buttonHandlerWorkflow(record);
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buttonHandlerClass: function (record) {
			if (
				Ext.isObject(record) && !Ext.Object.isEmpty(record) && !record.isEmpty()
				&& !record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
				&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
			) {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', {
					Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
					IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
					activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
						? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
				});

				if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).gridController.view.getStore().on('load', function (store, records, successful, eOpts) {
						Ext.callback(function () {
							CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).view.cardTabPanel.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });
					}, this, { single: true });
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buttonHandlerDefault: function (record) {
			if (
				Ext.isObject(record) && !Ext.Object.isEmpty(record) && !record.isEmpty()
				&& !record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
			) {
				var accordionController = CMDBuild.global.controller.MainViewport.cmfg(
					'mainViewportAccordionControllerWithNodeWithIdGet',
					record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
				);

				if (Ext.isObject(accordionController) && !Ext.Object.isEmpty(accordionController) && Ext.isFunction(accordionController.cmfg)) {
					Ext.apply(accordionController, {
						disableSelection: true,
						scope: this,
						callback: function () {
							accordionController.cmfg('accordionDeselect'); // Instruction required or selection doesn't work if exists another selection
							accordionController.cmfg('accordionNodeByIdSelect', { id: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]) });
						}
					});

					accordionController.cmfg('accordionExpand');
				}
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 *
		 * @returns {Void}
		 *
		 * @private
		 */
		buttonHandlerWorkflow: function (record) {
			if (
				Ext.isObject(record) && !Ext.Object.isEmpty(record)  && !record.isEmpty()
				&& !record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
				&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
			) {
				CMDBuild.global.controller.MainViewport.cmfg('mainViewportCardSelect', {
					Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
					IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
					activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
						? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
				});

				if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
					CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).gridController.view.getStore().on('load', function (store, records, successful, eOpts) {
						CMDBuild.global.controller.MainViewport.cmfg('mainViewportModuleControllerGet', record.get(CMDBuild.core.constants.Proxy.MODULE_ID)).view.cardTabPanel.activeTabSet(
							record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
						);
					}, this, { single: true });
			}
		}
	});

})();
