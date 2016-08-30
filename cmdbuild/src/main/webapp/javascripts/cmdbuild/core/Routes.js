(function () {

	Ext.define('CMDBuild.core.Routes', {

		singleton: true,

		/**
		 * @property {String}
		 *
		 * @private
		 */
		route: undefined,

		/**
		 * @returns {Void}
		 */
		exec: function () {
			if (Ext.isString (this.route) && !Ext.isEmpty(this.route)) {
				var route = this.route;

				delete this.route;

				Ext.Router.parse('exec/' + route);
			}
		},

		/**
		 * @param {String} path
		 *
		 * @returns {Void}
		 */
		setRoutePath: function (path) {
			if (Ext.isString (path) && !Ext.isEmpty(path))
				this.route = path;
		}
	});

})();
