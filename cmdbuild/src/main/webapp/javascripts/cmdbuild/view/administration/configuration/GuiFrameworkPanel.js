(function() {

	Ext.define('CMDBuild.view.administration.configuration.GuiFrameworkPanel', {
		extend: 'Ext.form.Panel',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Workflow}
		 */
		delegate: undefined,

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
									this.delegate.cmfg('onGuiFrameworkSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGuiFrameworkAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.general,
						autoHeight: true,
						defaultType: 'textfield',

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							this.enabledCheckBox,
							{
								fieldLabel: CMDBuild.Translation.serverUrl,
								name: 'endpoint',
								allowBlank: false,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH
							}
						]
					})
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

			if (CMDBuild.Config.guiFramework.enabled) {
				_CMMainViewportController.enableAccordionByName('guiFramework');
			} else {
				_CMMainViewportController.disableAccordionByName('guiFRamework');
			}
		}

	});

})();