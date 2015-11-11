(function() {

	Ext.require([
		'CMDBuild.core.constants.Proxy',
		'CMDBuild.core.LoadMask',
		'CMDBuild.core.Message'
	]);

	CMDBuild.core.Ajax =  new Ext.data.Connection({
		showMaskAndTrapCallbacks: function(object, options) {
			if (options.loadMask) {
				CMDBuild.core.LoadMask.show();
			}
			this.trapCallbacks(object, options);
		},

		trapCallbacks: function(object, options) {
			var failurefn;
			var callbackScope = options.scope || this;
			options.success = Ext.bind(this.unmaskAndCheckSuccess, callbackScope, [options.success], true);
			/**
			 * the error message is not shown if options.failure
			 * is present and returns false
			 */
			if (options.failure) {
				failurefn = Ext.Function.createInterceptor(this.defaultFailure, options.failure, callbackScope);
			} else {
				failurefn = Ext.bind(this.defaultFailure, this);
			}
			options.failure = Ext.bind(this.decodeFailure, this, [failurefn], true);
		},

		unmaskAndCheckSuccess: function(response, options, successfn) {
			if (options.loadMask) {
				CMDBuild.core.LoadMask.hide();
			}
			var decoded = CMDBuild.core.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
			CMDBuild.core.Ajax.displayWarnings(decoded);
			if (!decoded || decoded.success !== false) {
				Ext.callback(successfn, this, [response, options, decoded]);
			} else {
				Ext.callback(options.failure, this, [response, options]);
			}
		},

		/**
		 * @param {String} jsonResponse
		 *
		 * @returns {Object}
		 */
		decodeJSONwhenMultipartAlso: function(jsonResponse) {
			jsonResponse = Ext.isEmpty(jsonResponse) ? '{"success":true,"response":null}' : jsonResponse; // Empty response manage

			if (!Ext.isEmpty(jsonResponse))
				jsonResponse = jsonResponse.replace(/<\/\w+>$/, '');

			// If throws an error so that wasn't a valid json string
			try {
				return Ext.decode(jsonResponse);
			} catch (e) {
				_error(e, 'CMDBuild.core.Ajax');
			}

			return '';
		},

		displayWarnings: function(decoded) {
			if (decoded && decoded.warnings && decoded.warnings.length) {
				for (var i=0; i<decoded.warnings.length; ++i) {
					var w = decoded.warnings[i];
					var errorString = CMDBuild.core.Ajax.formatError(w.reason, w.reasonParameters);
					if (errorString) {
						CMDBuild.Msg.warn(null, errorString);
					} else {
						CMDBuild.log.warn("Cannot print warning message", w);
					}
				}
			}
		},

		decodeFailure: function(response, options, failurefn) {
			var decoded = CMDBuild.core.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
			Ext.callback(failurefn, this, [response, options, decoded]);
		},

		defaultFailure: function(response, options, decoded) {
			if (decoded && decoded.errors && decoded.errors.length) {
				for (var i=0; i<decoded.errors.length; ++i) {
					this.showError(response, decoded.errors[i], options);
				}
			} else {
				this.showError(response, null, options);
			}
		},

		showError: function(response, error, options) {
			var tr = CMDBuild.Translation.errors || {
				error_message : "Error",
				unknown_error : "Unknown error",
				server_error_code : "Server error: ",
				server_error : "Server error"
			};
			var errorTitle = null;
			var errorBody = {
					text: tr.unknown_error,
					detail: undefined
			};

			if (error) {
				// if present, add the url that generate the error
				var detail = "";
				if (options && options.url) {
					detail = "Call: " + options.url + "\n";
					var line = "";
					for (var i=0; i<detail.length; ++i) {
						line += "-";
					}

					detail += line + "\n";
				}

				// then add to the details the server stacktrace
				errorBody.detail = detail + "Error: " + error.stacktrace;
				var reason = error.reason;
				if (reason) {
					if (reason == 'AUTH_NOT_LOGGED_IN' || reason == 'AUTH_MULTIPLE_GROUPS') {
						var loginWindow = Ext.create('CMDBuild.core.LoginWindow', { ajaxOptions: options });
						loginWindow.setAuthFieldsEnabled(reason == 'AUTH_NOT_LOGGED_IN');
						loginWindow.show();

						return;
					}
					var translatedErrorString = CMDBuild.core.Ajax.formatError(reason, error.reasonParameters);
					if (translatedErrorString) {
						errorBody.text = translatedErrorString;
					}
				}
			} else {
				if (!response || response.status == 200 || response.status == 0) {
					errorTitle = tr.error_message;
					errorBody.text = tr.unknown_error;
				} else if (response.status) {
					errorTitle = tr.error_message;
					errorBody.text = tr.server_error_code+response.status;
				}
			}

			var popup = options.form || options.important;

			CMDBuild.Msg.error(errorTitle, errorBody, popup);
		},

		formatError: function(reasonName, reasonParameters) {
			var tr = CMDBuild.Translation.errors.reasons;

			if (tr && tr[reasonName]) {
				return Ext.String.format.apply(null, [].concat(tr[reasonName]).concat(reasonParameters));
			} else {
				return "";
			}
		},

		/*
		 * From Ext.Ajax
		 */
		autoAbort: false,
		serializeForm: function(form) {
			return Ext.lib.Ajax.serializeForm(form);
		}
	});

	CMDBuild.core.Ajax.on('beforerequest', CMDBuild.core.Ajax.showMaskAndTrapCallbacks);

})();