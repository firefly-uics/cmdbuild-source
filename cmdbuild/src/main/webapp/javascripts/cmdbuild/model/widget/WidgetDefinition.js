(function() {

	Ext.define('CMDBuild.model.widget.WidgetDefinition', {
		extend: 'Ext.data.Model',

		requires: ['CMDBuild.core.proxy.CMProxyConstants'],

		fields: [
			{ name: CMDBuild.core.proxy.CMProxyConstants.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.proxy.CMProxyConstants.ID, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.LABEL, type: 'string' },
			{ name: CMDBuild.core.proxy.CMProxyConstants.TYPE, type: 'string' }
		],

		idProperty: CMDBuild.core.proxy.CMProxyConstants.ID,

		/**
		 * @property {Object} raw
		 */
		constructor: function(raw) {
			// It's late, I need all the raw data but that are not always the same for all definitions. The solution is to subclass this class with the real widget
			// model implementation but I don't know if is possible to add different models to the grid. So use this hack to return to the behaviour of the models
			// before Extjs 4.1
			this.callParent(arguments);

			this.data = raw;
		}
	});

})();