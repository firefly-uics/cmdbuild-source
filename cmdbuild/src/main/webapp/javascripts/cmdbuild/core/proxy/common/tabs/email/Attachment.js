(function() {

	Ext.define('CMDBuild.core.proxy.common.tabs.email.Attachment', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		copy: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.attachment.copy,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		download: function(parameters) {
			parameters.params[CMDBuild.core.constants.Proxy.FORCE_DOWNLOAD_PARAM_KEY] = true;

			var form = Ext.create('Ext.form.Panel', {
				standardSubmit: true,
				url: CMDBuild.core.proxy.Index.email.attachment.download
			});

			form.submit({
				target: '_blank',
				params: parameters.params
			});

			Ext.defer(function() { // Form cleanup
				form.close();
			}, 100);
		},

		/**
		 * @param {Object} parameters
		 */
		getAll: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.attachment.readAll,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.email.attachment.remove,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
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
				url: CMDBuild.core.proxy.Index.email.attachment.upload,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();