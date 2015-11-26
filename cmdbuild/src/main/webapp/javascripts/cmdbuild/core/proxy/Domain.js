(function() {

	Ext.define('CMDBuild.core.proxy.Domain', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @property {Object} parameters
		 */
		getList: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.domain.getDomainList,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();