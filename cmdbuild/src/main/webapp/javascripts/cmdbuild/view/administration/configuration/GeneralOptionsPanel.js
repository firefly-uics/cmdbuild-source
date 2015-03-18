(function() {

	Ext.define('CMDBuild.view.administration.configuration.GeneralOptionsPanel', {
		extend: 'CMDBuild.view.administration.configuration.BasePanel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Configuration'
		],

		/**
		 * @cfg {CMDBuild.controller.administration.configuration.Main}
		 */
		delegate: undefined,

		/**
		 * @cfg {String}
		 */
		configFileName: 'cmdbuild',

		/**
		 * @property {CMDBuild.view.common.field.translatable.Text}
		 */
		instanceNameField: undefined,

		initComponent: function() {
			this.instanceNameField = Ext.create('CMDBuild.view.common.field.translatable.Text', {
				fieldLabel: CMDBuild.Translation.instanceName,
				name: 'instance_name',
				allowBlank: true,
				labelAlign: 'left',
				labelWidth: CMDBuild.CFG_LABEL_WIDTH,
				width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
				translationsKeyType: 'InstanceName'
			});

			Ext.apply(this, {
				title: this.baseTitle + this.titleSeparator + CMDBuild.Translation.generalOptions,
				items: [
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.general,

						items: [
							this.instanceNameField,
							Ext.create('CMDBuild.field.ErasableCombo', {
								name: 'startingclass',
								fieldLabel: CMDBuild.Translation.defaultClass,
								valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
								displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
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
								displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
								valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

								store: Ext.create('Ext.data.Store', {
									fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE, CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
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
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.popupWindows,

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
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.lockCardsInEdit,

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
			Ext.get('instance_name').dom.innerHTML = saveDataObject['instance_name'];
		}
	});

})();