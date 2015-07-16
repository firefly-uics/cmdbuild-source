(function () {

	Ext.define('CMDBuild.view.administration.users.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.Users'
		],

		mixins: {
			panelFunctions: 'CMDBuild.view.common.PanelFunctions'
		},

		/**
		 * @cfg {CMDBuild.controller.administration.users.Users}
		 */
		delegate: undefined,

		/**
		 * @param {Ext.button.Button}
		 */
		disableUser: undefined,

		/**
		 * @param {CMDBuild.field.ErasableCombo}
		 */
		defaultGroup: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		privilegedCheckbox: undefined,

		/**
		 * @property {Ext.form.field.Checkbox}
		 */
		serviceCheckbox: undefined,

		/**
		 * @param {Ext.form.FieldSet}
		 */
		userInfo: undefined,

		/**
		 * @param {Ext.form.FieldSet}
		 */
		userPassword: undefined,

		/**
		 * @param {Ext.form.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'fit',
		split: true,

		initComponent: function() {
			Ext.apply(this, {
				dockedItems: [
					Ext.create('Ext.toolbar.Toolbar', {
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: [
							Ext.create('CMDBuild.core.buttons.Modify', {
								text: CMDBuild.Translation.modifyUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Password', {
								text: CMDBuild.Translation.changePassword,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserChangePasswordButtonClick');
								}
							}),
							this.disableUser = Ext.create('CMDBuild.core.buttons.Delete', {
								text: CMDBuild.Translation.disableUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserDisableButtonClick');
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
									this.delegate.cmfg('onUserSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.Abort', {
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserAbortButtonClick');
								}
							})
						]
					})
				],
				items: [
					this.wrapper = Ext.create('Ext.form.Panel', { // Splitted-view wrapper
						bodyCls: 'cmgraypanel-nopadding',
						border: false,
						frame: false,

						layout: {
							type: 'hbox',
							align: 'stretch'
						},

						items: [
							this.userInfo = Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.userInformations,
								overflowY: 'auto',
								flex: 1,

								defaults: {
									xtype: 'textfield',
									labelWidth: CMDBuild.LABEL_WIDTH,
									maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
									anchor: '100%'
								},

								items: [
									{
										name: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
										id: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
										fieldLabel: CMDBuild.Translation.username,
										allowBlank: false,
										cmImmutable: true,
										vtype: 'alphanumextended'
									},
									{
										name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
										fieldLabel: CMDBuild.Translation.descriptionLabel,
										allowBlank: false
									},
									{
										name: CMDBuild.core.proxy.CMProxyConstants.EMAIL,
										fieldLabel: CMDBuild.Translation.email,
										allowBlank: true,
										vtype: 'emailOrBlank'
									},
									this.defaultGroup = Ext.create('CMDBuild.field.ErasableCombo', {
										name: 'defaultgroup',
										fieldLabel: CMDBuild.Translation.defaultGroup,
										labelWidth: CMDBuild.LABEL_WIDTH,
										width: CMDBuild.ADM_BIG_FIELD_WIDTH,
										valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
										displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
										editable: false,
										allowBlank: true,

										store: CMDBuild.core.proxy.Users.getDefaultGroupStore(),
										queryMode: 'local'
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE,
										fieldLabel: CMDBuild.Translation.active,
										labelWidth: CMDBuild.LABEL_WIDTH
									}),
									this.serviceCheckbox = Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.proxy.CMProxyConstants.SERVICE,
										fieldLabel: CMDBuild.Translation.service,
										labelWidth: CMDBuild.LABEL_WIDTH,

										listeners: {
											scope: this,
											change: function(field, newValue, oldValue, eOpts) {
												this.delegate.cmfg('onUserServiceChange');
											}
										}
									}),
									this.privilegedCheckbox = Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.proxy.CMProxyConstants.PRIVILEGED,
										fieldLabel: CMDBuild.Translation.privileged,
										labelWidth: CMDBuild.LABEL_WIDTH,

										listeners: {
											scope: this,
											change: function(field, newValue, oldValue, eOpts) {
												this.delegate.cmfg('onUserPrivilegedChange');
											}
										}
									})
								]
							}),
							{ xtype: 'splitter' },
							this.userPassword = Ext.create('Ext.form.FieldSet', {
								title: CMDBuild.Translation.password,
								overflowY: 'auto',
								flex: 1,

								defaults: {
									xtype: 'textfield',
									labelWidth: CMDBuild.LABEL_WIDTH,
									maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
									anchor: '100%'
								},

								items: [
									{
										name: CMDBuild.core.proxy.CMProxyConstants.PASSWORD,
										id: 'user_password',
										inputType: 'password',
										fieldLabel: CMDBuild.Translation.password,
										allowBlank: false
									},
									{
										name: CMDBuild.core.proxy.CMProxyConstants.CONFIRMATION,
										inputType: 'password',
										fieldLabel: CMDBuild.Translation.confirmation,
										allowBlank: false,
										initialPassField: 'user_password',
										vtype: 'password',
										submitValue: false
									}
								]
							})
						]
					})
				]
			});

			this.callParent(arguments);

			this.setDisabledModify(true);
		},

		/**
		 * Forwarding method
		 *
		 * @return {Ext.form.Basic}
		 *
		 * @override
		 */
		getForm: function() {
			return this.wrapper.getForm();
		}
	});

})();