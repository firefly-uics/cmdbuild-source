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
			this.getStore().getRootNode().removeAll();

			if (!this.isSectionDisabled(CMDBuild.core.constants.Proxy.CHANGE_PASSWORD))
				this.getStore().getRootNode().appendChild({
					text: CMDBuild.Translation.management.modutilities.changepassword.title,
					cmName: 'changepassword',
					leaf: true
				});

			if (!this.isSectionDisabled('bulkupdate'))
				this.getStore().getRootNode().appendChild({
					text: CMDBuild.Translation.management.modutilities.bulkupdate.title,
					cmName: 'bulkcardupdate',
					leaf: true
				});

			if (!this.isSectionDisabled('importcsv'))
				this.getStore().getRootNode().appendChild([
					{
						text: CMDBuild.Translation.management.modutilities.csv.title,
						cmName: 'importcsv',
						leaf: true
					}
				]);

			if (!this.isSectionDisabled('exportcsv'))
				this.getStore().getRootNode().appendChild({
					text: CMDBuild.Translation.management.modutilities.csv.title_export,
					cmName: 'exportcsv',
					leaf: true
				});

			this.callParent(arguments);
		},

		/**
		 * @param {String} moduleName
		 *
		 * @returns {Boolean}
		 */
		isSectionDisabled: function(moduleName) {
			switch (moduleName) {
				case 'changePassword':
					return !CMDBuild.Runtime.CanChangePassword;

				default:
					return CMDBuild.configuration.userInterface.isDisabledModule(moduleName);
			}
		}
	});

})();