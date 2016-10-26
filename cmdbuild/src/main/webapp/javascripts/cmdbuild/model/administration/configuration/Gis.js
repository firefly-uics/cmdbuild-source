(function () {

	Ext.require('CMDBuild.core.constants.Proxy');

	/**
	 * TODO: waiting for refactor (rename)
	 */
	Ext.define('CMDBuild.model.administration.configuration.Gis', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.CENTER_LATITUDE, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.CENTER_LONGITUDE, type: 'int' },
			{ name: CMDBuild.core.constants.Proxy.ENABLED, type: 'boolean' },
			{ name: CMDBuild.core.constants.Proxy.INITIAL_ZOOM_LEVEL, type: 'int' }
		],

		/**
		 * @param {Object} data
		 *
		 * @returns {Void}
		 *
		 * @override
		 */
		constructor: function (data) {
			data = Ext.isObject(data) ? data : {};
			data[CMDBuild.core.constants.Proxy.CENTER_LATITUDE] = data[CMDBuild.core.constants.Proxy.CENTER_LATITUDE] || data['center.lat'];
			data[CMDBuild.core.constants.Proxy.CENTER_LONGITUDE] = data[CMDBuild.core.constants.Proxy.CENTER_LONGITUDE] || data['center.lon'];

			this.callParent(arguments);
		},

		/**
		 * @returns {Object}
		 */
		getSubmitData: function () {
			var data = this.getData();

			return {
				'center.lat': data[CMDBuild.core.constants.Proxy.CENTER_LATITUDE],
				'center.lon': data[CMDBuild.core.constants.Proxy.CENTER_LONGITUDE],
				enabled: data[CMDBuild.core.constants.Proxy.ENABLED],
				initialZoomLevel: data[CMDBuild.core.constants.Proxy.INITIAL_ZOOM_LEVEL]
			};
		}
	});

})();
