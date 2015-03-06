(function() {

	Ext.define('CMDBuild.view.administration.configuration.Workflow', {
		extend: 'CMDBuild.view.administration.configuration.BasePanel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'workflow',

		/**
		 * @property {Ext.ux.form.XCheckbox}
		 */
		enabledCheckBox: undefined,

		initComponent: function() {
			this.enabledCheckBox = Ext.create('Ext.ux.form.XCheckbox', {
				fieldLabel: CMDBuild.Translation.enabled,
				name: CMDBuild.core.proxy.CMProxyConstants.ENABLED
			});

			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.workflowEngine,
				items: [
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.general,
						autoHeight: true,
						defaultType: 'textfield',

						items: [
							this.enabledCheckBox,
							{
								fieldLabel: CMDBuild.Translation.serverUrl,
								name: 'endpoint',
								allowBlank: false,
								width: CMDBuild.CFG_BIG_FIELD_WIDTH
							}
						]
					}, {
						xtype: 'fieldset',
						title: CMDBuild.Translation.credentials,
						autoHeight: true,
						defaultType: 'textfield',

						items: [
							{
								fieldLabel: CMDBuild.Translation.username,
								name: CMDBuild.core.proxy.CMProxyConstants.USER,
								allowBlank: false
							},
							{
								fieldLabel: CMDBuild.Translation.password,
								name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
								allowBlank: false,
								inputType: 'password'
							},
							{
								fieldLabel: CMDBuild.Translation.engineName,
								name: CMDBuild.core.proxy.CMProxyConstants.ENGINE,
								allowBlank: false,
								disabled: true
							},
							{
								fieldLabel: CMDBuild.Translation.scope,
								name: CMDBuild.core.proxy.CMProxyConstants.SCOPE,
								allowBlank: true,
								disabled: true
							}
						]
					}
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Object} saveDataObject
		 *
		 * @override
		 */
		afterSubmit: function(saveDataObject) {
			CMDBuild.Config.workflow.enabled = this.enabledCheckBox.getValue();

			if (CMDBuild.Config.workflow.enabled) {
				_CMMainViewportController.enableAccordionByName('process');
			} else {
				_CMMainViewportController.disableAccordionByName('process');
			}
		}
	});

})();