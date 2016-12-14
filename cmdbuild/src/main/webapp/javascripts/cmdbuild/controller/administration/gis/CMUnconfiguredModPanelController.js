(function () {

	Ext.define('CMDBuild.controller.administration.gis.CMUnconfiguredModPanelController', {
		extend: 'CMDBuild.controller.CMBasePanelController',

		/**
		 * @returns {Void}
		 *
		 * @override
		 */
		onViewOnFront: function () {
			this.view.update('<div>' + arguments[0] + '</div>');
		}
	});

})();
