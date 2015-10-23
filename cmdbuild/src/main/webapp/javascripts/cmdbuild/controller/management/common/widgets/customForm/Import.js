(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.Import', {
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
			'onCustomFormImportAbortButtonClick',
			'onCustomFormImportUploadButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.import.FormPanel}
		 */
		form: undefined,

		/**
		 * @cfg {Boolean}
		 */
		modeDisabled: false,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.ImportWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.import.ImportWindow', {
				delegate: this,
				modeDisabled: this.modeDisabled
			});

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onCustomFormImportAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Uses importCSV calls to store and get CSV data from server and check if CSV has right fields
		 */
		onCustomFormImportUploadButtonClick: function() {
			if (this.validate(this.form)) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.Csv.decode({
					form: this.form.getForm(),
					scope: this,
					failure: function(form, action) {
						CMDBuild.LoadMask.get().hide();

						CMDBuild.Msg.error(
							CMDBuild.Translation.common.failure,
							CMDBuild.Translation.errors.csvUploadOrDecodeFailure,
							false
						);
					},
					success: function(form, action) {
						var decodedRows = [];

						Ext.Array.forEach(action.result.response.elements, function(rowDataObject, i, allRowDataObjects) {
							if (!Ext.isEmpty(rowDataObject) && !Ext.isEmpty(rowDataObject.entries))
								decodedRows.push(rowDataObject.entries);
						}, this);

						this.cmfg('importData', {
							append: this.form.importModeCombo.getValue() == 'add',
							rowsObjects: decodedRows
						});

						this.onCustomFormImportAbortButtonClick();

						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		}
	});

})();