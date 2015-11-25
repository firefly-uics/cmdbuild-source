(function() {

	Ext.define('CMDBuild.view.management.accordion.Utility', {
		extend: 'CMDBuild.view.common.AbstractAccordion',

		/**
		 * @cfg {CMDBuild.controller.common.AbstractAccordionController}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		cmName: undefined,

		/**
		 * @cfg {Boolean}
		 */
		hideIfEmpty: true,

		title: CMDBuild.Translation.management.modutilities.title,

		/**
		 * @param {Number} nodeIdToSelect
		 *
		 * @override
		 */
		updateStore: function(nodeIdToSelect) {
			var nodes = [];

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.CHANGE_PASSWORD))
				nodes.push({
					cmName: 'changepassword',
					text: CMDBuild.Translation.management.modutilities.changepassword.title,
					description: CMDBuild.Translation.management.modutilities.changepassword.title,
					id: this.delegate.cmfg('accordionBuildId', { components: 'changepassword' }),
					sectionHierarchy: ['changepassword'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.BULK_UPDATE))
				nodes.push({
					cmName: 'bulkcardupdate',
					text: CMDBuild.Translation.management.modutilities.bulkupdate.title,
					description: CMDBuild.Translation.management.modutilities.bulkupdate.title,
					id: this.delegate.cmfg('accordionBuildId', { components: 'bulkcardupdate' }),
					sectionHierarchy: ['bulkcardupdate'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.IMPORT_CSV))
				nodes.push({
					cmName: 'importcsv',
					text: CMDBuild.Translation.management.modutilities.csv.title,
					description: CMDBuild.Translation.management.modutilities.csv.title,
					id: this.delegate.cmfg('accordionBuildId', { components: 'importcsv' }),
					sectionHierarchy: ['importcsv'],
					leaf: true
				});

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.EXPORT_CSV))
				nodes.push({
					cmName: 'exportcsv',
					text: CMDBuild.Translation.management.modutilities.csv.title_export,
					description: CMDBuild.Translation.management.modutilities.csv.title_export,
					id: this.delegate.cmfg('accordionBuildId', { components: 'exportcsv' }),
					sectionHierarchy: ['exportcsv'],
					leaf: true
				});

			this.getStore().getRootNode().removeAll();
			this.getStore().getRootNode().appendChild(nodes);

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
					return !CMDBuild.Runtime.CanChangePassword;

				default:
					return CMDBuild.configuration.userInterface.isDisabledModule(moduleName);
			}
		}
	});

})();