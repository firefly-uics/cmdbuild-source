(function() {
	var extAjaxRequest = Ext.Ajax.request;

	Ext.Ajax.setCMServer = function(s) {
		if (s) {
			Ext.Ajax.cmServer = s;
		}
	};

	Ext.Ajax.request = function(options) {
		var server = Ext.Ajax.cmServer;
		var url = options.url;
		if (server && url) {
			// set sinon.fakeServer to respond

			var cb = server.getCallbackForUrl(url);

			if (cb) {
				var method = options.method || "GET";
				var params = options.params || {};
				var out = cb.call(this, params);

				if (typeof out == "object") {
					out = Ext.JSON.encode(out);
				}

				// TODO build a object to configure a little more the response
				server.respondWith(method, url,
					["200", { "Content-Type": "application/json" }, out || ""]);
			}
		}

		// call the original function to do the request
		extAjaxRequest.call(this, options);
		server.respond();
	};
})();