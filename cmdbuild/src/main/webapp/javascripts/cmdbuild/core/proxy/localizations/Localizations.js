(function() {

	Ext.define('CMDBuild.core.proxy.localizations.Localizations', {

		requires: [
			'CMDBuild.core.proxy.CMProxyConstants',
			'CMDBuild.core.proxy.CMProxyUrlIndex',
			'CMDBuild.core.proxy.Configuration',
			'CMDBuild.model.localizations.Localization'
		],

		singleton: true,

		/**
		 * @param {Object} parameters
		 */
		create: function(parameters) { // TODO: future implementation of server-side parameter for sectionId
			var url = undefined;

			switch (parameters.params.sectionId) {
				case CMDBuild.core.proxy.CMProxyConstants.CLASSES: {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classCreate;
				} break;

				case 'classesAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classAttributeCreate;
				} break;

				case CMDBuild.core.proxy.CMProxyConstants.DOMAINS: {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.domainCreate;
				} break;

				case 'domainsAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.domainAttributeCreate;
				} break;

				case 'lookup': {

				} break;

				case 'menu': {

				} break;

				case 'report': {

				} break;
			}

			CMDBuild.Ajax.request({
				method: 'POST',
				url: url,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getCsvSeparatorStore: function() {
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.VALUE],
				data: [
					[';'],
					[','],
					['|']
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getCurrentLanguage: function(parameters) {
			CMDBuild.Ajax.request({
				method: 'POST',
				url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.getLanguage,
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
				fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.NAME],
				data: [
					['@@ CSV', CMDBuild.core.proxy.CMProxyConstants.CSV]
				],
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		getLanguages: function(parameters) {
			CMDBuild.Ajax.request({
				url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.listAvailableTranslations,
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
					url: CMDBuild.core.proxy.CMProxyUrlIndex.utils.listAvailableTranslations,
					reader: {
						type: 'json',
						root: 'translations'
					}
				},
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @return {Ext.data.ArrayStore}
		 */
		getSectionsStore: function(parameters) { // TODO real implementation
			return Ext.create('Ext.data.ArrayStore', {
				fields: [CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, CMDBuild.core.proxy.CMProxyConstants.NAME],
				data: [
					['@@ Classes', CMDBuild.core.proxy.CMProxyConstants.CLASSES],
					['@@ Domains', 'domains'], // TODO costants
					['@@ Lookup', 'lookup'], // TODO costants
					['@@ Menu', 'menu'], // TODO costants
					['@@ Report', 'report'] // TODO costants
				],
				sorters: [
					{ property: CMDBuild.core.proxy.CMProxyConstants.DESCRIPTION, direction: 'ASC' }
				]
			});
		},

		/**
		 * @param {Object} parameters
		 */
		read: function(parameters) { // TODO: future implementation of server-side parameter for sectionId
			var url = undefined;

			switch (parameters.params[CMDBuild.core.proxy.CMProxyConstants.SECTION_ID]) {
				case CMDBuild.core.proxy.CMProxyConstants.CLASSES: {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classRead;
				} break;

				case 'classesAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classAttributeRead;
				} break;

				case CMDBuild.core.proxy.CMProxyConstants.DOMAINS: {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.domainRead;
				} break;

				case 'domainsAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.domainAttributeRead;
				} break;

				case 'lookup': {

				} break;

				case 'menu': {

				} break;

				case 'report': {

				} break;

				default: {
_debug('Error', parameters);
				}
			}

			CMDBuild.Ajax.request({
				method: 'POST',
				url: url,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : false,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},

		update: function(parameters) { // TODO: future implementation of server-side parameter for sectionId
			var url = undefined;

			switch (parameters.params.sectionId) {
				case CMDBuild.core.proxy.CMProxyConstants.CLASSES: {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classUpdate;
				} break;

				case 'classesAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.classAttributeUpdate;
				} break;

				case 'domains': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.domainUpdate;
				} break;

				case 'domainsAttributes': {
					url = CMDBuild.core.proxy.CMProxyUrlIndex.localizations.domainAttributeUpdate;
				} break;

				case 'lookup': {

				} break;

				case 'menu': {

				} break;

				case 'report': {

				} break;
			}

			CMDBuild.Ajax.request({
				method: 'POST',
				url: url,
				params: parameters.params,
				scope: parameters.scope || this,
				loadMask: Ext.isBoolean(parameters.loadMask) ? parameters.loadMask : true,
				failure: parameters.failure || Ext.emptyFn(),
				success: parameters.success || Ext.emptyFn(),
				callback: parameters.callback || Ext.emptyFn()
			});
		},
	});

})();