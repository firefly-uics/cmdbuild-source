(function() {

	Ext.define('CMDBuild.view.administration.group.properties.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.group.Group'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.group.Properties}
		 */
		delegate: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		activeCheckbox: undefined,

		/**
		 * @property {CMDBuild.core.buttons.Delete}
		 */
		disableButton: undefined,

		/**
		 * @property {CMDBuild.core.buttons.iconized.state.Double}
		 */
		enableDisableButton: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		frame: false,
		overflowY: 'auto',
		split: true,

		layout: {
			type: 'vbox',
			align:'stretch'
		},

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,

						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyGroup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupPropertiesModifyButtonClick');
								}
							}),
							this.enableDisableButton = Ext.create('CMDBuild.core.buttons.iconized.state.Double', {
								state1text: CMDBuild.Translation.disableGroup,
								state2text: CMDBuild.Translation.enableGroup,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupPropertiesEnableDisableButtonClick', button.getClickedState());
								}
							})
						]
					}),
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
									this.delegate.cmfg('onGroupPropertiesSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onGroupPropertiesAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.proxy.CMProxyConstants.NAME,
						fieldLabel: CMDBuild.Translation.name,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						cmImmutable: true,
						vtype: 'alphanumextended'
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						fieldLabel: CMDBuild.Translation.descriptionLabel,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false
					}),
					Ext.create('Ext.form.field.ComboBox', {
						name: CMDBuild.core.proxy.CMProxyConstants.TYPE,
						fieldLabel: CMDBuild.Translation.type,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						editable: false,
						forceSelection: true,

						value: CMDBuild.core.proxy.CMProxyConstants.NORMAL,

						store: CMDBuild.core.proxy.group.Group.getTypeStore(),
						queryMode: 'local'
					}),
					Ext.create('Ext.form.field.Text', {
						name: CMDBuild.core.proxy.CMProxyConstants.EMAIL,
						fieldLabel: CMDBuild.Translation.email,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: true,
						vtype: 'emailOrBlank'
					}),
					Ext.create('CMDBuild.field.ErasableCombo', {
						name: CMDBuild.core.proxy.CMProxyConstants.STARTING_CLASS,
						fieldLabel: CMDBuild.Translation.startingPageAt,
						labelWidth: CMDBuild.LABEL_WIDTH,
						maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
						valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						editable: false,
						forceSelection: true,

						store: CMDBuild.core.proxy.group.Group.getStartingClassStore(),
						queryMode: 'local'
					}),
					this.activeCheckbox = Ext.create('Ext.form.field.Checkbox', {
						name: CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE,
						fieldLabel: CMDBuild.Translation.active,
						labelWidth: CMDBuild.LABEL_WIDTH,
						inputValue: true,
						uncheckedValue: false,
						checked: true
					}),
					{
						xtype: 'hiddenfield',
						name: CMDBuild.core.proxy.CMProxyConstants.ID
					}
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true, true, true);
		}
	});

})();