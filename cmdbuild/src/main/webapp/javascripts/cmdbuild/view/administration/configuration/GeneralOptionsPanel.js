(function() {

	Ext.define('CMDBuild.view.administration.configuration.GeneralOptionsPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Configuration'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.GeneralOptions}
		 */
		delegate: undefined,

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		instanceNameField: undefined,

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
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'bottom',
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_BOTTOM,
						ui: 'footer',

						layout: {
							type: 'hbox',
							align: 'middle',
							pack: 'center'
						},

						items: [
							Ext.create('CMDBuild.core.buttons.text.Save', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationGeneralOptionsSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onConfigurationGeneralOptionsAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.general,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							this.instanceNameField = Ext.create('CMDBuild.view.common.field.translatable.Text', {
								name: 'instance_name',
								fieldLabel: CMDBuild.Translation.instanceName,
								labelAlign: 'left',
								labelWidth: CMDBuild.CFG_LABEL_WIDTH,
								maxWidth: CMDBuild.CFG_BIG_FIELD_WIDTH,
								allowBlank: true,

								translationFieldConfig: {
									type: CMDBuild.core.constants.Proxy.INSTANCE_NAME,
									identifier: CMDBuild.core.constants.Proxy.INSTANCE_NAME, // Just for configuration validation
									field: CMDBuild.core.constants.Proxy.INSTANCE_NAME
								}
							}),
							Ext.create('CMDBuild.field.ErasableCombo', {
								name: 'startingclass',
								fieldLabel: CMDBuild.Translation.defaultClass,
								valueField: CMDBuild.core.constants.Proxy.ID,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								editable: false,

								store: CMDBuild.core.proxy.Configuration.getStartingClassStore(),
								queryMode: 'local'
							}),
							{
								xtype: 'numberfield',
								name: 'rowlimit',
								fieldLabel: CMDBuild.Translation.rowLimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'referencecombolimit',
								fieldLabel: CMDBuild.Translation.referenceComboLimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'relationlimit',
								fieldLabel: CMDBuild.Translation.relationLimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'grid_card_ratio',
								fieldLabel: CMDBuild.Translation.cardPanelHeight,
								allowBlank: false,
								maxValue: 100,
								minValue: 0
							},
							{
								xtype: 'combobox',
								name: 'card_tab_position',
								fieldLabel: CMDBuild.Translation.tabPositioInCardPanel,
								allowBlank: false,
								displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
								valueField: CMDBuild.core.constants.Proxy.VALUE,

								store: Ext.create('Ext.data.Store', {
									fields: [CMDBuild.core.constants.Proxy.VALUE, CMDBuild.core.constants.Proxy.DESCRIPTION],
									data: [
										{
											value: 'top',
											description: CMDBuild.Translation.top
										},
										{
											value: 'bottom',
											description: CMDBuild.Translation.bottom
										}
									]
								}),
								queryMode: 'local'
							},
							{
								xtype: 'numberfield',
								name: 'session.timeout',
								fieldLabel: CMDBuild.Translation.sessionTimeout,
								allowBlank: true,
								minValue: 0
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.popupWindows,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								xtype: 'numberfield',
								name: 'popuppercentageheight',
								fieldLabel: CMDBuild.Translation.popupPercentageHeight,
								maxValue: 100,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'popuppercentagewidth',
								fieldLabel: CMDBuild.Translation.popupPercentageWidth,
								maxValue: 100,
								allowBlank: false
							}
						]
					}),
					Ext.create('Ext.form.FieldSet', {
						title: CMDBuild.Translation.lockCardsAndProcessesInEdit,

						layout: {
							type: 'vbox',
							align:'stretch'
						},

						items: [
							{
								xtype: 'xcheckbox',
								name: 'lockcardenabled',
								fieldLabel: CMDBuild.Translation.enabled
							},
							{
								xtype: 'xcheckbox',
								name: 'lockcarduservisible',
								fieldLabel: CMDBuild.Translation.showLockerUserName
							},
							{
								xtype: 'numberfield',
								name: 'lockcardtimeout',
								fieldLabel: CMDBuild.Translation.lockTimeout
							}
						]
					})
				]
			});

			this.callParent(arguments);
		},

		listeners: {
			add: function(panel, component, index, eOpts) {
				panel.instanceNameField.translationsRead(); // Custom function call to read translations data
			}
		},

		/**
		 * @param {Object} saveDataObject
		 */
		afterSubmit: function(saveDataObject) {
			Ext.get('instance_name').dom.innerHTML = saveDataObject['instance_name'];
		}
	});

})();