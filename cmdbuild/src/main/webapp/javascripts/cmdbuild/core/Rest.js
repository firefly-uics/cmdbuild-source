(function() {

	/**
	 * Verify and setup rest session to be compatible with CAS or header authenticators
	 */
	Ext.define('CMDBuild.core.Rest', { // FIXME: atm header authentication widn't work

		requires: [
			'CMDBuild.core.proxy.session.Rest',
			'CMDBuild.core.proxy.Utils'
		],

		constructor: function() {
			if (Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.proxy.CMProxyConstants.SESSION_TOKEN))) {
				this.fakeCallToGetAuthorizationToken();
			} else {
				// Verify if session with cookie token exists
				var params = {};
				params[CMDBuild.core.proxy.CMProxyConstants.USERNAME] = CMDBuild.Runtime.Username;

				var urlParams = {};
				urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.proxy.CMProxyConstants.SESSION_TOKEN);

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
		 *
		 * @private
		 */
		fakeCallToGetAuthorizationToken: function(urlParams, authorizationKey) {
			var params = {};
			params[CMDBuild.core.proxy.CMProxyConstants.USERNAME] = CMDBuild.Runtime.Username;

			CMDBuild.core.proxy.Utils.generateId({
				scope: this,
				success: function(response, options, decodedResponse) {
//					var urlParams = {};
//					urlParams[CMDBuild.core.proxy.CMProxyConstants.TOKEN] = response.getResponseHeader(CMDBuild.core.proxy.CMProxyConstants.AUTHORIZATION_HEADER_KEY);

					CMDBuild.core.proxy.session.Rest.login({
						params: params,
//						urlParams: urlParams,
						loadMask: false,
						scope: this,
						success: function(response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.proxy.CMProxyConstants.DATA];

							if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse))
								Ext.util.Cookies.set(CMDBuild.core.proxy.CMProxyConstants.SESSION_TOKEN, decodedResponse['_id']);
						}
					});
				}
			});
		}
	});

})();