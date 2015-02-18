(function() {

	var tr = CMDBuild.Translation.administration.modsecurity.group;

	Ext.define('CMDBuild.view.administration.group.CMGroupForm', {
		extend: 'Ext.form.Panel',

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			// TODO: Require CMDBuild.ServiceProxy.group class
		],

		alias: 'groupform',

		mixins: {
			cmFormFunctions: 'CMDBUild.view.common.CMFormFunctions'
		},

		/**
		 * @param {CMDBuild.buttons.AbortButton}
		 */
		abortButton: undefined,

		/**
		 * @param {Ext.ux.form.XCheckbox}
		 */
		activeCheck: undefined,

		/**
		 * @param {Array}
		 */
		cmButtons: undefined,

		/**
		 * @param {Array}
		 */
		cmTBar: undefined,

		/**
		 * @param {Ext.button.Button}
		 */
		enableGroupButton: undefined,

		/**
		 * @param {Ext.form.field.Text}
		 */
		groupDescription: undefined,

		/**
		 * @param {Ext.form.field.Text}
		 */
		groupEmail: undefined,

		/**
		 * @param {Ext.form.field.Text}
		 */
		groupName: undefined,

		/**
		 * @param {Ext.form.field.ComboBox}
		 */
		groupType: undefined,

		/**
		 * @param {Ext.button.Button}
		 */
		modifyButton: undefined,

		/**
		 * @param {CMDBuild.buttons.SaveButton}
		 */
		saveButton: undefined,

		/**
		 * @param {CMDBuild.field.ErasableCombo}
		 */
		startingClass: undefined,

		/**
		 * @param {Ext.form.Panel}
		 */
		wrapper: undefined,

		bodyCls: 'cmgraypanel',
		border: false,
		buttonAlign: 'center',
		cls: 'x-panel-body-default-framed',
		frame: false,
		layout: 'border',

		initComponent: function() {
			// Buttons configuration
				this.modifyButton = Ext.create('Ext.button.Button', {
					iconCls: 'modify',
					text: tr.modify_group
				});

				this.enableGroupButton = Ext.create('Ext.button.Button', {
					iconCls: 'delete',
					text: tr.delete_group
				});

				this.cmTBar = [this.modifyButton, this.enableGroupButton ];

				this.saveButton = Ext.create('CMDBuild.buttons.SaveButton');
				this.abortButton = Ext.create('CMDBuild.buttons.AbortButton');
				this.cmButtons = [this.saveButton, this.abortButton];
			// END: Buttons configuration

			this.groupName = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.NAME,
				fieldLabel: tr.group_name,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false,
				cmImmutable: true,
				vtype: 'alphanumextended'
			});

			this.groupDescription = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				fieldLabel: tr.group_description,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: false
			});

			this.groupType = Ext.create('Ext.form.field.ComboBox', {
				name: CMDBuild.core.proxy.CMProxyConstants.TYPE,
				fieldLabel: CMDBuild.Translation.administration.modClass.attributeProperties.type,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: CMDBuild.core.proxy.CMProxyConstants.VALUE,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				editable: false,

				store: CMDBuild.ServiceProxy.group.getGroupTypeStore(),
				queryMode: 'local'
			});

			this.groupEmail = Ext.create('Ext.form.field.Text', {
				name: CMDBuild.core.proxy.CMProxyConstants.EMAIL,
				fieldLabel: tr.email,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				allowBlank: true,
				vtype: 'emailOrBlank'
			});

			this.activeCheck = Ext.create('Ext.ux.form.XCheckbox', {
				name: CMDBuild.core.proxy.CMProxyConstants.IS_ACTIVE,
				fieldLabel: tr.is_active,
				labelWidth: CMDBuild.LABEL_WIDTH,
				checked: true
			});

			this.startingClass = Ext.create('CMDBuild.field.ErasableCombo', {
				name: CMDBuild.core.proxy.CMProxyConstants.STARTING_CLASS,
				fieldLabel: tr.starting_class,
				labelWidth: CMDBuild.LABEL_WIDTH,
				maxWidth: CMDBuild.ADM_BIG_FIELD_WIDTH,
				valueField: CMDBuild.core.proxy.CMProxyConstants.ID,
				displayField: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION,
				editable: false,

				store: CMDBuild.ServiceProxy.group.getStartingClassStore(),
				queryMode: 'local'
			});

			this.wrapper = Ext.create('Ext.form.Panel', {
				region: 'center',
				frame: true,
				border: false,

				layout: {
					type: 'vbox',
					align:'stretch'
				},

				items: [
					this.groupName,
					this.groupDescription,
					this.groupType,
					this.groupEmail,
					this.startingClass,
					this.activeCheck
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
			this.disableModify(false);
		},

		/**
		 * @param {CMDBuild.cache.CMGroupModel} group
		 */
		loadGroup: function(group) {
			this.reset();
			this.getForm().loadRecord(group);

			// FIX: to avoid default int value (0) to be displayed
			if (this.startingClass.getValue() == 0)
				this.startingClass.setValue();

			this.groupType.setValue(group.getType());
			this.updateDisableEnableGroup();
		},

		setDefaults: function() {
			this.groupType.setValue(CMDBuild.cache.CMGroupModel.type.NORMAL);
		},

		updateDisableEnableGroup: function() {
			if (this.activeCheck.getValue()) {
				this.enableGroupButton.setText(tr.delete_group);
				this.enableGroupButton.setIconCls('delete');
			} else {
				this.enableGroupButton.setText(tr.enable_group);
				this.enableGroupButton.setIconCls('ok');
			}
		},
	});

})();