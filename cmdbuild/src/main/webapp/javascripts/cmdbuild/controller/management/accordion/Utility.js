(function() {

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
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.accordion.Utility', { delegate: this });

			this.cmfg('accordionUpdateStore');
		},

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		accordionUpdateStore: function(nodeIdToSelect) {
			nodeIdToSelect = Ext.isNumber(nodeIdToSelect) ? nodeIdToSelect : null;

			var nodes = [];

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.CHANGE_PASSWORD))
				nodes.push({
					cmName: 'changepassword',
					text: CMDBuild.Translation.management.modutilities.changepassword.title,
					description: CMDBuild.Translation.management.modutilities.changepassword.title,
					id: this.cmfg('accordionBuildId', { components: 'changepassword' }),
					sectionHierarchy: ['changepassword'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.BULK_UPDATE))
				nodes.push({
					cmName: 'bulkcardupdate',
					text: CMDBuild.Translation.management.modutilities.bulkupdate.title,
					description: CMDBuild.Translation.management.modutilities.bulkupdate.title,
					id: this.cmfg('accordionBuildId', { components: 'bulkcardupdate' }),
					sectionHierarchy: ['bulkcardupdate'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.IMPORT_CSV))
				nodes.push({
					cmName: 'importcsv',
					text: CMDBuild.Translation.management.modutilities.csv.title,
					description: CMDBuild.Translation.management.modutilities.csv.title,
					id: this.cmfg('accordionBuildId', { components: 'importcsv' }),
					sectionHierarchy: ['importcsv'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.EXPORT_CSV))
				nodes.push({
					cmName: 'exportcsv',
					text: CMDBuild.Translation.management.modutilities.csv.title_export,
					description: CMDBuild.Translation.management.modutilities.csv.title_export,
					id: this.cmfg('accordionBuildId', { components: 'exportcsv' }),
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
		isSectionDisabled: function(moduleName) {
			switch (moduleName) {
				case CMDBuild.core.constants.Proxy.CHANGE_PASSWORD:
					return !CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.ALLOW_PASSWORD_CHANGE);

				default:
					return CMDBuild.configuration.userInterface.isDisabledModule(moduleName);
			}
		}
	});

})();