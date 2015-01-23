(function () {

	var tr = CMDBuild.Translation.administration.modsecurity.user;

	Ext.define('CMDBuild.view.administration.user.CMUserForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			// TODO: Require CMDBuild.ServiceProxy.group class
		],

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		/**
		 * @param {CMDBuild.buttons.AbortButton}
		 */
		abortButton: undefined,

		/**
		 * @param {Array}
		 */
		cmButtons: undefined,

		/**
		 * @param {Array}
		 */
		cmTBar: undefined,

		/**
		 * @param {CMDBuild.field.ErasableCombo}
		 */
		defaultGroup: undefined,

		/**
		 * @param {Ext.button.Button}
		 */
		disableUser: undefined,

		/**
		 * @param {Ext.button.Button}
		 */
		modifyButton: undefined,

		/**
		 * @param {Ext.button.Button}
		 */
		modifyPassword: undefined,

		/**
		 * @param {CMDBuild.buttons.SaveButton}
		 */
		saveButton: undefined,

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
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'border',
		split: true,

		initComponent: function () {
			// Buttons configuration
				this.modifyButton = Ext.create('Ext.button.Button', {
					iconCls: 'modify',
					text: tr.modify_user,
					scope: this,

					handler: function () {
						this.enableFieldset(this.userInfo);
					}
				});

				this.modifyPassword = Ext.create('Ext.button.Button', {
					iconCls: 'password',
					text: tr.change_password,
					scope: this,

					handler: function () {
						this.enableFieldset(this.userPassword);
					}
				});

				this.disableUser = Ext.create('Ext.button.Button', {
					iconCls: 'delete',
					text: tr.disable_user
				});

				this.cmTBar = [this.modifyButton, this.modifyPassword, this.disableUser];

				this.saveButton = Ext.create('CMDBuild.buttons.SaveButton');
				this.abortButton = Ext.create('CMDBuild.buttons.AbortButton');
				this.cmButtons = [this.saveButton, this.abortButton];
			// END: Buttons configuration

			// Page FieldSets configuration
				// User info
					this.defaultGroup = Ext.create('CMDBuild.field.ErasableCombo', {
						name: 'defaultgroup',
						fieldLabel: tr.defaultgroup,
						labelWidth: CMDBuild.LABEL_WIDTH,
						width: CMDBuild.ADM_BIG_FIELD_WIDTH,
						triggerAction: 'all',
						valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
						displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
						editable: false,
						allowBlank: true,

						store: CMDBuild.ServiceProxy.group.getDefaultGroupStore(),
						queryMode: 'local',

						listConfig: {
							loadMask: false
						}
					});

					this.userInfo = Ext.create('Ext.form.FieldSet', {
						title: tr.user_info,
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
								fieldLabel: tr.username,
								allowBlank: false,
								cmImmutable: true,
								vtype: 'alphanumextended'
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
								fieldLabel: tr.description,
								allowBlank: false
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.EMAIL,
								fieldLabel: tr.email,
								allowBlank: true,
								vtype: 'emailOrBlank'
							},
							this.defaultGroup,
							Ext.create('Ext.ux.form.XCheckbox', {
								name: CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE,
								fieldLabel: tr.isactive,
								checked: true
							})
						]
					});
				// Password
					this.userPassword = Ext.create('Ext.form.FieldSet', {
						title: tr.password,
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
								fieldLabel: tr.password,
								allowBlank: false
							},
							{
								name: CMDBuild.core.proxy.CMProxyConstants.CONFIRMATION,
								inputType: 'password',
								fieldLabel: tr.confirmation,
								allowBlank: false,
								initialPassField: 'user_password',
								vtype: 'password'
							}
						]
					});
			// END: Page FieldSets configuration

			// Splitted-view wrapper
			this.wrapper = Ext.create('Ext.form.Panel', {
				region: 'center',
				frame: true,
				border: false,

				layout: {
					type: 'hbox',
					align:'stretch'
				},

				defaults: {
					overflowY: 'auto'
				},

				items: [
					this.userInfo,
					{ xtype: 'splitter' },
					this.userPassword
				]
			});

			Ext.apply(this, {
				dockedItems: [
					{
						xtype: 'toolbar',
						dock: 'top',
						itemId: CMDBuild.core.proxy.CMProxyConstants.TOOLBAR_TOP,
						items: this.cmTBar
					}
				],
				items: [this.wrapper],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
		},

		/**
		 * @param {Ext.form.FieldSet} fieldset
		 */
		enableFieldset: function(fieldset) {
			fieldset.cascade(function (item) {
				if (
					item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.FieldSet
						|| item.considerAsFieldToDisable
					)
					&& !item.cmImmutable
					&& item.isVisible()
				) {
					item.enable();
				}
			});

			this.disableCMTbar();
			this.enableCMButtons();
			this.focusOnFirstEnabled();
		},

		/**
		 * @return {Ext.form.Basic}
		 */
		getForm: function() {
			return this.wrapper.getForm();
		},

		onAddUserClick: function() {
			this.reset();
			this.enableModify(true);
			this.defaultGroup.disable();
		},

		/**
		 * @param {CMDBuild.cache.CMUserForGridModel} user
		 */
		onUserSelected: function(user) {
			var me = this;
			var store = this.defaultGroup.getStore();

			this.reset();
			this.disableModify(true);
			this.updateDisableActionTextAndIconClass(user.get(CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE));

			store.load({
				params: {
					userid: user.get('userid')
				},
				callback: function() {
					var defaultGroup = store.findRecord('isdefault', true);

					if (defaultGroup)
						user.set('defaultgroup', defaultGroup.getId());

					me.getForm().loadRecord(user);

					// FIX: to avoid default int value (0) to be displayed
					if (me.defaultGroup.getValue() == 0)
						me.defaultGroup.setValue();
				}
			});
		},

		/**
		 * @param {Boolean} isActive
		 */
		updateDisableActionTextAndIconClass: function(isActive) {
			if (isActive) {
				this.disableUser.setText(tr.disable_user);
				this.disableUser.setIconCls('delete');
			} else {
				this.disableUser.setText(tr.enable_user);
				this.disableUser.setIconCls('ok');
			}
		}
	});

})();