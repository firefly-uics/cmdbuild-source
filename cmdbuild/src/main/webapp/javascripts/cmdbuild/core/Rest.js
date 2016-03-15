(function() {

	/**
	 * Verify and setup rest session to be compatible with CAS or header authenticators
	 */
	Ext.define('CMDBuild.core.Rest', { // FIXME: atm header authentication don't works

		requires: [
			'CMDBuild.core.proxy.session.Rest',
			'CMDBuild.core.proxy.Utils'
		],

		constructor: function () {
//			if (Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN))) {
//				this.fakeCallToGetAuthorizationToken();
//			} else {
//				// Verify if session with cookie token exists
//				var params = {};
//				params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.Runtime.Username;
//
//				var urlParams = {};
//				urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//				CMDBuild.core.proxy.session.Rest.read({
//					params: params,
//					urlParams: urlParams,
//					loadMask: false,
//					scope: this,
//					failure: function (response, opts) {
//						this.fakeCallToGetAuthorizationToken();
//					}
//				});
//			}
		},

		/**
		 * @param {Object} urlParams
		 * @param {String} authorizationKey
		 *
		 * @private
		 */
		fakeCallToGetAuthorizationToken: function (urlParams, authorizationKey) {
			var params = {};
			params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.Runtime.Username;

			CMDBuild.core.proxy.Utils.generateId({
				scope: this,
				success: function(response, options, decodedResponse) {
//					var urlParams = {};
//					urlParams[CMDBuild.core.constants.Proxy.TOKEN] = response.getResponseHeader(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);

					CMDBuild.core.proxy.session.Rest.login({
						params: params,
//						urlParams: urlParams,
						loadMask: false,
						scope: this,
						success: function (response, options, decodedResponse) {
							decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

							if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse))
								Ext.util.Cookies.set(CMDBuild.core.constants.Proxy.SESSION_TOKEN, decodedResponse['_id']);
						}
					});
				}
			});
		}
	});

})();
