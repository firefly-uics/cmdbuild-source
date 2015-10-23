(function() {

	Ext.define('CMDBuild.view.administration.configuration.BimPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Bim}
		 */
		delegate: undefined,

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
			maxWidth: CMDBuild.CFG_MEDIUM_FIELD_WIDTH
		},

		initComponent: function() {
			this.enabledCheckBox = Ext.create('Ext.ux.form.XCheckbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.ENABLED,
				fieldLabel: CMDBuild.Translation.enabled
			});

			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
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
									this.delegate.cmfg('onBimSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onBimAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.enabledCheckBox,
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.URL,
						fieldLabel: CMDBuild.Translation.url,
						maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
					},
					{
						xtype: 'textfield',
						name: 'username',
						fieldLabel: CMDBuild.Translation.username
					},
					{
						xtype: 'textfield',
						name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
						fieldLabel: CMDBuild.Translation.password,
						inputType: 'password'
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
				_CMMainViewportController.enableAccordionByName(this.delegate.configFileName);
			} else {
				_CMMainViewportController.disableAccordionByName(this.delegate.configFileName);
			}
		}
	});

})();