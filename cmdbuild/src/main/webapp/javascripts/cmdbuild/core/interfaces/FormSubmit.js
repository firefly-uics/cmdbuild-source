(function() {

	Ext.define('CMDBuild.core.interfaces.FormSubmit', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.messages.Error',
			'CMDBuild.core.interfaces.messages.Warning',
			'CMDBuild.core.LoadMask'
		],

		singleton: true,

		/**
		 * Adapter to override parameters and manage error/warning display
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options config object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterCallback: function(form, action, originalFunction) {
			CMDBuild.core.interfaces.FormSubmit.manageLoadMask(action.loadMask, false);

			CMDBuild.core.interfaces.messages.Warning.display(action.result);
			CMDBuild.core.interfaces.messages.Error.display(action.result, action);

			Ext.callback(originalFunction, action.scope, [action, action.result.success, action.response]);
		},

		/**
		 * Adapter to override parameters
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterSuccess: function(form, action, originalFunction) {
			Ext.callback(originalFunction, action.scope, [action.response, action, action.result]);
		},

		/**
		 * Adapter to override parameters
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		adapterFailure: function(form, action, originalFunction) {
			Ext.callback(originalFunction, action.scope, [action.response, action, action.result]);
		},

		/**
		 * Manage loadMaskParameter to display global loadMask or specific panel's one
		 *
		 * @param {Object or Boolean} loadMaskParameter
		 * @param {Boolean} show
		 *
		 * @private
		 */
		manageLoadMask: function(loadMaskParameter, show) {
			show = Ext.isBoolean(show) ? show : false;

			if (!Ext.isEmpty(loadMaskParameter)) {
				switch (Ext.typeOf(loadMaskParameter)) {
					case 'object': {
						if (Ext.isFunction(loadMaskParameter.setLoading))
							loadMaskParameter.setLoading(show);
					} break;

					case 'boolean':
					default: {
						if (show) {
							CMDBuild.core.LoadMask.show();
						} else {
							CMDBuild.core.LoadMask.hide();
						}
					}
				}
			}
		},

		/**
		 * @param {Object} action - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorCallback: function(action) {
			return Ext.bind(CMDBuild.core.interfaces.FormSubmit.adapterCallback, action.scope, [action.callback], true);
		},

		/**
		 * Builds failure interceptor to create sequence with callback
		 *
		 * @param {Object} action - the options configuration object passed to the request method
		 *
		 * @private
		 */
		interceptorFailure: function(action) {
			return Ext.Function.createSequence(
				Ext.bind(CMDBuild.core.interfaces.FormSubmit.adapterFailure, action.scope, [action.failure], true),
				action.callback,
				action.scope
			);
		},

		/**
		 * Builds success interceptor to create sequence with callback
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Function} originalFunction
		 *
		 * @private
		 */
		interceptorSuccess: function(action) {
			return Ext.Function.createSequence(
				Ext.bind(CMDBuild.core.interfaces.FormSubmit.adapterSuccess, action.scope, [action.success], true),
				action.callback,
				action.scope
			);
		},

		/**
		 * Manually builds callback's interceptors to manage loadMask property and callbacks build
		 *
		 * @param {Ext.form.Basic} form
		 * @param {Object} action - the options configuration object passed to the request method
		 * @param {Object} eOpts
		 *
		 * @private
		 */
		trapCallbacks: function(form, action, eOpts) {
			CMDBuild.core.interfaces.FormSubmit.manageLoadMask(action.loadMask, true);

			action.callback = CMDBuild.core.interfaces.FormSubmit.interceptorCallback(action); // First of all because is related to others
			action.failure = CMDBuild.core.interfaces.FormSubmit.interceptorFailure(action);
			action.success = CMDBuild.core.interfaces.FormSubmit.interceptorSuccess(action);
		},

		/**
		 * @param {Object} parameters
		 * @param {String} parameters.method
		 * @param {String} parameters.url
		 * @param {Object} parameters.params
		 * @param {Object} parameters.headers
		 * @param {Object} parameters.scope
		 * @param {Function} parameters.callback
		 * @param {Function} parameters.failure
		 * @param {Function} parameters.success
		 */
		submit: function(parameters) {
			if (!Ext.isEmpty(parameters.form)) {
				// Set default values
				Ext.applyIf(parameters, {
					method: 'POST',
					loadMask: true,
					scope: this,
					callback: Ext.emptyFn,
					failure: Ext.emptyFn,
					success: Ext.emptyFn
				});

				parameters.form.on('beforeaction', CMDBuild.core.interfaces.FormSubmit.trapCallbacks, this, { single: true });
				parameters.form.submit(parameters);
			} else {
				_error('form object not managed', 'CMDBuild.core.interfaces.FormSubmit');
			}
		}
	});

})();