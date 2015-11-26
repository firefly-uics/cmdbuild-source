(function($) {
	
	var API_URL_KEY = "apiUrl";
	var APP_ROOT_URL_KEY = "appRootUrl";
	var GUI_JAVA_URL_KEY = "guiJavaUrlKey";
	var APP_CONFIG_URL_KEY = "appConfigUrl";
	var NAME_GUI_FRAMEWORK = "cmdbuild-gui-framework-";

	var defaultparams = {
		debug: false,
		start: 'start.xml',
		theme: null,
		cssClass : null,
		customjs: [],
		stylesheets: [],
		authentication: null,
		httpCallParameters: {}
	};

	$.fn.cmdbuildAP = function(params) { 
		// merge default params with given params
		params = $.extend(defaultparams, params);

		// create $.Cmdbuild object
		$.Cmdbuild = {
			standard : {
				backend : {}
			},
			custom : {
				backend : {}
			}
		};

		// api url
		if (!params[API_URL_KEY]) {
			throw new Error(API_URL_KEY + " is required");
		}
		$.Cmdbuild.apiUrl = params[API_URL_KEY];
		//if there is the ~ I have to replace the home else the address remains the same
		$.Cmdbuild.apiUrl = $.Cmdbuild.apiUrl.replace("~", params.httpCallParameters.basePath);

		// application url
		if (!params[APP_ROOT_URL_KEY]) {
			throw new Error(APP_ROOT_URL_KEY + " is required");
		}
		var rootGui = params[APP_ROOT_URL_KEY];
		var nameGuiFramework = NAME_GUI_FRAMEWORK + params.httpCallParameters.coreVersion;
		rootGui = rootGui.replace("$CORE", nameGuiFramework);
		rootGui = rootGui.replace("~", params.httpCallParameters.basePath);
		$.Cmdbuild.guiJavaUrlKey = rootGui;
		$.Cmdbuild.appRootUrl = rootGui + "api/";
		// application url
		if (!params[APP_CONFIG_URL_KEY]) {
			throw new Error(APP_CONFIG_URL_KEY + " is required");
		}
		$.Cmdbuild.appConfigUrl = params[APP_CONFIG_URL_KEY];

		// add container id to params object
		params.containerId = $(this).attr("id");
		var container = $(this);
		// load start script and start application
		var nameFile = $.Cmdbuild.appRootUrl + "core/start.js";
		$.getScript(nameFile,
				function() {
					$.Cmdbuild.start.loadAndStart(params, container);
				});

		return this;
	};
})(jQuery);