(function () {

	/**
	 * Verify and setup rest session to be compatible with CAS or header authenticators
	 */
	Ext.define('CMDBuild.core.Rest', { // FIXME: atm header authentication works with service user from graph configuration

		requires: [
			'CMDBuild.core.proxy.session.Rest',
			'CMDBuild.core.proxy.Utils'
		],

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function () {
//_debug('constructor', Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN)));
//			if (Ext.isEmpty(Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN))) {
//				this.doLoginWithConfigurationUser();
//			} else {
//				// Verify if session with cookie token exists
//				var params = {};
//				params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);
//
//				var urlParams = {};
//				urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//
//				CMDBuild.core.proxy.session.Rest.read({
//					params: params,
//					urlParams: urlParams,
//					loadMask: false,
//					scope: this,
//					failure: function (response, options, decodedResponse) {
//						this.doLoginWithConfigurationUser();
//					},
////					success: function (response, options, decodedResponse) {
////						decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];
////
////						if (Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.ROLE])) {
////							var params = {};
////							params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);
////							params[CMDBuild.core.constants.Proxy.ROLE] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);
////
////							CMDBuild.core.proxy.session.Rest.setGroup({
////								params: params,
////								urlParams: urlParams,
////								loadMask: false,
////								scope: this,
////								success: function (response, options, decodedResponse) {
////	_debug('setGroup success');
////								}
////							});
////						}
////					}
//				});
//			}
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		doLoginWithConfigurationUser: function () {
_debug('doLoginWithConfigurationUser');
			Ext.create('CMDBuild.core.configurations.builder.RelationGraph', {
				callback: this.fakeCallToGetAuthorizationToken,
				scope: this
			});
		},

		/**
		 * @returns {Void}
		 *
		 * @private
		 */
		fakeCallToGetAuthorizationToken: function () {
_debug('fakeCallToGetAuthorizationToken');
			if (
				!Ext.isEmpty(CMDBuild.configuration.graph.get(CMDBuild.core.constants.Proxy.USERNAME))
				&& !Ext.isEmpty(CMDBuild.configuration.graph.get(CMDBuild.core.constants.Proxy.PASSWORD))
			) {
				CMDBuild.core.proxy.Utils.generateId({
					scope: this,
					success: function (response, options, decodedResponse) {
						var params = {};
						params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.graph.get(CMDBuild.core.constants.Proxy.USERNAME);
						params[CMDBuild.core.constants.Proxy.PASSWORD] = CMDBuild.configuration.graph.get(CMDBuild.core.constants.Proxy.PASSWORD);

						var urlParams = {};
						urlParams[CMDBuild.core.constants.Proxy.TOKEN] = response.getResponseHeader(CMDBuild.core.constants.Proxy.AUTHORIZATION_HEADER_KEY);

						CMDBuild.core.proxy.session.Rest.login({
							params: params,
							urlParams: urlParams,
							loadMask: false,
							scope: this,
							success: function (response, options, decodedResponse) {
								decodedResponse = decodedResponse[CMDBuild.core.constants.Proxy.DATA];

								if (Ext.isObject(decodedResponse) && !Ext.Object.isEmpty(decodedResponse)) {
									Ext.util.Cookies.set(CMDBuild.core.constants.Proxy.SESSION_TOKEN, decodedResponse['_id']);

//									this.impersonate(decodedResponse);
								}
							}
						});
					}
				});
			}
		},
//
//		/**
//		 * Impersonate original user to preserve original permissions
//		 *
//		 * @param {Object} loginResponse
//		 *
//		 * @returns {Void}
//		 *
//		 * @private
//		 */
//		impersonate: function (loginResponse) {
//_debug('impersonate', loginResponse);
//			var urlParams = {};
//			urlParams[CMDBuild.core.constants.Proxy.TOKEN] = Ext.util.Cookies.get(CMDBuild.core.constants.Proxy.SESSION_TOKEN);
//			urlParams[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);
//
//			CMDBuild.core.proxy.session.Rest.impersonate({
//				urlParams: urlParams,
//				loadMask: false,
//				scope: this,
//				success: function (response, options, decodedResponse) {
//					if (Ext.isEmpty(loginResponse[CMDBuild.core.constants.Proxy.ROLE])) {
//						var params = {};
//						params[CMDBuild.core.constants.Proxy.USERNAME] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.USERNAME);
//						params[CMDBuild.core.constants.Proxy.ROLE] = CMDBuild.configuration.runtime.get(CMDBuild.core.constants.Proxy.DEFAULT_GROUP_NAME);
//
//						CMDBuild.core.proxy.session.Rest.setGroup({
//							params: params,
//							urlParams: urlParams,
//							loadMask: false,
//							scope: this,
//							success: function (response, options, decodedResponse) {
//_debug('setGroup success');
//							}
//						});
//					}
//				}
//			});
//		}
	});

})();
