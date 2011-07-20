Ext.onReady(function() {
	Ext.QuickTips.init();
	CMDBuild.ChainedAjax.execute({		
		requests: [{
			url: 'services/json/schema/setup/getconfiguration',
			params: { name: 'cmdbuild' },
			success: function(response, options, decoded) {
				CMDBuild.Config.cmdbuild = decoded.data;
			}
		}],
		fn: function() {
			var window = new CMDBuild.LoginPanel({ id : "login"});
		}
	});
});

Ext.define("CMDBuild.LoginPanel", {
	extend: "Ext.panel.Panel",
	tr: CMDBuild.Translation.login,
	initComponent: function() {
		this.buildLanguagesCombo();
		var scope = this;

		var enterKeyListener = {
			'specialkey': function(field, event) {
				if(event.getKey() == event.ENTER) {
					scope.doLogin(field, event);
				}
			}
		};
		
		this.user = new Ext.form.TextField({
			fieldLabel : this.tr.username,
			name : 'username',
			allowBlank : false,
			listeners: enterKeyListener,
			scope: this
		});
		
		this.password = new Ext.form.TextField({
			fieldLabel : this.tr.password,
			name : 'password',
			inputType : 'password',
			allowBlank : false,
			listeners: enterKeyListener,
			scope: this
		});	

		this.role = new Ext.form.ComboBox({
			id: 'rolefield',
			fieldLabel : this.tr.multi_group,
			hideMode: "offsets",
			name : 'role',
			hiddenName: 'role',
			valueField : 'name',
			displayField : 'value',
			queryMode : 'local',
			store: new Ext.data.Store({
				fields : ['name', 'value']
			}),
			listeners: enterKeyListener,
			scope: this
		});	
		
		var fields = this.buildFieldsArray();
		
		this.form = new Ext.form.FormPanel({
			xtype: 'form',
			labelWidth : 100,
			title : this.tr.title,
			frame : true,
			defaultType : 'textfield',
			items : fields,
			buttonAlign: 'center',
			buttons : [{
				text : this.tr.login,
				handler: this.doLogin,
				scope: this
			}]
		});

		Ext.apply(this, {
			renderTo : 'login_box',
			frame: false,
			border: false,
			hideMode: "offsets",
			border: false,
			items: [this.form,{
				xtype: 'panel',
				border: false,
				contentEl: 'release_box'
			}]
		});

		this.user.on('change', this.disableRoles, this);
		this.callParent(arguments);

		this.on('afterrender', this.setupFields, this);
		this.role.on('render', this.setupFields, this); //backward compatibility wit Ext2.2
	},
	
	//private
	setupFields: function() {
		if (CMDBuild.Runtime && CMDBuild.Runtime.Username) {
			this.user.setValue(CMDBuild.Runtime.Username);
			this.user.disable();
			this.password.hide();
			this.password.disable();
		} else {
			this.user.focus();
		}

		if (CMDBuild.Runtime && CMDBuild.Runtime.Groups) {
			this.enableRoles(CMDBuild.Runtime.Groups)
		} else {
			this.disableRoles();
		}
	},
	
	//private
	buildFieldsArray: function() {
		if (this.language) {
			return [this.language, this.user, this.password, this.role];
		} else {
			return [this.user, this.password, this.role];
		}
	},
	
	//private
	buildLanguagesCombo: function() {
		if (CMDBuild.Config.cmdbuild.languageprompt == "true") {

			this.language = new Ext.form.ComboBox({
				fieldLabel: this.tr.language,
				store: new Ext.data.Store( {
					model : 'TranslationModel',
					proxy : {
						type : 'ajax',
						url : 'services/json/utils/listavailabletranslations',
						reader : {
							type : 'json',
							root : 'translations'
						}
					},
					autoLoad : true
				}),
				valueField: 'name',
				displayField: 'value',
				queryMode: 'local',
				iconClsField: 'name',
				iconClsPrefix: 'ux-flag-'
			});
			
			this.language.on('select', function(combo, record) {
				window.location = Ext.String.format('?language={0}', record[0].get("name"));
			}, this);

			this.language.store.on('load', function() {
				this.setValue(getCurrentLanguage());
			}, this.language);
		}
	},
	
	//private
	enableRoles: function(roles) {
		this.role.store.loadData(roles);
		this.role.enable();
		this.role.show();
		this.role.focus();
	},
	
	//private
	disableRoles: function() {
		this.role.disable();
		this.role.hide();
	},
	
	//private
	doLogin: function(field, event) {
		var form = this.form.getForm();
		if (!form.isValid()) {
			return;
		}
		
		CMDBuild.LoadMask.get().show();
		CMDBuild.ServiceProxy.doLogin({
			params: form.getValues(),
			scope : this,
			success : function() {
				if (/administration.jsp$/.test(window.location)) {
					window.location = 'administration.jsp';
				} else {
					window.location = 'management.jsp';
				}
			},
			failure : function(response, options, decoded) {
				CMDBuild.LoadMask.get().hide();
				if (decoded && decoded.reason == 'AUTH_MULTIPLE_GROUPS') {
					// multiple groups for this user
					// TODO Disable user/pass on multiple groups
					this.enableRoles(decoded.groups);
					return false;
				} else {
					decoded.stacktrace = undefined; //to not show the detail link in the error pop-up
				}
			}
		});
	}
});