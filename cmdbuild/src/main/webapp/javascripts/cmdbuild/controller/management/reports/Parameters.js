(function () {

	Ext.define('CMDBuild.controller.management.reports.Parameters', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.Message',
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.reports.Reports'
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
			'onReportParametersWindowAbortButtonClick',
			'onReportParametersWindowPrintButtonClick'
		],

		/**
		 * @cfg {Boolean}
		 */
		forceDownload: false,

		/**
		 * @property {CMDBuild.view.management.reports.ParametersWindow} emailWindows
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.management.reports.ParametersWindow', {
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
						field.maxWidth = field.width;

						if (attribute.defaultvalue)
							field.setValue(attribute.defaultvalue);

						this.view.form.add(field);
					}
				}, this);
		},

		onReportParametersWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onReportParametersWindowPrintButtonClick: function() {
			if (this.view.form.getForm().isValid()) {
				this.cmfg('currentReportParametersSet', {
					callIdentifier: 'update',
					params: this.view.form.getValues()
				});

				this.cmfg('updateReport', this.forceDownload);

				this.onReportParametersWindowAbortButtonClick();
			}
		}
	});

})();