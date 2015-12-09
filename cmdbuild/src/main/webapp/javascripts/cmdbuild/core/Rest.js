(function() {

	/**
	 * Verify and setup rest session to be compatible with CAS or header authenticators
	 */
	Ext.define('CMDBuild.core.Rest', {

		requires: [
			'CMDBuild.core.proxy.session.Rest',
			'CMDBuild.core.proxy.Utils'
		],

		constructor: function() {
			if (Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN))) {
				this.fakeCallToGetAuthorizationToken();
			} else {
				// Verify if session with cookie token exists
				var params = {};
				params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);

				var urlParams = {};
				urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN);

				CMDBuild.core.proxy.session.Rest.read({
					params: params,
					urlParams: urlParams,
					loadMask: false,
					scope: this,
					failure: function(response, opts) {
						this.fakeCallToGetAuthorizationToken();
					}
				});
			}
		},

		/**
		 * @param {Object} urlParams
		 * @param {String} authorizationKey
		 */
		fakeCallToGetAuthorizationToken: function(urlParams, authorizationKey) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);

			CMDBuild.core.proxy.Utils.generateId({
				scope: this,
				success: function(response, options, decodedResponse) {
					var urlParams = {};
					urlParams[CMDBuild.core.constants.Proxy.TOKEN] = response.getResponseHeader(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);

					CMDBuild.core.proxy.session.Rest.login({
						params: params,
						urlParams: urlParams,
						loadMask: false,
						scope: this,
						success: function(response, options, decodedResponse) {
							Ext.util.Cookies.set(CMDBuild.core.constants.Proxy.REST_SESSION_TOKEN, urlParams[CMDBuild.core.constants.Proxy.TOKEN]);
						}
					});
				}
			});
		}
	});

})();