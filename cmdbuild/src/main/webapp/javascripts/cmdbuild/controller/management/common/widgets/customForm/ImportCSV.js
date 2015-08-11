(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.ImportCSV', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Csv'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onCustomFormImportCSVAbortButtonClick',
			'onCustomFormImportCSVUploadButtonClick'
		],

		/**
		 * @cfg {Number}
		 */
		classId: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.ImportCSVWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 * @param {Number} configurationObject.classId
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.ImportCSVWindow', {
				delegate: this
			});

			this.view.classIdField.setValue(this.classId);

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onCustomFormImportCSVAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onCustomFormImportCSVUploadButtonClick: function() {
			CMDBuild.LoadMask.get().show();
			CMDBuild.core.proxy.Csv.upload({
				form: this.view.csvUploadForm.getForm(),
				scope: this,
				success: function(form, action) {
					CMDBuild.core.proxy.Csv.getRecords({
						scope: this,
						success: function(result, options, decodedResult) {
							this.cmfg('setGridDataFromCsv', { // TODO
								rawData: decodedResult.rows,
								mode: this.view.csvImportModeCombo.getValue()
							});

							this.onCustomFormImportCSVAbortButtonClick();
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