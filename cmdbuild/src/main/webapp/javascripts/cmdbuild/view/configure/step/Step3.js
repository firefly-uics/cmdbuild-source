(function() {

	Ext.define('CMDBuild.view.configure.step.Step3',{
		extend: 'Ext.panel.Panel',

		border: false,
		bodyCls: 'cmdb-blue-panel-no-padding',
		frame: false,
		disabled: true, // Disable this step by default

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.ADMINISTRATOR_USER_NAME,
						fieldLabel: CMDBuild.core.Utils.prependMandatoryLabel(CMDBuild.Translation.username),
						labelWidth: CMDBuild.LABEL_WIDTH_CONFIGURATION,
						maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
						allowBlank: false
					}),
					this.adminPassword = Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.constants.Proxy.ADMINISTRATOR_PASSWORD,
						fieldLabel: CMDBuild.core.Utils.prependMandatoryLabel(CMDBuild.Translation.password),
						labelWidth: CMDBuild.LABEL_WIDTH_CONFIGURATION,
						maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
						inputType: 'password',
						allowBlank: false
					}),
					Ext.create('Ext.form.field.Text', {
						fieldLabel: CMDBuild.core.Utils.prependMandatoryLabel(CMDBuild.Translation.confirmPassword),
						labelWidth: CMDBuild.LABEL_WIDTH_CONFIGURATION,
						maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
						inputType: 'password',
						vtype: 'password',
						twinFieldId: this.adminPassword.getId(),
						submitValue: false,
						allowBlank: false
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			show: function(panel, eOpts) {
				this.delegate.cmfg('onConfigurationViewportWizardPanelShow', {
					displayPreviusButton: true,
					displayFinishButton: true
				});
			}
		}
	});

})();