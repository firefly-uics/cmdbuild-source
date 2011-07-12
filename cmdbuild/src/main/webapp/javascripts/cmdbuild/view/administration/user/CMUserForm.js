(function() {
	var tr = CMDBuild.Translation.administration.modsecurity.user;

	Ext.define("CMDBuild.view.administration.user.CMUserForm", {
		extend : "Ext.panel.Panel",
		mixins : {
			cmFormFunctions : "CMDBUild.view.common.CMFormFunctions"
		},
		initComponent : function() {

			this.modifyButton = new Ext.button.Button( {
				iconCls : 'modify',
				text : tr.modify_user,
				scope : this,
				handler : function() {
					this.enableFieldset(this.userInfo);
				}
			});

			this.modifyPassword = new Ext.button.Button( {
				iconCls : 'password',
				text : tr.change_password,
				scope : this,
				handler : function() {
					this.enableFieldset(this.userPassword);
				}
			});

			this.disableUser = new Ext.button.Button( {
				iconCls : 'delete',
				text : tr.disable_user
			});

			this.saveButton = new CMDBuild.buttons.SaveButton();
			this.abortButton = new CMDBuild.buttons.AbortButton();

			this.cmTBar = [ this.modifyButton, this.modifyPassword, this.disableUser ];
			this.cmButtons = [ this.saveButton, this.abortButton ];

			var userName = new Ext.form.TextField( {
				xtype : 'textfield',
				id : 'username',
				fieldLabel : tr.username,
				width : 220,
				allowBlank : false,
				name : 'username',
				cmImmutable : true
			});

			var userDescription = new Ext.form.TextField( {
				xtype : 'textfield',
				fieldLabel : tr.description,
				width : 220,
				allowBlank : false,
				name : 'description'
			});

			var userEmail = new Ext.form.TextField( {
				xtype : 'textfield',
				vtype : 'emailOrBlank',
				fieldLabel : tr.email,
				width : 220,
				allowBlank : true,
				name : 'email'
			});

			this.defaultGroupStore = new Ext.data.JsonStore( {
				autoLoad : true,
				model : "CMDBuild.cache.CMGroupModelForCombo",
				proxy : {
					type : 'ajax',
					url : "services/json/schema/modsecurity/getusergrouplist",
					reader : {
						type : 'json',
						root : 'result'
					}
				},
				sorters : [ {
					property : 'description',
					direction : "ASC"
				} ]
			});
			
			//TODO 3 to 4 something went wrong with the group reload
			this.defaultGroup = new Ext.form.ComboBox( {
				name : 'defaultgroup',
				fieldLabel : tr.defaultgroup,
				triggerAction : 'all',
				valueField : 'id',
				displayField : 'description',
				editable : false,
				allowBlank : true,
				store : this.defaultGroupStore,
				queryMode : 'local'
			});

			this.userInfo = new Ext.form.FieldSet( {
				title : tr.user_info,
				region : 'west',
				margins : '5 0 5 5',
				split : true,
				autoHeight : true,
				flex : 1,
				items : [ userName, userDescription, userEmail,
					this.defaultGroup, {
						xtype : 'xcheckbox',
						fieldLabel : tr.isactive,
						name : 'isactive',
						checked : true
					} ]
			});

			this.userPassword = new Ext.form.FieldSet( {
				title : tr.password,
				region : 'center',
				autoHeight : true,
				autoScroll : true,
				margins : '5 5 5 5',
				flex : 1,
				items : [ {
					xtype : 'textfield',
					inputType : 'password',
					id : 'user_password',
					name : 'password',
					fieldLabel : tr.password,
					width : 220,
					allowBlank : false
				}, {
					xtype : 'textfield',
					inputType : 'password',
					fieldLabel : tr.confirmation,
					width : 220,
					allowBlank : false,
					name : 'confirmation',
					vtype : 'password',
					initialPassField : 'user_password'
				} ]
			});

			this.form = new Ext.form.Panel({
				autoScroll: true,
				region : "center",
				layout : 'hbox',
				frame : false,
				items : [ this.userInfo, this.userPassword ]
			});

			Ext.apply(this, {
				frame : true,
				border : false,
				layout : "border",
				tbar : this.cmTBar,
				items : [ this.form ],
				buttonAlign: true,
				buttons : this.cmButtons
			});

			this.callParent(arguments);
			this.disableModify();
		},

		getForm : function() {
			return this.form.getForm();
		},

		onUserSelected : function(user) {
			this.reset();
			this.getForm().loadRecord(user);
			this.disableModify(enableCMTBar = true);
			this.updateDisableActionTextAndIconClass(user.get("isactive"));
			this.defaultGroupStore.load( {
				params : {
					userid : user.get("userid")
				}
			});
		},

		onAddUserClick : function() {
			this.reset();
			this.enableModify(all=true);
			this.defaultGroup.disable();
		},

		enableFieldset : function(f) {
			f.cascade(function(item) {
				if (item && (item instanceof Ext.form.Field)) {
					var name = item._name || item.name;// for compatibility I can not change the name of old attrs
					if (!item.cmImmutable) {
						item.enable();
					}
				}
			});
			this.disableCMTbar();
			this.enableCMButtons();
			this.focusOnFirstEnabled();
		},

		updateDisableActionTextAndIconClass : function(isactive) {
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