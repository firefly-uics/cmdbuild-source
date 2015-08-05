(function() {

	Ext.define('CMDBuild.core.proxy.localizations.Localizations', {

		requires: [
			'CMDBuild.core.proxy.Constants',
			'CMDBuild.core.proxy.Index',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.model.localizations.Localization'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		getCurrentLanguage: function(parameters) {
			CMDBuild.Ajax.request({
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
				fields: [CMDBuild.core.proxy.Constants.DESCRIPTION, CMDBuild.core.proxy.Constants.NAME],
				data: [
					['@@ CSV', CMDBuild.core.proxy.Constants.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getLanguages: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.Index.utils.listAvailableTranslations,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
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
				model: 'CMDBuild.model.localizations.Localization',
				proxy: {
					type: 'ajax',
					url: CMDBuild.core.proxy.Index.utils.listAvailableTranslations,
					reader: {
						type: 'json',
						root: 'translations'
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getSectionsStore: function(parameters) { // TODO real implementation
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.Constants.DESCRIPTION, CMDBuild.core.proxy.Constants.NAME],
				data: [
					['@@ Classes', CMDBuild.core.proxy.Constants.CLASSES],
					['@@ Domains', 'domains'], // TODO costants
					['@@ Lookup', 'lookup'], // TODO costants
					['@@ Menu', 'menu'], // TODO costants
					['@@ Reports', 'reports'], // TODO costants
					['@@ Processes', 'processes'], // TODO costants
					['@@ Views', 'views'] // TODO costants
				],
				sorters: [
					{ property: CMDBuild.core.proxy.Constants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.localizations.translation.read,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		readStructure: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.localizations.translation.readStructure,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @param {Object} parameters
		 */
		update: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.Index.localizations.translation.update,
				params: parameters.params,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				scope: parameters.scope || this,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		}
	});

})();