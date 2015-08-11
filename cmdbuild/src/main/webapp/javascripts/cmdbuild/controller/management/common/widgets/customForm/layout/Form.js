(function() {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.layout.Form', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.Message'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onCustomFormLayoutFormCSVImportButtonClick',
//			'onAddRowButtonClick' ,
//			'onCSVImportButtonClick',
//			'onDeleteRowButtonClick',
//			'onEditRowButtonClick',
//			'setGridDataFromCsv'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.layout.FormPanel}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.layout.FormPanel', {
				delegate: this
			});
		},

		/**
		 * @returns {Array}
		 */
		getData: function() { // TODO: implementation
			return this.view.getRecord();
		},

		/**
		 * Opens importCSV configuration pop-up window
		 */
		onCustomFormLayoutFormCSVImportButtonClick: function() {
			Ext.create('CMDBuild.controller.management.common.widgets.customForm.ImportCSV', {
				parentDelegate: this,
				classId: this.classType.get(CMDBuild.core.proxy.CMProxyConstants.ID) // TODO: why??? Should be deleted??
			});
		},

		/**
		 * @param {Object} data // TODO: edit with record's model
		 */
		setData: function(data) { // TODO: implementation
			return this.view.loadRecord(data);
		}
	});

})();