(function () {

	Ext.define('CMDBuild.controller.management.report.Parameters', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onReportParametersWindowAbortButtonClick',
			'onReportParametersWindowSaveButtonClick'
		],

		/**
		 * @cfg {Array}
		 */
		attributeList: undefined,

		/**
		 * @cfg {Boolean}
		 */
		forceDownload: undefined,

		/**
		 * @property {CMDBuild.view.management.report.ParametersWindow} emailWindows
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.report.ParametersWindow', {
				delegate: this
			});

			// Show window
			if (!Ext.isEmpty(this.view)) {
				this.buildFields();

				this.view.show();
			}
		},

		buildFields: function() {
			if (this.attributeList.length > 0)
				Ext.Array.forEach(this.attributeList, function(attribute, index, allAttributes) {
					var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

					if (!Ext.isEmpty(field)) {
						if (attribute.defaultvalue)
							field.setValue(attribute.defaultvalue);

						this.view.form.add(field);
					}
				}, this);
		},

		onReportParametersWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onReportParametersWindowSaveButtonClick: function() {
			if (this.view.form.getForm().isValid()) {
				CMDBuild.core.proxy.Report.updateReport({
					form: this.view.form.getForm(),
					scope: this,
					failure: function(response, options, decodedResponse) {
						this.onReportParametersWindowAbortButtonClick();
					},
					success: function(response, options) {
						this.cmfg('showReport');

						this.onReportParametersWindowAbortButtonClick();
					}
				});
			}
		}
	});

})();