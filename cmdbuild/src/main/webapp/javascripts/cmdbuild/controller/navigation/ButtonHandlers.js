(function() {

	/**
	 * @private
	 */
	Ext.define('CMDBuild.controller.navigation.ButtonHandlers', {

		requires: [
			'CMDBuild.core.constants.Proxy',
		],

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 */
		navigationChronologyButtonHandler: function(record) {
			var accordion = _CMMainViewportController.findAccordionByCMName(record.get(CMDBuild.core.constants.Proxy.MODULE_ID));

			if (!Ext.isEmpty(accordion)) {
				switch (record.get(CMDBuild.core.constants.Proxy.MODULE_ID)) {
					case 'class': {
						this.navigationChronologyButtonHandlerClass(record);
					} break;

					case 'workflow': {
						this.navigationChronologyButtonHandlerWorkflow(record);
					} break;

					// TODO: ...
				}

				accordion.expand();
				accordion.selectNodeById(record.accordionItem);
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 */
		navigationChronologyButtonHandlerClass: function(record) {
			if (!Ext.isEmpty(record) && !record.isEmpty()) {
				if (
					!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
				) {
					_CMMainViewportController.openCard({
						Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
						IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
						activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
					});

					if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
						_CMMainViewportController.panelControllers['class'].gridController.view.getStore().on('load', function() {
							_CMMainViewportController.viewport.findModuleByCMName('class').cardTabPanel.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });

					if (!record.isEmpty([CMDBuild.core.constants.Proxy.SUB_SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
						_CMMainViewportController.panelControllers['class'].gridController.view.getStore().on('load', function() {
							_CMMainViewportController.panelControllers['class'].mdController.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SUB_SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });
				} else if (!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])) {
					_CMMainViewportController.findAccordionByCMName('class').selectNodeById(
						record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					);
				}
			}
		},

		/**
		 * @param {CMDBuild.model.navigation.chronology.Record} record
		 */
		navigationChronologyButtonHandlerWorkflow: function(record) {
			if (!Ext.isEmpty(record) && !record.isEmpty()) {
				if (
					!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					&& !record.isEmpty([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID])
				) {
					_CMMainViewportController.openCard({
						Id: record.get([CMDBuild.core.constants.Proxy.ITEM, CMDBuild.core.constants.Proxy.ID]),
						IdClass: record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID]),
						activateFirstTab: record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							? true : record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
					});

					if (!record.isEmpty([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT]))
						_CMMainViewportController.panelControllers['workflow'].gridController.view.getStore().on('load', function() {
							_CMMainViewportController.viewport.findModuleByCMName('workflow').cardTabPanel.activeTabSet(
								record.get([CMDBuild.core.constants.Proxy.SECTION, CMDBuild.core.constants.Proxy.OBJECT])
							);
						}, this, { single: true });
				} else if (!record.isEmpty([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])) {
					_CMMainViewportController.findAccordionByCMName('workflow').selectNodeById(
						record.get([CMDBuild.core.constants.Proxy.ENTRY_TYPE, CMDBuild.core.constants.Proxy.ID])
					);
				}
			}
		}
	});

})();