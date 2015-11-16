(function() {

	Ext.define('CMDBuild.core.proxy.common.tabs.email.Attachment', {

		requires: [
			'CMDBuild.core.Ajax',
			'CMDBuild.core.cache.Cache',
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.common.tabs.email.attachments.TargetClass'
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
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getTargetClassComboStore: function() {
			return CMDBuild.core.cache.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.common.tabs.email.attachments.TargetClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.classes.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.CLASSES
					},
					extraParams: {
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				filters: [
					function(record) { // Filters root of all classes
						return record.get(CMDBuild.core.constants.Proxy.NAME) != 'Class';
					}
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.TEXT, direction: 'ASC' }
				]
			});
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