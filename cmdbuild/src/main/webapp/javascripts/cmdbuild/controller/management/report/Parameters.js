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
		attributeList: undefined,

		/**
		 * @cfg {Boolean}
		 */
		forceDownload: undefined,

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onParametersAbortButtonClick',
			'onParametersSaveButtonClick'
		],

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

		onParametersSaveButtonClick: function() {
			if (this.view.form.getForm().isValid()) {
				CMDBuild.core.proxy.Report.updateReport({
					form: this.view.form.getForm(),
					scope: this,
					failure: function(response, options, decodedResponse) {
						this.onParametersAbortButtonClick();
					},
					success: function(response, options) {
						if (Ext.isEmpty(this.parentDelegate)) {
							var popup = window.open(
								CMDBuild.core.proxy.CMProxyUrlIndex.reports.printReportFactory,
								'Report',
								'height=400,width=550,status=no,toolbar=no,scrollbars=yes,menubar=no,location=no,resizable'
							);

							if (Ext.isEmpty(popup))
								CMDBuild.Msg.warn(
									CMDBuild.Translation.warnings.warning_message,
									CMDBuild.Translation.warnings.popup_block
								);
						} else if (!Ext.isEmpty(this.parentDelegate) && Ext.isFunction(this.parentDelegate.showReport)) {
							this.parentDelegate.showReport(this.forceDownload); // TODO: shound be used cmfg()
						}

						this.onParametersAbortButtonClick();
					}
				});
			}
		}
	});

})();