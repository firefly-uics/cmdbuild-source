(function() {
	Ext.define("CMDBuild.test.CMServer", {
		statics: {
			create: function() {
				var server = new CMDBuild.test.CMServer();
				Ext.Ajax.setCMServer(server);

				return server;
			}
		},

		constructor: function() {
			this.server = sinon.fakeServer.create();
			this.callbacks = {};
		},

		restore: function() {
			this.server.restore();
			this.callbacks = {};
		},

		respond: function() {
			this.server.respond();
		},

		respondWith: function(method, url, response) {
			url = buildRegExp(removeParamsFromUrl(url));
			// remove previous response configuration with the same url
			var responses = this.server.responses || [];
			var BODY_POSITION = 2;
			for (var i = 0, l = responses.length; i < l; i++) {
				if (responses[i].url.toString() == url.toString()) {
					responses[i].response = response;
					return;
				}
			}

			this.server.respondWith(method, url, response);
		},

		bindUrl: function(url, fn) {
			this.callbacks[removeParamsFromUrl(url)] = fn;
		},

		getCallbackForUrl: function(url) {
			if (url) {
				return this.callbacks[removeParamsFromUrl(url)];
			} else {
				return undefined;
			}
		},

		hasResponses: function() {
			if (!this.server.response && !this.server.responses) {
				return false;
			} else if (this.server.response) {
				return true;
			} else {
				return (this.server.responses && this.server.responses.length > 0); 
			}
		}
	});

	function removeParamsFromUrl(url) {
		return url.split("?")[0] || "";
	}

	function buildRegExp(url) {
		return new RegExp("^" + url);
	}
})();