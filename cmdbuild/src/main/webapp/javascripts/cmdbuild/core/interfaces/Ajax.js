(function() {

	Ext.define('CMDBuild.core.interfaces.Ajax', {
		extend: 'Ext.data.Connection',

		requires: [
			'CMDBuild.core.interfaces.messages.Error',
			'CMDBuild.core.interfaces.messages.Warning',
			'CMDBuild.core.interfaces.service.LoadMask',
			'CMDBuild.core.Utils'
		],

		singleton: true,

		/**
		 * @cfg {Boolean}
		 */
		autoAbort: false,

		listeners: {
			beforerequest: function(conn, options, eOpts) {
				CMDBuild.core.interfaces.Ajax.trapCallbacks(conn, options);
			}
		},

		/**
		 * Adapter to manage error/warning display and LoadMask
		 *
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Boolean} success
		 * @param {Object} response
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterCallback: function(options, success, response, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);

			CMDBuild.core.interfaces.service.LoadMask.manage(options.loadMask, false);

			CMDBuild.core.interfaces.messages.Warning.display(decodedResponse);
			CMDBuild.core.interfaces.messages.Error.display(decodedResponse, options);

			Ext.callback(originalFunction, options.scope, [options, success, response]);
		},

		/**
		 * Adapter to add decodedResponse parameters
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterSuccess: function(response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);

			Ext.callback(originalFunction, options.scope, [response, options, decodedResponse]);
		},

		/**
		 * Adapter to add decodedResponse parameters
		 *
		 * @param {Object} response
		 * @param {Object} options - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterFailure: function(response, options, originalFunction) {
			var decodedResponse = CMDBuild.core.interfaces.Ajax.decodeJson(response.responseText);

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
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterCallback, options.scope, [options.callback], true);
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorFailure: function(options) {
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterFailure, options.scope, [options.failure], true);
		},

		/**
		 * @param {Object} options - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorSuccess: function(options) {
			return Ext.bind(CMDBuild.core.interfaces.Ajax.adapterSuccess, options.scope, [options.success], true);
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

			Ext.apply(options, {
				callback: CMDBuild.core.interfaces.Ajax.interceptorCallback(options),
				failure: CMDBuild.core.interfaces.Ajax.interceptorFailure(options),
				success: CMDBuild.core.interfaces.Ajax.interceptorSuccess(options)
			});
		}
	});

})();