(function() {

	Ext.define('CMDBuild.core.proxy.widgets.manageEmail.Attachment', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		copy: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.attachment.copy,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getAll: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.attachment.readAll,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.attachment.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		upload: function(parameters) {
			parameters.form.submit({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.widgets.manageEmail.attachment.upload,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: parameters.loadMask || false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();