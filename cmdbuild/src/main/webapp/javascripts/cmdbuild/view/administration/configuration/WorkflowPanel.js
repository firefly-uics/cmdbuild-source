(function() {

	Ext.define('CMDBuild.view.administration.configuration.WorkflowPanel', {
		extend: 'Ext.form.Panel',

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

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,
		overflowY: 'auto',

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		fieldDefaults: {
			labelAlign: 'left',
			labelWidth: CMDBuild.CFG_LABEL_WIDTH,
			width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
		},

		initComponent: function() {
			this.enabledCheckBox = Ext.create('Ext.ux.form.XCheckbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
				fieldLabel: CMDBuild.Translation.enabled
			});

			Ext.apply(this, {
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
					},
					{
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
				],
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'bottom',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onConfigurationSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmOn('onConfigurationAbortButtonClick');
								}
							})
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