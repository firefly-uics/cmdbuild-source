CMDBuild.LoadMask = {
	get: function(text) {
		if (!CMDBuild.LoadMask.instance) {
			CMDBuild.LoadMask.instance = new Ext.LoadMask(Ext.getBody());
		}
		CMDBuild.LoadMask.instance.msg = text || CMDBuild.Translation.common.wait_title;
		return CMDBuild.LoadMask.instance;
	}
};

/**
 * @class CMDBuild.Ajax
 * @extends Ext.data.Connection
 * Ajax request class that automatically checks for success and implements a
 * default failure method. The success and failure methods are called with an
 * additional parameter representing the decoded response. Example usage:
 * <pre><code>
CMDBuild.Ajax.request({
	important: true, // errors are popups
	url: 'services/json/schema/setup/getconfiguration',
	params: { name: 'cmdbuild' },
	success: function(response, options, decoded) {
		CMDBuild.Config.cmdbuild = decoded.data;
		initLogin();
	}
});
 * @singleton
 */
CMDBuild.Ajax =  new Ext.data.Connection({
	showMaskAndTrapCallbacks: function(object, options) {
		if (options.loadMask) {
			CMDBuild.LoadMask.get().show();
		}
		this.trapCallbacks(object, options);
	},

	trapCallbacks: function(object, options) {
		var callbackScope = options.scope || this;
		options.success = this.unmaskAndCheckSuccess.createDelegate(callbackScope, [options.success], true);
		// the error message is not shown if options.failure is present and returns false
		if (options.failure) 
			var failurefn = this.defaultFailure.createInterceptor(options.failure, callbackScope);
		else
			var failurefn = this.defaultFailure.createDelegate(this);
		options.failure = this.decodeFailure.createDelegate(this, [failurefn], true);
	},

	unmaskAndCheckSuccess: function(response, options, successfn) {
		if (options.loadMask) {
			CMDBuild.LoadMask.get().hide();
		}
		var decoded = CMDBuild.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
		CMDBuild.Ajax.displayWarnings(decoded);
		if (!decoded || decoded.success !== false) {
			Ext.callback(successfn, this, [response, options, decoded]);
		} else {
			Ext.callback(options.failure, this, [response, options]);
		}
	},

	decodeJSONwhenMultipartAlso: function(jsonResponse) {
		var fixedResponseForMultipartExtBug = jsonResponse;
		if (jsonResponse) {
			fixedResponseForMultipartExtBug = jsonResponse.replace(/<\/\w+>$/,"");
		}
		return Ext.util.JSON.decode(fixedResponseForMultipartExtBug);
	},

	displayWarnings: function(decoded) {
		if (decoded && decoded.warnings && decoded.warnings.length) {
			for (var i=0; i<decoded.warnings.length; ++i) {
				var w = decoded.warnings[i];
				var errorString = CMDBuild.Ajax.formatError(w.reason, w.reason_parameters);
				if (errorString) {
					CMDBuild.Msg.warn(null, errorString);
				} else {
					CMDBuild.log.warn("Cannot print warning message", w);
				}
			}
		}
	},

	decodeFailure: function(response, options, failurefn) {
		var decoded = CMDBuild.Ajax.decodeJSONwhenMultipartAlso(response.responseText);
		Ext.callback(failurefn, this, [response, options, decoded]);
	},

	defaultFailure: function(response, options, decoded) {
		if (decoded && (decoded.reason == 'AUTH_NOT_LOGGED_IN' || decoded.reason == 'AUTH_MULTIPLE_GROUPS')) {
			CMDBuild.LoginWindow.addAjaxOptions(options);
			CMDBuild.LoginWindow.setAuthFieldsEnabled(decoded.reason == 'AUTH_NOT_LOGGED_IN');
			CMDBuild.LoginWindow.show();
			return false;
		}
		this.showError(response, decoded, options);
	},

	showError: function(response, decoded, options) {
		var popup = options.form || options.important;
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
		
		if (decoded) {
			errorBody.detail = decoded.stacktrace;
			if (decoded.reason) {
				var translatedErrorString = CMDBuild.Ajax.formatError(decoded.reason, decoded.reason_parameters);
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
		
		CMDBuild.Msg.error(errorTitle, errorBody, popup);
	},

	formatError: function(reasonName, reasonParameters) {
		var tr = CMDBuild.Translation.errors.reasons;
		if (tr && tr[reasonName]) {
			var functionParameters = [tr[reasonName]];
			for (var i=0; i<reasonParameters.length; ++i)
				functionParameters.push(reasonParameters[i]);
			return String.format.apply(String, functionParameters);
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

CMDBuild.Ajax.on('beforerequest', CMDBuild.Ajax.showMaskAndTrapCallbacks);
Ext.Ajax = CMDBuild.Ajax;

/**
 * @class CMDBuild.ChainedAjax
 * Executes a series of CMDBuild.Ajax.request one after the other. When it has
 * finished, it executes the fn function with the specified scope or this.
 * Example usage:
 * <pre><code>
	CMDBuild.ChainedAjax.execute({
		loadMask: true,
		requests: [{
			url: 'services/json/utils/success',
			success: function(response, options, decoded) {
				alert('First');
			}
		},{
			url: 'services/json/utils/success',
			success: function(response, options, decoded) {
				alert('Second');
			}
		}],
		fn: function() {
			alert('Done');
		}
	});
	</code></pre>
 * @singleton
 */
CMDBuild.ChainedAjax = {
    execute: function(o) {
		this.executeNextAndWait(o, 0);
    },

    // private
    executeNextAndWait: function(o, index) {
    	if (index < o.requests.length) {
    		this.showMask(o, index);
	    	var requestObject = Ext.apply(o.requests[index]);
	    	var execNext = this.executeNextAndWait.createDelegate(this, [o, index+1]);
			if (requestObject.success)
		    	requestObject.success = requestObject.success.createSequence(execNext);
		    else
		    	requestObject.success = execNext;
			requestObject.loadMask = requestObject.loadMask && !o.loadMask;
	    	CMDBuild.Ajax.request(requestObject);
    	} else {
    		if (o.loadMask) {
    			CMDBuild.LoadMask.get().hide();
    		}
    		Ext.callback(o.fn, o.scope || this);
    	}
    },
    
    //private
    showMask: function(o, index) {
    	if (o.loadMask) {
			if (o.requests[index].maskMsg) {
				var m = CMDBuild.LoadMask.get(o.requests[index].maskMsg);
				m.show();
			}
		}
    }
};


