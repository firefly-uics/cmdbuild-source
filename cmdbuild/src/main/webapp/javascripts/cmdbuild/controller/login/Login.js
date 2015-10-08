(function() {

	Ext.define('CMDBuild.controller.login.Login', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.session.JsonRpc',
			'CMDBuild.core.proxy.session.Rest',
			'CMDBuild.core.proxy.Configuration'
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

		disableRoles: function() {
			this.form.role.disable();
			this.form.role.hide();
		},

		/**
		 * @param {Array} roles
		 */
		enableRoles: function(roles) {
			this.form.role.getStore().loadData(roles);
			this.form.role.enable();
			this.form.role.show();
			this.form.role.focus();
		},

		onLoginDoLogin: function() {
			if (!Ext.isEmpty(this.form.role.getValue()) || this.form.getForm().isValid()) {
				var params = {};
				params[CMDBuild.core.constants.Proxy.PASSWORD] = this.form.password.getValue();
				params[CMDBuild.core.constants.Proxy.USERNAME] = this.form.user.getValue();

				if (!this.form.role.isHidden())
					params[CMDBuild.core.constants.Proxy.ROLE] = this.form.role.getValue();

				CMDBuild.core.proxy.session.JsonRpc.login({
					params: params,
					scope: this,
					success: function(response, options, decodedResponse) {
						var urlParams = {};
						urlParams[CMDBuild.core.constants.Proxy.TOKEN] = response.getResponseHeader(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);

						CMDBuild.core.proxy.session.Rest.login({
							params: params,
							urlParams: urlParams,
							scope: this,
							success: function(response, options, decodedResponse) {
								Ext.util.Cookies.set(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN, urlParams[CMDBuild.core.constants.Proxy.TOKEN]);
							},
							callback: function(records, operation, success) {
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
						if (!Ext.isEmpty(decodedResult) && decodedResult[CMDBuild.core.constants.Proxy.REASON] == 'AUTH_MULTIPLE_GROUPS') {
							// Multiple groups for this user
							// TODO Disable user/pass on multiple groups
							this.enableRoles(decodedResult[CMDBuild.core.constants.Proxy.GROUPS]);

							return false;
						} else {
							decodedResult.stacktrace = undefined; // To not show the detail link in the error pop-up
						}
					}
				});
			}
		},

		onLoginUserChange: function() {
			this.disableRoles();
		},

		setupFields: function() {
			if (
				!Ext.isEmpty(CMDBuild.Runtime)
				&& !Ext.isEmpty(CMDBuild.Runtime.Username)
			) {
				this.form.user.setValue(CMDBuild.Runtime.Username);
				this.form.user.disable();

				this.form.password.hide();
				this.form.password.disable();
			} else {
				this.form.user.focus();
			}

			if (
				!Ext.isEmpty(CMDBuild.Runtime)
				&& !Ext.isEmpty(CMDBuild.Runtime.Groups)
			) {
				this.enableRoles(CMDBuild.Runtime.Groups);
			} else {
				this.disableRoles();
			}
		}
	});

})();