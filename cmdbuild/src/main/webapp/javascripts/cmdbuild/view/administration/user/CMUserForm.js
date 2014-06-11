(function () {

	var tr = CMDBuild.Translation.administration.modsecurity.user;

	Ext.define('CMDBuild.view.administration.user.CMUserForm', {
		extend: 'Ext.panel.Panel',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed cmbordertop',
		frame: false,
		layout: 'fit',
		split: true,

		initComponent: function () {
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

			var userName = Ext.create('Ext.form.field.Text', {
				id: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
				fieldLabel: tr.username,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false,
				name: CMDBuild.core.proxy.CMProxyConstants.USERNAME,
				cmImmutable: true
			});

			var userDescription = Ext.create('Ext.form.field.Text', {
				fieldLabel: tr.description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false,
				name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION
			});

			var userEmail = Ext.create('Ext.form.field.Text', {
				vtype: 'emailOrBlank',
				fieldLabel: tr.email,
				labelWidth: CMDBuild.LABEL_WIDTH,
				width: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: true,
				name: 'email'
			});

			this.defaultGroupStore = Ext.create('Ext.data.JsonStore', {
				autoLoad: false,
				model: 'CMDBuild.cache.CMGroupModelForCombo',
				proxy: {
					type: 'ajax',
					url: 'services/json/schema/modsecurity/getusergrouplist',
					reader: {
						type: 'json',
						root: 'result'
					}
				},
				sorters: [{
					property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
					direction: 'ASC'
				}]
			});

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

				store: this.defaultGroupStore,
				queryMode: 'local',

				listConfig: {
					loadMask: false
				}
			});

			this.userInfo = Ext.create('Ext.form.FieldSet', {
				title: tr.user_info,
				region: 'west',
				margins: '0 3 0 0',
				overflowY: 'auto',
				flex: 1,

				items: [
					userName,
					userDescription,
					userEmail,
					this.defaultGroup,
					{
						xtype: 'xcheckbox',
						fieldLabel: tr.isactive,
						labelWidth: CMDBuild.LABEL_WIDTH,
						name: 'isActive',
						checked: true
					}
				]
			});

			this.userPassword = Ext.create('Ext.form.FieldSet', {
				title: tr.password,
				region: 'center',
				overflowY: 'auto',
				margins: '0 0 0 3',
				flex: 1,

				items: [
					{
						xtype: 'textfield',
						inputType: 'password',
						id: 'user_password',
						name: 'password',
						labelWidth: CMDBuild.LABEL_WIDTH,
						width: CMDBuild.ADM_BIG_FIELD_WIDTH,
						fieldLabel: tr.password,
						allowBlank: false
					},
					{
						xtype: 'textfield',
						inputType: 'password',
						fieldLabel: tr.confirmation,
						labelWidth: CMDBuild.LABEL_WIDTH,
						width: CMDBuild.ADM_BIG_FIELD_WIDTH,
						allowBlank: false,
						name: 'confirmation',
						vtype: 'password',
						initialPassField: 'user_password'
					}
				]
			});

			this.form = Ext.create('Ext.form.Panel', {
				region: 'center',
				bodyCls: 'cmgraypanel',
				layout: {
					type: 'hbox',
					align: 'stretch'
				},
				frame: false,
				border: false,

				items: [this.userInfo, this.userPassword]
			});

			Ext.apply(this, {
				tbar: this.cmTBar,
				items: [this.form],
				buttons: this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
		},

		getForm: function() {
			return this.form.getForm();
		},

		onUserSelected: function(user) {
			var me = this;
			var store = this.defaultGroupStore;

			this.reset();
			this.disableModify(enableCMTBar = true);
			this.updateDisableActionTextAndIconClass(user.get('isActive'));

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

		onAddUserClick: function() {
			this.reset();
			this.enableModify(all = true);
			this.defaultGroup.disable();
		},

		enableFieldset: function(fieldset) {
			fieldset.cascade(function (item) {
				if (
					item
					&& (
						item instanceof Ext.form.Field
						|| item instanceof Ext.form.FieldSet
						|| item.considerAsFieldToDisable
					)
				) {
					var name = item._name || item.name; // for compatibility I can not change the name of old attrs
					var toBeEnabled = (true || !item.cmImmutable) && item.isVisible();

					if (toBeEnabled)
						item.enable();
				}
			});

			this.disableCMTbar();
			this.enableCMButtons();
			this.focusOnFirstEnabled();
		},

		updateDisableActionTextAndIconClass: function(isactive) {
			if (isactive) {
				this.disableUser.setText(tr.disable_user);
				this.disableUser.setIconCls('delete');
			} else {
				this.disableUser.setText(tr.enable_user);
				this.disableUser.setIconCls('ok');
			}
		}
	});

})();