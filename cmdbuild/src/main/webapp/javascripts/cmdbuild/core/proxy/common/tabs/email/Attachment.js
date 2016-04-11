(function () {

	Ext.define('CMDBuild.core.proxy.common.tabs.email.Attachment', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.interfaces.FormSubmit',
			'CMDBuild.core.proxy.index.Json',
			'CMDBuild.model.common.tabs.email.attachments.TargetClass'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		copy: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.email.attachment.copy });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		download: function (parameters) {
			if (
				Ext.isObject(parameters) && !Ext.Object.isEmpty(parameters)
				&& Ext.isObject(parameters.params) && !Ext.Object.isEmpty(parameters.params)
			) {
				window.open(
					CMDBuild.core.proxy.index.Json.email.attachment.download + '?' + Ext.urlEncode(parameters.params),
					'_blank'
				);
			}
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		getAll: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, {
				loadMask: false,
				url: CMDBuild.core.proxy.index.Json.email.attachment.readAll
			});

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.ATTACHMENT, {
				autoLoad: false,
				model: 'CMDBuild.model.CMAttachment', // TODO: waiting for refactor
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.attachment.readAll,
					reader: {
						type: 'json',
						root: CMDBuild.core.constants.Proxy.ROWS
					},
					extraParams: { // Avoid to send limit, page and start parameters in server calls
						limitParam: undefined,
						pageParam: undefined,
						startParam: undefined
					}
				},
				sorters: [
					{ property: 'Filename', direction: 'ASC' }
				]
			});
		},

		/**
		 * @returns {Ext.data.Store or CMDBuild.core.cache.Store}
		 */
		getTargetClassComboStore: function () {
			return CMDBuild.global.Cache.requestAsStore(CMDBuild.core.constants.Proxy.CLASS, {
				autoLoad: true,
				model: 'CMDBuild.model.common.tabs.email.attachments.TargetClass',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.index.Json.classes.readAll,
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
					function (record) { // Filters root of all classes
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
		 *
		 * @returns {Void}
		 */
		remove: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.email.attachment.remove });

			CMDBuild.global.Cache.request(CMDBuild.core.constants.Proxy.ATTACHMENT, parameters, true);
		},

		/**
		 * @param {Object} parameters
		 *
		 * @returns {Void}
		 */
		upload: function (parameters) {
			parameters = Ext.isEmpty(parameters) ? {} : parameters;

			Ext.apply(parameters, { url: CMDBuild.core.proxy.index.Json.email.attachment.upload });

			CMDBuild.core.interfaces.FormSubmit.submit(parameters);
		}
	});

})();
