(function() {

	Ext.define('CMDBuild.view.management.classes.map.proxy.Cards', {

		requires : [ 'CMDBuild.core.constants.Proxy', 'CMDBuild.proxy.index.Json' ],

		singleton : true,

		/**
		 * @param {Object}
		 *            parameters
		 *
		 * @returns {Void}
		 */
		read : function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				url : CMDBuild.proxy.index.Json.card.readAll
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.CARD, parameters);
		}
	});

})();
