(function() {

	Ext.define('CMDBuild.controller.common.sessionExpired.SessionExpired', {
		extend: 'CMDBuild.controller.common.AbstractController',

		requires: [
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.session.JsonRpc'
		],

		/**
		 * @cfg {Mixed}
		 */
		parentDelegate: undefined,

		/**
		 * @cfg {Array}
		 */
		ajaxParameters: {},

		/**
		 * @cfg {Array}
		 */
		cmfgCatchedFunctions: [
			'onSessionExpiredChangeUserButtonClick',
			'onSessionExpiredLoginButtonClick = onSessionExpiredConfirmButtonClick'
		],

		/**
		 * @cfg {Boolean}
		 */
		passwordFieldEnable: true,

		/**
		 * @property {CMDBuild.view.common.sessionExpired.SessionExpiredWindow}
		 */
		view: undefined,

		/**
		 * @param {Object} configurationObject
		 * @param {Mixed} configurationObject.parentDelegate
		 *
		 * @override
		 */
		constructor: function(configurationObject) {
			this.callParent(arguments);

			this.view = Ext.create('CMDBuild.view.common.sessionExpired.SessionExpiredWindow', { delegate: this });

			// Shorthands
			this.form = this.view.form;

			this.form.password.setDisabled(!this.passwordFieldEnable);

			if (!Ext.isEmpty(this.view))
				this.view.show();
		},

		onSessionExpiredChangeUserButtonClick: function() {
			window.location = '.';
		},

		onSessionExpiredLoginButtonClick: function() {
			this.view.hide();

			var params = {};
			params[CMDBuild.core.constants.Proxy.PASSWORD] = this.form.password.getValue();
			params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);
			params[CMDBuild.core.constants.Proxy.ROLE] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);

			// LoadMask manual manage to avoid to hide on success
			CMDBuild.core.LoadMask.show();
			CMDBuild.core.proxy.session.JsonRpc.login({
				params: params,
				loadMask: false,
				scope: this,
				success: function(response, options, decodedResponse) {
					if (Ext.Object.isEmpty(this.ajaxParameters)) {
						window.location.reload();
					} else {
						CMDBuild.core.LoadMask.hide();

						CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.UNCACHED, this.ajaxParameters);
					}
				},
				failure: function(response, options, decodedResponse) {
					var oldToFront = this.view.toFront;
					this.toFront = Ext.emptyFn;
					this.show();
					this.toFront = oldToFront;

					CMDBuild.core.LoadMask.hide();
				}
			});
		}
	});

})();