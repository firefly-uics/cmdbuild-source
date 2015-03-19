(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.ImportCSV', {
		extend: 'CMDBuild.controller.common.CMBasePanelController',

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Number}
		 */
		classId: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.ImportCSVWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configObject
		 * @param {CMDBuild.controller.management.common.widgets.grid.Main} configObject.parentDelegate
		 * @param {Number} configObject.classId
		 */
		constructor: function(configObject) {
			Ext.apply(this, configObject); // Apply config

			this.view = Ext.create('CMDBuild.view.management.common.widgets.grid.ImportCSVWindow', {
				delegate: this
			});

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		/**
		 * Gatherer function to catch events
		 *
		 * @param {String} name
		 * @param {Object} param
		 * @param {Function} callback
		 */
		cmOn: function(name, param, callBack) {
			switch (name) {
				case 'onImportCSVAbortButtonClick':
					return this.onImportCSVAbortButtonClick();

				case 'onImportCSVUploadButtonClick':
					return this.onImportCSVUploadButtonClick();

				default: {
					if (!Ext.isEmpty(this.parentDelegate))
						return this.parentDelegate.cmOn(name, param, callBack);
				}
			}
		},

		onImportCSVAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onImportCSVUploadButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.widgets.Grid.uploadCsv({
				form: this.view.csvUploadForm.getForm(),
				scope: this,
				success: function(response, options) {
					CMDBuild.core.proxy.widgets.Grid.getCsvRecords({
						scope: this,
						success: function(result, options, decodedResult) {
							this.parentDelegate.setGridDataFromCsv(decodedResult.rows);

							this.onImportCSVAbortButtonClick();
						}
					});
				},
				failure: function() {
					CMDBuild.LoadMask.get().hide();
				}
			});
		}
	});

})();