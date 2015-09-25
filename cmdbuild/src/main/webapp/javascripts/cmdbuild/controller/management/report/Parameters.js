(function () {

	Ext.define('CMDBuild.controller.management.report.Parameters', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.controller.common.AbstractBaseWidgetController',
			'CMDBuild.core.Message',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.report.Report'
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
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

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

			this.view = Ext.create('CMDBuild.view.management.report.ParametersWindow', { delegate: this });

			// ShortHands
			this.form = this.view.form;

			// Show window
			if (!Ext.isEmpty(this.view)) {
				this.buildFields();

				this.view.show();
			}
		},

		buildFields: function() {
			var me = this;

			if (this.attributeList.length > 0) {
				Ext.Array.forEach(this.attributeList, function(attribute, i, allAttributes) {
					new CMDBuild.Management.TemplateResolver({
						clientForm: this.form.getForm(),
						xaVars: attribute,
						serverVars: CMDBuild.controller.common.AbstractBaseWidgetController.getTemplateResolverServerVars(attribute)
					}).resolveTemplates({
						attributes: Ext.Object.getKeys(attribute),
						callback: function(out, ctx) {
							var field = CMDBuild.Management.FieldManager.getFieldForAttr(out, false, false);

							if (!Ext.isEmpty(field)) {
								field.maxWidth = field.width;

								if (attribute.defaultvalue)
									field.setValue(attribute.defaultvalue);

								me.form.add(field);
							}
						}
					});
				}, this);
			}
		},

		onReportParametersWindowAbortButtonClick: function() {
			this.view.destroy();
		},

		onReportParametersWindowPrintButtonClick: function() {
			if (this.view.form.getForm().isValid()) {
				this.cmfg('currentReportParametersSet', {
					callIdentifier: 'update',
					params: this.form.getValues()
				});

				this.cmfg('updateReport', this.forceDownload);

				this.onReportParametersWindowAbortButtonClick();
			}
		}
	});

})();