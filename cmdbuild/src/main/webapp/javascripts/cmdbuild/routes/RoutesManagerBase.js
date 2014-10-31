(function() {

	Ext.define('CMDBuild.routes.RoutesManagerBase', {
		extend: 'Ext.app.Controller',

		requires: ['CMDBuild.routes.Routes'],

		/**
		 * @param {Object} params - url parameters
		 * @param {String} path
		 * @param {Object} router
		 */
		saveRoute: function(params, path, router) {
			CMDBuild.routes.Routes.setRoutePath(path);
		}
	});

})();