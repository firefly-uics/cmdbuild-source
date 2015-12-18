(function () {

	Ext.define('CMDBuild.controller.management.common.widgets.customForm.Export', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.widget.CustomForm'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onWidgetCustomFormExportAbortButtonClick',
			'onWidgetCustomFormExportExportButtonClick'
		],

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.export.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.management.common.widgets.customForm.export.ExportWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.common.widgets.customForm.export.ExportWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onWidgetCustomFormExportAbortButtonClick: function() {
			this.view.destroy();
		},

		/**
		 * Uses exportCSV calls to build and download file
		 */
		onWidgetCustomFormExportExportButtonClick: function() {
			if (this.validate(this.form)) {
				var params = this.form.getData();
				params[CMDBuild.core.constants.Proxy.DATA] = Ext.encode(this.cmfg('widgetCustomFormDataGet'));
				params[CMDBuild.core.constants.Proxy.HEADERS] = Ext.encode(params[CMDBuild.core.constants.Proxy.HEADERS].split(','));

				CMDBuild.core.proxy.widget.CustomForm.exports({ params: params });

				this.cmfg('onWidgetCustomFormExportAbortButtonClick');
			}
		}
	});

})();