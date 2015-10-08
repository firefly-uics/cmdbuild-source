(function() {

	Ext.require('CMDBuild.core.constants.Proxy');

	Ext.define('CMDBuild.model.widget.WidgetDefinition', {
		extend: 'Ext.data.Model',

		fields: [
			{ name: CMDBuild.core.constants.Proxy.ACTIVE, type: 'boolean', defaultValue: true },
			{ name: CMDBuild.core.constants.Proxy.ID, type: 'int', useNull: true },
			{ name: CMDBuild.core.constants.Proxy.LABEL, type: 'string' },
			{ name: CMDBuild.core.constants.Proxy.TYPE, type: 'string' }
		],

		idProperty: CMDBuild.core.constants.Proxy.ID,

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