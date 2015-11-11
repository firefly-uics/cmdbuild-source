(function() {

	/**
	 * To apply extraParams to ProxyServer if defined in store create object [16/01/2015]
	 */
	Ext.define('CMDBuild.override.data.proxy.Server', {
		override: 'Ext.data.proxy.Server',

		constructor: function() {
			this.callParent(arguments);

			Ext.apply(this, this.extraParams);
		}
	});

})();