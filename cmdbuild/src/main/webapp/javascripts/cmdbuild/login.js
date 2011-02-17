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
			var window = new CMDBuild.LoginPanel();
		}
	});
});

CMDBuild.LoginPanel = Ext.extend(Ext.Panel, {
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
			width : 180,
			name : 'username',
			allowBlank : false,
			listeners: enterKeyListener,
			scope: this
		});
		
		this.password = new Ext.form.TextField({
			fieldLabel : this.tr.password,
			name : 'password',
			width : 180,
			inputType : 'password',
			allowBlank : false,
			listeners: enterKeyListener,
			scope: this
		});	

		this.role = new Ext.form.ComboBox({
			id : 'rolefield',
			fieldLabel : this.tr.multi_group,
			width : 180,
			name : 'role',
			hiddenName: 'role',
			triggerAction : 'all',
			valueField : 'name',
			displayField : 'value',
			mode : 'local',
			store: new Ext.data.JsonStore({
				fields : ['name', 'value'],
				data : []
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
			url : 'services/json/login/login',
			defaultType : 'textfield',
			monitorValid : true,	
			items : fields,
			buttonAlign: 'center',
			buttons : [{
				text : this.tr.login,
				formBind : true,
				handler: this.doLogin,
				scope: this
			}]
		});

		Ext.apply(this, {
			renderTo : 'login_box',
			border: false,
			items: [this.form,{
				xtype: 'panel',
				border: false,
				contentEl: 'release_box'
			}]
		});
		this.user.on('change', this.disableRoles, this);
		CMDBuild.LoginPanel.superclass.initComponent.apply(this, arguments);

		this.on('afterrender', this.setupFields, this);
		this.role.on('render', this.setupFields, this); //backward compatibility wit Ext2.2
	},
	
	//private
	setupFields: function() {
		if (CMDBuild.Runtime && CMDBuild.Runtime.Username) {
			this.user.setValue(CMDBuild.Runtime.Username);
			this.user.disable();
			this.password.hideContainer();
		} else {
			this.user.focus();
		}
		if (CMDBuild.Runtime && CMDBuild.Runtime.Groups) {
			this.role.store.loadData(CMDBuild.Runtime.Groups);
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
			var languageStore = new Ext.data.JsonStore({
		        url: 'services/json/utils/listavailabletranslations',
		        root: "translations",
		        fields: ['name', 'value'],
		        autoLoad: true,
		        sortInfo: { field: 'value', direction: 'ASC' }
			});
			
			this.language = new Ext.form.ComboBox({
				fieldLabel: this.tr.language,
				width : 180,
				triggerAction: 'all',
				store: languageStore,
				valueField: 'name',
				displayField: 'value',
				mode: 'local',
				plugins: new Ext.ux.plugins.IconCombo(),
				iconClsField: 'name',
				iconClsPrefix: 'ux-flag-'
			});
			
			this.language.on('select', function(combo, record) {
	            window.location = String.format('?language={0}', record.data.name);
			}, this);
			
			languageStore.on('load', function() {
				this.setValue(getCurrentLanguage());
			}, this.language);
		}
	},
	
	//private
	enableRoles: function(roles) {
		this.role.store.loadData(roles);
		this.role.enable();
		this.role.showContainer();
		this.role.focus();
	},
	
	//private
	disableRoles: function() {
		this.role.disable();
		this.role.hideContainer();
	},
	
	//private
	doLogin: function(field, event) {
		var form = this.form.getForm();		
		if (!form.isValid())
			return;
		CMDBuild.LoadMask.get().show();
		CMDBuild.Ajax.request({
			important: true,
			url: form.url,
			params: form.getValues(),
			method : 'POST',
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