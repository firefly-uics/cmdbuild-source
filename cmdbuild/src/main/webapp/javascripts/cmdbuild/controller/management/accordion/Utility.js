(function () {

	Ext.define('CMDBuild.controller.management.accordion.Utility', {
		extend: 'CMDBuild.controller.common.abstract.Accordion',

		/**
		 * @cfg {CMDBuild.controller.common.MainViewport}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		/**
		 * @cfg {String}
		 */
		identifier: undefined,

		/**
		 * @property {CMDBuild.view.management.accordion.Utility}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 *
		 * @override
		 */
		constructor: function (configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.accordion.Utility', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function (nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			var nodes = [];

//			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.CHANGE_PASSWORD))
				nodes.push({
//					cmName: 'changepassword',
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.changePassword,
					description: CMDBuild.Translation.changePassword,
					id: this.cmfg('accordionBuildId', 'changepassword'),
					sectionHierarchy: ['changepassword'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.BULK_UPDATE))
				nodes.push({
//					cmName: 'bulkcardupdate',
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.multipleUpdate,
					description: CMDBuild.Translation.multipleUpdate,
					id: this.cmfg('accordionBuildId', 'bulkcardupdate'),
					sectionHierarchy: ['bulkcardupdate'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.IMPORT_CSV))
				nodes.push({
//					cmName: 'importcsv',
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.importCsvFile,
					description: CMDBuild.Translation.importCsvFile,
					id: this.cmfg('accordionBuildId', 'importcsv'),
					sectionHierarchy: ['importcsv'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.EXPORT_CSV))
				nodes.push({
//					cmName: 'exportcsv',
					cmName: 'utility',
					iconCls: 'cmdb-tree-utility-icon',
					text: CMDBuild.Translation.exportCsvFile,
					description: CMDBuild.Translation.exportCsvFile,
					id: this.cmfg('accordionBuildId', 'exportcsv'),
					sectionHierarchy: ['exportcsv'],
					leaf: true
				});

			if (!Ext.isEmpty(nodes)) {
				this.view.getStore().getRootNode().removeAll();
				this.view.getStore().getRootNode().appendChild(nodes);
			}

			// Alias of this.callParent(arguments), inside proxy function doesn't work
			this.updateStoreCommonEndpoint(nodeIdToSelect);

			this.callParent(arguments);
		},

		/**
		 * @param {String} moduleName
		 *
		 * @returns {Boolean}
		 */
		isSectionDisabled: function (moduleName) {
			switch (moduleName) {
				case CMDBuild.core.constants.Proxy.CHANGE_PASSWORD:
					return !CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE);

				default:
					return CMDBuild.configuration.userInterface.isDisabledModule(moduleName);
			}
		},

		/**
		 * Avoid to try to bring on front utility related module, that doesn't exists
		 *
		 * TODO: waiting for refactor
		 *
		 * @override
		 */
		onAccordionExpand: function () {
			// Update store
			if (!this.disableStoreLoad) {
				if (this.view.getSelectionModel().hasSelection()) {
					var selection = this.view.getSelectionModel().getSelection()[0];

					this.cmfg('accordionUpdateStore', selection.get(CMDBuild.core.constants.Proxy.ENTITY_ID));
				} else {
					this.cmfg('accordionUpdateStore');
				}
			}

			// DisableStoreLoad flag reset
			this.disableStoreLoad = false;
		}
	});

})();
