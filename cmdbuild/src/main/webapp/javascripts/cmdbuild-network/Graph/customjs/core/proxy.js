(function($) {
	var methods = {
		GET: "GET",
		POST: "POST",
		PUT: "PUT",
		DELETE: "DELETE"
	};

	var proxy = {
		getGraphConfiguration: function(callback, callbackScope) {
			var url = $.Cmdbuild.global.getApiUrl() + 'configuration/graph';
			$.Cmdbuild.authProxy.makeAjaxRequest(url, methods.GET, function(
					response) {
				callback.apply(callbackScope, [response]);
			});
		}
	};
	$.Cmdbuild.g3d.proxy = proxy;
})(jQuery);
