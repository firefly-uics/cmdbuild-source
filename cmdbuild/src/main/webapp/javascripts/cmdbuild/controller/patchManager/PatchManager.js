(function() {

	Ext.define('CMDBuild.controller.patchManager.PatchManager', {
		extend: 'CMDBuild.controller.common.abstract.Base',

		requires: ['CMDBuild.core.proxy.PatchManager'],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onPatchManagerApplyButtonClick'
		],

		/**
		 * @property {CMDBuild.view.patchManager.GridPanel}
		 */
		grid: undefined,

		/**
		 * @property {CMDBuild.view.patchManager.PatchManagerViewport}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.patchManager.PatchManagerViewport', { delegate: this });

			// Shorthands
			this.grid = this.view.gridContainer.grid;
		},

		onPatchManagerApplyButtonClick: function() {
			CMDBuild.core.proxy.PatchManager.update({
				scope: this,
				failure: function(response, options, decodedResponse) {
					this.grid.getStore().load();
				},
				success: function(response, options, decodedResponse) {
					window.location = 'management.jsp';
				}
			});
		}
	});

})();