(function($) {
	var methods = {
		GET : "GET",
		POST : "POST",
		PUT : "PUT",
		DELETE : "DELETE"
	};

	var proxy = {
		getGraphConfiguration : function(callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'configuration/graph';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(
					response) {
				callback.apply(callbackScope, [ response ]);
			});
		},
		getClassAttributes : function(type, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.cacheProcess.isProcess(type)) {
				$.Cmdbuild.utilities.proxy.getProcessAttributes(type, callback,
						callbackScope);

			} else {
				$.Cmdbuild.utilities.proxy.getClassAttributes(type, callback,
						callbackScope);
			}
		},
		getCardData : function(type, cardId, config, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.cacheProcess.isProcess(type)) {
				$.Cmdbuild.utilities.proxy.getCardProcess(type, cardId, config,
						callback, callbackScope);

			} else {
				$.Cmdbuild.utilities.proxy.getCardData(type, cardId, config,
						callback, callbackScope);
			}
		},
		getClass : function(type, callback, callbackScope) {
			if ($.Cmdbuild.customvariables.cacheProcess.isProcess(type)) {
				$.Cmdbuild.utilities.proxy.getProcess(type, callback,
						callbackScope);

			} else {
				$.Cmdbuild.utilities.proxy.getClass(type, callback,
						callbackScope);
			}
		}
	};
	$.Cmdbuild.g3d.proxy = proxy;
})(jQuery);
