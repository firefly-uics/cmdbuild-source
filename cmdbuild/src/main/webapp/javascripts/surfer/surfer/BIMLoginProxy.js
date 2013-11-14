(function () {

	// Cookies constants
	var TOKEN = "token", ADDRESS = "address";

	// TODO: remove jQuery dependency
	// for cookies 
	BIMLoginProxy = function() {
		var api = null;

		this.initServerApi = function(url) {
			api = new BimServerApi(url);
			window._BIM_SERVER_API = api;
		};

		this.tryAutoLogin = function(callback) {
			if ($.cookie(TOKEN) && $.cookie(ADDRESS)) {
				this.initServerApi($.cookie(ADDRESS));
				api.token = $.cookie(TOKEN);

				if (typeof callback == "function") {
					callback();
				}
			}
		};

		/**
		 * request: {
		 * 		url: the url of the bimServer
		 * 		username: the user name to use for the login
		 * 		password: the password to use for the login
		 * 		rememberMe: true to stay logged in after page refresh
		 * 		success: a callback to call if the login goes right
		 * 		failure: a callback to call if something goes wrong
		 * }
		 * 
		 */
		this.login = function(request) {
			this.initServerApi(request.url);
			api.login( //
				request.username, //
				request.password, //
				request.rememberMe, //
				function() {

					/*
					 * I have not understand why
					 * this call does not call
					 * the failure callback if
					 * does not reach the server...
					 * 
					 * The only thing I can do,
					 * is check if the token
					 * is not set assume that
					 * the login goes wrong
					 */
					if (!api.token) {
						if (typeof request.failure == "function") {
							request.failure();
							return;
						}
					}

					if (request.rememberMe) {
						// Set the cookies to do auto login later
						$.cookie(ADDRESS, request.url);
						$.cookie(TOKEN, api.token);
					}

					if (typeof request.success == "function") {
						request.success();
					}

				}, //

				request.failure //
			);
		};

		/**
		 * Do the logout and call the
		 * given callback
		 */
		this.logout = function(callback) {
			api.logout(function onLogoutSuccess() {
				$.removeCookie(ADDRESS);
				$.removeCookie(TOKEN);

				if (typeof callback == "function") {
					callback();
				}
			});
		};

	};
})();
