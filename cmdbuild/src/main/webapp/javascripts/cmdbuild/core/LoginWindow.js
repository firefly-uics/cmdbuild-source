CMDBuild.LoginWindowClass = Ext.extend(Ext.Window, {
	ajaxOptions: [],

	initComponent: function() {
		var tr = CMDBuild.Translation.login;
		
		var doLogin = function () {
			CMDBuild.LoadMask.get().show();
			this.form.getForm().submit({
				success: function() {
					this.passwordField.reset();
					this.hide();
					if (this.refreshOnLogin) {
						window.location.reload();
					} else {
						CMDBuild.LoadMask.get().hide();
						for (var requestOption; requestOption=this.ajaxOptions.pop();) {
							CMDBuild.Ajax.request(requestOption);
						}
					}
				},
				failure: function(form, action) {					
					var tr = CMDBuild.Translation.errors.reasons;
					if (this.messageCmp.items) {
						this.messageCmp.remove(0);
					}
					if (tr && action.result && action.result.reason) {
						var errorTranslation = tr[action.result.reason];
						if (errorTranslation) {
							this.messageCmp.add({html: errorTranslation});
						} else {
							this.messageCmp.add({html: CMDBuild.Translation.errors.unknown_error});
						}
					}
					this.messageCmp.doLayout();
					return false;
				},
				scope: this
			});
		};
		
		Ext.apply(this, {
			title: tr.relogin_title,
			width: 300,
			height: 155,
			layout: 'fit',
			border: true,
			frame: true,			
			items: [this.form = new Ext.form.FormPanel({
				url: 'services/json/login/login',
				frame: true,
				border: false,			
				trackResetOnLoad: true,
				items: [this.messageCmp = new Ext.Panel({
					xtype: 'panel',
					frame: false,
					border: false,
					style: {padding: '5px 5px 15px 0'},
					html: tr.relogin_message
				}),
				this.usernameField = new Ext.form.Hidden({
					name: 'username',
					value: CMDBuild.Runtime.Username
				}),
				this.passwordField = new Ext.form.Field({
					name: 'password',
					inputType : 'password',
					fieldLabel : tr.password,
					allowBlank : false
				}),
				{
					name: 'role',
					xtype: 'hidden',
					value: CMDBuild.Runtime.RoleId
				}],
				buttonAlign: 'center',
				buttons: [{
					text : tr.login,
					formBind : true,
					handler: doLogin,
					scope: this
				},{
					text : tr.change_user,
					handler : function() {
						window.location = '.';
					}
				}]
			})]
		});
		CMDBuild.LoginWindowClass.superclass.initComponent.apply(this);
	},

	addAjaxOptions: function(requestOption) {
		this.ajaxOptions.push(requestOption);
	},

	setAuthFieldsEnabled: function(enabled) {
		this.usernameField.setDisabled(!enabled);
		this.passwordField.setDisabled(!enabled);
		if (!enabled) {
			this.passwordField.setValue("******");
		}
	}
});

CMDBuild.LoginWindow = new CMDBuild.LoginWindowClass({
	modal:true,
	refreshOnLogin: true
});
