(function () {

	Ext.define('CMDBuild.controller.management.report.Parameters', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
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
		attributeList: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onParametersAbortButtonClick',
			'onParametersPrintButtonClick'
		],

		/**
		 * @cfg {Boolean}
		 */
		forceDownload: false,

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
				this.view.show();

				this.buildFields();
			}
		},

		buildFields: function() {
			if (this.attributeList.length > 0)
				Ext.Array.forEach(this.attributeList, function(attribute, index, allAttributes) {
					var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false, false);

					if (!Ext.isEmpty(field)) {
						field.maxWidth = field.width;

						if (attribute.defaultvalue)
							field.setValue(attribute.defaultvalue);

						this.view.form.add(field);
					}
				}, this);
		},

		onParametersAbortButtonClick: function() {
			this.view.destroy();
		},

		onParametersPrintButtonClick: function() {
			if (this.view.form.getForm().isValid()) {
				this.cmfg('currentReportParametersSet', {
					callIdentifier: 'update',
					params: this.view.form.getValues()
				});

				this.cmfg('updateReport', this.forceDownload);

				this.onParametersAbortButtonClick();
			}
		}
	});

})();