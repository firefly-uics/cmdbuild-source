(function() {

	Ext.define('CMDBuild.core.proxy.localization.Localization', {

		requires: [
			'CMDBuild.core.constants.Proxy',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.model.localization.Localization'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		getCurrentLanguage: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.utils.getLanguage,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getFileFormatStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					['@@ CSV', CMDBuild.core.constants.Proxy.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getLanguages: function(parameters) {
			CMDBuild.core.Ajax.request({
				url: CMDBuild.core.proxy.Index.utils.listAvailableTranslations,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				scope: parameters.scope || this,
				success: parameters.success || Ext.emptyFn,
				failure: parameters.failure || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @return {Ext.data.Store}
		 */
		getLanguagesStore: function() {
			return Ext.create('Ext.data.Store', {
				autoLoad: true,
				model: 'CMDBuild.model.localization.Localization',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.utils.listAvailableTranslations,
					reader: {
						type: 'json',
						root: 'translations'
					}
				},
				sorters: [
					{ property: CMDBuild.core.constants.Proxy.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getSectionsStore: function(parameters) {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.constants.Proxy.DESCRIPTION, CMDBuild.core.constants.Proxy.NAME],
				data: [
					['@@ All', CMDBuild.core.constants.Proxy.ALL],
					['@@ Class', CMDBuild.core.constants.Proxy.CLASS],
					['@@ Processes', CMDBuild.core.constants.Proxy.PROCESS],
					['@@ Domains', CMDBuild.core.constants.Proxy.DOMAIN],
					['@@ Views', CMDBuild.core.constants.Proxy.VIEW],
					['@@ Search filters', CMDBuild.core.constants.Proxy.FILTER],
					['@@ Lookup', CMDBuild.core.constants.Proxy.LOOKUP],
					['@@ Reports', CMDBuild.core.constants.Proxy.REPORT],
					['@@ Menu', CMDBuild.core.constants.Proxy.MENU]
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.localizations.translation.read,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		readAll: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.localizations.translation.readAll,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.core.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.localizations.translation.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn,
				success: parameters.success || Ext.emptyFn,
				callback: parameters.callback || Ext.emptyFn
			});
		}
	});

})();