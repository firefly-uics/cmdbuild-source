(function () {

	Ext.define('CMDBuild.core.CookiesManager', {

		requires: ['CMDBuild.core.constants.Proxy'],

		singleton: true,

		// Authorization cooky manage methods
			/**
			 * @returns {Void}
			 *
			 * @public
			 */
			authorizationClear: function () {
				Ext.util.Cookies.clear(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);
			},

			/**
			 * @returns {String}
			 *
			 * @public
			 */
			authorizationGet: function () {
				return Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);
			},

			/**
			 * @param {String} sessionId
			 *
			 * @returns {Void}
			 *
			 * @public
			 */
			authorizationInit: function (sessionId) {
				if (!Ext.isEmpty(sessionId))
					return Ext.util.Cookies.set(
						CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY, // Name
						sessionId, // Value
						null, // Expiration date
						Ext.Array.slice(window.location.pathname.split('/'), 0, -1).join('/') + '/'
					);

				return _error('empty session id parameter', this);
			},

			/**
			 * @returns {Boolean}
			 *
			 * @public
			 */
			authorizationIsEmpty: function () {
				return Ext.isEmpty(CMDBuild.core.CookiesManager.authorizationGet());
			},

			/**
			 * @returns {Void}
			 *
			 * @public
			 */
			authorizationExpirationUpdate: function () {
				var expirationDate = null;

				if (!Ext.isEmpty(CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.SESSION_TIMEOUT))) {
					expirationDate = new Date();
					expirationDate.setSeconds(expirationDate.getSeconds() + CMDBuild.configuration.instance.get(CMDBuild.core.constants.Proxy.SESSION_TIMEOUT))
				}

				Ext.util.Cookies.set(
					CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY, // Name
					CMDBuild.core.CookiesManager.authorizationGet(), // Value
					expirationDate, // Expiration date
					Ext.Array.slice(window.location.pathname.split('/'), 0, -1).join('/') + '/'
				);
			}
	});

})();
