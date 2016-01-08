(function () {

	Ext.define('CMDBuild.view.administration.user.FormPanel', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.User'
		],

		mixins: ['CMDBuild.view.common.PanelFunctions'],

		/**
		 * @cfg {CMDBuild.controller.administration.user.User}
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
						itemId: CMDBuild.core.constants.Proxy.TOOLBAR_TOP,
						items: [
							Ext.create('CMDBuild.core.buttons.iconized.Modify', {
								text: CMDBuild.Translation.modifyUser,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserModifyButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.iconized.Password', {
								text: CMDBuild.Translation.changePassword,
								scope: this,

								handler: function(button, e) {
									this.delegate.cmfg('onUserChangePasswordButtonClick');
								}
							}),
							this.disableUser = Ext.create('CMDBuild.core.buttons.iconized.Delete', {
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
									this.delegate.cmfg('onUserSaveButtonClick');
								}
							}),
							Ext.create('CMDBuild.core.buttons.text.Abort', {
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
										name: CMDBuild.core.constants.Proxy.USERNAME,
										id: CMDBuild.core.constants.Proxy.USERNAME,
										fieldLabel: CMDBuild.Translation.username,
										allowBlank: false,
										cmImmutable: true,
										vtype: 'alphanumextended'
									},
									{
										name: CMDBuild.core.constants.Proxy.DESCRIPTION,
										fieldLabel: CMDBuild.Translation.descriptionLabel,
										allowBlank: false
									},
									{
										name: CMDBuild.core.constants.Proxy.EMAIL,
										fieldLabel: CMDBuild.Translation.email,
										allowBlank: true,
										vtype: 'emailOrBlank'
									},
									this.defaultGroup = Ext.create('CMDBuild.field.ErasableCombo', {
										name: 'defaultgroup',
										fieldLabel: CMDBuild.Translation.defaultGroup,
										labelWidth: CMDBuild.LABEL_WIDTH,
										width: CMDBuild.ADM_BIG_FIELD_WIDTH,
										valueField: CMDBuild.core.constants.Proxy.ID,
										displayField: CMDBuild.core.constants.Proxy.DESCRIPTION,
										editable: false,
										allowBlank: true,

										store: CMDBuild.core.proxy.User.getDefaultGroupStore(),
										queryMode: 'local'
									}),
									Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.IS_ACTIVE,
										fieldLabel: CMDBuild.Translation.active,
										labelWidth: CMDBuild.LABEL_WIDTH
									}),
									this.serviceCheckbox = Ext.create('Ext.form.field.Checkbox', {
										name: CMDBuild.core.constants.Proxy.SERVICE,
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
										name: CMDBuild.core.constants.Proxy.PRIVILEGED,
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
										name: CMDBuild.core.constants.Proxy.PASSWORD,
										id: 'user_password',
										inputType: 'password',
										fieldLabel: CMDBuild.Translation.password,
										allowBlank: false
									},
									{
										name: CMDBuild.core.constants.Proxy.CONFIRMATION,
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