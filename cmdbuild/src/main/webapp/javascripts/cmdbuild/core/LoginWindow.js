(function() {

	Ext.define('CMDBuild.core.LoginWindow', {
		extend: 'Ext.window.Window',

		requires: ['CMDBuild.core.constants.Proxy'],

		/**
		 * @cfg {Array}
		 */
		ajaxOptions: [],

		/**
		 * @cfg {Boolean}
		 */
		refreshOnLogin: true,

		/**
		 * @property {Ext.form.Panel}
		 */
		form: undefined,

		/**
		 * @property {Ext.form.field.Text}
		 */
		passwordField: undefined,

		/**
		 * @property {Ext.form.field.Hidden}
		 */
		usernameField: undefined,

		height: 155,
		layout: 'fit',
		modal:true,
		title: CMDBuild.Translation.sessionExpired,
		width: 300,

		bodyStyle: {
			padding: '5px 5px 10px 5px'
		},

		initComponent: function() {
			Ext.apply(this, {
				items: [
					this.form = Ext.create('Ext.form.Panel', {
						bodyCls: 'x-panel-body-default-framed',
						border: false,
						cls: 'x-panel-body-default-framed',
						frame: false,
						trackResetOnLoad: true,

						bodyStyle: {
							padding: '5px'
						},

						url: 'services/json/login/login',

						dockedItems: [
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
									Ext.create('CMDBuild.core.buttons.text.Login', {
										hidden: !CMDBuild.Runtime.AllowsPasswordLogin,
										formBind: true,
										scope: this,

										handle: this.doLogin
									}),
									Ext.create('CMDBuild.core.buttons.text.ChangeUser', {
										hidden: !CMDBuild.Runtime.AllowsPasswordLogin,
										scope: this,

										handler: this.reloadPage
									}),
									Ext.create('CMDBuild.core.buttons.text.Confirm', {
										hidden: CMDBuild.Runtime.AllowsPasswordLogin,
										scope: this,

										handler: this.reloadPage
									})
								]
							})
						],

						items: [
							this.messageCmp = Ext.create('Ext.form.field.Display', {
								value: CMDBuild.Translation.sessionExpiredMessage,
								style: {
									padding: '0px 0px 5px 0px'
								}
							}),
							this.usernameField = Ext.create('Ext.form.field.Hidden', {
								name: CMDBuild.core.constants.Proxy.USERNAME,
								value: CMDBuild.Runtime.Username
							}),
							this.passwordField = Ext.create('Ext.form.field.Text', {
								name: CMDBuild.core.constants.Proxy.PASSWORD,
								inputType: 'password',
								fieldLabel: CMDBuild.Translation.password,
								hidden: !CMDBuild.Runtime.AllowsPasswordLogin,
								allowBlank: false,

								listeners: {
									scope: this,
									specialkey: function(field, e, eOpts) {
										if(e.getKey() == e.ENTER)
											this.doLogin(field, e);
									}
								}
							}),
							Ext.create('Ext.form.field.Hidden', {
								name: CMDBuild.core.constants.Proxy.ROLE,
								value: CMDBuild.Runtime.DefaultGroupName
							})
						]
					})
				]
			});

			this.callParent(arguments);
		},

		/**
		 * @param {Array} requestOption
		 */
		addAjaxOptions: function(requestOption) {
			this.ajaxOptions.push(requestOption);
		},

		doLogin: function() {
			CMDBuild.core.LoadMask.show();

			this.hide();

			this.form.getForm().submit({
				important: true,
				scope: this,
				success: function() {
					this.passwordField.reset();

					if (this.refreshOnLogin) {
						window.location.reload();
					} else {
						CMDBuild.core.LoadMask.hide();

						for (var requestOption; requestOption=this.ajaxOptions.pop();) {
							CMDBuild.core.Ajax.request(requestOption);
						}
					}
				},
				failure: function(form, action) {
					this.showWithoutBringingToFront();

					CMDBuild.core.LoadMask.hide();
				}
			});
		},

		reloadPage: function() {
			window.location = '.';
		},

		/**
		 * @param {Boolean} enabled
		 */
		setAuthFieldsEnabled: function(enabled) {
			this.usernameField.setDisabled(!enabled);
			this.passwordField.setDisabled(!enabled);

			if (!enabled)
				this.passwordField.setValue('******');
		},

		/*
		 * Hack to let the error messages appear on top of it.
		 * Ext.Component.toFrontOnShow does not work because Ext.Component.afterShow does not check it and calls toFront anyway!
		 */
		showWithoutBringingToFront: function() {
			var oldToFront = this.toFront;
			this.toFront = Ext.emptyFn;
			this.show();
			this.toFront = oldToFront;
		}
	});

})();