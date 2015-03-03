(function() {

	Ext.define('CMDBuild.view.management.report.ParametersWindow', {
		extend: 'CMDBuild.core.PopupWindow',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Report'
		],

		/**
		 * @cfg {CMDBuild.controller.management.report.SingleReport}
		 */
		delegate: undefined,

		/**
		 * @cfg {Array}
		 */
		attributeList: [],

		buttonsAlign: 'center',
		border: false,
		frame: false,
		layout: 'fit',
		title: CMDBuild.Translation.management.modreport.report_parameters,

		initComponent: function() {
			// Buttons configuration
				this.saveButton = Ext.create('CMDBuild.buttons.SaveButton', {
					scope: this,

					handler: function() {
						this.submitParameters();
					}
				});

				this.cancelButton = Ext.create('CMDBuild.buttons.AbortButton', {
					scope: this,

					handler: function() {
						this.destroy();
					}
			});
			// END: Buttons configuration

			this.formPanel = Ext.create('Ext.form.Panel', {
				labelAlign: 'right',
				frame: true,
				border: false
			});

			Ext.apply(this, {
				items: [this.formPanel],
				buttons: [this.saveButton, this.cancelButton]
			});

			this.callParent(arguments);
		},

		listeners: {
			render: function() {
				if (this.attributeList.length > 0)
					Ext.Array.forEach(this.attributeList, function(attribute, index, allAttributes) {
						var field = CMDBuild.Management.FieldManager.getFieldForAttr(attribute, false);

						if (!Ext.isEmpty(field)) {
							if(attribute.defaultvalue)
								field.setValue(attribute.defaultvalue);

							this.formPanel.add(field);
						}
					}, this);
			}
		},

		/**
		 * Left here only because separate controller for this window doesn't exists, but if in future this class will grow up the controller will be created
		 */
		submitParameters: function() {
			var form = this.formPanel.getForm();

			if (form.isValid()) {
				CMDBuild.LoadMask.get().show();
				CMDBuild.core.proxy.Report.updateReport({
					form: form,
					scope: this,
					failure: function(response, options, decodedResponse) {
						this.destroy();

						CMDBuild.LoadMask.get().hide();
					},
					success: function(response, options) {
						if (Ext.isEmpty(this.delegate)) {
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
						} else {
							this.delegate.showReport();
						}

						this.destroy();

						CMDBuild.LoadMask.get().hide();
					}
				});
			}
		}
	});

})();