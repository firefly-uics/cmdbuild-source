(function() {

	var tr = CMDBuild.Translation.administration.setup.cmdbuild;

	Ext.define('CMDBuild.view.administration.configuration.CMModConfigurationGenericOption', {
		extend: 'CMDBuild.view.administration.configuration.CMBaseModConfiguration',

		alias: 'widget.configuregenericoptions',

		configFileName: 'cmdbuild',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Configuration'
		],

		title: tr.title,

		constructor: function() {
			this.instanceNameField = Ext.create('Ext.form.CMTranslatableText', {
				fieldLabel: tr.instancename,
				name: 'instance_name',
				allowBlank: true,
				// This configuration is on the parent but for this special field is repeated here
				labelAlign: 'left',
				labelWidth: CMDBuild.CFG_LABEL_WIDTH,
				width: CMDBuild.CFG_MEDIUM_FIELD_WIDTH,
				// END - Duplicated configuration
				translationsKeyType: 'InstanceName'
			});

			Ext.apply(this, {
				items: [
					{
						xtype: 'fieldset',
						title: tr.fieldsetgeneraltitle,

						items: [
							this.instanceNameField,
							Ext.create('CMDBuild.field.ErasableCombo', {
								name: 'startingclass',
								fieldLabel: tr.startingClass,
								valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
								displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
								editable: false,

								store: CMDBuild.core.proxy.Configuration.getStartingClassStore(),
								queryMode: 'local'
							}),
							{
								xtype: 'numberfield',
								name: 'rowlimit',
								fieldLabel: tr.rowlimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'referencecombolimit',
								fieldLabel: tr.referencecombolimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'relationlimit',
								fieldLabel: tr.relationlimit,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'grid_card_ratio',
								fieldLabel: tr.cardpanelheight,
								allowBlank: false,
								maxValue: 100,
								minValue: 0
							},
							{
								xtype: 'combobox',
								name: 'card_tab_position',
								fieldLabel: tr.tabs_position.label,
								allowBlank: false,
								displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
								valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,

								store: Ext.create('Ext.data.Store', {
									fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE, CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION],
									data: [
										{
											value: 'top',
											description: tr.tabs_position.top
										},
										{
											value: 'bottom',
											description: tr.tabs_position.bottom
										}
									]
								}),
								queryMode: 'local'
							},
							{
								xtype: 'numberfield',
								name: 'session.timeout',
								fieldLabel: tr.sessiontimeout,
								allowBlank: true,
								minValue: 0
							}
						]
					},
					{
						xtype: 'fieldset',
						title: tr.fieldsetpopupwindowtitle,

						items: [
							{
								xtype: 'numberfield',
								name: 'popuppercentageheight',
								fieldLabel: tr.popupheightlabel,
								maxValue: 100,
								allowBlank: false
							},
							{
								xtype: 'numberfield',
								name: 'popuppercentagewidth',
								fieldLabel: tr.popupwidthlabel,
								maxValue: 100,
								allowBlank: false
							}
						]
					},
					{
						xtype: 'fieldset',
						title: CMDBuild.Translation.lock_cards_in_edit,

						items: [
							{
								xtype: 'xcheckbox',
								name: 'lockcardenabled',
								fieldLabel: CMDBuild.Translation.enabled
							},
							{
								xtype: 'xcheckbox',
								name: 'lockcarduservisible',
								fieldLabel: CMDBuild.Translation.show_name_of_locker_user
							},
							{
								xtype: 'numberfield',
								name: 'lockcardtimeout',
								fieldLabel: CMDBuild.Translation.lock_timeout
							}
						]
					}
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @override
		 */
		afterSubmit: function() {
			var hdInstanceName = Ext.get('instance_name');

			hdInstanceName.dom.innerHTML = this.instanceNameField.getValue();
		}
	});

})();