(function() {

	Ext.define('CMDBuild.core.interfaces.Ajax', {
		extend: 'Ext.data.Connection',

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.messages.Error',
			'CMDBuild.core.interfaces.messages.Warning',
			'CMDBuild.core.interfaces.service.LoadMask',
			'CMDBuild.core.LoadMask',
			'CMDBuild.core.Utils'
		],

		singleton: true,

		/**
		 * @cfg {Boolean}
		 */
		autoAbort: false,

		listeners: {
			beforerequest: function(conn, options, eOpts) {
//				if (!Ext.isEmpty(options.loadMask) && options.loadMask)
//					CMDBuild.core.LoadMask.show();

				CMDBuild.core.interfaces.Ajax.trapCallbacks(conn, options);
			}
		},

		/**
		 * Adapter to override parameters and manage error/warning display
		 *
		 * @param {Object} response
		 * @param {Object} options - the options config object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterCallback: function(response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);
			decodedResponse.success = Ext.isBoolean(decodedResponse.success) ? decodedResponse.success : true; // Should be false but if it's here without errors response is a valid JSON
_debug('adapterCallback', arguments);
			CMDBuild.core.interfaces.service.LoadMask.manage(options.loadMask, false);

			CMDBuild.core.interfaces.messages.Warning.display(options.result);
			CMDBuild.core.interfaces.messages.Error.display(options.result, options);

			Ext.callback(originalFunction, options.scope, [options, decodedResponse.success, response]);
		},

		/**
		 * Adapter to override parameters
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterSuccess: function(response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);
_debug('adapterSuccess', arguments);
			Ext.callback(originalFunction, options.scope, [response, options, decodedResponse]);
		},

		/**
		 * Adapter to override parameters
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterFailure: function(response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);
_debug('adapterFailure', arguments);
			Ext.callback(originalFunction, options.scope, [response, options, decodedResponse]);
		},

		/**
		 * @param {String} jsonResponse
		 *
		 * @returns {Object or String}
		 *
		 * @private
		 */
		decodeJson: function(jsonResponse) {
			jsonResponse = Ext.isEmpty(jsonResponse) ? '{"success":true,"response":null}' : jsonResponse; // Empty response manage

			if (CMDBuild.core.Utils.isJsonString(jsonResponse)) {
				if (!Ext.isEmpty(jsonResponse))
					jsonResponse = jsonResponse.replace(/<\/\w+>$/, '');

				// If throws an error so that wasn't a valid json string
				try {
					return Ext.decode(jsonResponse);
				} catch (e) {
					_error(e, 'CMDBuild.core.interfaces.Ajax');
				}

				return '';
			}

			_error('invalid json string: "' + jsonResponse + '"', 'CMDBuild.core.interfaces.Ajax');

			return '';
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorCallback: function(options) {
_debug('interceptorCallback', options);
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterCallback, options.scope, [options.callback], true);
		},

		/**
		 * Builds failure interceptor to create sequence with callback
		 *
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorFailure: function(options) {
_debug('interceptorFailure', options);
			return Ext.Function.createSequence(
				Ext.bind(CMDBuild.core.interfaces.Ajax.adapterFailure, options.scope, [options.failure], true),
				options.callback,
				options.scope
			);
		},

		/**
		 * Builds success interceptor to create sequence with callback
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		interceptorSuccess: function(options) {
_debug('interceptorSuccess', options);
			return Ext.Function.createSequence(
				Ext.bind(CMDBuild.core.interfaces.Ajax.adapterSuccess, options.scope, [options.success], true),
				options.callback,
				options.scope
			);
		},

		/**
		 * Manually builds callback's interceptors to manage loadMask property and callbacks build
		 *
		 * @param {Object} conn
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		trapCallbacks: function(conn, options) {
			CMDBuild.core.interfaces.service.LoadMask.manage(options.loadMask, true);

			options.callback = CMDBuild.core.interfaces.Ajax.interceptorCallback(options); // First of all because is related to others
			options.failure = CMDBuild.core.interfaces.Ajax.interceptorFailure(options);
			options.success = CMDBuild.core.interfaces.Ajax.interceptorSuccess(options);
		}
	});

})();

//(function() {
//
//	/**
//	 * @alias Ext.ajax
//	 */
//	Ext.define('CMDBuild.core.Ajax', {
//		extend: 'Ext.data.Connection',
//
//		requires: [
//			'CMDBuild.core.constants.Proxy',
//			'CMDBuild.core.LoadMask',
//			'CMDBuild.core.Message'
//		],
//
//		singleton: true,
//
//		/**
//		 * @cfg {Boolean}
//		 */
//		autoAbort: false,
//
//		listeners: {
//			beforerequest: function(conn, options, eOpts) {
//				if (!Ext.isEmpty(options.loadMask) && options.loadMask)
//					CMDBuild.core.LoadMask.show();
//
//				CMDBuild.core.Ajax.trapCallbacks(conn, options);
//			}
//		},
//
//		/**
//		 * @param {Object} response
//		 * @param {Object} options
//		 * @param {Function} failure
//		 */
//		decodeFailure: function(response, options, failure) {
//			var decodedResponse = CMDBuild.core.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
//
//			Ext.callback(failure, this, [response, options, decodedResponse]);
//		},
//
//		/**
//		 * @param {String} jsonResponse
//		 *
//		 * @returns {Object or String}
//		 */
//		decodeJSONwhenMultipartAlso: function(jsonResponse) {
//			jsonResponse = Ext.isEmpty(jsonResponse) ? '{"success":true,"response":null}' : jsonResponse; // Empty response manage
//
//			if (!Ext.isEmpty(jsonResponse))
//				jsonResponse = jsonResponse.replace(/<\/\w+>$/, '');
//
//			// If throws an error so that wasn't a valid json string
//			try {
//				return Ext.decode(jsonResponse);
//			} catch (e) {
//				_error(e, 'CMDBuild.core.Ajax');
//			}
//
//			return '';
//		},
//
//		/**
//		 * @param {Object} response
//		 * @param {Object} options
//		 * @param {Object} decodedResponse
//		 */
//		displayErrors: function(response, options, decodedResponse) {
//			if (
//				!Ext.isEmpty(decodedResponse)
//				&& !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.ERRORS]) && Ext.isArray(decodedResponse[CMDBuild.core.constants.Proxy.ERRORS])
//			) {
//				Ext.Array.forEach(decodedResponse[CMDBuild.core.constants.Proxy.ERRORS], function(errorObject, i, allErrorObjects) {
//					if (!Ext.Object.isEmpty(errorObject))
//						CMDBuild.core.Ajax.showError(response, errorObject, options);
//				}, this);
//			}
//		},
//
//		/**
//		 * @param {Object} decodedResponse
//		 */
//		displayWarnings: function(decodedResponse) {
//			if (
//				!Ext.isEmpty(decodedResponse)
//				&& !Ext.isEmpty(decodedResponse[CMDBuild.core.constants.Proxy.WARNINGS]) && Ext.isArray(decodedResponse[CMDBuild.core.constants.Proxy.WARNINGS])
//			) {
//				Ext.Array.forEach(decodedResponse[CMDBuild.core.constants.Proxy.WARNINGS], function(warningObject, i, allWrarningObjects) {
//					if (!Ext.Object.isEmpty(warningObject))
//						CMDBuild.core.Ajax.showWarning(warningObject);
//				}, this);
//			}
//		},
//
//		/**
//		 * @param {String} reasonName
//		 * @param {Object} reasonParameters
//		 *
//		 * @returns {String}
//		 */
//		formatMessage: function(reasonName, reasonParameters) {
//			if (
//				!Ext.isEmpty(CMDBuild.Translation.errors.reasons)
//				&& !Ext.isEmpty(CMDBuild.Translation.errors.reasons[reasonName])
//			) {
//				return Ext.String.format.apply(null, [].concat(CMDBuild.Translation.errors.reasons[reasonName]).concat(reasonParameters));
//			}
//
//			return '';
//		},
//
//		/**
//		 * @param {Object} response
//		 * @param {Object} error
//		 * @param {Object} options
//		 */
//		showError: function(response, errorObject, options) {
//			var errorTitle = null;
//			var errorBody = {
//				text: CMDBuild.Translation.errors.unknown_error,
//				detail: undefined
//			};
//
//			if (!Ext.Object.isEmpty(errorObject)) {
//				var detail = '';
//				var reason = errorObject.reason;
//
//				// Add URL that generate the error
//				if (
//					!Ext.Object.isEmpty(options)
//					&& !Ext.isEmpty(options.url)
//				) {
//					detail = 'Call: ' + options.url + '\n';
//
//					var line = '';
//
//					for (var i = 0; i < detail.length; ++i)
//						line += '-';
//
//					detail += line + '\n';
//				}
//
//				detail += 'Error: ' + errorObject.stacktrace; // Add to the details the server stacktrace
//
//				errorBody.detail = detail;
//
//				if (!Ext.isEmpty(reason)) {
//					if (reason == 'AUTH_NOT_LOGGED_IN' || reason == 'AUTH_MULTIPLE_GROUPS') {
//						var loginWindow = Ext.create('CMDBuild.core.LoginWindow', { ajaxOptions: options });
//						loginWindow.setAuthFieldsEnabled(reason == 'AUTH_NOT_LOGGED_IN');
//						loginWindow.show();
//
//						return;
//					}
//
//					var errorString = CMDBuild.core.Ajax.formatMessage(reason, errorObject.reasonParameters);
//
//					if (Ext.isEmpty(errorString)) {
//						_error('cannot format error message from "' + errorObject + '"', 'CMDBuild.core.Ajax');
//					} else {
//						errorBody.text = errorString;
//					}
//				}
//			} else {
//				if (
//					Ext.isEmpty(response)
//					|| response.status == 200
//					|| response.status == 0
//				) {
//					errorTitle = CMDBuild.Translation.errors.error_message;
//					errorBody.text = CMDBuild.Translation.errors.unknown_error;
//				} else if (response.status) {
//					errorTitle = CMDBuild.Translation.errors.error_message;
//					errorBody.text = CMDBuild.Translation.errors.server_error_code+response.status;
//				}
//			}
//
//			CMDBuild.core.Message.error(
//				errorTitle,
//				errorBody,
//				options.form
//			);
//		},
//
//		/**
//		 * @param {Object} warningObject
//		 */
//		showWarning: function(warningObject) {
//			if (!Ext.Object.isEmpty(warningObject)) {
//				var warningString = CMDBuild.core.Ajax.formatMessage(warningObject.reason, warningObject.reasonParameters);
//
//				if (Ext.isEmpty(warningString)) {
//					_error('cannot format warning message from "' + warningObject + '"', 'CMDBuild.core.Ajax');
//				} else {
//					CMDBuild.core.Message.warning(null, warningString);
//				}
//			}
//		},
//
//		/**
//		 * @param {Ext.data.Connection} conn
//		 * @param {Object} options - the options config object passed to the request method
//		 */
//		trapCallbacks: function(conn, options) {
//			var callbackScope = options.scope || this;
//			var failure = Ext.emptyFn;
//
//			options.success = Ext.bind(CMDBuild.core.Ajax.unmaskAndCheckSuccess, callbackScope, [options.success], true);
//
//			// The error message is not shown if options.failure is present and returns false
//			if (!Ext.isEmpty(options.failure) && Ext.isFunction(options.failure)) {
//
//				failure = Ext.Function.createInterceptor(CMDBuild.core.Ajax.displayErrors, options.failure, callbackScope);
//			} else {
//
//				failure = Ext.bind(CMDBuild.core.Ajax.displayErrors, this);
//			}
//
//			options.failure = Ext.bind(CMDBuild.core.Ajax.decodeFailure, this, [failure], true);
//		},
//
//		/**
//		 * @param {Object} response
//		 * @param {Object} options
//		 * @param {Function} success
//		 */
//		unmaskAndCheckSuccess: function(response, options, success) {
//			var decodedResponse = CMDBuild.core.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
//
//			if (!Ext.isEmpty(options.loadMask) && options.loadMask)
//				CMDBuild.core.LoadMask.hide();
//
//			CMDBuild.core.Ajax.displayWarnings(decodedResponse);
//
//			if (!Ext.isEmpty(decodedResponse) && decodedResponse.success) {
//				Ext.callback(success, this, [response, options, decodedResponse]);
//			} else {
//				Ext.callback(options.failure, this, [response, options]);
//			}
//		}
//	});
//
//})();