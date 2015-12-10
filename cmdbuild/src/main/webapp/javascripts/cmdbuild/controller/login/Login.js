(function() {

	Ext.define('CMDBuild.controller.login.Login', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest'
		],

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onLoginDoLogin',
			'onLoginUserChange'
		],

		/**
		 * @property {CMDBuild.view.login.FormPanel}
		 */
		form: undefined,

		/**
		 * @property {CMDBuild.view.login.LoginViewport}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.login.LoginViewport', { delegate: this });

			// Shorthands
			this.form = this.view.formContainer.form;

			this.setupFields();
		},

		onLoginDoLogin: function() {
			if (!Ext.isEmpty(this.form.role.getValue()) || this.form.getForm().isValid()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.PASSWORD] = this.form.password.getValue();
				params[CMDBuild.core.constants.Proxy.USERNAME] = this.form.user.getValue();

				if (!this.form.role.isHidden())
					params[CMDBuild.core.constants.Proxy.ROLE] = this.form.role.getValue();

				// LoadMask manual manage to avoid to hide on success
				CMDBuild.core.LoadMask.show();
				CMDBuild.core.proxy.session.JsonRpc.login({
					params: params,
					loadMask: false,
					scope: this,
					success: function(response, options, decodedResponse) {
						var urlParams = {};
						urlParams[CMDBuild.core.constants.Proxy.TOKEN] = response.getResponseHeader(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);

						CMDBuild.core.proxy.session.Rest.login({
							params: params,
							urlParams: urlParams,
							scope: this,
							success: function(response, options, decodedResponse) {
								Ext.util.Cookies.set(CMDBuild.core.constants.Proxy.SESSION_TOKEN, urlParams[CMDBuild.core.constants.Proxy.TOKEN]);
							},
							callback: function(options, success, response) {
								// CMDBuild redirect
								if (/administration.jsp$/.test(window.location)) {
									window.location = 'administration.jsp' + window.location.hash;
								} else {
									window.location = 'management.jsp' + window.location.hash;
								}
							}
						});
					},
					failure: function(result, options, decodedResult) {
						CMDBuild.core.LoadMask.hide();

						if (!Ext.isEmpty(decodedResult) && decodedResult[CMDBuild.core.constants.Proxy.REASON] == 'AUTH_MULTIPLE_GROUPS') {
							CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.USERNAME, this.form.user.getValue());
							CMDBuild.configuration.runtime.set(CMDBuild.core.constants.Proxy.GROUPS, decodedResult[CMDBuild.core.constants.Proxy.GROUPS]);

							this.setupFields();

							return false;
						} else {
							decodedResult.stacktrace = undefined; // To not show the detail link in the error pop-up
						}
					}
				});
			}
		},

		onLoginUserChange: function() {
			this.setupFieldsRole(false);
		},

		setupFields: function() {
			if (!Ext.isEmpty(CMDBuild.configuration.runtime)) {
				if (Ext.isEmpty(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME))) {
					this.form.user.focus();
				} else {
					this.form.user.setValue(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME));
					this.form.user.disable();

					this.form.password.hide();
					this.form.password.disable();
				}

				this.setupFieldsRole(!Ext.isEmpty(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUPS)));
			}
		},

		/**
		 * @param {Boolean} state
		 */
		setupFieldsRole: function(state) {
			state = Ext.isBoolean(state) ? state : false;

			if (state && !Ext.isEmpty(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUPS))) {
				this.form.role.getStore().loadData(CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.GROUPS));
				this.form.role.show();
				this.form.role.focus();
			} else {
				this.form.role.hide();
			}
		}
	});

})();