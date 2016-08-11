(function () {

	Ext.define('CMDBuild.routes.Base', {
		extend: 'Ext.app.Controller',

		requires: ['CMDBuild.routes.Routes'],

		/**
		 * @param {Object} params
		 *
		 * @returns {Boolean}
		 *
		 * @abstract
		 */
		paramsValidation: function (params) {
			return true;
		},

		/**
		 * @param {Object} params - url parameters
		 * @param {String} path
		 * @param {Object} router
		 *
		 * @returns {Void}
		 */
		saveRoute: function (params, path, router) {
			CMDBuild.routes.Routes.setRoutePath(path);
		}
	});

})();
