(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.grid.ImportCSV', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Csv'
		],

		/**
		 * @cfg {CMDBuild.controller.management.common.widgets.grid.Main}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onImportCSVAbortButtonClick',
			'onImportCSVUploadButtonClick'
		],

		/**
		 * @cfg {Number}
		 */
		classId: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.grid.ImportCSVWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {CMDBuild.controller.management.common.widgets.grid.Main} configurationObject.parentDelegate
		 * @param {Number} configurationObject.classId
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.grid.ImportCSVWindow', {
				delegate: this
			});

			this.view.classIdField.setValue(this.classId);

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onImportCSVAbortButtonClick: function() {
			this.getView().destroy();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onImportCSVUploadButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.Csv.upload({
				form: this.getView().csvUploadForm.getForm(),
				scope: this,
				success: function(form, action) {
					CMDBuild.core.proxy.Csv.getRecords({
						scope: this,
						success: function(result, options, decodedResult) {
							this.parentDelegate.setGridDataFromCsv(decodedResult.rows); // TODO: to implement AbstractController also in main class

							this.onImportCSVAbortButtonClick();
						}
					});
				},
				failure: function(form, action) {
					CMDBuild.LoadMask.get().hide();

					CMDBuild.Msg.error(
						CMDBuild.Translation.common.failure,
						CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
						false
					);
				}
			});
		}
	});

})();