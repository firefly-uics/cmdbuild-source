(function() {

	Ext.define('CMDBuild.core.proxy.common.tabs.email.Attachment', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.proxy.Index'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		copy: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.attachment.copy
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
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
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.attachment.readAll
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		remove: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.Index.email.attachment.remove
			});

			CMDBuild.core.cache.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 */
		upload: function(parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.Index.email.attachment.upload });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();